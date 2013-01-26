/*
 * Copyright (c) 2013 LabKey Corporation
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

import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.ActionButton;
import org.labkey.api.data.ButtonBar;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.data.RuntimeSQLException;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.module.ModuleProperty;
import org.labkey.api.ms2.MS2Service;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.DataView;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.VBox;
import org.labkey.api.view.ViewContext;
import org.labkey.api.view.template.ClientDependency;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class RunGridWebPart extends VBox
{
    public RunGridWebPart(final ViewContext viewContext)
    {
        setFrame(FrameType.PORTAL);
        setTitle("MS2 Runs With Peptide Counts");

        String errorMessage = updatePeptideCounts(viewContext);
        if (errorMessage != null)
        {
            addView(new HtmlView("<span class=\"labkey-error\">Unable to update peptide counts: " + PageFlowUtil.filter(errorMessage) + "</span>"));
        }

        JspView<ViewContext> v = new JspView<ViewContext>("/org/labkey/ms2extensions/runGridFilters.jsp", viewContext);
        addClientDependencies(Collections.singleton(ClientDependency.fromFilePath("/MS2/inlineViewDesigner.js")));
        addView(v);

        UserSchema ms2Schema = MS2Service.get().createSchema(viewContext.getUser(), viewContext.getContainer());
        QuerySettings settings = new QuerySettings(viewContext, "MS2ExtensionsRunGrid", "MS2SearchRuns");
        addView(new CustomRunGridView(ms2Schema, settings));

    }

    private String updatePeptideCounts(ViewContext viewContext)
    {
        // Figure out if any runs need to have aggregates calculated
        // Exclude runs that are deleted or haven't been successfully loaded (StatusId = 1)
        SQLFragment sql = new SQLFragment("SELECT r.Run FROM ms2.Runs r LEFT OUTER JOIN ms2extensions.Ms2RunAggregates a ON r.run = a.ms2Run WHERE r.Container = ? AND r.Deleted = ? AND r.StatusId = 1 AND (a.totalPeptides IS NULL OR a.distinctPeptides IS NULL)");
        sql.add(viewContext.getContainer().getEntityId());
        sql.add(false);
        Collection<Integer> runIds = new SqlSelector(DbSchema.get("ms2Extensions"), sql).getCollection(Integer.class);

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
            UserSchema userSchema = QueryService.get().getUserSchema(viewContext.getUser(), viewContext.getContainer(), schemaName);
            if (userSchema == null)
            {
                return "Could not find schema '" + schemaName + "' specified in Module Properties.";
            }
            TableInfo table = userSchema.getTable(queryName);
            if (table == null)
            {
                return "Could not find query '" + queryName + "' in schema '" + schemaName + "' specified in Module Properties.";
            }

            // Make sure it has the right columns
            ColumnInfo ms2RunColumn = table.getColumn("ms2Run");
            ColumnInfo totalPeptidesColumn = table.getColumn("totalPeptides");
            ColumnInfo distinctPeptidesColumn = table.getColumn("distinctPeptides");

            if (ms2RunColumn == null || totalPeptidesColumn == null || distinctPeptidesColumn == null)
            {
                return "Could not expected columns ('ms2Run', 'totalPeptides', and 'distinctPeptides') in query '" + schemaName + "." + queryName + "' specified in Module Properties.";
            }

            // Execute the query, filtering to just the runs that need aggregates calculated
            SimpleFilter filter = new SimpleFilter(new SimpleFilter.InClause(ms2RunColumn.getFieldKey(), runIds));
            Collection<Map> results = new TableSelector(table, Arrays.asList(ms2RunColumn, totalPeptidesColumn, distinctPeptidesColumn), filter, null).getCollection(Map.class);

            // Iterate through the results and insert them into the table so they're cached and fast to show
            for (Map<String, Object> result : results)
            {
                Map<String, Object> toInsert = new CaseInsensitiveHashMap<Object>(result);
                toInsert.put("container", viewContext.getContainer().getEntityId());
                try
                {
                    Table.insert(null, DbSchema.get(MS2ExtensionsModule.SCHEMA_NAME).getTable("Ms2RunAggregates"), toInsert);
                }
                catch (SQLException e)
                {
                    throw new RuntimeSQLException(e);
                }
            }
        }
        return null;
    }

    /** Simple wrapper to add a few buttons that rely on the UI added in the JspView above */
    public static class CustomRunGridView extends QueryView
    {
        public CustomRunGridView(UserSchema ms2Schema, QuerySettings settings)
        {
            super(ms2Schema, settings, null);
            setShowRecordSelectors(true);
        }

        @Override
        protected void populateButtonBar(DataView view, ButtonBar bar)
        {
            super.populateButtonBar(view, bar);
            
            ActionButton comparePeptidesButton = new ActionButton("Compare Peptides");
            comparePeptidesButton.setScript("comparePeptides(" + PageFlowUtil.jsString(getDataRegionName()) + "); return false;", false);
            comparePeptidesButton.setActionType(ActionButton.Action.SCRIPT);
            comparePeptidesButton.setRequiresSelection(true);
            bar.add(comparePeptidesButton);

            ActionButton exportBluemap = new ActionButton("Export Protein Coverage");
            exportBluemap.setScript("exportPeptideBluemap(" + PageFlowUtil.jsString(getDataRegionName()) + "); return false;", false);
            exportBluemap.setActionType(ActionButton.Action.SCRIPT);
            exportBluemap.setRequiresSelection(true);
            bar.add(exportBluemap);
        }
    }
}
