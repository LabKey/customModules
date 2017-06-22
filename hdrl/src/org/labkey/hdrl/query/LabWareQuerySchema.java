/*
 * Copyright (c) 2015-2017 LabKey Corporation
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
import org.labkey.api.data.DbSchemaType;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.SchemaTableInfo;
import org.labkey.api.data.TableInfo;
import org.labkey.api.module.Module;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.hdrl.HDRLModule;

/**
 * Created by susanh on 6/19/15.
 */
public class LabWareQuerySchema extends SimpleUserSchema
{
    public static final String NAME = "GW_LABKEY";
    public static final String DATA_SOURCE_NAME = "labware";
    public static final String DESCRIPTION = "LabWare tables for test requests and specimens";

    public static final String TABLE_INBOUND_REQUESTS = "X_LK_INBND_REQUESTS";
    public static final String TABLE_INBOUND_SPECIMENS = "X_LK_INBND_SPECIMENS";
    public static final String TABLE_OUTBOUND_REQUESTS = "X_LK_OUTBD_REQUESTS";
    public static final String TABLE_OUTBOUND_SPECIMENS = "X_LK_OUTBD_SPECIMENS";

    public LabWareQuerySchema(User user, Container container)
    {
        super(NAME, DESCRIPTION, user, container, DbSchema.get(getFullyQualifiedDataSource(), DbSchemaType.Module));
        _hidden = true;
    }

    public static String getFullyQualifiedDataSource()
    {
        return DATA_SOURCE_NAME + "." + NAME;
    }

    public static void register(final HDRLModule module)
    {
        DefaultSchema.registerProvider(NAME, new DefaultSchema.SchemaProvider(module)
        {
            @Override
            public QuerySchema createSchema(DefaultSchema schema, Module module)
            {
                return new LabWareQuerySchema(schema.getUser(), schema.getContainer());
            }
        });
    }

    public static void verifyLabWareDataSource() throws ValidationException
    {
        String name = DATA_SOURCE_NAME + "DataSource";
        DbScope scope = DbScope.getDbScope(name);
        if (scope == null)
        {
            throw new ValidationException("Expected to find a data source with name '" + name + "'.");
        }
    }

    public DbSchema getSchema()
    {
        return DbSchema.get(getFullyQualifiedDataSource(), DbSchemaType.Module);
    }
}
