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

import org.apache.log4j.Logger;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.util.SystemMaintenance;

import java.util.Calendar;

/**
 * Created by: jeckels
 * Date: 5/10/15
 */
public class HDRLMaintenanceTask implements SystemMaintenance.MaintenanceTask
{
    private static final Logger LOG = Logger.getLogger(HDRLMaintenanceTask.class);

    @Override
    public String getDescription()
    {
        return "HDRL Request Portal PHI Deletion";
    }

    @Override
    public String getName()
    {
        return "HdrlPhiDeletion";
    }

    @Override
    public boolean canDisable()
    {
        return true;
    }

    @Override
    public boolean hideFromAdminPage()
    {
        return false;
    }

    @Override
    public void run()
    {
        // Get this from the configurable setting
        int retentionDays = 30;

        // Depending on strategy for remembering the number of specimens in the request, might need to stash the value
        // here, prior to deleting the specimen rows


        // Delete based on the completion date being at least X days in the past
        SQLFragment specimenDeleteSQL = new SQLFragment("DELETE FROM ");
        specimenDeleteSQL.append(HDRLSchema.getInstance().getTableInfoInboundSpecimen());
        specimenDeleteSQL.append(" WHERE InboundRequestId IN (SELECT RequestId FROM ");
        specimenDeleteSQL.append(HDRLSchema.getInstance().getTableInfoInboundRequest(), "r");
        specimenDeleteSQL.append(" WHERE ");
        specimenDeleteSQL.append(HDRLSchema.getInstance().getSchema().getSqlDialect().getDateDiff(Calendar.DATE, "NOW()", "r.Completed"));
        specimenDeleteSQL.append(" > ?)");
        specimenDeleteSQL.add(retentionDays);

        LOG.info("Starting to delete HDRL specimen data");

        int rowCount = new SqlExecutor(HDRLSchema.getInstance().getScope()).execute(specimenDeleteSQL);

        LOG.info("Deleted " + rowCount + " row(s) of HDRL specimen data");

        // Also delete from specimen results when we're mapping them back from LabWare
    }
}
