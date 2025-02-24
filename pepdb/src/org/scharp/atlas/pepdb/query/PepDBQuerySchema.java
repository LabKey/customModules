package org.scharp.atlas.pepdb.query;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.TableInfo;
import org.labkey.api.module.Module;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;

/**
 * Created by klum on 9/17/2014.
 */
public class PepDBQuerySchema extends SimpleUserSchema
{
    public static final String SCHEMA_NAME = "pepdb";
    public static final String SCHEMA_DESCR = "Provides peptide and peptide pool information.";

    static public void register(Module module)
    {
        DefaultSchema.registerProvider(SCHEMA_NAME, new DefaultSchema.SchemaProvider(module)
        {
            @Override
            public QuerySchema createSchema(DefaultSchema schema, Module module)
            {
                return new PepDBQuerySchema(schema.getUser(), schema.getContainer());
            }
        });
    }

    public PepDBQuerySchema(User user, Container container)
    {
        super(SCHEMA_NAME, SCHEMA_DESCR, user, container, DbSchema.get("pepdb"));
    }

    @Override
    protected TableInfo createWrappedTable(String name, @NotNull TableInfo sourceTable, ContainerFilter cf)
    {
        TableInfo table = super.createWrappedTable(name, sourceTable, cf);
        if (table instanceof SimpleTable)
        {
            // Setting readOnly to false so that tests can reset the data tables via Remote API commands. S. Langley.
            ((SimpleTable) table).setReadOnly(false);
            //((SimpleTable)table).setReadOnly(true);
        }
        return table;
    }
}
