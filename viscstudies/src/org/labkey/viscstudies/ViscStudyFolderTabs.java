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

import org.labkey.api.data.ContainerManager;
import org.labkey.api.portal.ProjectUrls;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.study.Study;
import org.labkey.api.study.StudyService;
import org.labkey.api.study.StudyUrls;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.FolderTab;
import org.labkey.api.view.Portal;
import org.labkey.api.view.ViewContext;
import org.labkey.api.view.WebPartFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User: migra
 * Date: Nov 7, 2011
 */
public class ViscStudyFolderTabs
{
    public static abstract class BasePage extends FolderTab.PortalPage
    {
       public BasePage(String pageId, String caption)
       {
           super(pageId, caption);
       }

        @Override
        public boolean isVisible(ViewContext context)
        {
            Study study = StudyService.get().getStudy(context.getContainer());
            return (study != null);
        }
    }

    public static class OverviewPage extends FolderTab
    {
        public OverviewPage(String caption)
        {
            super(caption);
        }

        @Override
        public ActionURL getURL(ViewContext context)
        {
            return PageFlowUtil.urlProvider(ProjectUrls.class).getBeginURL(context.getContainer());
        }

        @Override
        public boolean isSelectedPage(ViewContext viewContext)
        {
            // Actual container we use doesn't matter, we just care about the controller and action names
            ActionURL defaultURL = PageFlowUtil.urlProvider(ProjectUrls.class).getBeginURL(ContainerManager.getHomeContainer());
            ActionURL currentURL = viewContext.getActionURL();
            return currentURL.getPageFlow().equalsIgnoreCase(defaultURL.getPageFlow()) && currentURL.getAction().equalsIgnoreCase(defaultURL.getAction()) && currentURL.getParameter("pageId") == null;
        }
    }

    public static class VaccineDesignPage extends BasePage
    {
        public static final String PAGE_ID = "viscstudy.VACCINE_DESIGN";

        protected VaccineDesignPage(String caption)
        {
            super(PAGE_ID, caption);
        }

        @Override
        public List<Portal.WebPart> createWebParts()
        {
            List<Portal.WebPart> parts = new ArrayList<Portal.WebPart>();
            return parts;
        }
    }

    public static class ImmunizationsPage extends BasePage
    {
        public static final String PAGE_ID = "viscstudy.IMMUNIZATIONS";

        protected ImmunizationsPage(String caption)
        {
            super(PAGE_ID, caption);
        }

        @Override
        public List<Portal.WebPart> createWebParts()
        {
            List<Portal.WebPart> parts = new ArrayList<Portal.WebPart>();
            return parts;
        }
    }

    public static class AssaysPage extends BasePage
    {
        public static final String PAGE_ID = "viscstudy.ASSAYS";

        protected AssaysPage(String caption)
        {
            super(PAGE_ID, caption);
        }

        @Override
        public List<Portal.WebPart> createWebParts()
        {
            List<Portal.WebPart> parts = new ArrayList<Portal.WebPart>();
            return parts;
        }
    }

    public static class DataAnalysisPage extends BasePage
    {
        public static final String PAGE_ID = "study.DATA_ANALYSIS";

        protected DataAnalysisPage(String caption)
        {
            super(PAGE_ID, caption);
        }

        @Override
        public boolean isSelectedPage(ViewContext viewContext)
        {
            ActionURL currentURL = viewContext.getActionURL();
            return super.isSelectedPage(viewContext) ||
                    currentURL.getPageFlow().equalsIgnoreCase("study-reports") ||
                    currentURL.getPageFlow().equalsIgnoreCase("dataset") ||
                    currentURL.getAction().equalsIgnoreCase("dataset") ||
                    currentURL.getAction().equalsIgnoreCase("subjectList") ||
                    currentURL.getAction().equalsIgnoreCase("participant");
        }

        @Override
         public List<Portal.WebPart> createWebParts()
        {
            List<Portal.WebPart> parts = new ArrayList<Portal.WebPart>();
            parts.add(Portal.getPortalPart("Data Views").createWebPart());
            Portal.WebPart toolsWebPart = Portal.getPortalPart(StudyService.DATA_TOOLS_WEBPART_NAME).createWebPart();
            toolsWebPart.setLocation(WebPartFactory.LOCATION_RIGHT);
            parts.add(toolsWebPart);
            return parts;
        }

        @Override
        public boolean isVisible(ViewContext context)
        {
            Study study = StudyService.get().getStudy(context.getContainer());
            return (study != null);
        }
    }


    public static class ManagePage extends FolderTab
    {
        protected ManagePage(String caption)
        {
            super(caption);
        }

        @Override
        public ActionURL getURL(ViewContext context)
        {
            return PageFlowUtil.urlProvider(StudyUrls.class).getManageStudyURL(context.getContainer());
        }

        @Override
        public boolean isSelectedPage(ViewContext viewContext)
        {
            ActionURL currentURL = viewContext.getActionURL();
            return currentURL.getPageFlow().equalsIgnoreCase("study-definition") ||
                    currentURL.getPageFlow().equalsIgnoreCase("cohort") ||
                    currentURL.getPageFlow().equalsIgnoreCase("study-properties");
        }

        @Override
        public boolean isVisible(ViewContext context)
        {
            if (!context.getContainer().hasPermission(context.getUser(), AdminPermission.class))
                return false;

            Study study = StudyService.get().getStudy(context.getContainer());
            return (study != null);
        }
    }
}
