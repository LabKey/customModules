package org.labkey.icemr.assay;

import org.labkey.api.assay.dilution.DilutionAssayProvider;
import org.labkey.api.assay.dilution.DilutionAssayRun;
import org.labkey.api.assay.dilution.DilutionCurve;
import org.labkey.api.assay.dilution.DilutionDataHandler;
import org.labkey.api.assay.dilution.DilutionMaterialKey;
import org.labkey.api.assay.dilution.DilutionSummary;
import org.labkey.api.exp.api.DataType;
import org.labkey.api.exp.api.ExpData;
import org.labkey.api.exp.api.ExpRun;
import org.labkey.api.security.User;
import org.labkey.api.study.Plate;
import org.labkey.api.study.WellGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: klum
 * Date: 5/13/13
 */
public class DrugSensitivityAssayRun extends DilutionAssayRun
{
    protected Plate _plate;
    private DilutionSummary[] _dilutionSummaries;

    public DrugSensitivityAssayRun(DilutionAssayProvider provider, ExpRun run, Plate plate,
                                   User user, List<Integer> cutoffs, DilutionCurve.FitType renderCurveFitType)
    {
        super(provider, run, user, cutoffs, renderCurveFitType);

        _plate = plate;
        List<? extends WellGroup> specimenGroups = _plate.getWellGroups(WellGroup.Type.SPECIMEN);
        _dilutionSummaries = getDilutionSumariesForWellGroups(specimenGroups);
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
            List<SampleResult> sampleResults = new ArrayList<SampleResult>();

            DilutionDataHandler handler = _provider.getDataHandler();
            DataType dataType = handler.getDataType();

            ExpData[] outputDatas = _run.getOutputDatas(null); //handler.getDataType());
            ExpData outputObject = null;
            if (outputDatas.length == 1 && outputDatas[0].getDataType() == dataType)
            {
                outputObject = outputDatas[0];
            }
            else if (outputDatas.length > 1)
            {
                // If there is a transformed dataType, use that
                ExpData dataWithHandlerType = null;
                ExpData dataWithTransformedType = null;
                for (ExpData expData : outputDatas)
                {
                    if (dataType.equals(expData.getDataType()))
                    {
                        if (null != dataWithHandlerType)
                            throw new IllegalStateException("Expected a single data file output for this NAb run. Found at least 2 expDatas with the expected datatype and a total of " + outputDatas.length);
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
                throw new IllegalStateException("Expected a single data file output for this NAb run, but none matching the expected datatype found. Found a total of " + outputDatas.length);

            Map<String, DilutionResultProperties> allProperties = getSampleProperties(outputObject);
            Set<String> captions = new HashSet<String>();
            boolean longCaptions = false;

            for (DilutionSummary summary : getSummaries())
            {
                if (!summary.isBlank())
                {
                    DilutionMaterialKey key = summary.getMaterialKey();
                    String shortCaption = key.getDisplayString(false);
                    if (captions.contains(shortCaption))
                        longCaptions = true;
                    captions.add(shortCaption);

                    DilutionResultProperties props = allProperties.get(getSampleKey(summary));
                    sampleResults.add(new SampleResult(_provider, outputObject, summary, key, props.getSampleProperties(), props.getDataProperties()));
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
    public DilutionSummary[] getSummaries()
    {
        return _dilutionSummaries;
    }

    @Override
    public List<Plate> getPlates()
    {
        return Collections.singletonList(_plate);
    }
}
