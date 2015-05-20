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

import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.SQLFragment;

/**
 * Created by susanh on 4/13/15.
 */
public class PatientCountColumn extends ColumnInfo
{
    public PatientCountColumn(ColumnInfo columnInfo)
    {
        super(columnInfo);
    }

    @Override
    public SQLFragment getValueSql(String tableAliasName)
    {
        return new SQLFragment("COALESCE(" + tableAliasName + "."+ HDRLQuerySchema.COL_ARCHIVED_REQUEST_COUNT + ", (SELECT COUNT(DISTINCT(SSN,FMPId)) FROM hdrl.InboundSpecimen spec WHERE spec.InboundRequestId = " + tableAliasName + ".RequestId))");
    }
}
