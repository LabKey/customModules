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
