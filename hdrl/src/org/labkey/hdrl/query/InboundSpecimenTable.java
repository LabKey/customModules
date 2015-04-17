package org.labkey.hdrl.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.DatabaseTableType;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.Permission;

/**
 * Created by klum on 4/15/2015.
 */
public class InboundSpecimenTable extends FilteredTable<HDRLSchema>
{
    public InboundSpecimenTable(HDRLSchema schema)
    {
        super(schema.getDbSchema().getTable(schema.TABLE_SPECIMEN), schema);

        // wrap all the existing columns
        wrapAllColumns(true);
    }

    @Nullable
    @Override
    public QueryUpdateService getUpdateService()
    {
        TableInfo table = getRealTable();
        if (table != null && table.getTableType() == DatabaseTableType.TABLE)
            return new InboundSpecimenUpdateService(this, table);

        return null;
    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        return getContainer().hasPermission(user, perm);
    }
}
