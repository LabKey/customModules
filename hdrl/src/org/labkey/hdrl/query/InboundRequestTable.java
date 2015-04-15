package org.labkey.hdrl.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.ContainerForeignKey;
import org.labkey.api.data.DatabaseTableType;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.Permission;

/**
 * Created by susanh on 4/13/15.
 */
public class InboundRequestTable extends FilteredTable<HDRLSchema>
{
    public InboundRequestTable(HDRLSchema schema)
    {
        super(schema.getDbSchema().getTable(schema.TABLE_INBOUND_REQUEST), schema);

        // wrap all the existing columns
        wrapAllColumns(true);

        ColumnInfo containerCol = getColumn("Container");
        ContainerForeignKey.initColumn(containerCol, schema);

        // add column for the number of patients
        addColumn(wrapColumn("Number of Patients", new PatientCountColumn(getRealTable().getColumn("RequestId"))));
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
