/*
 * Copyright (c) 2015 LabKey Corporation
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.ContainerForeignKey;
import org.labkey.api.data.DatabaseTableType;
import org.labkey.api.data.ForeignKey;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.Permission;

/**
 * Created by susanh on 4/13/15.
 */
public class InboundRequestTable extends FilteredTable<HDRLQuerySchema>
{
    public InboundRequestTable(HDRLQuerySchema schema)
    {
        super(schema.getDbSchema().getTable(schema.TABLE_INBOUND_REQUEST), schema);

        // wrap all the existing columns
        wrapAllColumns(true);

        ColumnInfo containerCol = getColumn("Container");
        ContainerForeignKey.initColumn(containerCol, schema);
        containerCol.setLabel("Folder");

        ColumnInfo archivedRequestCountCol = getColumn("ArchivedRequestCount");
        archivedRequestCountCol.setHidden(true);

        // add column for the number of patients
        addColumn(wrapColumn("Number of Patients", new PatientCountColumn(getRealTable().getColumn("RequestId"))));
//        addCoalescedStatusColumn();
        addCustomerNoteColumn();
        addReceivedColumn();
        addCompletedColumn();
    }

    private void addCustomerNoteColumn()
    {
        SQLFragment sql = new SQLFragment("(SELECT customerNote FROM " + HDRLQuerySchema.NAME + "." + HDRLQuerySchema.TABLE_REQUEST_RESULT + " R  WHERE R.RequestId = " + ExprColumn.STR_TABLE_ALIAS + ".requestId)");
        ExprColumn col = new ExprColumn(this, "Customer Note", sql, JdbcType.VARCHAR);
        addColumn(col);
    }

    private void addReceivedColumn()
    {
        SQLFragment sql = new SQLFragment("(SELECT received FROM " + HDRLQuerySchema.NAME + "." + HDRLQuerySchema.TABLE_REQUEST_RESULT + " R  WHERE R.RequestId = " + ExprColumn.STR_TABLE_ALIAS + ".requestId)");
        ExprColumn col = new ExprColumn(this, "Received", sql, JdbcType.VARCHAR);
        addColumn(col);
    }

    private void addCompletedColumn()
    {
        SQLFragment sql = new SQLFragment("(SELECT completed FROM " + HDRLQuerySchema.NAME + "." + HDRLQuerySchema.TABLE_REQUEST_RESULT + " R  WHERE R.RequestId = " + ExprColumn.STR_TABLE_ALIAS + ".requestId)");
        ExprColumn col = new ExprColumn(this, "Completed", sql, JdbcType.VARCHAR);
        addColumn(col);
    }

    private void addCoalescedStatusColumn()
    {
        ColumnInfo statusCol = getColumn("RequestStatusId");
        ForeignKey statusColFk = statusCol.getFk();
        removeColumn(statusCol);
        SQLFragment sql = new SQLFragment("COALESCE((SELECT R.requestStatusId FROM " + HDRLQuerySchema.NAME + "." + HDRLQuerySchema.TABLE_REQUEST_RESULT + " R WHERE R.RequestId = " + ExprColumn.STR_TABLE_ALIAS + ".requestId), RequestStatusId)");

        ExprColumn col = new ExprColumn(this, "Status", sql, JdbcType.INTEGER);
//        col.setAlias("Status");
        col.setFk(statusColFk);
        addColumn(col);
    }

    @Nullable
    @Override
    public QueryUpdateService getUpdateService()
    {
        TableInfo table = getRealTable();
        if (table != null && table.getTableType() == DatabaseTableType.TABLE)
            return new InboundRequestUpdateService(this, table);

        return null;
    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        return getContainer().hasPermission(user, perm);
    }
}
