/*
 * Copyright (c) 2013-2017 LabKey Corporation
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

import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.assay.dilution.DilutionAssayProvider;
import org.labkey.api.assay.dilution.DilutionAssayRun;
import org.labkey.api.assay.dilution.DilutionDataHandler;
import org.labkey.api.assay.dilution.DilutionSummary;
import org.labkey.api.assay.dilution.SampleProperty;
import org.labkey.api.assay.nab.NabSpecimen;
import org.labkey.api.data.Container;
import org.labkey.api.data.statistics.StatsService;
import org.labkey.api.exp.ExperimentException;
import org.labkey.api.exp.Lsid;
import org.labkey.api.exp.ObjectProperty;
import org.labkey.api.exp.OntologyManager;
import org.labkey.api.exp.PropertyType;
import org.labkey.api.exp.api.DataType;
import org.labkey.api.exp.api.ExpData;
import org.labkey.api.exp.api.ExpMaterial;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.api.ExpRun;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.exp.property.DomainProperty;
import org.labkey.api.query.ValidationException;
import org.labkey.api.reader.DataLoader;
import org.labkey.api.reader.DataLoaderService;
import org.labkey.api.security.User;
import org.labkey.api.study.Plate;
import org.labkey.api.study.PlateService;
import org.labkey.api.study.PlateTemplate;
import org.labkey.api.study.WellData;
import org.labkey.api.study.WellGroup;
import org.labkey.api.study.assay.AssayDataType;
import org.labkey.api.util.FileType;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: klum
 * Date: 5/13/13
 */
public class DrugSensitivityDataHandler extends DilutionDataHandler
{
    public static final String DATA_ROW_LSID_PREFIX = "AssayRunDrugSensitivityDataRow";
    public static final AssayDataType DRUG_SENSITIVITY_DATA_TYPE = new AssayDataType("AssayRunDrugSensitivityData", new FileType(Arrays.asList(".txt", ".tsv"), ".txt"));

    public static final String DRUG_SENSITIVITY_PROPERTY_LSID_PREFIX = "DrugSensitivityProperty";

    public DrugSensitivityDataHandler()
    {
        super(DATA_ROW_LSID_PREFIX);
    }

    @Override
    protected String getPreferredDataFileExtension()
    {
        return "txt";
    }

    protected double[][] getCellValues(File dataFile, PlateTemplate template) throws ExperimentException
    {
        double[][] cellValues = new double[template.getRows()][template.getColumns()];

        try
        {
            DataLoader loader = DataLoaderService.get().createLoader(dataFile, null, false, null, null);

            for (Map<String, Object> dataRow : loader.load())
            {
                Object well = dataRow.get("column0");
                if (well != null)
                {
                    String wellStr = well.toString().toLowerCase();
                    if (wellStr.length() == 3)
                    {
                        int row = wellStr.charAt(0) - 'a';
                        int col = NumberUtils.toInt(wellStr.substring(1));

                        if ((row >= 0 && row < template.getRows()) && (col >= 1 && col <= template.getColumns()))
                        {
                            Object count = dataRow.get("column2");
                            Object total = dataRow.get("column3");
                            if (count != null && total != null)
                            {
                                String countStr = count.toString();
                                String totalStr = total.toString();
                                countStr = countStr.replaceAll(",", "");
                                totalStr = totalStr.replaceAll(",", "");

                                cellValues[row][col-1] = 100 * (NumberUtils.toDouble(countStr) / NumberUtils.toDouble(totalStr));
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new ExperimentException(e);
        }

        return cellValues;
    }

    protected DataLoader getDataLoader(File dataFile) throws ExperimentException
    {
        try
        {
            return DataLoaderService.get().createLoader(dataFile, null, false, null, null);
        }
        catch (IOException e)
        {
            throw new ExperimentException(e);
        }
    }

    /**
     * If specimens get more dilute as you move down or right on the plate, return true, else
     * it is assumed that specimens get more dilute as you move up or left on the plate.
     * @return
     */
    @Override
    protected boolean isDilutionDownOrRight()
    {
        return true;
    }

    @Override
    protected void prepareWellGroups(List<WellGroup> wellgroups, ExpMaterial material, Map<String, DomainProperty> samplePropertyMap) throws ExperimentException
    {
        if (wellgroups.size() != 1)
            throw new IllegalStateException("Expected exactly 1 well group per material for drug sensitivity runs.  Found " + wellgroups.size());
        WellGroup group = wellgroups.get(0);
        for (DomainProperty property : samplePropertyMap.values())
            group.setProperty(property.getName(), material.getProperty(property));

        List<? extends WellData> wells = group.getWellData(true);
        boolean reverseDirection = Boolean.parseBoolean((String) group.getProperty(SampleProperty.ReverseDilutionDirection.name()));
        applyDilution(wells, material, samplePropertyMap, reverseDirection);
    }

    @Override
    protected DilutionAssayRun createDilutionAssayRun(DilutionAssayProvider provider, ExpRun run, List<Plate> plates, User user, List<Integer> sortedCutoffs, StatsService.CurveFitType fit)
    {
        return new DrugSensitivityAssayRun(provider, run, plates.get(0), user, sortedCutoffs, fit);
    }

    @Override
    public DilutionAssayRun getAssayResults(ExpRun run, User user, @Nullable StatsService.CurveFitType fit) throws ExperimentException
    {
        File dataFile = getDataFile(run);
        if (dataFile == null)
            throw new MissingDataFileException(getResourceName(run) +  " data file could not be found for run " + run.getName() + ".  Deleted from file system?");
        return getAssayResults(run, user, dataFile, fit, false, false);
    }

    @Override
    /**
     * consider pushing this into the base data handler class
     */
    public Map<DilutionSummary, DilutionAssayRun> getDilutionSummaries(User user, StatsService.CurveFitType fit, int... dataObjectIds) throws ExperimentException, SQLException
    {
        Map<DilutionSummary, DilutionAssayRun> summaries = new LinkedHashMap<>();
        if (dataObjectIds == null || dataObjectIds.length == 0)
            return summaries;

        Map<Integer, DilutionAssayRun> dataToAssay = new HashMap<>();
        List<Integer> nabSpecimenIds = new ArrayList<>(dataObjectIds.length);
        for (int nabSpecimenId : dataObjectIds)
            nabSpecimenIds.add(nabSpecimenId);
        List<NabSpecimen> nabSpecimens = DrugSensitivityManager.get().getNabSpecimens(nabSpecimenIds);
        for (NabSpecimen nabSpecimen : nabSpecimens)
        {
            String wellgroupName = nabSpecimen.getWellgroupName();
            if (null == wellgroupName)
                continue;

            int runId = nabSpecimen.getRunId();
            DilutionAssayRun assay = dataToAssay.get(runId);
            if (assay == null)
            {
                ExpRun run = ExperimentService.get().getExpRun(runId);
                if (null == run)
                    continue;
                assay = getAssayResults(run, user, fit);
                if (null == assay)
                    continue;
                dataToAssay.put(runId, assay);
            }

            for (DilutionSummary summary : assay.getSummaries())
            {
                if (wellgroupName.equals(summary.getFirstWellGroup().getName()))
                {
                    summaries.put(summary, assay);
                    break;
                }
            }
        }
        return summaries;
    }

    @Override
    /**
     * consider pushing this into the base class
     */
    protected void importRows(ExpData data, ExpRun run, ExpProtocol protocol, List<Map<String, Object>> rawData, User user) throws ExperimentException
    {
        try
        {
            Container container = run.getContainer();
            OntologyManager.ensureObject(container, data.getLSID());
            Map<Integer, String> cutoffFormats = getCutoffFormats(protocol, run);

            Map<String, ExpMaterial> inputMaterialMap = new HashMap<>();

            for (ExpMaterial material : run.getMaterialInputs().keySet())
                inputMaterialMap.put(material.getLSID(), material);

            for (Map<String, Object> group : rawData)
            {
                if (!group.containsKey(WELLGROUP_NAME_PROPERTY))
                    throw new ExperimentException("The row must contain a value for the well group name : " + WELLGROUP_NAME_PROPERTY);

                if (group.get(WELLGROUP_NAME_PROPERTY) == null)
                    throw new ExperimentException("The row must contain a value for the well group name : " + WELLGROUP_NAME_PROPERTY);

                if (group.get(DILUTION_INPUT_MATERIAL_DATA_PROPERTY) == null)
                    throw new ExperimentException("The row must contain a value for the specimen lsid : " + DILUTION_INPUT_MATERIAL_DATA_PROPERTY);

                String groupName = group.get(WELLGROUP_NAME_PROPERTY).toString();
                String specimenLsid = group.get(DILUTION_INPUT_MATERIAL_DATA_PROPERTY).toString();

                ExpMaterial material = inputMaterialMap.get(specimenLsid);

                if (material == null)
                    throw new ExperimentException("The row must contain a value for the specimen lsid : " + DILUTION_INPUT_MATERIAL_DATA_PROPERTY);

                String dataRowLsid = getDataRowLSID(data, groupName, material.getPropertyValues()).toString();

                OntologyManager.ensureObject(container, dataRowLsid,  data.getLSID());
                int objectId = 0;

                // New code to insert into NAbSpecimen and CutoffValue tables instead of Ontology properties
                Map<String, Object> nabSpecimenEntries = new HashMap<>();
                nabSpecimenEntries.put(WELLGROUP_NAME_PROPERTY, groupName);
                nabSpecimenEntries.put("ObjectId", objectId);                       // TODO: this will go away  when nab table transfer is complete
                nabSpecimenEntries.put("ObjectUri", dataRowLsid);
                nabSpecimenEntries.put("ProtocolId", protocol.getRowId());
                nabSpecimenEntries.put("DataId", data.getRowId());
                nabSpecimenEntries.put("RunId", run.getRowId());
                nabSpecimenEntries.put("SpecimenLsid", group.get(DILUTION_INPUT_MATERIAL_DATA_PROPERTY));
                nabSpecimenEntries.put("FitError", group.get(FIT_ERROR_PROPERTY));
                nabSpecimenEntries.put("Auc_Poly", group.get(AUC_PREFIX + POLY_SUFFIX));
                nabSpecimenEntries.put("PositiveAuc_Poly", group.get(pAUC_PREFIX + POLY_SUFFIX));
                nabSpecimenEntries.put("Auc_4pl", group.get(AUC_PREFIX + PL4_SUFFIX));
                nabSpecimenEntries.put("PositiveAuc_4pl", group.get(pAUC_PREFIX + PL4_SUFFIX));
                nabSpecimenEntries.put("Auc_5pl", group.get(AUC_PREFIX + PL5_SUFFIX));
                nabSpecimenEntries.put("PositiveAuc_5pl", group.get(pAUC_PREFIX + PL5_SUFFIX));
                int nabRowid = DrugSensitivityManager.get().insertNabSpecimenRow(null, nabSpecimenEntries);

                for (Integer cutoffValue : cutoffFormats.keySet())
                {
                    Map<String, Object> cutoffEntries = new HashMap<>();
                    cutoffEntries.put("NabSpecimenId", nabRowid);
                    cutoffEntries.put("Cutoff", (double)cutoffValue);

                    String cutoffStr = cutoffValue.toString();
                    String icKey = POINT_IC_PREFIX + cutoffStr;
                    cutoffEntries.put("Point", group.get(icKey));
                    icKey = POINT_IC_PREFIX + cutoffStr + OOR_SUFFIX;
                    cutoffEntries.put("PointOORIndicator", group.get(icKey));
                    icKey = CURVE_IC_PREFIX + cutoffStr + POLY_SUFFIX;
                    cutoffEntries.put("IC_Poly", group.get(icKey));
                    icKey = CURVE_IC_PREFIX + cutoffStr + POLY_SUFFIX + OOR_SUFFIX;
                    cutoffEntries.put("IC_PolyOORIndicator", group.get(icKey));
                    icKey = CURVE_IC_PREFIX + cutoffStr + PL4_SUFFIX;
                    cutoffEntries.put("IC_4pl", group.get(icKey));
                    icKey = CURVE_IC_PREFIX + cutoffStr + PL4_SUFFIX + OOR_SUFFIX;
                    cutoffEntries.put("IC_4plOORIndicator", group.get(icKey));
                    icKey = CURVE_IC_PREFIX + cutoffStr + PL5_SUFFIX;
                    cutoffEntries.put("IC_5pl", group.get(icKey));
                    icKey = CURVE_IC_PREFIX + cutoffStr + PL5_SUFFIX + OOR_SUFFIX;
                    cutoffEntries.put("IC_5plOORIndicator", group.get(icKey));
                    DrugSensitivityManager.get().insertCutoffValueRow(null, cutoffEntries);
                }

                if (group.containsKey(STD_DEV_PROPERTY_NAME))
                {
                    // save the standard deviation for each drug well group
                    Lsid propertyURI = new Lsid(DRUG_SENSITIVITY_PROPERTY_LSID_PREFIX, protocol.getName(), STD_DEV_PROPERTY_NAME);
                    ObjectProperty prop = new ObjectProperty(dataRowLsid, container, propertyURI.toString(),
                            group.get(STD_DEV_PROPERTY_NAME), PropertyType.DOUBLE, STD_DEV_PROPERTY_NAME);
                    prop.setFormat("0.0");

                    OntologyManager.insertProperties(container, dataRowLsid, prop);
                }
            }
        }
        catch (SQLException | ValidationException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataType getDataType()
    {
        return DRUG_SENSITIVITY_DATA_TYPE;
    }

    @Override
    public void beforeDeleteData(List<ExpData> data) throws ExperimentException
    {
        try
        {
            DrugSensitivityManager.get().deleteRunData(data);
        }
        catch(SQLException e)
        {
            throw new ExperimentException(e);
        }
    }

    @Override
    public boolean isValidDataProperty(String propertyName)
    {
        if (STD_DEV_PROPERTY_NAME.equals(propertyName))
            return true;
        return super.isValidDataProperty(propertyName);
    }
}
