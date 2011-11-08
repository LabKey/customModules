/*
 * Copyright (c) 2011 LabKey Corporation
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

package org.labkey.viscstudies;

import org.labkey.api.action.HasViewContext;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.data.Container;
import org.labkey.api.module.FolderType;
import org.labkey.api.security.ActionNames;
import org.labkey.api.security.RequiresPermissionClass;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.view.HttpView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.Portal;
import org.labkey.api.view.VBox;
import org.labkey.api.view.ViewContext;
import org.labkey.api.view.template.HomeTemplate;
import org.labkey.api.view.template.PageConfig;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class ViscStudiesController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(ViscStudiesController.class);

    public ViscStudiesController()
    {
        setActionResolver(_actionResolver);
    }

    public static class PageForm implements HasViewContext
    {
        private String _pageName;
        private String _caption = null;
        private ViewContext _viewContext;

        @Override
        public void setViewContext(ViewContext context)
        {
            _viewContext = context;
        }

        @Override
        public ViewContext getViewContext()
        {
            return _viewContext;
        }

        public String getPageName()
        {
            return _pageName;
        }

        public void setPageName(String pageName)
        {
            _pageName = pageName;
        }

        public String getPageCaption()
        {
            if (_caption == null)
            {
                FolderType type = getViewContext().getContainer().getFolderType();
                if (type instanceof ViscStudyFolderType)
                    _caption = ((ViscStudyFolderType) type).getPageCaption(getPageName());
            }
            return _caption;
        }
    }

    @ActionNames("begin, page")
    @RequiresPermissionClass(ReadPermission.class)
    public class BeginAction extends SimpleViewAction<PageForm>
    {
        private PageForm _page;

        @Override
        public ModelAndView getView(PageForm pageForm, BindException errors) throws Exception
        {
            Container c = getViewContext().getContainer();
            _page = pageForm;

            HttpView template = new HomeTemplate(getViewContext(), c, new VBox(), getPageConfig(), new NavTree[0]);
            Portal.populatePortalView(getViewContext(), pageForm.getPageName(), template);

            getPageConfig().setTitle(pageForm.getPageCaption());
            getPageConfig().setTemplate(PageConfig.Template.None);
            return template;
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return root;
        }
    }
}