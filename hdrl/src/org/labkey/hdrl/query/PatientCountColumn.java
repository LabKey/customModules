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
        return new SQLFragment("(SELECT COUNT(DISTINCT(SSN,FMPId,TestingSourceId)) FROM hdrl.InboundSpecimen spec WHERE spec.InboundRequestId = " + tableAliasName + ".RequestId)");
    }
}
