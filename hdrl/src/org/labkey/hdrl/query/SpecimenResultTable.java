package org.labkey.hdrl.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.DatabaseTableType;
import org.labkey.api.data.TableInfo;
import org.labkey.api.etl.DataIteratorBuilder;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DefaultQueryUpdateService;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.Permission;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by susanh on 7/7/15.
 */
public class SpecimenResultTable extends SimpleUserSchema.SimpleTable<HDRLQuerySchema>
{
    public SpecimenResultTable(@NotNull HDRLQuerySchema schema)
    {
        super(schema, schema.getDbSchema().getTable(schema.TABLE_SPECIMEN_RESULT));
    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        return getContainer().hasPermission(user, perm);
    }

    @Nullable
    @Override
    public QueryUpdateService getUpdateService()
    {
        TableInfo table = getRealTable();
        if (table != null && table.getTableType() == DatabaseTableType.TABLE)
            return new DefaultQueryUpdateService(this, table) {

                @Override
                public int mergeRows(User user, Container container, DataIteratorBuilder rows, BatchValidationException
                errors, @Nullable Map<Enum, Object> configParameters, Map<String, Object> extraScriptContext)
                throws SQLException
                {
                    return _importRowsUsingETL(user, container, rows, null,  getDataIteratorContext(errors, InsertOption.MERGE, configParameters), extraScriptContext);
                }
            };

        return null;
    }
}
