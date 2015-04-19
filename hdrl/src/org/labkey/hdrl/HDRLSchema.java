package org.labkey.hdrl;

import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.TableInfo;

/**
 * Created by klum on 4/18/15.
 */
public class HDRLSchema
{
    private static final HDRLSchema instance = new HDRLSchema();
    private static final String SCHEMA_NAME = "hdrl";

    public static HDRLSchema getInstance()
    {
        return instance;
    }

    private HDRLSchema()
    {
    }

    public String getSchemaName()
    {
        return SCHEMA_NAME;
    }

    public DbSchema getSchema()
    {
        return DbSchema.get(SCHEMA_NAME);
    }

    public DbScope getScope()
    {
        return getSchema().getScope();
    }

    public TableInfo getTableInfoInboundRequest()
    {
        return getSchema().getTable("InboundRequest");
    }

    public TableInfo getTableInfoInboundSpecimen()
    {
        return getSchema().getTable("InboundSpecimen");
    }
}
