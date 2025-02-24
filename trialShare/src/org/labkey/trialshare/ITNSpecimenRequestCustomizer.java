package org.labkey.trialshare;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.security.Group;
import org.labkey.api.security.GroupManager;
import org.labkey.api.security.User;
import org.labkey.api.study.SpecimenUrls;
import org.labkey.api.study.SpecimenService;
import org.labkey.api.util.HtmlString;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.security.xml.GroupEnumType;

import static org.labkey.api.util.PageFlowUtil.link;

public class ITNSpecimenRequestCustomizer implements SpecimenService.SpecimenRequestCustomizer
{
    private static final Logger LOG = LogManager.getLogger(ITNSpecimenRequestCustomizer.class);

    @Override
    public boolean allowEmptyRequests()
    {
        return true;
    }

    @Override
    public Integer getDefaultDestinationSiteId()
    {
        return 9999999;
    }

    @Override
    public boolean omitTypeGroupingsWhenReporting()
    {
        return true;
    }

    @Override
    public boolean canChangeStatus(User user)
    {
        Group specimenRequestAdmins = GroupManager.getGroup(ContainerManager.getRoot(), "Specimen Request Administrators", GroupEnumType.SITE);
        if (specimenRequestAdmins != null)
        {
            return user.hasSiteAdminPermission() || user.isInGroup(specimenRequestAdmins.getUserId());
        }
        LOG.error("Unable to find site group \"Specimen Request Administrators\", allowing user to update specimen request status");
        return true;
    }

    @Override
    public HtmlString getSubmittedMessage(Container c, int requestId)
    {
        SpecimenUrls specimenUrls = PageFlowUtil.urlProvider(SpecimenUrls.class);
        return HtmlString.unsafe("Thank you for your request. A representative from the ITN will be in touch " +
                "with you. You can also contact us at " +
                link("trialsharesupport@immunetolerance.org").href("mailto:trialsharesupport@immunetolerance.org?Body=" + PageFlowUtil.filter(specimenUrls.getManageRequestURL(c, requestId).getURIString())).clearClasses() +
                " to follow up with us regarding this request.<br/>" +
                "You may also update this request at any calendar to add comments or supporting " +
                "documents by clicking here " + link("Update Request").href(specimenUrls.getManageRequestStatusURL(c, requestId)));
    }

    @Override
    public boolean hideRequestWarnings()
    {
        return true;
    }
}
