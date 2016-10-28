/*
 * Copyright (c) 2013-2016 LabKey Corporation
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
import org.json.JSONException;
import org.json.JSONObject;
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.TableInfo;
import org.labkey.api.dataiterator.DataIterator;
import org.labkey.api.dataiterator.DataIteratorContext;
import org.labkey.api.dataiterator.QueryDataIteratorBuilder;
import org.labkey.api.exp.ExperimentException;
import org.labkey.api.exp.api.ExpData;
import org.labkey.api.exp.api.ExpExperiment;
import org.labkey.api.exp.api.ExpMaterial;
import org.labkey.api.exp.api.ExpObject;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.api.ExpRun;
import org.labkey.api.exp.api.ExperimentJSONConverter;
import org.labkey.api.exp.property.DomainProperty;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.SchemaKey;
import org.labkey.api.query.UserSchema;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.study.assay.AssayProvider;
import org.labkey.api.study.assay.AssaySaveHandler;
import org.labkey.api.view.ViewContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
  * User: Dax
 * Date: 10/9/13
 * Time: 11:43 AM
  */

//
// Adapation specific protocol behavior for the Tracking Assay save handler
//
public class AdaptationSaveHandler implements AssaySaveHandler
{
    public static final String PROTOCOL_NAME = "Culture Adaptation";
    public static final String SAMPLE_ID = "SampleID";

    private static final String FINISH_PARASITEMIA = "FinishParasitemia";
    private static final String ADAPTATION_SCHEMA = "assay.Tracking.Culture Adaptation";
    private static final String CHECK_ADAPTATION_QUERY = "adapted_numdays";
    private static final String ADAPTATION_DATE = "AdaptationDate";
    private static final String SUCCESSFUL_ADAPTATION = "SuccessfulAdaptation";
    private static final String MAINTENANCE_DATE = "MaintenanceDate";
    private static final String ADAPTED = "Yes";
    private static final String ADAPTATION_FLASKS = "Adaptation Flasks";
    private static final String INVALID_ACTION = "Invalid call to the AdaptationSaveHandler";
    private static final Map<String, Object> CHECK_ADAPTATION_PARAMS;
    private Set<String> _samplesToCheckForAdaptation;
    static
    {
        Map<String, Object> mappings = new HashMap<>();
        mappings.put("isSelectionFlask", 0);
        CHECK_ADAPTATION_PARAMS = Collections.unmodifiableMap(mappings);
    }

    public AdaptationSaveHandler()
    {
        _samplesToCheckForAdaptation = new HashSet<>();
    }

    //
    // if any growth test finished with this material then add it to list of materials
    // that we need to check to see if they adapted
    //
    @Override
    public ExpMaterial handleMaterial(ViewContext context, JSONObject materialObject) throws ValidationException
    {
        if (materialObject.has(ExperimentJSONConverter.PROPERTIES))
        {
            JSONObject materialProperties = materialObject.getJSONObject(ExperimentJSONConverter.PROPERTIES);
            if (materialProperties.size() > 0)
            {
                for (int test = 1; test < 4; test ++)
                {
                    String growthTest = FINISH_PARASITEMIA + String.valueOf(test);
                    if (materialProperties.has(growthTest) && (null != materialProperties.get(growthTest)))
                    {
                        _samplesToCheckForAdaptation.add(materialProperties.getString(SAMPLE_ID));
                        break;
                    }
                }
            }
        }

        return null;
    }

    //
    // If we have samples that we need to check for adaptation
    // then do so here.  If a flask adapted then save the maintenance date
    // as the adaptation date.
    //
    @Override
    public void afterSave(ViewContext context, List<? extends ExpExperiment> batches, ExpProtocol protocol) throws Exception
    {
        if (_samplesToCheckForAdaptation.size() > 0)
        {
            List<Map<String, Object>> samples = getSamplesToUpdate(context.getContainer(), context.getUser());
            if (samples.size() > 0)
            {
                TableInfo table = getFlasksTableInfo(context.getContainer(), context.getUser());
                QueryUpdateService qus = table.getUpdateService();


                try (DbScope.Transaction transaction = table.getSchema().getScope().ensureTransaction())
                {
                    qus.updateRows(context.getUser(), context.getContainer(), samples, samples, null, null);
                    transaction.commit();
                }
            }
        }
    }

    // return the Culture Adaptation sample set table
    private TableInfo getFlasksTableInfo(Container c, User u)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, "Samples");
        return schema.getTable(ADAPTATION_FLASKS);
    }

    private Map<String, Integer> buildColumnMap(DataIterator it)
    {
        Map<String, Integer> indices = new CaseInsensitiveHashMap<>();

        for (int i = 0; i < it.getColumnCount(); i++)
        {
            ColumnInfo ci = it.getColumnInfo(i);
            indices.put(ci.getColumnName(), i);
        }

        return indices;
    }

    private List<Map<String, Object>> getSamplesToUpdate(Container c, User u) throws Exception
    {
        QueryDataIteratorBuilder qb = new QueryDataIteratorBuilder(c, u, SchemaKey.fromString(ADAPTATION_SCHEMA), CHECK_ADAPTATION_QUERY, null, null);
        qb.setParameters(CHECK_ADAPTATION_PARAMS);

        List<Map<String, Object>> samples = new ArrayList<>();

        try ( DataIterator it = qb.getDataIterator(new DataIteratorContext()))
        {
            Map<String, Integer> indices = buildColumnMap(it);

            while (it.next())
            {
                String sampleId = String.valueOf(it.get(indices.get(SAMPLE_ID)));
                if (_samplesToCheckForAdaptation.contains(sampleId))
                {
                    // a flask that just adapted will have a null AdaptationDate but
                    // the query will return True for SuccessfulAdaptation
                    if (null == it.get(indices.get(ADAPTATION_DATE)) &&
                            (ADAPTED.equalsIgnoreCase(String.valueOf(it.get(indices.get(SUCCESSFUL_ADAPTATION))))))
                    {
                        Map<String, Object> sample = new CaseInsensitiveHashMap<>();
                        sample.put(SAMPLE_ID, sampleId);
                        sample.put(ADAPTATION_DATE, it.get(indices.get(MAINTENANCE_DATE)));
                        samples.add(sample);
                    }
                }
            }
        }
        return samples;
    }

    public void setProvider(AssayProvider provider)
    { throw new IllegalStateException(INVALID_ACTION); }
    public AssayProvider getProvider()
    { throw new IllegalStateException(INVALID_ACTION); }
    public ExpExperiment handleBatch(ViewContext context, JSONObject batchJson, ExpProtocol protocol) throws Exception
    { throw new IllegalStateException(INVALID_ACTION); }
    public ExpRun handleRun(ViewContext context, JSONObject runJson, ExpProtocol protocol, ExpExperiment batch) throws JSONException, ValidationException, ExperimentException, SQLException
    { throw new IllegalStateException(INVALID_ACTION); }
    public ExpData handleData(ViewContext context, JSONObject dataJson) throws ValidationException
    { throw new IllegalStateException(INVALID_ACTION); }
    public void handleProperties(ViewContext context, ExpObject object, List<? extends DomainProperty> dps, JSONObject propertiesJson) throws ValidationException, JSONException
    { throw new IllegalStateException(INVALID_ACTION); }
    public void beforeSave(ViewContext context, JSONObject rootJson, ExpProtocol protocol)throws Exception
    { throw new IllegalStateException(INVALID_ACTION); }
    public void handleProtocolApplications(ViewContext context, ExpProtocol protocol, ExpExperiment batch, ExpRun run, JSONArray inputDataArray,
        JSONArray dataArray, JSONArray inputMaterialArray, JSONObject runJsonObject, JSONArray outputDataArray,
        JSONArray outputMaterialArray) throws ExperimentException, ValidationException
    { throw new IllegalStateException(INVALID_ACTION); }

}
