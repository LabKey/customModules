package org.labkey.hdrl.query;

import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.ContainerForeignKey;
import org.labkey.api.data.DataColumn;
import org.labkey.api.data.DisplayColumn;
import org.labkey.api.data.DisplayColumnFactory;
import org.labkey.api.data.SchemaTableInfo;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.WrappedColumn;
import org.labkey.api.query.FilteredTable;

/**
 * Created by susanh on 4/13/15.
 */
public class InboundRequestTable extends FilteredTable<HDRLSchema>
{
    public InboundRequestTable(HDRLSchema schema)
    {
        super(schema.getDbSchema().getTable(schema.TABLE_INBOUND_REQUEST), schema);

        // wrap all the existing columns
        wrapAllColumns(true);

        ColumnInfo containerCol = getColumn("Container");
        ContainerForeignKey.initColumn(containerCol, schema);

        // add column for the number of patients
        addColumn(wrapColumn("Number of Patients", new PatientCountColumn(getRealTable().getColumn("RequestId"))));

    }
}
