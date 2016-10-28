/*
 * Copyright (c) 2015-2016 LabKey Corporation
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

package org.labkey.hdrl;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.labkey.api.attachments.InputStreamAttachmentFile;
import org.labkey.api.data.CompareType;
import org.labkey.api.data.Container;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.hdrl.query.HDRLQuerySchema;
import org.labkey.hdrl.query.LabWareQuerySchema;
import org.labkey.hdrl.view.InboundRequestBean;
import org.labkey.hdrl.view.InboundSpecimenBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HDRLManager
{
    private static final Logger LOG = Logger.getLogger(HDRLManager.class);
    private static final String HDRL_SENSITIVE_DATA_TIME_WINDOW = "hdrlSensitiveDataDeletionTimeWindow";
    private static final String NUM_OF_DAYS_KEY = "HDRLSensitiveDataDeletionWindow";
    private static final int DEFAULT_NUM_OF_DAYS = 30; //default number of days after which data will be deleted

    private static final HDRLManager _instance = new HDRLManager();

    private HDRLManager()
    {
        // prevent external construction with a private default constructor
    }

    public static HDRLManager get()
    {
        return _instance;
    }

    public InboundRequestBean getInboundRequest(User user, Container container, Integer requestId)
    {
        UserSchema schema = QueryService.get().getUserSchema(user, container, HDRLQuerySchema.NAME);
        SQLFragment sql = new SQLFragment("SELECT r.RequestId, r.ShippingNumber, s.Name as RequestStatus, c.Name as ShippingCarrier, t.Name as TestType FROM ");
        sql.append("(SELECT * FROM hdrl.InboundRequest WHERE (Container = ?) AND (RequestId = ?)) r ");
        sql.add(container);
        sql.add(requestId);
        sql.append("LEFT JOIN hdrl.ShippingCarrier c on r.ShippingCarrierId = c.RowId ")
                .append("LEFT JOIN hdrl.TestType t on r.TestTypeId = t.RowId ")
                .append("LEFT JOIN hdrl.RequestStatus s on r.RequestStatusId = s.RowId ");


        SqlSelector sqlSelector = new SqlSelector(schema.getDbSchema(), sql);
        return sqlSelector.getObject(InboundRequestBean.class);
    }

    public List<InboundSpecimenBean> getInboundSpecimen(int requestId)
    {
        String joinStatement = "SELECT hdrl.inboundspecimen.*, hdrl.gender.code as genderId, hdrl.familymemberprefix.code as fmpCode, hdrl.dutycode.code as ducCode, hdrl.sourceoftesting.code as sotCode" +
                " FROM hdrl.inboundspecimen" +
                " LEFT JOIN hdrl.gender" +
                " ON hdrl.inboundspecimen.genderId=hdrl.gender.rowid" +
                " LEFT JOIN hdrl.familymemberprefix" +
                " ON hdrl.inboundspecimen.fmpid=hdrl.familymemberprefix.rowid" +
                " LEFT JOIN hdrl.dutycode" +
                " ON hdrl.inboundspecimen.dutycodeid=hdrl.dutycode.rowid" +
                " LEFT JOIN hdrl.sourceoftesting" +
                " ON hdrl.inboundspecimen.testingsourceid=hdrl.sourceoftesting.rowid" +
                " WHERE hdrl.inboundspecimen.inboundrequestid = ?";

        SQLFragment sql = new SQLFragment();
        sql.append(joinStatement);
        sql.add(requestId);

        SqlSelector selector = new SqlSelector(HDRLSchema.getInstance().getTableInfoInboundSpecimen().getSchema(), sql);
        return selector.getArrayList(InboundSpecimenBean.class);
    }

    public static void saveProperties(HDRLController.SensitiveDataForm sensitiveDataForm)
    {
        PropertyManager.PropertyMap map = PropertyManager.getNormalStore().getWritableProperties(HDRL_SENSITIVE_DATA_TIME_WINDOW, true);
        map.clear();
        map.put(NUM_OF_DAYS_KEY, String.valueOf(sensitiveDataForm.getTimeWindowInDays()));
        map.save();
    }

    private static Map<String, String> getProperties()
    {
        return PropertyManager.getNormalStore().getProperties(HDRL_SENSITIVE_DATA_TIME_WINDOW);
    }

    public static int getNumberOfDays()
    {
        String days = getProperties().get(NUM_OF_DAYS_KEY);

        if (StringUtils.isEmpty(days))
        {
            return DEFAULT_NUM_OF_DAYS;
        }

        return Integer.parseInt(days);
    }

    /**
     * This method is used only for populating results in the Postgres Labware outbound tables for testing.
     * @param resultData - the data to populate the row in the table
     * @param user - current user
     * @param container - current container
     */
    public void insertLabWareOutboundRequest(HDRLController.LabWareOutboundRequestForm resultData, User user, Container container)
    {
        LabWareQuerySchema lwSchema = new LabWareQuerySchema(user, container);
        // create the request data for LabWare
        Map<String, Object> labWareData = new HashMap<>();
        labWareData.put("batch_id", resultData.getBatchId());
        labWareData.put("hdrl_status", resultData.getHdrlStatus());
        labWareData.put("customer_note", resultData.getCustomerNote());
        labWareData.put("date_received", resultData.getDateReceived());
        labWareData.put("date_completed", resultData.getDateCompleted());
        labWareData.put("date_modified", resultData.getDateModified());
        Table.insert(user, lwSchema.getDbSchema().getTable(LabWareQuerySchema.TABLE_OUTBOUND_REQUESTS), labWareData);
    }

    /**
     * This method is used only for populating results in the Postgres LabWare outbound tables for testing.
     * If a clinical report file is provided, this will not work when the labwareDataSource is an Oracle database
     * @param resultData - the data to populate the row in the table
     * @param user - current user
     * @param container - current container
     * @throws FileNotFoundException if the name of the clinical report file supplied is not available
     */
    public void insertLabWareOutboundSpecimen(HDRLController.LabWareOutboundSpecimenForm resultData, User user, Container container) throws FileNotFoundException
    {
        LabWareQuerySchema lwSchema = new LabWareQuerySchema(user, container);
        // create the request data for LabWare
        Map<String, Object> labWareData = new HashMap<>();
        labWareData.put("test_request_id", resultData.getTestRequestId());
        labWareData.put("hdrl_status", resultData.getHdrlStatus());
        labWareData.put("batch_id", resultData.getBatchId());
        labWareData.put("date_received", resultData.getDateReceived());
        labWareData.put("date_completed", resultData.getDateCompleted());
        labWareData.put("date_modified", resultData.getDateModified());
        labWareData.put("sample_integrity", resultData.getSampleIntegrity());
        labWareData.put("test_result", resultData.getTestResult());
        labWareData.put("customer_code", resultData.getCustomerCode());
        labWareData.put("modified_result_flag", resultData.getModifiedResultFlag());
        labWareData.put("report_file_name", resultData.getReportFileName());

        if (resultData.getClinicalReportFile() != null)
        {
            FileInputStream stream = new FileInputStream(new File(resultData.getClinicalReportFile()));
            InputStreamAttachmentFile attachment = new InputStreamAttachmentFile(stream, resultData.getClinicalReportFile());
            labWareData.put("clinical_report", attachment);
        }
        Table.insert(user, lwSchema.getDbSchema().getTable(LabWareQuerySchema.TABLE_OUTBOUND_SPECIMENS), labWareData);
    }

    public boolean hasClinicalReport(int specimenId, User user, Container container)
    {
        LabWareQuerySchema lwSchema = new LabWareQuerySchema(user, container);
        SimpleFilter filter = new SimpleFilter(FieldKey.fromParts("test_request_id"), specimenId);
        filter.addCondition(FieldKey.fromParts("clinical_report"), null, CompareType.NONBLANK);

        return new TableSelector(lwSchema.getSchema().getTable(LabWareQuerySchema.TABLE_OUTBOUND_SPECIMENS), filter, null).exists();
    }
}