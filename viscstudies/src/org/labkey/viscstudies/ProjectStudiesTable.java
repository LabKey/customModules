/*
 * Copyright (c) 2012-2019 LabKey Corporation
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
import org.labkey.api.data.ContainerFilterable;
import org.labkey.api.data.DisplayColumn;
import org.labkey.api.data.DisplayColumnFactory;
import org.labkey.api.data.TableInfo;
import org.labkey.api.exp.PropertyColumn;
import org.labkey.api.query.DetailsURL;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.HttpView;

import java.util.ArrayList;
import java.util.List;

/**
 * Always show the full list of studies that the user has permission to see within the current project.
 * A little wacky since we'd normally only show the study from the current container, but it's a requirement
 * that this be a viable target for a lookup from a list (which is scoped to a single container).
 */
public class ProjectStudiesTable extends FilteredTable<ViscStudySchema>
{
    public ProjectStudiesTable(ViscStudySchema schema, TableInfo studyTable)
    {
        // Pretend that the project is our base container
        super(studyTable, schema.getContainer().getProject() == null ? schema : new ViscStudySchema(schema.getUser(), schema.getContainer().getProject()));
        // Set the ContainerFilter to show everything from the project
        ContainerFilter containerFilter = ContainerFilter.Type.CurrentAndSubfolders.create(getUserSchema());
        ((ContainerFilterable)studyTable).setContainerFilter(containerFilter);
        // don't need to apply the same container filter twice and the inner table already has the CF applied
        applyContainerFilter(ContainerFilter.EVERYTHING);

        for (ColumnInfo col : getRealTable().getColumns())
        {
            // Don't bother with the extra property columns that are specific to a single study 
            if (!(col instanceof PropertyColumn))
            {
                var newCol = addWrapColumn(col);
                newCol.setHidden(col.isHidden());
            }
        }
        setDescription("Contains one row per study in the current project. Includes a Dataset Status column that shows all the datasets with their status");

        // Tweak the label column
        var labelColumn = getMutableColumn("Label");
        labelColumn.setLabel("Study Name");

        // Set up a URL to the study schedule in the right container
        DetailsURL studyScheduleURL;
        if (HttpView.hasCurrentView())
        {
            studyScheduleURL = DetailsURL.fromString("/study-studySchedule.view?" + ActionURL.Param.returnUrl + "=" + HttpView.currentContext().getActionURL());
        }
        else
        {
            studyScheduleURL = DetailsURL.fromString("/study-studySchedule.view");
        }
        labelColumn.setURL(studyScheduleURL);
        setDetailsURL(studyScheduleURL);
            
        // Set up a column that is going to show the dataset status
        var datasetColumn = wrapColumn("Dataset Status", studyTable.getColumn("Container"));
        datasetColumn.setConceptURI(null); // don't treat like container column
        datasetColumn.clearFk();
        datasetColumn.setKeyField(false);
        addColumn(datasetColumn);
        datasetColumn.setDisplayColumnFactory(DatasetListWithStatusColumn::new);

        // Configure the set of default column
        List<FieldKey> defaultCols = new ArrayList<>();
        defaultCols.add(labelColumn.getFieldKey());
        defaultCols.add(datasetColumn.getFieldKey());
        setDefaultVisibleColumns(defaultCols);
        setTitle("Study");
        setName(ViscStudySchema.STUDY_TABLE_NAME);
    }

    @Override
    public boolean supportsContainerFilter()
    {
        return false;
    }
}
