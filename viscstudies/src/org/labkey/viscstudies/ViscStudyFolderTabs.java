/*
 * Copyright (c) 2011-2016 LabKey Corporation
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

import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.files.view.FilesWebPart;
import org.labkey.api.portal.ProjectUrls;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.ReadPermission;
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
        public boolean isVisible(Container c, User user)
        {
            Study study = StudyService.get().getStudy(c);
            return (study != null);
        }

        protected abstract String getPanelName();

        @Override
        public ActionURL getURL(Container container, User user)
        {
            if (container.hasPermission(user, ReadPermission.class) && showGWTStudyDesigner(container, user) && getPanelName() != null)
            {
                ActionURL actionURL = new ActionURL("study-designer", "designer", container);
                actionURL.addParameter("panel", getPanelName());
                return actionURL;
            }
            else
                return super.getURL(container, user);
        }

        public boolean showGWTStudyDesigner(Container c, User user)
        {
            // Issue 21092: show deprecated GWT study designer if we have a non-empty XML study design and no data in the study design hard tables
            Study study = StudyService.get().getStudy(c);
            return study != null
                    && study.hasGWTStudyDesign(c, user)
                    && study.getStudyProducts(user, null).size() == 0
                    && study.getStudyTreatments(user).size() == 0
                    && study.getAssaySpecimenConfigs("AssayName").size() == 0;
        }
    }

    public static class OverviewPage extends FolderTab
    {
        public OverviewPage(String caption)
        {
            super(caption);
        }

        @Override
        public ActionURL getURL(Container container, User user)
        {
            return PageFlowUtil.urlProvider(ProjectUrls.class).getBeginURL(container);
        }

        @Override
        public boolean isSelectedPage(ViewContext viewContext)
        {
            // Actual container we use doesn't matter, we just care about the controller and action names
            ActionURL defaultURL = PageFlowUtil.urlProvider(ProjectUrls.class).getBeginURL(ContainerManager.getHomeContainer());
            ActionURL currentURL = viewContext.getActionURL();
            return currentURL.getController().equalsIgnoreCase(defaultURL.getController()) && currentURL.getAction().equalsIgnoreCase(defaultURL.getAction()) && currentURL.getParameter("pageId") == null;
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
        public boolean isSelectedPage(ViewContext viewContext)
        {
            ActionURL currentURL = viewContext.getActionURL();
            return super.isSelectedPage(viewContext) ||
                    currentURL.getAction().equalsIgnoreCase("manageStudyProducts");
        }

        @Override
        public List<Portal.WebPart> createWebParts()
        {
            List<Portal.WebPart> parts = new ArrayList<>();
            parts.add(Portal.getPortalPart("Vaccine Design").createWebPart());
            return parts;
        }

        @Override
        protected String getPanelName()
        {
            return "VACCINE";
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
        public boolean isSelectedPage(ViewContext viewContext)
        {
            ActionURL currentURL = viewContext.getActionURL();
            return super.isSelectedPage(viewContext) ||
                    currentURL.getAction().equalsIgnoreCase("manageTreatments");
        }

        @Override
        public List<Portal.WebPart> createWebParts()
        {
            List<Portal.WebPart> parts = new ArrayList<>();
            parts.add(Portal.getPortalPart("Immunization Schedule").createWebPart());
            return parts;
        }

        @Override
        protected String getPanelName()
        {
            return "IMMUNIZATIONS";
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
        public boolean isSelectedPage(ViewContext viewContext)
        {
            ActionURL currentURL = viewContext.getActionURL();
            return super.isSelectedPage(viewContext) ||
                    currentURL.getAction().equalsIgnoreCase("manageAssaySchedule");
        }

        @Override
        public List<Portal.WebPart> createWebParts()
        {
            List<Portal.WebPart> parts = new ArrayList<>();
            parts.add(Portal.getPortalPart("Assay Schedule").createWebPart());
            return parts;
        }

        @Override
        protected String getPanelName()
        {
            return "ASSAYS";
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
                    currentURL.getController().equalsIgnoreCase("study-reports") ||
                    currentURL.getController().equalsIgnoreCase("dataset") ||
                    currentURL.getAction().equalsIgnoreCase("dataset") ||
                    currentURL.getAction().equalsIgnoreCase("subjectList") ||
                    currentURL.getAction().equalsIgnoreCase("participant");
        }

        @Override
         public List<Portal.WebPart> createWebParts()
        {
            List<Portal.WebPart> parts = new ArrayList<>();
            parts.add(Portal.getPortalPart("Data Views").createWebPart());

            Portal.WebPart specimenWebPart = Portal.getPortalPart(StudyService.SPECIMEN_BROWSE_WEBPART).createWebPart();
            specimenWebPart.setLocation(WebPartFactory.LOCATION_RIGHT);
            parts.add(specimenWebPart);

            Portal.WebPart filesWebPart = Portal.getPortalPart(FilesWebPart.PART_NAME).createWebPart();
            filesWebPart.setLocation(WebPartFactory.LOCATION_BODY);
            parts.add(filesWebPart);

            return parts;
        }

        @Override
        protected String getPanelName()
        {
            return null;
        }
    }


    public static class ManagePage extends FolderTab
    {
        protected ManagePage(String caption)
        {
            super(caption);
        }

        @Override
        public ActionURL getURL(Container container, User user)
        {
            return PageFlowUtil.urlProvider(StudyUrls.class).getManageStudyURL(container);
        }

        @Override
        public boolean isSelectedPage(ViewContext viewContext)
        {
            ActionURL currentURL = viewContext.getActionURL();
            return currentURL.getController().equalsIgnoreCase("study-definition") ||
                    currentURL.getController().equalsIgnoreCase("cohort") ||
                    currentURL.getController().equalsIgnoreCase("study-properties");
        }

        @Override
        public boolean isVisible(Container c, User user)
        {
            if (!c.hasPermission(user, AdminPermission.class))
                return false;

            Study study = StudyService.get().getStudy(c);
            return (study != null);
        }
    }
}
