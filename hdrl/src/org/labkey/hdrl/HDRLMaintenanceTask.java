/*
 * Copyright (c) 2015-2016 LabKey Corporation
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
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.util.SystemMaintenance.MaintenanceTask;
import org.labkey.hdrl.query.HDRLQuerySchema;

import java.util.Calendar;

/**
 * Created by: jeckels
 * Date: 5/10/15
 */
public class HDRLMaintenanceTask implements MaintenanceTask
{
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
    public void run(Logger log)
    {
        // Get this from the configurable setting
        int retentionDays = HDRLManager.getNumberOfDays();
        final String requestStatusStatement = HDRLQuerySchema.COL_REQUEST_STATUS_ID + " = " + "(SELECT rowid FROM hdrl.requeststatus WHERE name = ?)";
        final String submittedCondition1 = " WHERE (" + HDRLSchema.getInstance().getSchema().getSqlDialect().getDateDiff(Calendar.DATE, "NOW()", "r.Completed") + " >= ?";
        final String submittedCondition2 = " AND i." + requestStatusStatement + ")";

        DbSchema hdrl = HDRLSchema.getInstance().getSchema();
        try (DbScope.Transaction transaction = hdrl.getScope().ensureTransaction())
        {
            // Need to stash the number of specimens in the request table, prior to deleting the specimen rows
            SQLFragment addToArchivedRequestCountColSQL = new SQLFragment("UPDATE ");
            addToArchivedRequestCountColSQL.append(HDRLSchema.getInstance().getTableInfoInboundRequest(), "i");
            addToArchivedRequestCountColSQL.append(" SET " + HDRLQuerySchema.COL_ARCHIVED_REQUEST_COUNT + " = (SELECT COUNT(*) FROM ");
            addToArchivedRequestCountColSQL.append(HDRLSchema.getInstance().getTableInfoInboundSpecimen(), "s");
            addToArchivedRequestCountColSQL.append(" WHERE s." + HDRLQuerySchema.COL_INBOUND_REQUEST_ID + " = i.RequestId)");
            addToArchivedRequestCountColSQL.append(" FROM ").append(HDRLSchema.getInstance().getTableInfoRequestResult(), "r");
            addToArchivedRequestCountColSQL.append(submittedCondition1);
            addToArchivedRequestCountColSQL.add(retentionDays);
            addToArchivedRequestCountColSQL.append(submittedCondition2);
            addToArchivedRequestCountColSQL.add(HDRLQuerySchema.STATUS_SUBMITTED);

            // Need to stash the number of specimens in the request table, prior to deleting the specimen rows
            SQLFragment updateStatusSql = new SQLFragment("UPDATE ");
            updateStatusSql.append(HDRLSchema.getInstance().getTableInfoRequestResult(), "r");
            updateStatusSql.append(" SET " + requestStatusStatement);
            updateStatusSql.add(HDRLQuerySchema.STATUS_ARCHIVED);
            updateStatusSql.append(" FROM ").append(HDRLSchema.getInstance().getTableInfoInboundRequest(), "i");
            updateStatusSql.append(submittedCondition1);
            updateStatusSql.add(retentionDays);
            updateStatusSql.append(submittedCondition2);
            updateStatusSql.add(HDRLQuerySchema.STATUS_SUBMITTED);

            log.info("Adding specimen count to column '" + HDRLQuerySchema.COL_ARCHIVED_REQUEST_COUNT + "' in table " + HDRLSchema.getInstance().getTableInfoInboundRequest().getName());

            int numRequests = new SqlExecutor(HDRLSchema.getInstance().getScope()).execute(addToArchivedRequestCountColSQL);

            log.info("Finished adding specimen count to column '" + HDRLQuerySchema.COL_ARCHIVED_REQUEST_COUNT + "' in table "
                    + HDRLSchema.getInstance().getTableInfoInboundRequest().getName()
                    +". Added specimen counts to " + numRequests +" rows.");

            log.info("Updating request status in table " + HDRLSchema.getInstance().getTableInfoRequestResult().getName());

            numRequests = new SqlExecutor(HDRLSchema.getInstance().getScope()).execute(updateStatusSql);

            log.info("Updated " + numRequests + " rows ");

            log.info("Starting to delete HDRL specimen data");

            // Delete specimens for requests that have been archived
            // specimen results are deleted via cascading
            SQLFragment specimenDeleteSQL = new SQLFragment("DELETE FROM ");
            specimenDeleteSQL.append(HDRLSchema.getInstance().getTableInfoInboundSpecimen(), "s");
            specimenDeleteSQL.append(" WHERE " + HDRLQuerySchema.COL_INBOUND_REQUEST_ID + " IN (SELECT r.RequestId FROM ");
            specimenDeleteSQL.append(HDRLSchema.getInstance().getTableInfoRequestResult(), "r");
            specimenDeleteSQL.append(" WHERE " + requestStatusStatement);
            specimenDeleteSQL.add(HDRLQuerySchema.STATUS_ARCHIVED);
            specimenDeleteSQL.append(")");

            int inboundRowCount = new SqlExecutor(HDRLSchema.getInstance().getScope()).execute(specimenDeleteSQL);


            log.info("Deleted " + inboundRowCount + " row(s) of HDRL inbound specimen data");

            transaction.commit();
        }
    }
}
