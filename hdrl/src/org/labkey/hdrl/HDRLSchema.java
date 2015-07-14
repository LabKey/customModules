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
package org.labkey.hdrl;

import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbSchemaType;
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
        return DbSchema.get(SCHEMA_NAME, DbSchemaType.Module);
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

    public TableInfo getTableInfoRequestResult()
    {
        return getSchema().getTable("RequestResult");
    }

    public TableInfo getTableInfoSpecimenResult()
    {
        return getSchema().getTable("SpecimenResult");
    }
}
