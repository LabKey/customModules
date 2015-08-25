/*
 * Copyright (c) 2013-2015 LabKey Corporation
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

package org.labkey.icemr;

import org.labkey.api.action.SpringActionController;
import org.labkey.api.assay.dilution.DilutionAssayRun;
import org.labkey.api.assay.nab.GraphForm;
import org.labkey.api.assay.nab.NabGraph;
import org.labkey.api.assay.nab.RenderAssayBean;
import org.labkey.api.assay.nab.view.DilutionGraphAction;
import org.labkey.api.assay.nab.view.GraphSelectedAction;
import org.labkey.api.assay.nab.view.GraphSelectedBean;
import org.labkey.api.assay.nab.view.GraphSelectedForm;
import org.labkey.api.assay.nab.view.MultiGraphAction;
import org.labkey.api.assay.nab.view.RunDetailsAction;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.security.ContextualRoles;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.study.assay.AssayProtocolSchema;
import org.labkey.api.study.assay.AssayProvider;
import org.labkey.api.study.assay.AssayService;
import org.labkey.api.study.assay.AssayUrls;
import org.labkey.api.study.assay.RunDatasetContextualRoles;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.DataView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.ViewContext;
import org.labkey.icemr.assay.DrugSensitivity.DrugSensitivityProtocolSchema;
import org.labkey.icemr.assay.DrugSensitivity.actions.DrugSensitivityUploadWizardAction;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

public class IcemrController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(IcemrController.class,
            DrugSensitivityUploadWizardAction.class);

    public static final String GRAPH_Y_LABEL = "Percent Proliferation";

    public IcemrController()
    {
        setActionResolver(_actionResolver);
    }

    @RequiresPermission(ReadPermission.class)
    public class DetailsAction extends RunDetailsAction<RenderAssayBean>
    {
        @Override
        public ModelAndView getView(RenderAssayBean form, BindException errors) throws Exception
        {
            form.setSampleNoun("Drug Treatment");
            form.setNeutralizationAbrev("Prol.");
            form.setGraphURL(new ActionURL(GraphAction.class, getContainer()));
            form.setPlateDataFormat("%.3f");

            return super.getView(form, errors);
        }

        public NavTree appendNavTrail(NavTree root)
        {
            ActionURL runDataURL = PageFlowUtil.urlProvider(AssayUrls.class).getAssayResultsURL(getContainer(), _protocol, _runRowId);
            return root.addChild(_protocol.getName() + " Data", runDataURL).addChild("Run " + _runRowId + " Details");
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class GraphAction extends DilutionGraphAction
    {
        @Override
        protected NabGraph.Config getGraphConfig(GraphForm form, DilutionAssayRun assay)
        {
            NabGraph.Config config = super.getGraphConfig(form, assay);

            config.setyAxisLabel(GRAPH_Y_LABEL);
            //config.setxAxisLabel("Concentration");
            return config;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class DrugSensitivityGraphSelectedAction extends GraphSelectedAction<GraphSelectedForm>
    {
        @Override
        protected GraphSelectedBean createSelectionBean(ViewContext context, ExpProtocol protocol, int[] cutoffs, int[] dataObjectIds, String caption, String title)
        {
            return new DrugSensitivityGraphSelectedBean(context, protocol, cutoffs, dataObjectIds, caption, title);
        }
    }

    public static class DrugSensitivityGraphSelectedBean extends GraphSelectedBean
    {
        public DrugSensitivityGraphSelectedBean(ViewContext context, ExpProtocol protocol, int[] cutoffs, int[] dataObjectIds, String captionColumn, String chartTitle)
        {
            super(context, protocol, cutoffs, dataObjectIds, captionColumn, chartTitle);
        }

        @Override
        public QueryView createQueryView()
        {
            AssayProvider provider = AssayService.get().getProvider(_protocol);
            AssayProtocolSchema schema = provider.createProtocolSchema(_context.getUser(), _context.getContainer(), _protocol, null);
            QuerySettings settings = schema.getSettings(_context, AssayProtocolSchema.DATA_TABLE_NAME, AssayProtocolSchema.DATA_TABLE_NAME);
            QueryView dataView = new DrugSensitivityProtocolSchema.ResultsQueryView(_protocol, _context, settings)
            {
                public DataView createDataView()
                {
                    DataView view = super.createDataView();
                    SimpleFilter filter = new SimpleFilter();
                    SimpleFilter existingFilter = (SimpleFilter) view.getRenderContext().getBaseFilter();
                    if (existingFilter != null)
                        filter.addAllClauses(existingFilter);
                    List<Integer> objectIds = new ArrayList<>(_dataObjectIds.length);
                    for (int dataObjectId : _dataObjectIds)
                        objectIds.add(new Integer(dataObjectId));

                    filter.addInClause(FieldKey.fromString("RowId"), objectIds);
                    view.getDataRegion().setRecordSelectorValueColumns("RowId");
                    view.getRenderContext().setBaseFilter(filter);
                    return view;
                }
            };
            return dataView;
        }

        @Override
        public ActionURL getGraphRenderURL()
        {
            return new ActionURL(DrugSensitivityMultiGraphAction.class, _context.getContainer());
        }
    }

    @RequiresPermission(ReadPermission.class)
    @ContextualRoles(RunDatasetContextualRoles.class)
    public class DrugSensitivityMultiGraphAction extends MultiGraphAction<GraphSelectedForm>
    {
        @Override
        protected NabGraph.Config getGraphConfig(GraphSelectedForm form)
        {
            NabGraph.Config config = super.getGraphConfig(form);
            config.setyAxisLabel(GRAPH_Y_LABEL);

            return config;
        }
    }
}