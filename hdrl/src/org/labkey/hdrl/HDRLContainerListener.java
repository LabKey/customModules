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

package org.labkey.hdrl;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager.ContainerListener;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.security.User;
import org.labkey.api.util.ContainerUtil;

import java.util.Collections;
import java.util.Collection;

import java.beans.PropertyChangeEvent;

public class HDRLContainerListener implements ContainerListener
{
    @Override
    public void containerCreated(Container c, User user)
    {
    }

    @Override
    public void containerDeleted(Container c, User user)
    {
        DbSchema hdrl = HDRLSchema.getInstance().getSchema();
        try (DbScope.Transaction transaction = hdrl.getScope().ensureTransaction())
        {
            SqlExecutor executor = new SqlExecutor(hdrl.getScope());
            SQLFragment deleteSQL = getLabWareDeleteSQL("X_LK_OUTBD_SPECIMENS", c);
            executor.execute(deleteSQL);
            deleteSQL = getLabWareDeleteSQL("X_LK_OUTBD_REQUESTS", c);
            executor.execute(deleteSQL);
            deleteSQL = getLabWareDeleteSQL("X_LK_INBND_SPECIMENS", c);
            executor.execute(deleteSQL);
            deleteSQL = getLabWareDeleteSQL("X_LK_INBND_REQUESTS", c);
            executor.execute(deleteSQL);
            ContainerUtil.purgeTable(hdrl.getTable("InboundRequest"), c, null);
            transaction.commit();
        }
    }

    private SQLFragment getLabWareDeleteSQL(String labWareTable, Container container)
    {
        SQLFragment deleteSQL = new SQLFragment("DELETE FROM gw_labkey.").append(labWareTable).append(" WHERE batch_id in (SELECT RequestId from hdrl.InboundRequest WHERE Container = ?)");
        deleteSQL.add(container.getId());
        return deleteSQL;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
    }

    @Override
    public void containerMoved(Container c, Container oldParent, User user)
    {
    }

    @NotNull @Override
    public Collection<String> canMove(Container c, Container newParent, User user)
    {
        return Collections.emptyList();
    }
}