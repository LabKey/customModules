/*
 * Copyright (c) 2013-2017 LabKey Corporation
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
package org.labkey.ms2extensions;

import org.apache.log4j.Logger;
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.module.ModuleProperty;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Updates the cached total and distinct peptide counts stored in the ms2extensions.ms2runaggregates table.
 * User: jeckels
 * Date: 10/15/13
 */
public class PeptideCountUpdater
{
    private static final Lock LOCK = new ReentrantLock();

    private static final Logger LOG = Logger.getLogger(PeptideCountUpdater.class);

    /** @return error message with any problems encountered during the update */
    public String update(Container container, User user)
    {
        // Figure out if any runs need to have aggregates calculated
        // Exclude runs that are deleted or haven't been successfully loaded (StatusId = 1)
        SQLFragment sql = new SQLFragment("SELECT r.Run FROM ms2.Runs r LEFT OUTER JOIN ms2extensions.Ms2RunAggregates a ON r.run = a.ms2Run WHERE r.Container = ? AND r.Deleted = ? AND r.StatusId = 1 AND (a.totalPeptides IS NULL OR a.distinctPeptides IS NULL)");
        sql.add(container.getEntityId());
        sql.add(false);
        Collection<Integer> runIds = new SqlSelector(MS2ExtensionsModule.getSchema(), sql).getCollection(Integer.class);

        if (!runIds.isEmpty())
        {
            // Figure out what query to use to calculate the aggregates
            Module module = ModuleLoader.getInstance().getModule(MS2ExtensionsModule.NAME);

            ModuleProperty schemaProperty = module.getModuleProperties().get(MS2ExtensionsModule.PEPTIDE_COUNT_SCHEMA_PROPERTY);
            ModuleProperty queryProperty = module.getModuleProperties().get(MS2ExtensionsModule.PEPTIDE_COUNT_QUERY_PROPERTY);

            String schemaName = PropertyManager.getCoalecedProperty(PropertyManager.SHARED_USER, ContainerManager.getRoot(), schemaProperty.getCategory(), schemaProperty.getName());
            String queryName = PropertyManager.getCoalecedProperty(PropertyManager.SHARED_USER, ContainerManager.getRoot(), queryProperty.getCategory(), queryProperty.getName());

            if (schemaName == null || schemaName.isEmpty())
            {
                schemaName = schemaProperty.getDefaultValue();
            }
            if (queryName == null || queryName.isEmpty())
            {
                queryName = queryProperty.getDefaultValue();
            }

            // Look up the query
            UserSchema userSchema = QueryService.get().getUserSchema(user, container, schemaName);
            if (userSchema == null)
            {
                return "Could not find schema '" + schemaName + "' specified in Module Properties for " + container.getPath();
            }
            TableInfo table = userSchema.getTable(queryName);
            if (table == null)
            {
                return "Could not find query '" + queryName + "' in schema '" + schemaName + "' specified in Module Properties for " + container.getPath();
            }

            // Make sure it has the right columns
            ColumnInfo ms2RunColumn = table.getColumn("ms2Run");
            ColumnInfo totalPeptidesColumn = table.getColumn("totalPeptides");
            ColumnInfo distinctPeptidesColumn = table.getColumn("distinctPeptides");

            if (ms2RunColumn == null || totalPeptidesColumn == null || distinctPeptidesColumn == null)
            {
                return "Could not expected columns ('ms2Run', 'totalPeptides', and 'distinctPeptides') in query '" + schemaName + "." + queryName + "' specified in Module Properties for " + container.getPath();
            }

            // Only allow one thread to update at a time
            LOG.info("Starting to update peptide counts for " + runIds);
            try (DbScope.Transaction transaction = MS2ExtensionsModule.getSchema().getScope().ensureTransaction(LOCK))
            {
                // Execute the query, filtering to just the runs that need aggregates calculated
                SimpleFilter filter = new SimpleFilter(new SimpleFilter.InClause(ms2RunColumn.getFieldKey(), runIds));
                Collection<Map<String, Object>> results = new TableSelector(table, Arrays.asList(ms2RunColumn, totalPeptidesColumn, distinctPeptidesColumn), filter, null).getMapCollection();

                LOG.info("Inserting peptide counts for " + runIds);

                // Iterate through the results and insert them into the table so they're cached and fast to show
                for (Map<String, Object> result : results)
                {
                    Map<String, Object> toInsert = new CaseInsensitiveHashMap<>(result);
                    toInsert.put("container", container.getEntityId());
                    Table.insert(null, MS2ExtensionsModule.getSchema().getTable("Ms2RunAggregates"), toInsert);
                }
                transaction.commit();
                LOG.info("Finished updating peptide counts for " + runIds);
            }
        }
        return null;
    }
}
