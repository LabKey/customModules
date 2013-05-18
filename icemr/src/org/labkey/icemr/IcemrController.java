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

package org.labkey.icemr;

import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.assay.dilution.DilutionAssayRun;
import org.labkey.api.assay.dilution.DilutionCurve;
import org.labkey.api.assay.nab.RenderAssayBean;
import org.labkey.api.assay.nab.view.RunDetailsAction;
import org.labkey.api.exp.ExperimentException;
import org.labkey.api.exp.api.ExpRun;
import org.labkey.api.security.ContextualRoles;
import org.labkey.api.security.RequiresPermissionClass;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.study.assay.AssayUrls;
import org.labkey.api.study.assay.RunDataSetContextualRoles;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.icemr.assay.actions.DrugSensitivityUploadWizardAction;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class IcemrController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(IcemrController.class,
            DrugSensitivityUploadWizardAction.class);

    public IcemrController()
    {
        setActionResolver(_actionResolver);
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class DetailsAction extends RunDetailsAction<RenderAssayBean>
    {
        @Override
        public ModelAndView getView(RenderAssayBean form, BindException errors) throws Exception
        {
            form.setSampleNoun("Drug Treatment");
            return super.getView(form, errors);
        }

        public NavTree appendNavTrail(NavTree root)
        {
            ActionURL runDataURL = PageFlowUtil.urlProvider(AssayUrls.class).getAssayResultsURL(getContainer(), _protocol, _runRowId);
            return root.addChild(_protocol.getName() + " Data", runDataURL).addChild("Run " + _runRowId + " Details");
        }
    }
}