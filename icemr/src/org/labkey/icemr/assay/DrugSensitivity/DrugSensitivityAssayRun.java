/*
 * Copyright (c) 2013-2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.icemr.assay.DrugSensitivity;

import org.labkey.api.assay.dilution.DilutionAssayProvider;
import org.labkey.api.assay.dilution.DilutionAssayRun;
import org.labkey.api.assay.dilution.DilutionDataHandler;
import org.labkey.api.assay.dilution.DilutionManager;
import org.labkey.api.assay.dilution.DilutionMaterialKey;
import org.labkey.api.assay.dilution.DilutionSummary;
import org.labkey.api.assay.nab.Luc5Assay;
import org.labkey.api.assay.nab.view.RunDetailOptions;
import org.labkey.api.data.statistics.FitFailedException;
import org.labkey.api.data.statistics.StatsService;
import org.labkey.api.exp.PropertyDescriptor;
import org.labkey.api.exp.api.DataType;
import org.labkey.api.exp.api.ExpData;
import org.labkey.api.exp.api.ExpRun;
import org.labkey.api.security.User;
import org.labkey.api.study.Plate;
import org.labkey.api.study.Position;
import org.labkey.api.study.WellData;
import org.labkey.api.study.WellGroup;
import org.labkey.api.study.assay.AbstractAssayProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: klum
 * Date: 5/13/13
 */
public class DrugSensitivityAssayRun extends DilutionAssayRun
{
    protected Plate _plate;
    private DilutionSummary[] _dilutionSummaries;
    private Double _initialParasitemia;

    public DrugSensitivityAssayRun(DilutionAssayProvider provider, ExpRun run, Plate plate,
                                   User user, List<Integer> cutoffs, StatsService.CurveFitType renderCurveFitType)
    {
        super(provider, run, user, cutoffs, renderCurveFitType);

        for (Map.Entry<PropertyDescriptor, Object> property : getRunProperties().entrySet())
        {
            if (DrugSensitivityAssayProvider.INITIAL_PARASITEMIA_PROPERTY_NAME.equals(property.getKey().getName()))
            {
                _initialParasitemia = (Double)property.getValue();
                break;
            }
        }
        _plate = plate;
        List<? extends WellGroup> specimenGroups = _plate.getWellGroups(WellGroup.Type.SPECIMEN);
        _dilutionSummaries = getDilutionSumariesForWellGroups(specimenGroups);
    }

    @Override
    protected DilutionSummary[] getDilutionSumariesForWellGroups(List<? extends WellGroup> specimenGroups)
    {
        int sampleIndex = 0;
        DilutionSummary[] dilutionSummaries = new DilutionSummary[specimenGroups.size()];
        for (WellGroup specimenGroup : specimenGroups)
            dilutionSummaries[sampleIndex++] = new DrugSensitivityDilutionSummary(this, Collections.singletonList(specimenGroup), null, _renderedCurveFitType);
        return dilutionSummaries;
    }

    /**
     * Todo : investigate whether to push this into the base class
     * @return
     */
    @Override
    public List<SampleResult> getSampleResults()
    {
        if (_sampleResults == null)
        {
            List<SampleResult> sampleResults = new ArrayList<>();

            DilutionDataHandler handler = _provider.getDataHandler();
            DataType dataType = handler.getDataType();

            List<? extends ExpData> outputDatas = _run.getOutputDatas(null); //handler.getDataType());
            ExpData outputObject = null;
            if (outputDatas.size() == 1 && outputDatas.get(0).getDataType() == dataType)
            {
                outputObject = outputDatas.get(0);
            }
            else if (outputDatas.size() > 1)
            {
                // If there is a transformed dataType, use that
                ExpData dataWithHandlerType = null;
                ExpData dataWithTransformedType = null;
                for (ExpData expData : outputDatas)
                {
                    if (dataType.equals(expData.getDataType()))
                    {
                        if (null != dataWithHandlerType)
                            throw new IllegalStateException("Expected a single data file output for this NAb run. Found at least 2 expDatas with the expected datatype and a total of " + outputDatas.size());
                        dataWithHandlerType = expData;
                    }
                }
                if (null != dataWithTransformedType)
                {
                    outputObject = dataWithTransformedType;
                }
                else if (null != dataWithHandlerType)
                {
                    outputObject = dataWithHandlerType;
                }
            }
            if (null == outputObject)
                throw new IllegalStateException("Expected a single data file output for this NAb run, but none matching the expected datatype found. Found a total of " + outputDatas.size());

            Map<String, Map<PropertyDescriptor, Object>> samplePropertiesMap = getSampleProperties();
            DilutionSummary[] dilutionSummaries = getSummaries();
            Map<String, DilutionResultProperties> allProperties = getSampleProperties(outputObject, dilutionSummaries, samplePropertiesMap);
            Set<String> captions = new HashSet<>();
            boolean longCaptions = false;

            for (DilutionSummary summary : dilutionSummaries)
            {
                if (!summary.isBlank())
                {
                    DilutionMaterialKey key = summary.getMaterialKey();
                    String shortCaption = key.getDisplayString(RunDetailOptions.DataIdentifier.DefaultFormat);
                    if (captions.contains(shortCaption))
                        longCaptions = true;
                    captions.add(shortCaption);

                    Map<PropertyDescriptor, Object> sampleProperties = samplePropertiesMap.get(getSampleKey(summary));
                    DilutionResultProperties props = allProperties.get(getSampleKey(summary));
                    sampleResults.add(new SampleResult(_provider, outputObject, summary, key, sampleProperties, props));
                }
            }

            if (longCaptions)
            {
                for (SampleResult result : sampleResults)
                    result.setLongCaptions(true);
            }

            _sampleResults = sampleResults;
        }
        return _sampleResults;
    }

    @Override
    public double getPercent(WellGroup group, WellData data) throws FitFailedException
    {
        Plate plate = group.getPlate();

        WellGroup virusControl = plate.getWellGroup(WellGroup.Type.CONTROL, DilutionManager.VIRUS_CONTROL_SAMPLE);
        if (virusControl == null)
            throw new FitFailedException("Invalid plate template: no virus control well group was found.");

        double controlRange = 0.0;

        if (data instanceof WellGroup)
        {
            Position position = ((WellGroup)data).getPositions().get(0);
            if (position != null)
            {
                WellGroup replicateGroup = getControlReplicateGroup(position.getRow(), virusControl);
                controlRange = replicateGroup.getMean();
            }
        }

        if (controlRange == 0.0)
            controlRange = virusControl.getMean();

        double cellControl = _initialParasitemia != null ? _initialParasitemia.doubleValue() : 0.0;
        if (data.getMean() < cellControl)
            return 0.0;
        else
            return (data.getMean() - cellControl) / (controlRange - cellControl);
    }

    /**
     * Try to locate, if any, the control replicate group at the same row level specified
     * @param row
     * @param controlGroup
     * @return
     */
    private WellGroup getControlReplicateGroup(int row, WellGroup controlGroup)
    {
        for (WellGroup group : controlGroup.getOverlappingGroups(WellGroup.Type.REPLICATE))
        {
            boolean rowsMatched = true;
            // find the control replicate group in the specified row
            for (Position pos : group.getPositions())
            {
                if (pos.getRow() != row)
                    rowsMatched = false;
            }

            if (rowsMatched)
                return group;
        }
        return controlGroup;
    }

    @Override
    public double getControlRange(Plate plate, String virusWellGroupName)
    {
        WellData virusControl = plate.getWellGroup(WellGroup.Type.CONTROL, DilutionManager.VIRUS_CONTROL_SAMPLE);
        return virusControl.getMean();
    }

    @Override
    public double getCellControlMean(Plate plate, String virusWellGroupName)
    {
        return 0.0;
    }

    @Override
    public double getCellControlPlusMinus(Plate plate, String virusWellGroupName)
    {
        return 0.0;
    }

    @Override
    public DilutionSummary[] getSummaries()
    {
        return _dilutionSummaries;
    }

    @Override
    public List<Plate> getPlates()
    {
        return Collections.singletonList(_plate);
    }

    public static class DrugSensitivityDilutionSummary extends DilutionSummary
    {
        public DrugSensitivityDilutionSummary(Luc5Assay assay, List<WellGroup> sampleGroups, String lsid, StatsService.CurveFitType curveFitType)
        {
            super(assay, sampleGroups, lsid, curveFitType);
        }

        @Override
        public DilutionMaterialKey getMaterialKey()
        {
            if (_materialKey == null)
            {
                WellGroup firstWellGroup = getFirstWellGroup();
                Double visitId = (Double) firstWellGroup.getProperty(AbstractAssayProvider.VISITID_PROPERTY_NAME);
                String participantId = (String) firstWellGroup.getProperty(AbstractAssayProvider.PARTICIPANTID_PROPERTY_NAME);
                Date visitDate = (Date) firstWellGroup.getProperty(AbstractAssayProvider.DATE_PROPERTY_NAME);
                String treatmentName = firstWellGroup.getProperty(DrugSensitivityAssayProvider.TREATMENT_NAME_PROPERTY_NAME).toString();

                _materialKey = new DilutionMaterialKey(_container, treatmentName, participantId, visitId, visitDate, null);
            }
            return _materialKey;
        }
    }
}
