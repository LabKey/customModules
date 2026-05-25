/*
 * Copyright (c) 2024-2026 LabKey Corporation
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
package org.labkey.viscstudies;

import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.ContainerForeignKey;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbSchemaType;
import org.labkey.api.query.FilteredTable;

import static org.labkey.viscstudies.ViscStudySchema.STUDY_DESIGN_TABLE_NAME;

public class StudyDesignTable extends FilteredTable<ViscStudySchema>
{
    public StudyDesignTable(ViscStudySchema schema, ContainerFilter cf)
    {
        super(DbSchema.get("study", DbSchemaType.Module).getTable(STUDY_DESIGN_TABLE_NAME), schema, null);
        setName(STUDY_DESIGN_TABLE_NAME);
        for (ColumnInfo baseColumn : _rootTable.getColumns())
        {
            String name = baseColumn.getName();

            var colInfo = addWrapColumn(baseColumn);
            if ("Container".equalsIgnoreCase(name) || "_ts".equalsIgnoreCase(name))
                colInfo.setHidden(true);
        }

        getMutableColumn("Container").setFk(new ContainerForeignKey(schema));
    }
}
