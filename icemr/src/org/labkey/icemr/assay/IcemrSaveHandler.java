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
package org.labkey.icemr.assay;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.exp.ExperimentException;
import org.labkey.api.exp.api.ExpData;
import org.labkey.api.exp.api.ExpExperiment;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.api.ExpRun;
import org.labkey.api.exp.api.ExperimentJSONConverter;
import org.labkey.api.query.ValidationException;
import org.labkey.api.study.assay.AssayRunUploadContext;
import org.labkey.api.study.assay.DefaultAssayRunCreator;
import org.labkey.api.study.assay.DefaultAssaySaveHandler;
import org.labkey.api.view.ViewContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
  * User: Dax
 * Date: 10/9/13
 * Time: 11:43 AM
  */
public class IcemrSaveHandler extends DefaultAssaySaveHandler
{
    public static final String SCIENTIST = "Scientist";
    public static final String SCIENTIST_LABEL = "Scientist";
    public static final String SCIENTIST_USER = "ScientistUser";

    //
    // The diagnostic assay should only send in a dataArray for the run
    // The species-specific assay may include a data input if a gel image is attached
    //
    // Since a new run is created there is no need to delete the existing run and recreate it.  Just save the new
    // run with any data inputs and attach the data rows
    //
    @Override
    public void handleProtocolApplications(ViewContext context, ExpProtocol protocol, ExpExperiment batch, ExpRun run, JSONArray inputDataArray,
        JSONArray dataArray, JSONArray inputMaterialArray, JSONObject runJsonObject, JSONArray outputDataArray,
        JSONArray outputMaterialArray) throws ExperimentException, ValidationException
    {
        if (inputMaterialArray.length() != 0 || outputDataArray.length() != 0 || outputMaterialArray.length() != 0)
        {
            throw new IllegalArgumentException("Unexpected input material or output data found while inserting results " +
                    "into the " + protocol.getName());
        }

        if (runJsonObject.has(ExperimentJSONConverter.ID))
        {
            throw new IllegalArgumentException("Invalid attempt to update an existing run in the " + protocol.getName());
        }

        List<Map<String, Object>> dataRows = dataArray != null ? dataArray.toMapList() : Collections.emptyList();
        Map<ExpData, String> outputData = new HashMap<>();
        ExpData newData = DefaultAssayRunCreator.generateResultData(context.getUser(), run.getContainer(), getProvider(), dataRows, (Map)outputData);

        if (dataArray != null)
        {
            AssayRunUploadContext uploadContext = createRunUploadContext(context, protocol, runJsonObject, dataRows,
                    getInputData(context, inputDataArray),
                    outputData,
                    Collections.emptyMap(),        // input materials
                    Collections.emptyMap());       // output materials

            saveExperimentRun(uploadContext, batch, run);
        }
    }
}
