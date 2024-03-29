/*
 * Copyright (c) 2015-2019 LabKey Corporation
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
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.query.FieldKey;
import org.labkey.hdrl.HDRLSchema;

/**
 * Created by susanh on 4/6/16.
 */
public class RequestResultTable extends ResultTable
{
    public RequestResultTable(@NotNull HDRLQuerySchema schema, String name, ContainerFilter cf)
    {
        super(schema, name, cf);
    }

    @Override
    protected void applyContainerFilter(ContainerFilter filter)
    {
        FieldKey containerFieldKey = FieldKey.fromParts("Container");
        clearConditions(containerFieldKey);
        SQLFragment sql = new SQLFragment(getIdField() + " IN (SELECT r.RequestId FROM ");
        sql.append(HDRLSchema.getInstance().getTableInfoInboundRequest(), "r");
        sql.append(" WHERE ");
        sql.append(filter.getSQLFragment(getSchema(), new SQLFragment("r.Container")));
        sql.append(")");
        addCondition(sql, containerFieldKey);
    }

    private String getIdField()
    {
        if (_rootTable.getName().equalsIgnoreCase(HDRLQuerySchema.TABLE_REQUEST_RESULT))
            return "RequestId";
        else if (_rootTable.getName().equalsIgnoreCase(HDRLQuerySchema.TABLE_LABWARE_OUTBOUND_RESULTS))
            return "batch_id";
        return null;
    }
}
