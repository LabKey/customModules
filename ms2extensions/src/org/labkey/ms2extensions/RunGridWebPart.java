/*
 * Copyright (c) 2013-2016 LabKey Corporation
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

import org.labkey.api.data.ActionButton;
import org.labkey.api.data.ButtonBar;
import org.labkey.api.exp.ExperimentRunListView;
import org.labkey.api.exp.ExperimentRunType;
import org.labkey.api.ms2.MS2Service;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.UserSchema;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.DataView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.VBox;
import org.labkey.api.view.ViewContext;
import org.labkey.api.view.template.ClientDependency;

import java.util.Collections;

public class RunGridWebPart extends VBox
{
    public RunGridWebPart(final ViewContext viewContext)
    {
        setFrame(FrameType.PORTAL);
        setTitle("MS2 Runs With Peptide Counts");

        JspView<ViewContext> v = new JspView<>("/org/labkey/ms2extensions/runGridFilters.jsp", viewContext);
        addClientDependencies(Collections.singleton(ClientDependency.fromPath("/MS2/inlineViewDesigner.js")));
        addView(v);

        UserSchema ms2Schema = MS2Service.get().createSchema(viewContext.getUser(), viewContext.getContainer());
        QuerySettings settings = new QuerySettings(viewContext, "MS2ExtensionsRunGrid", "MS2SearchRuns");
        settings.setSchemaName(ms2Schema.getSchemaName());
        addView(new CustomRunGridView(ms2Schema, settings));

    }

    /** Simple wrapper to add a few buttons that rely on the UI added in the JspView above */
    public static class CustomRunGridView extends ExperimentRunListView
    {
        public CustomRunGridView(UserSchema ms2Schema, QuerySettings settings)
        {
            super(ms2Schema, settings, ExperimentRunType.ALL_RUNS_TYPE);
            setShowRecordSelectors(true);
            setShowAddToRunGroupButton(true);
        }

        @Override
        protected void populateButtonBar(DataView view, ButtonBar bar)
        {
            super.populateButtonBar(view, bar);

            RunGridWebPart.populateButtonBar(bar, getDataRegionName());
        }
    }

    public static void populateButtonBar(ButtonBar bar, String dataRegionName)
    {
        ActionButton comparePeptidesButton = new ActionButton("Compare Peptides");
        comparePeptidesButton.setScript("comparePeptides(" + PageFlowUtil.jsString(dataRegionName) + "); return false;", false);
        comparePeptidesButton.setActionType(ActionButton.Action.SCRIPT);
        comparePeptidesButton.setRequiresSelection(true);
        bar.add(comparePeptidesButton);

        ActionButton spectraCountButton = new ActionButton("Spectra Count");
        spectraCountButton.setScript("spectraCount(" + PageFlowUtil.jsString(dataRegionName) + "); return false;", false);
        spectraCountButton.setActionType(ActionButton.Action.SCRIPT);
        spectraCountButton.setRequiresSelection(true);
        bar.add(spectraCountButton);

        ActionButton exportBluemap = new ActionButton("Export Protein Coverage");
        exportBluemap.setScript("exportPeptideBluemap(" + PageFlowUtil.jsString(dataRegionName) + "); return false;", false);
        exportBluemap.setActionType(ActionButton.Action.SCRIPT);
        exportBluemap.setRequiresSelection(true);
        bar.add(exportBluemap);
    }

}
