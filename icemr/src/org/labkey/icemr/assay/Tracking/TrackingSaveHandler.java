package org.labkey.icemr.assay.Tracking;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.exp.ExperimentException;
import org.labkey.api.exp.api.ExpData;
import org.labkey.api.exp.api.ExpExperiment;
import org.labkey.api.exp.api.ExpMaterial;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.api.ExpRun;
import org.labkey.api.exp.api.ExperimentJSONConverter;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.query.ValidationException;

import org.labkey.api.study.assay.AssaySaveHandler;
import org.labkey.api.view.ViewBackgroundInfo;
import org.labkey.api.view.ViewContext;
import org.labkey.icemr.assay.IcemrSaveHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
  * User: Dax
 * Date: 10/9/13
 * Time: 11:43 AM
  */

//
// The TrackingSaveHandler handles both Drug Selection and Culture Adaptation protocols.
// Currently no specific behavior is needed for Drug Selection.  Code in this class
// should be specific to both protocols.
//
public class TrackingSaveHandler extends IcemrSaveHandler
{
    private AssaySaveHandler _delegate;

    public TrackingSaveHandler()
    {
    }

    @Override
    public void beforeSave(ViewContext context, JSONObject rootJson, ExpProtocol protocol)
    {
        if (protocol.getName().equalsIgnoreCase(AdaptationSaveHandler.ProtocolName))
        {
            _delegate = new AdaptationSaveHandler();
        }
    }

    @Override
    public ExpMaterial handleMaterial(ViewContext context, JSONObject materialObject) throws ValidationException
    {
        //
        // any material we see for either Day 0 or Daily uploads must have already been inserted into the
        // sample set before we associate it with the batch
        //
        if (!materialObject.has(ExperimentJSONConverter.ID))
            throw new IllegalArgumentException("Invalid attempt to update this run with an invalid material/flask.");

        if (null != _delegate)
            _delegate.handleMaterial(context, materialObject);

        return super.handleMaterial(context, materialObject);
    }

    @Override
    public void afterSave(ViewContext context, ExpExperiment batch, ExpProtocol protocol) throws Exception
    {
        if (null != _delegate)
            _delegate.afterSave(context, batch, protocol);
    }

    @Override
    public void handleProtocolApplications(ViewContext context, ExpProtocol protocol, ExpRun run,
                                           JSONArray inputDataArray, JSONArray dataArray,
                                           JSONArray inputMaterialArray, JSONObject runJsonObject,
                                           JSONArray outputDataArray, JSONArray outputMaterialArray)
            throws ExperimentException, ValidationException
    {
        if (inputDataArray.length() != 0 || outputDataArray.length() != 0 || outputMaterialArray.length() != 0)
        {
            throw new IllegalArgumentException("Unexpected input data or output data found while inserting results " +
                    "into the " + protocol.getName());
        }

        //
        // This call will verify that the material already exists in the sample set. See
        // the HandleMaterial override above in this class
        //
        Map<ExpMaterial, String> inputMaterial = getInputMaterial(context, inputMaterialArray);

        if (runJsonObject.has(ExperimentJSONConverter.ID))
        {
            //
            // Verify that we are updating the run with flasks that are already associated
            // with this run.  It is invalid to assocate a new flask with a run after the
            // day 0 upload (the UI doesn't allow it).
            //
            Map<ExpMaterial, String> existingMaterial = run.getMaterialInputs();
            for (Map.Entry<ExpMaterial, String> entry : inputMaterial.entrySet())
            {
                if (!existingMaterial.containsKey(entry.getKey()))
                    throw new IllegalArgumentException("Invalid attempt to update experiment with invalid flask");
            }

            //
            // clear out any old data results
            //
            clearOutputDatas(context, run);

            //
            // save the run with a new data object and import all the rows
            //
            Map<ExpData, String> outputData = new HashMap<>();
            ExpData newData = generateResultData(context, run, dataArray, outputData);

            run = ExperimentService.get().saveSimpleExperimentRun(run,
                    inputMaterial,
                    Collections.<ExpData, String>emptyMap(),
                    Collections.<ExpMaterial, String>emptyMap(),
                    outputData,
                    Collections.<ExpData, String>emptyMap(),
                    new ViewBackgroundInfo(context.getContainer(), context.getUser(), context.getActionURL()), LOG, false);

            importRows(context, run, newData, protocol, runJsonObject, dataArray);
        }
        else
        {
            //
            // Day 0 Upload
            //
            if (dataArray.length() != 0)
            {
                throw new IllegalArgumentException("Unexpected data rows on Day 0 upload for the " + protocol.getName());
            }

            //
            // no data so just save the experiment
            //
            ExperimentService.get().saveSimpleExperimentRun(run,
                inputMaterial,
                Collections.<ExpData, String>emptyMap(),
                Collections.<ExpMaterial, String>emptyMap(),
                Collections.<ExpData, String>emptyMap(),
                Collections.<ExpData, String>emptyMap(),
                new ViewBackgroundInfo(context.getContainer(), context.getUser(), context.getActionURL()), LOG, false);
        }
    }
}