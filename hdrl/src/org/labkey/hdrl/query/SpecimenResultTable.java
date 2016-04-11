package org.labkey.hdrl.query;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.query.FieldKey;
import org.labkey.hdrl.HDRLSchema;

/**
 * Created by susanh on 4/6/16.
 */
public class SpecimenResultTable extends ResultTable
{

    public SpecimenResultTable(@NotNull HDRLQuerySchema schema, String name)
    {
        super(schema, name);
    }

    @Override
    protected void applyContainerFilter(ContainerFilter filter)
    {
        FieldKey containerFieldKey = FieldKey.fromParts("Container");
        clearConditions(containerFieldKey);
        SQLFragment sql = new SQLFragment(getIdField() + " IN (SELECT s.RowId FROM ");
        sql.append(HDRLSchema.getInstance().getTableInfoInboundSpecimen(), "s");
        sql.append(" WHERE ");
        sql.append(filter.getSQLFragment(getSchema(), new SQLFragment("s.Container"), getContainer()));
        sql.append(")");
        addCondition(sql, containerFieldKey);
    }


    private String getIdField()
    {
        if (_rootTable.getName().equalsIgnoreCase(HDRLQuerySchema.TABLE_SPECIMEN_RESULT))
            return "SpecimenId";
        else if (_rootTable.getName().equalsIgnoreCase(HDRLQuerySchema.TABLE_LABWARE_OUTBOUND_SPECIMENS))
            return "test_request_id";
        return null;
    }
}
