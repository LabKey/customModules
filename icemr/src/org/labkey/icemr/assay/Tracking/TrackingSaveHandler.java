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
import org.labkey.api.study.assay.AssayRunUploadContext;
import org.labkey.api.study.assay.AssaySaveHandler;
import org.labkey.api.study.assay.DefaultAssayRunCreator;
import org.labkey.api.view.ViewBackgroundInfo;
import org.labkey.api.view.ViewContext;
import org.labkey.icemr.assay.IcemrSaveHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
        if (protocol.getName().equalsIgnoreCase(AdaptationSaveHandler.PROTOCOL_NAME))
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
    public void afterSave(ViewContext context, List<? extends ExpExperiment> batches, ExpProtocol protocol) throws Exception
    {
        if (null != _delegate)
            _delegate.afterSave(context, batches, protocol);
    }

    @Override
    public void handleProtocolApplications(ViewContext context, ExpProtocol protocol, ExpExperiment batch, ExpRun run,
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
            List<Map<String, Object>> dataRows = dataArray != null ? dataArray.toMapList() : Collections.emptyList();
            ExpData newData = DefaultAssayRunCreator.generateResultData(context.getUser(), run.getContainer(), getProvider(), dataRows, (Map)outputData);

            if (dataArray != null)
            {
                AssayRunUploadContext uploadContext = createRunUploadContext(context, protocol, runJsonObject, dataRows,
                        Collections.emptyMap(),            // input data
                        outputData,
                        inputMaterial,
                        Collections.emptyMap());       // output materials

                saveExperimentRun(uploadContext, batch, run);
            }
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
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                new ViewBackgroundInfo(context.getContainer(), context.getUser(), context.getActionURL()), LOG, false);
        }
    }
}
