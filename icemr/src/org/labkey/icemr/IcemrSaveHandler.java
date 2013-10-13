/*
 * Copyright (c) 2013 LabKey Corporation
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
package org.labkey.icemr;

import org.json.JSONException;
import org.json.JSONObject;
import org.labkey.api.exp.ExperimentException;
import org.labkey.api.exp.api.ExpExperiment;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.api.ExpRun;
import org.labkey.api.query.ValidationException;
import org.labkey.api.study.assay.DefaultAssaySaveHandler;
import org.labkey.api.view.ViewContext;

import java.sql.SQLException;

/**
  * User: Dax
 * Date: 10/9/13
 * Time: 11:43 AM
  */
public class IcemrSaveHandler extends DefaultAssaySaveHandler
{
    @Override
    public ExpRun handleRun(ViewContext context, JSONObject runJsonObject, ExpProtocol protocol, ExpExperiment batch) throws JSONException, ValidationException, ExperimentException, SQLException
    {
        setDeleteProtocolApplications(false);
        return super.handleRun(context, runJsonObject, protocol, batch);
    }
}
