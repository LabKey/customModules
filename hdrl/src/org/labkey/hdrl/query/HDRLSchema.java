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

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.SchemaTableInfo;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.dialect.SqlDialect;
import org.labkey.api.module.Module;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;
import org.labkey.hdrl.HDRLModule;

import java.util.HashSet;
import java.util.Set;

public class HDRLSchema extends SimpleUserSchema
{
    public static final String NAME = "hdrl";
    public static final String DESCRIPTION = "Provides information on test requests and sample sets";

    public static final String TABLE_INBOUND_REQUEST = "InboundRequest";
    public static final String TABLE_SPECIMEN = "InboundSpecimen";

    public static void register(final HDRLModule module)
    {
        DefaultSchema.registerProvider(NAME, new DefaultSchema.SchemaProvider(module)
        {
            @Override
            public QuerySchema createSchema(DefaultSchema schema, Module module)
            {
                return new HDRLSchema(schema.getUser(), schema.getContainer());
            }
        });
    }

    public HDRLSchema(User user, Container container)
    {
        super(NAME, DESCRIPTION, user, container, DbSchema.get(NAME));
    }

    public DbSchema getSchema()
    {
        return DbSchema.get(NAME);
    }

    public SqlDialect getSqlDialect()
    {
        return getSchema().getSqlDialect();
    }

    @Nullable
    @Override
    protected TableInfo createTable(String name)
    {
        if (TABLE_INBOUND_REQUEST.equalsIgnoreCase(name))
        {
            return new InboundRequestTable(this);
        }

        //just return a filtered table over the db table if it exists
        SchemaTableInfo tableInfo = getDbSchema().getTable(name);
        if (null == tableInfo)
            return null;

        FilteredTable filteredTable = new FilteredTable<>(tableInfo, this);
        filteredTable.wrapAllColumns(true);
        return filteredTable;
    }

    @Override
    public Set<String> getTableNames()
    {
        return new HashSet<>(getDbSchema().getTableNames());
    }
}
