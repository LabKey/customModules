package org.labkey.icemr.assay.Tracking;

import org.json.JSONException;
import org.json.JSONObject;
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
import org.labkey.api.etl.DataIterator;
import org.labkey.api.etl.DataIteratorContext;
import org.labkey.api.etl.QueryDataIteratorBuilder;
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
    public static final String ProtocolName = "Culture Adaptation";

    private static final String FinishParasitemia = "FinishParasitemia";
    private static final String AdaptationSchema = "assay.Tracking.Culture Adaptation";
    private static final String CheckAdaptationQuery = "adapted_numdays";
    private static final String AdaptationDate = "AdaptationDate";
    private static final String SuccessfulAdaptation = "SuccessfulAdaptation";
    private static final String MaintenanceDate = "MaintenanceDate";
    private static final String Adapted = "Yes";
    private static final String AdaptationFlasks = "Adaptation Flasks";
    private static final String SampleId = "SampleID";
    private static final Map<String, Object> CheckAdaptationParams;
    private Set<String> _samplesToCheckForAdaptation;
    static
    {
        Map<String, Object> mappings = new HashMap<>();
        mappings.put("isSelectionFlask", 0);
        CheckAdaptationParams = Collections.unmodifiableMap(mappings);
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
                    String growthTest = FinishParasitemia + String.valueOf(test);
                    if (materialProperties.has(growthTest) && (null != materialProperties.get(growthTest)))
                    {
                        _samplesToCheckForAdaptation.add(materialProperties.getString(SampleId));
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
    public void afterSave(ViewContext context, ExpExperiment batch, ExpProtocol protocol) throws Exception
    {
        if (_samplesToCheckForAdaptation.size() > 0)
        {
            List<Map<String, Object>> samples = getSamplesToUpdate(context.getContainer(), context.getUser());

            // if we do have to update the samples, then do so here
            if (samples.size() > 0)
            {
                TableInfo table = getFlasksTableInfo(context.getContainer(), context.getUser());
                QueryUpdateService qus = table.getUpdateService();

                table.getSchema().getScope().ensureTransaction();
                try
                {
                    //UNDONE: do we need to enforce author permissions here or something?
                    List<Map<String, Object>> updatedRows = qus.updateRows(context.getUser(), context.getContainer(), samples, samples, null);
                    table.getSchema().getScope().commitTransaction();
                }
                finally
                {
                    table.getSchema().getScope().closeConnection();
                }
            }
        }
    }

    // return the Culture Adaptation sample set table
    private TableInfo getFlasksTableInfo(Container c, User u)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, "Samples");
        return schema.getTable(AdaptationFlasks);
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
        QueryDataIteratorBuilder qb = new QueryDataIteratorBuilder(c, u, SchemaKey.fromString(AdaptationSchema), CheckAdaptationQuery, null, null);
        qb.setParameters(CheckAdaptationParams);

        List<Map<String, Object>> samples = new ArrayList<>();

        try ( DataIterator it = qb.getDataIterator(new DataIteratorContext()))
        {
            Map<String, Integer> indices = buildColumnMap(it);

            while (it.next())
            {
                String sampleId = String.valueOf(it.get(indices.get(SampleId)));
                if (_samplesToCheckForAdaptation.contains(sampleId))
                {
                    // a flask that just adapted will have a null AdaptationDate but
                    // the query will return True for SuccessfulAdaptation
                    if (null == it.get(indices.get(AdaptationDate)) &&
                            (Adapted.equalsIgnoreCase(String.valueOf(it.get(indices.get(SuccessfulAdaptation))))))
                    {
                        Map<String, Object> sample = new CaseInsensitiveHashMap<>();
                        sample.put(SampleId, sampleId);
                        sample.put(AdaptationDate, it.get(indices.get(MaintenanceDate)));
                        samples.add(sample);
                    }
                }
            }
        }
        return samples;
    }

    public void setProvider(AssayProvider provider)
    { throw new IllegalStateException("invalid call to this SaveHandler"); }
    public AssayProvider getProvider()
    { throw new IllegalStateException("invalid call to this SaveHandler"); }
    public ExpExperiment handleBatch(ViewContext context, JSONObject batchJson, ExpProtocol protocol) throws Exception
    { throw new IllegalStateException("invalid call to this SaveHandler"); }
    public ExpRun handleRun(ViewContext context, JSONObject runJson, ExpProtocol protocol, ExpExperiment batch) throws JSONException, ValidationException, ExperimentException, SQLException
    { throw new IllegalStateException("invalid call to this SaveHandler"); }
    public ExpData handleData(ViewContext context, JSONObject dataJson) throws ValidationException
    { throw new IllegalStateException("invalid call to this SaveHandler"); }
    public void handleProperties(ViewContext context, ExpObject object, DomainProperty[] dps, JSONObject propertiesJson) throws ValidationException, JSONException
    { throw new IllegalStateException("invalid call to this SaveHandler"); }
    public void beforeSave(ViewContext context, JSONObject rootJson, ExpProtocol protocol)throws Exception
    { throw new IllegalStateException("invalid call to this SaveHandler"); }
}
