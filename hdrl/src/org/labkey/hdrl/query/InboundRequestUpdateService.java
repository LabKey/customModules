package org.labkey.hdrl.query;

import org.labkey.api.data.TableInfo;
import org.labkey.api.query.DefaultQueryUpdateService;

/**
 * Created by klum on 4/14/2015.
 */
public class InboundRequestUpdateService extends DefaultQueryUpdateService
{
    public InboundRequestUpdateService(TableInfo queryTable, TableInfo dbTable)
    {
        super(queryTable, dbTable);
    }

}
