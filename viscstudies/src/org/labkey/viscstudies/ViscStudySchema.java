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
    protected TableInfo createTable(String name)
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
