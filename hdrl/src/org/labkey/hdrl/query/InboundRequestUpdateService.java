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
package org.labkey.hdrl.query;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.Results;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.DefaultQueryUpdateService;
import org.labkey.api.query.DuplicateKeyException;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.hdrl.HDRLSchema;
import org.labkey.hdrl.view.InboundSpecimenBean;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by klum on 4/14/2015.
 */
public class InboundRequestUpdateService extends DefaultQueryUpdateService
{
    public InboundRequestUpdateService(TableInfo queryTable, TableInfo dbTable)
    {
        super(queryTable, dbTable);
    }

    @Override
    protected Map<String, Object> insertRow(User user, Container container, Map<String, Object> row) throws DuplicateKeyException, ValidationException, QueryUpdateServiceException, SQLException
    {
        HDRLQuerySchema lkSchema = new HDRLQuerySchema(user, container);

        try (DbScope.Transaction lkTransaction = lkSchema.getSchema().getScope().ensureTransaction())
        {
            boolean isSubmit = ((int) row.get("requestStatusId")) == 2; // this is a "Submitted" status from the requestStatus lookup table
            if (isSubmit)
            {
                row.put("SubmittedBy", user.getUserId());
                row.put("Submitted", new Date());
            }
            Map<String, Object> insertedRow = super.insertRow(user, container, row);
            if (isSubmit)
            {
                pushDataToLabWare(user, container, insertedRow);
            }
            lkTransaction.commit();
            return insertedRow;
        }

    }

    @Override
    protected Map<String, Object> updateRow(final User user, final Container container, Map<String, Object> row, @NotNull Map<String, Object> oldRow) throws InvalidKeyException, ValidationException, QueryUpdateServiceException, SQLException
    {
        HDRLQuerySchema lkSchema = new HDRLQuerySchema(user, container);

        try (DbScope.Transaction lkTransaction = lkSchema.getSchema().getScope().ensureTransaction())
        {
            boolean isNewSubmit = (boolean) row.get("isNewSubmit");
            if (isNewSubmit)
            {
                validateUniqueness((Integer) row.get("requestId"));
                row.put("SubmittedBy", user.getUserId());
                row.put("Submitted", new Date());
            }
            final Map<String, Object> updatedRow = super.updateRow(user, container, row, oldRow);
            if (isNewSubmit)
            {
                pushDataToLabWare(user, container, updatedRow);
            }
            lkTransaction.commit();
            return updatedRow;
        }
    }

    public static void validateUniqueness(Integer requestId) throws QueryUpdateServiceException
    {
        // check that there are no duplicate barcodes
        List<String> duplicates = findDuplicates(requestId, "CustomerBarcode");
        StringBuilder message = new StringBuilder();
        if (duplicates.size() > 0)
        {
            message.append("Request has specimens with duplicate fields: Customer Barcode - ").append(StringUtils.join(duplicates, ", "));
        }

        // check that all existing DODId in the specimens in this request are unique
        duplicates = findDuplicates(requestId, "DoDId");
        if (duplicates.size() > 0)
        {
            if (message.length() == 0)
                message.append("Request has specimens with duplicate fields: ");
            else
                message.append("; ");
            message.append("DoDID - ").append(StringUtils.join(duplicates, ", "));
        }
        // check that all the existing SSN + FMP pairs in this request are unique
        duplicates = findDuplicates(requestId, "SSN, FMPId");
        if (duplicates.size() > 0)
        {
            if (message.length() == 0)
                message.append("Request has specimens with duplicate fields: ");
            else
                message.append("; ");
            message.append("SSN + FMP - ").append(StringUtils.join(duplicates, ", "));
        }

        if (message.length() > 0)
            throw new QueryUpdateServiceException(message.toString());

    }

    /**
     * Returns a list of duplicates for the combination of fields given where the field values are not null.
     *
     * @param requestId id of the test request
     * @param fields comma-separated list of fields to check for unique combinations of
     * @return value of the fields that are duplicates; for the case of multiple fields the value of the first field in @fields will be in the returned list
     */
    private static List<String> findDuplicates(Integer requestId, String fields)
    {
        SQLFragment sql = new SQLFragment("SELECT ")
                .append(fields).append(" FROM hdrl.inboundspecimen WHERE inboundrequestid = ").append(requestId);
        for (String field : fields.split(", "))
        {
            sql.append(" AND ").append(field).append(" IS NOT NULL ");
        }
        sql.append(" GROUP BY ").append(fields)
                .append(" HAVING COUNT(*) > 1 ");
        SqlSelector sqlSelector = new SqlSelector(HDRLSchema.getInstance().getScope(), sql);
        return sqlSelector.getArrayList(String.class);

    }

    private String getLookupValue(HDRLQuerySchema schema, String tableName, Integer rowId, String fieldName) throws SQLException
    {
        if (rowId == null)
            return null;

        SimpleFilter filter = new SimpleFilter(FieldKey.fromParts("RowId"), rowId);
        TableSelector selector = new TableSelector(schema.getTable(tableName), PageFlowUtil.set(fieldName), filter, null);
        try (Results results = selector.getResults())
        {
            results.next();
            String value = results.getString(FieldKey.fromParts(fieldName));
            return value;
        }

    }

    private void pushDataToLabWare(User user, Container container, Map<String, Object> labkeyTableRow) throws SQLException
    {
        HDRLQuerySchema lkSchema = new HDRLQuerySchema(user, container);

        // create the request data for LabWare
        Map<String, Object> labWareData = new HashMap<>();
        labWareData.put("Batch_ID", labkeyTableRow.get("RequestId"));
        labWareData.put("Carrier", getLookupValue(lkSchema, HDRLQuerySchema.TABLE_SHIPPING_CARRIER, (Integer) labkeyTableRow.get("ShippingCarrierId"), "Name"));
        labWareData.put("Tracking_Number", labkeyTableRow.get("ShippingNumber"));
        labWareData.put("Customer", user.getEmail());

        // now get the specimen rows using the requestId from the labKeyTable
        SimpleFilter filter = new SimpleFilter(FieldKey.fromParts("InboundRequestId"), labkeyTableRow.get("RequestId"));
        TableSelector selector = new TableSelector(lkSchema.getTable(HDRLQuerySchema.TABLE_INBOUND_SPECIMEN), filter, null);
        List<InboundSpecimenBean> specimens = selector.getArrayList(InboundSpecimenBean.class);

        LabWareQuerySchema lwSchema = new LabWareQuerySchema(user, container);

        try (DbScope.Transaction lwTransaction = lwSchema.getSchema().getScope().ensureTransaction())
        {
            Table.insert(user, lwSchema.getDbSchema().getTable(LabWareQuerySchema.TABLE_INBOUND_REQUESTS), labWareData);
            for (InboundSpecimenBean specimen : specimens)
            {
                Map<String, Object> lwSpecimen = new HashMap<>();
                lwSpecimen.put("Test_Request_ID", specimen.getRowId());
                lwSpecimen.put("Batch_ID", specimen.getInboundRequestId());
                lwSpecimen.put("Test_Requested", getLookupValue(lkSchema, HDRLQuerySchema.TABLE_TEST_TYPE, (Integer) labkeyTableRow.get("TestTypeId"), "Name"));
                lwSpecimen.put("Cust_Barcode", specimen.getCustomerBarCode());
                lwSpecimen.put("FMP", getLookupValue(lkSchema, HDRLQuerySchema.TABLE_FAMILY_MEMBER_PREFIX, specimen.getFmpId(), "Code"));
                lwSpecimen.put("SSN", specimen.getSsn());
                lwSpecimen.put("Draw_Date", specimen.getDrawDate());
                lwSpecimen.put("Specimen_Type", specimen.getSpecimenType());
                lwSpecimen.put("Num_Containers", specimen.getNumberOfContainers());
                lwSpecimen.put("SOT",  getLookupValue(lkSchema, HDRLQuerySchema.TABLE_SOURCE_OF_TESTING, specimen.getTestingSourceId(), "Code"));
                lwSpecimen.put("DUC", getLookupValue(lkSchema, HDRLQuerySchema.TABLE_DUTY_CODE, specimen.getDutyCodeId(), "Code"));
                lwSpecimen.put("DOD_ID", specimen.getDodId());
                lwSpecimen.put("First_Name", specimen.getFirstName());
                lwSpecimen.put("Middle_Name", specimen.getMiddleName());
                lwSpecimen.put("Last_Name", specimen.getLastName());
                lwSpecimen.put("Birth_Date", specimen.getBirthDate());
                lwSpecimen.put("Gender", specimen.getGenderId());
                lwSpecimen.put("Initials", specimen.getInitials());

                Table.insert(user, lwSchema.getDbSchema().getTable(LabWareQuerySchema.TABLE_INBOUND_SPECIMENS), lwSpecimen);
            }
            lwTransaction.commit();
        }
    }


}
