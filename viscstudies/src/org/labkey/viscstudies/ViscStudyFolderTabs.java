package org.labkey.viscstudies;

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
import org.springframework.web.servlet.mvc.Controller;

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
       public BasePage(String pageId, String caption, Class<? extends Controller> clazz)
       {
           super(pageId, caption, clazz);
       }

        @Override
        public boolean isVisible(ViewContext context)
        {
            Study study = StudyService.get().getStudy(context.getContainer());
            return (study != null);
        }
    }

    public static class OverviewPage extends BasePage
    {
        public static final String PAGE_ID = "study.OVERVIEW";

        protected OverviewPage(String caption)
        {
            super(PAGE_ID, caption, ViscStudiesController.BeginAction.class);
        }

        @Override
        public List<Portal.WebPart> createWebParts()
        {
            List<Portal.WebPart> parts = new ArrayList<Portal.WebPart>();
            parts.add(Portal.getPortalPart("Study Overview").createWebPart());
            return parts;
        }

        @Override
        public boolean isVisible(ViewContext context)
        {
            Study study = StudyService.get().getStudy(context.getContainer());
            return (study != null);
        }
    }

    public static class VaccineDesignPage extends BasePage
    {
        public static final String PAGE_ID = "viscstudy.VACCINE_DESIGN";

        protected VaccineDesignPage(String caption)
        {
            super(PAGE_ID, caption, ViscStudiesController.BeginAction.class);
        }

        @Override
        public boolean isSelectedPage(ActionURL currentURL)
        {
            return super.isSelectedPage(currentURL);
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
            super(PAGE_ID, caption, ViscStudiesController.BeginAction.class);
        }

        @Override
        public boolean isSelectedPage(ActionURL currentURL)
        {
            return super.isSelectedPage(currentURL);
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
            super(PAGE_ID, caption, ViscStudiesController.BeginAction.class);
        }

        @Override
        public boolean isSelectedPage(ActionURL currentURL)
        {
            return super.isSelectedPage(currentURL);
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
            super(PAGE_ID, caption, ViscStudiesController.BeginAction.class);
        }

        @Override
        public boolean isSelectedPage(ActionURL currentURL)
        {
            return super.isSelectedPage(currentURL) ||
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
        public boolean isSelectedPage(ActionURL currentURL)
        {
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
