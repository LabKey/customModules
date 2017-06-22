/*
 * Copyright (c) 2012-2017 LabKey Corporation
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

import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;

import java.util.Collections;
import java.util.Set;

/**
 * User: jeckels
 * Date: May 21, 2012
 */
public class ViscStudySchema extends UserSchema
{
    public static final String NAME = "viscstudies";
    public static final String STUDY_TABLE_NAME = "studies";

    public ViscStudySchema(User user, Container container)
    {
        super(NAME, "Contains special study queries that are specific to VISC", user, container, DbSchema.get("study"));
    }

    @Override
    public TableInfo createTable(String name)
    {
        if (STUDY_TABLE_NAME.equalsIgnoreCase(name))
        {
            // This is a wrapped table over the study schema's StudyProperties query (one row per study)
            Container c = getContainer().getProject() == null ? getContainer() : getContainer().getProject();
            UserSchema studySchema = QueryService.get().getUserSchema(getUser(), c, "study");
            TableInfo studyTable = studySchema.getTable("StudyProperties");
            if (studyTable == null)
            {
                return null;
            }
            return new ProjectStudiesTable(this, studyTable);
        }
        return null;
    }

    @Override
    public Set<String> getTableNames()
    {
        return Collections.singleton(STUDY_TABLE_NAME);
    }
}
