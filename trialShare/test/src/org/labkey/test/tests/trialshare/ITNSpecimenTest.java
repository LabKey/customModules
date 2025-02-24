package org.labkey.test.tests.trialshare;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.remoteapi.Connection;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestFileUtils;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.Git;
import org.labkey.test.components.study.specimen.SpecimenDetailGrid;
import org.labkey.test.pages.study.specimen.ManageRequestPage;
import org.labkey.test.pages.study.specimen.ManageRequestStatusPage;
import org.labkey.test.pages.study.specimen.ShowCreateSpecimenRequestPage;
import org.labkey.test.pages.study.specimen.TypeSummaryReportPage;
import org.labkey.test.util.ApiPermissionsHelper;
import org.labkey.test.util.OptionalFeatureHelper;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.PermissionsHelper.MemberType;
import org.labkey.test.util.TestLogger;
import org.labkey.test.util.study.specimen.SpecimenHelper;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Category({Git.class})
public class ITNSpecimenTest extends BaseWebDriverTest
{
    private static final File STUDY_ARCHIVE = TestFileUtils.getSampleData("studies/LabkeyDemoStudy.zip");
    private static final String ITN_SPECIMEN_HANDLING = "ITNSpecimenHandling";

    private static final String SPECIMEN_COORDINATOR = "specimen_coordinator@itnspecimen.test";
    private static final String MAGIC_GROUP = "Specimen Request Administrators";

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        super.doCleanup(afterTest);
        enableITNSpecimenHandling(false);
        _userHelper.deleteUsers(false, SPECIMEN_COORDINATOR);
    }

    @BeforeClass
    public static void setupProject()
    {
        ITNSpecimenTest init = (ITNSpecimenTest) getCurrentTest();

        init.doSetup();
    }

    private void doSetup()
    {
        _containerHelper.createProject(getProjectName(), "Study");
        _containerHelper.enableModule("Specimen");
        importStudyFromZip(STUDY_ARCHIVE);
        new SpecimenHelper(this).setupRequestStatuses();
    }

    @Before
    public void preTest()
    {
        enableITNSpecimenHandling(true);
        goToProjectHome();
    }

    @Test
    public void testSpecimenReportCustomization()
    {
        final String primaryType = "Blood (Whole)";
        final String derivativeType1 = "PBMC Cells, Viable";
        final String derivativeType2 = "Plasma, Unknown Processing";

        TestLogger.log("Check standard specimen report behavior to ensure Locators are accurate");
        enableITNSpecimenHandling(false);
        TypeSummaryReportPage typeSummaryReportPage = TypeSummaryReportPage.beginAt(this, getProjectName());
        assertThat("Default type breakdown.", typeSummaryReportPage.getTypeBreakdown(), CoreMatchers.containsString("Show results by"));
        assertTextPresent(primaryType, derivativeType1, derivativeType2);

        TestLogger.log("Verify customized specimen report behavior");
        enableITNSpecimenHandling(true);
        typeSummaryReportPage = TypeSummaryReportPage.beginAt(this, getProjectName());
        try
        {
            typeSummaryReportPage.getTypeBreakdown();
            Assert.fail("Type breakdown select should be removed from custom ITN specimen report");
        }
        catch (NoSuchElementException ignore) { /* expected exception */ }

        assertTextPresent(primaryType);
        assertTextNotPresent(derivativeType1, derivativeType2);
    }

    @Test
    public void testSpecimenRequestCustomization()
    {
        SpecimenHelper specimenHelper = new SpecimenHelper(this);
        clickPortalTab("Specimen Data");

        waitAndClickAndWait(Locator.linkWithText("Urine"));
        SpecimenDetailGrid specimenGrid = specimenHelper.findSpecimenDetailGrid();
        specimenGrid.checkCheckbox(0);
        ShowCreateSpecimenRequestPage requestPage = specimenGrid.createNewRequest();

        // TODO: Refactor to use SampleManagementErrorLogger after it has been moved out of sampleManagement
        TestLogger.log("\"Requesting Location\" should not be available for ITN specimen requests");
        assertElementNotVisible(Locator.id("destinationLocation"));

        ManageRequestPage manageRequestPage = requestPage
                .setDetails("plan", "info") // required fields
                .clickCreateAndViewDetails();

        String requestingLocation = manageRequestPage.getRequestInformation("Requesting Location");
        assertEquals("Wrong 'Requesting Location'.", "User Request", requestingLocation);

        specimenGrid = specimenHelper.findSpecimenDetailGrid();
        specimenGrid.checkCheckbox(0);
        specimenGrid.clickHeaderButtonAndWait("Remove Selected");

        TestLogger.log("Should be able to submit a request without any specimens");
        manageRequestPage = manageRequestPage
                .submitRequest();

        TestLogger.log("Validate message from 'ITNSpecimenRequestCustomizer'");
        assertElementPresent(Locator.linkWithText("trialsharesupport@immunetolerance.org"));
        assertTextPresent("Thank you for your request. A representative from the ITN will be in touch with you.");
    }

    @Test
    public void testRequestPermissionCustomization()
    {
        ApiPermissionsHelper permissions = new ApiPermissionsHelper(this);

        _userHelper.createUser(SPECIMEN_COORDINATOR);
        permissions.addMemberToRole(SPECIMEN_COORDINATOR, "Specimen Coordinator", MemberType.user, getProjectName());
        permissions.addMemberToRole(SPECIMEN_COORDINATOR, "Reader", MemberType.user, getProjectName());
        permissions.createGlobalPermissionsGroup(MAGIC_GROUP);

        ManageRequestStatusPage manageRequestStatusPage = ShowCreateSpecimenRequestPage.beginAt(this, getProjectName())
                .setDetails("A", "B")
                .clickCreateAndViewDetails()
                .submitRequest()
                .clickUpdateRequest();

        TestLogger.log("Site admin should be able to set request status");
        manageRequestStatusPage
                .setStatus("Processing")
                .clickSave();

        int id = Integer.parseInt(getUrlParam("id"));

        impersonate(SPECIMEN_COORDINATOR);
        {
            TestLogger.log("Need to be in special group to update requrest status");
            manageRequestStatusPage = ManageRequestStatusPage.beginAt(this, getProjectName(), id);
            try
            {
                manageRequestStatusPage.getStatus();
                Assert.fail("Only users in the correct group should see 'status' dropdown");
            }
            catch (UnexpectedTagNameException ignore) { /* expected exception */ }

            String readOnlyStatus = Locator.input("status").parent().findElement(getDriver()).getText();
            assertEquals("Should see status", "Processing", readOnlyStatus);
        }
        stopImpersonating();

        permissions.addUserToSiteGroup(SPECIMEN_COORDINATOR, MAGIC_GROUP);

        impersonate(SPECIMEN_COORDINATOR);
        {
            ManageRequestStatusPage.beginAt(this, getProjectName(), id)
                    .setStatus("Completed")
                    .clickSave();
        }
        stopImpersonating();

        String status = ManageRequestPage.beginAt(this, getProjectName(), id).getRequestInformation("Status");
        assertEquals("Final Status", "Completed", status);
    }

    @LogMethod(quiet = true)
    public void enableITNSpecimenHandling(boolean enable)
    {
        Connection cn = createDefaultConnection(false);
        OptionalFeatureHelper.setOptionalFeature(cn, ITN_SPECIMEN_HANDLING, enable);
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    protected String getProjectName()
    {
        return "TrialShareSpecimenTest Project";
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("trialShare");
    }
}
