/*
 * Copyright (c) 2007-2013 LabKey Corporation
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

package org.labkey.test.tests;

import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.WebTestHelper;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LabKeyExpectedConditions;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.LoggedParam;
import org.labkey.test.util.PasswordUtil;
import org.labkey.test.util.ext4cmp.Ext4FieldRefWD;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * User: brittp
 * Date: Mar 9, 2006
 * Time: 1:54:57 PM
 */
public class SpecimenTest extends StudyBaseTestWD
{
    protected static final String PROJECT_NAME = "SpecimenVerifyProject";
    public static final String SPECIMEN_DETAIL = "SpecimenDetail";
    private static final String DESTINATION_SITE = "Aurum Health KOSH Lab, Orkney, South Africa (Endpoint Lab, Repository)";
    private static final String SOURCE_SITE = "Contract Lab Services, Johannesburg, South Africa (Repository, Clinic)";
    private static final String USER1 = "user1@specimen.test";
    private static final String USER2 = "user2@specimen.test";
    private static final String REQUESTABILITY_QUERY = "RequestabilityRule";
    private static final String UNREQUESTABLE_SAMPLE = "BAA07XNP-02";
    private final File REQUEST_ATTACHMENT = new File(getPipelinePath() + "specimens", "labs.txt");
    private static final String[] PTIDS = {"999320396","999320812"};
    private int _requestId;


    public String getAssociatedModuleDirectory()
    {
        return "server/modules/study";
    }

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    @Override
    protected boolean isFileUploadTest()
    {
        return true;
    }

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        deleteUsers(afterTest, USER1, USER2);
        super.doCleanup(afterTest);
    }

    @Override
    @LogMethod(category = LogMethod.MethodType.SETUP)
    protected void doCreateSteps()
    {
        enableEmailRecorder();
        initializeFolder();

        clickButton("Create Study");
        setFormElement(Locator.name("label"), getStudyLabel());
        click(Locator.radioButtonByNameAndValue("simpleRepository", "false"));
        clickButton("Create Study");

        setPipelineRoot(getPipelinePath());

        setupRequestabilityRules();
        startSpecimenImport(1);
        waitForSpecimenImport();
        _studyHelper.createCustomParticipantGroup(getProjectName(), getFolderName(), "Category1", "Participant", null, false, PTIDS[0], PTIDS[1]);
        checkTubeType();
        setupSpecimenManagement();
        setupActorsAndGroups();
        setupDefaultRequirements();
        setupRequestForm();
        setupActorNotification();
        uploadSpecimensFromFile();
    }

    @Override
    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    protected void doVerifySteps()
    {
        verifyActorDetails();
        createRequest();
        verifyViews();
        verifyAdditionalRequestFields();
        verifyNotificationEmails();
        verifyInactiveUsersInRequests();
        verifyRequestCancel();
        verifyReports();
        exportSpecimenTest();
        verifyRequestingLocationRestriction();
        verifySpecimenTableAttachments();
        searchTest();
        verifySpecimenGroupings();
    }

    @LogMethod
    private void setupRequestabilityRules()
    {
        // Create custom query to test requestability rules.
        goToSchemaBrowser();
        selectQuery("study", SPECIMEN_DETAIL);
        clickButton("Create New Query");
        setFormElement(Locator.name("ff_newQueryName"), REQUESTABILITY_QUERY);
        clickAndWait(Locator.linkWithText("Create and Edit Source"));
        setQueryEditorValue("queryText",
                "SELECT \n" +
                SPECIMEN_DETAIL + ".GlobalUniqueId AS GlobalUniqueId\n" +
                "FROM " + SPECIMEN_DETAIL + "\n" +
                "WHERE " + SPECIMEN_DETAIL + ".GlobalUniqueId='" + UNREQUESTABLE_SAMPLE + "'");
        clickButton("Save", 0);
        waitForText("Saved", WAIT_FOR_JAVASCRIPT);

        clickAndWait(Locator.linkWithText(getFolderName()));
        waitAndClick(Locator.linkWithText("Manage Study"));
        waitAndClick(Locator.linkWithText("Manage Requestability Rules"));
        // Verify that LOCKED_IN_REQUEST is the last rule
        waitForElement(Locator.xpath("//div[contains(@class, 'x-grid3-row-last')]//div[text()='Locked In Request Check']"));
        click(Locator.xpath("//div[contains(@class, 'x-grid3-row-last')]//div[text()='Locked In Request Check']"));
        // Verify that LOCKED_IN_REQUEST rule cannot be moved or deleted
        assertElementPresent(Locator.xpath("//table[@id='btn_deleteEngine' and contains(@class, 'x-item-disabled')]"));
        assertElementPresent(Locator.xpath("//table[@id='btn_moveUp' and contains(@class, 'x-item-disabled')]"));
        assertElementPresent(Locator.xpath("//table[@id='btn_moveDown' and contains(@class, 'x-item-disabled')]"));
        click(Locator.xpath("//div[contains(@class, 'x-grid3-col-numberer') and text()='2']"));
        assertElementPresent(Locator.xpath("//table[@id='btn_deleteEngine' and not(contains(@class, 'x-item-disabled'))]"));
        assertElementPresent(Locator.xpath("//table[@id='btn_moveUp' and not(contains(@class, 'x-item-disabled'))]"));
        assertElementPresent(Locator.xpath("//table[@id='btn_moveDown' and contains(@class, 'x-item-disabled')]"));
        clickButton("Add Rule", 0);
        click(Locator.menuItem("Custom Query"));
        _extHelper.selectComboBoxItem(Locator.xpath("//div[@id='x-form-el-userQuery_schema']"), "study" );
        _extHelper.selectComboBoxItem(Locator.xpath("//div[@id='x-form-el-userQuery_query']"), REQUESTABILITY_QUERY );
        _extHelper.selectComboBoxItem(Locator.xpath("//div[@id='x-form-el-userQuery_action']"), "Unavailable" );
        clickButton("Submit",0);
        clickButton("Save");
    }

    @LogMethod
    private void checkTubeType()
    {
        // Field check for Tube Type column (including conflict)
        clickAndWait(Locator.linkWithText(getStudyLabel()));
        addWebPart("Specimens");
        clickAndWait(Locator.linkWithText("By Individual Vial"));
        setFilter(SPECIMEN_DETAIL, "PrimaryType", "Is Blank");
        // Verify that there's only one vial of unknown type:
        assertLinkPresentWithTextCount("[history]", 1);
        // There's a conflict in TubeType for this vial's events; verify that no TubeType is populated at the vial level
        assertTextNotPresent("Cryovial");
        clickAndWait(Locator.linkWithText("[history]"));
        // This vial has three events, each of which list a different tube type:
        assertTextPresent("15ml Cryovial");
        assertTextPresent("20ml Cryovial");
        assertTextPresent("25ml Cryovial");
        clickAndWait(Locator.linkWithText("Specimen Overview"));
        click(Locator.linkWithText("Vials by Derivative Type"));
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Tear Flo Strips"), WAIT_FOR_PAGE);
        // For these three vials, there should be no conflict in TubeType, so we should see the text once for each of three vials:
        assertLinkPresentWithTextCount("[history]", 3);
        assertTextPresent("15ml Cryovial", 3);
    }

    @LogMethod
    private void setupActorsAndGroups()
    {
        clickAndWait(Locator.linkWithText("Manage Actors and Groups"));
        setFormElement(Locator.name("newLabel"), "SLG");
        selectOptionByText(Locator.name("newPerSite"), "One Per Study");
        clickButton("Save");
        clickAndWait(Locator.linkWithText("Update Members"));
        setFormElement(Locator.name("names"), USER1);
        uncheckCheckbox("sendEmail");
        clickButton("Update Members");
        setFormElement(Locator.name("newLabel"), "IRB");
        selectOptionByText(Locator.name("newPerSite"), "Multiple Per Study (Location Affiliated)");
        clickButton("Save");
        clickAndWait(Locator.linkWithText("Update Members").index(1));
        clickAndWait(Locator.linkWithText(DESTINATION_SITE));
        setFormElement(Locator.name("names"), USER2);
        uncheckCheckbox("sendEmail");
        clickAndWait(Locator.linkWithText("Update Members"));
        clickAndWait(Locator.linkWithText(getStudyLabel()));
    }

    @LogMethod (quiet = true)
    private void setupDefaultRequirements()
    {
        clickAndWait(Locator.linkWithText("Manage Study"));
        clickAndWait(Locator.linkWithText("Manage Default Requirements"));
        selectOptionByText(Locator.name("originatorActor"), "IRB");
        setFormElement(Locator.name("originatorDescription"), "Originating IRB Approval");
        clickButton("Add Requirement");
        selectOptionByText(Locator.name("providerActor"), "IRB");
        setFormElement(Locator.name("providerDescription"), "Providing IRB Approval");
        clickAndWait(Locator.xpath("//input[@name='providerDescription']/../.." + Locator.navButton("Add Requirement").getPath()));
        selectOptionByText(Locator.name("receiverActor"), "IRB");
        setFormElement(Locator.name("receiverDescription"), "Receiving IRB Approval");
        clickAndWait(Locator.xpath("//input[@name='receiverDescription']/../.." + Locator.navButton("Add Requirement").getPath()));
        selectOptionByText(Locator.name("generalActor"), "SLG");
        setFormElement(Locator.name("generalDescription"), "SLG Approval");
        clickAndWait(Locator.xpath("//input[@name='generalDescription']/../.." + Locator.navButton("Add Requirement").getPath()));
        clickTab("Manage");
    }

    @LogMethod (quiet = true)
    private void setupRequestForm()
    {
        clickAndWait(Locator.linkWithText("Manage New Request Form"));
        clickButton("Add New Input", 0);
        setFormElement(Locator.xpath("//descendant::input[@name='title'][4]"), "Last One");
        setFormElement(Locator.xpath("//descendant::input[@name='helpText'][4]"), "A test input");
        click(Locator.xpath("//descendant::input[@name='required'][4]"));
        clickButton("Save");
        clickAndWait(Locator.linkWithText(getStudyLabel()));
    }

    @LogMethod
    private void setupActorNotification()
    {
        log("Check Configure Defaults for Actor Notification");
        clickAndWait(Locator.linkWithText(getStudyLabel()));
        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Manage Notifications"));
        assertTextPresent("Default Email Recipients");
        checkRadioButton("defaultEmailNotify", "All");
        clickButton("Save");
    }

    @LogMethod
    private void uploadSpecimensFromFile()
    {
        log("Check Upload Specimen List from file");
        clickAndWait(Locator.linkWithText(getStudyLabel()));
        clickAndWait(Locator.linkWithText("Specimen Data"));
        waitAndClick(Locator.linkWithText("Specimen Requests")); // expand node in Specimens webpart
        clickAndWait(Locator.linkWithText("Create New Request"));
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input2"), "Comments");
        setFormElement(Locator.id("input1"), "Shipping");
        setFormElement(Locator.id("input3"), "Last one");
        clickButton("Create and View Details");
        clickAndWait(Locator.linkWithText("Upload Specimen Ids"));
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-01");     // add specimen
        clickButton("Submit");    // Submit button

        waitAndClick(Locator.linkWithText("Upload Specimen Ids"));
        waitForElement(Locator.xpath("//textarea[@id='tsv3']"));
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-01");     // try to add again
        clickButton("Submit", 0);    // Submit button
        waitForText("Specimen AAA07XK5-01 not available", 20000);
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-02");     // try to add one that doesn't exist
        clickButton("Submit", 0);    // Submit button
        waitForText("Specimen AAA07XK5-02 not available", 20000);
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-04\nAAA07XK5-06\nAAA07XSF-03");     // add different one
        clickButton("Submit");    // Submit button
    }

    @LogMethod (quiet = true)
    private void verifyActorDetails()
    {
        // Check each Actor's Details for "Default Actor Notification" feature;
        // In Details, for each actor the ether Notify checkbox should be set or disabled, because we set Notifications to All
        List<Locator> detailsLinks = findAllMatches(Locator.xpath("//td[a='Details']/a"));
        for (Locator link : detailsLinks)
        {
            clickAndWait(link);
            List<Locator> allCheckBoxes = findAllMatches(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs']"));
            List<Locator> checkedCheckBoxes = findAllMatches(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs' and @checked]"));
            List<Locator> disabledCheckBoxes = findAllMatches(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs' and @disabled]"));
            Assert.assertTrue("Actor Notification: All actors should be notified if addresses configured.", allCheckBoxes.size() == checkedCheckBoxes.size() + disabledCheckBoxes.size());
            clickButton("Cancel");
        }

        waitForElement(Locator.css("span.labkey-wp-title-text").withText("Associated Specimens"));
        assertTextPresent("AAA07XK5-01", "AAA07XK5-04", "AAA07XK5-06", "AAA07XSF-03");


        clickButton("Cancel Request", 0);
        assertAlert("Canceling will permanently delete this pending request.  Continue?");
        waitForElement(Locator.id("dataregion_SpecimenRequest"));
    }

    @LogMethod
    private void createRequest()
    {
        clickAndWait(Locator.linkWithText(getStudyLabel()));
        clickAndWait(Locator.linkWithText("Specimen Data"));
        click(Locator.linkWithText("Vials by Derivative Type"));
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Plasma, Unknown Processing"), WAIT_FOR_PAGE);
        // Verify unavailable sample
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "' and @disabled]"));
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "']/../../td[contains(text(), 'This vial is unavailable because it was found in the set called \"" + REQUESTABILITY_QUERY + "\".')]"));
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "']/../a[contains(@onmouseover, 'This vial is unavailable because it was found in the set called \\\"" + REQUESTABILITY_QUERY + "\\\".')]"));
        checkCheckbox(".toggle");

        clickMenuButton("Page Size", "Show All");
         clickAndWait(Locator.linkContainingText("history"));
        assertTextPresent("Vial History");
        goBack();

        clickMenuButton("Request Options", "Create New Request");
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input2"), "Comments");
        setFormElement(Locator.id("input1"), "Shipping");
        clickButton("Create and View Details");
        assertTextPresent("Please provide all required input.");
        setFormElement(Locator.id("input3"), "sample last one input");
        clickButton("Create and View Details");
        assertTextPresent("sample last one input");
        assertTextPresent("IRB");
        assertTextPresent("KCMC, Moshi, Tanzania");
        assertTextPresent("Originating IRB Approval");
        assertTextPresent(SOURCE_SITE);
        assertTextPresent("Providing IRB Approval");
        assertTextPresent(DESTINATION_SITE);
        assertTextPresent("Receiving IRB Approval");
        assertTextPresent("SLG");
        assertTextPresent("SLG Approval");
        assertTextPresent("BAA07XNP-01");
        assertTextNotPresent(UNREQUESTABLE_SAMPLE);
        // verify that the swab specimen isn't present yet
        assertTextNotPresent("DAA07YGW-01");
        assertTextNotPresent("Complete");
        clickAndWait(Locator.linkWithText(getStudyLabel()));

        // add additional specimens
        clickAndWait(Locator.linkWithText("Specimen Data"));
        waitAndClick(Locator.linkWithText("Vials by Derivative Type"));
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Swab"), WAIT_FOR_PAGE);
        checkCheckbox(".toggle");
        clickMenuButtonAndContinue("Request Options", "Add To Existing Request");
        _extHelper.waitForExtDialog("Request Vial", WAIT_FOR_JAVASCRIPT);
        waitForElement(Locator.css("#request-vial-details .x-grid3-row"));
        clickButton("Add 8 Vials to Request", 0);
        _extHelper.waitForExtDialog("Success", WAIT_FOR_JAVASCRIPT * 5);
        clickButton("OK", 0);
        clickMenuButton("Request Options", "View Existing Requests");
        clickButton("Details");
        assertTextPresent("sample last one input");
        assertTextPresent("IRB");
        assertTextPresent("KCMC, Moshi, Tanzania");
        assertTextPresent("Originating IRB Approval");
        assertTextPresent(SOURCE_SITE);
        assertTextPresent("Providing IRB Approval");
        assertTextPresent(DESTINATION_SITE);
        assertTextPresent("Receiving IRB Approval");
        assertTextPresent("SLG");
        assertTextPresent("SLG Approval");
        assertTextPresent("BAA07XNP-01");
        assertTextPresent("DAA07YGW-01");

        // submit request
        assertTextPresent("Not Yet Submitted");
        assertTextNotPresent("New Request");
        clickButton("Submit Request", 0);
        assertAlert("Once a request is submitted, its specimen list may no longer be modified.  Continue?");
        assertTextNotPresent("Not Yet Submitted");
        assertTextPresent("New Request");

        // Add request attachment
        click(Locator.linkWithText("Update Request"));
        waitForElement(Locator.name("formFiles[0]"));
        setFormElement(Locator.name("formFiles[0]"), REQUEST_ATTACHMENT);
        clickButton("Save Changes and Send Notifications");
        waitForElement(Locator.linkWithText(" " + REQUEST_ATTACHMENT.getName()));

        // modify request
        selectOptionByText(Locator.name("newActor"), "SLG");
        setFormElement(Locator.name("newDescription"), "Other SLG Approval");
        clickButton("Add Requirement");
        clickAndWait(Locator.linkWithText("Details"));
        checkCheckbox("complete");
        checkCheckbox("notificationIdPairs");
        checkCheckbox("notificationIdPairs", 1);
        clickButton("Save Changes and Send Notifications");
        waitForElement(Locator.css(".labkey-message").withText("Complete"));
    }

    @LogMethod
    private void verifyViews()
    {
        clickAndWait(Locator.linkWithText("View History"));
        assertTextPresent("Request submitted for processing.");
        assertTextPresent("Notification Sent", 2);
        assertTextPresent(USER1);
        assertTextPresent(USER2);
        clickAndWait(Locator.linkWithText("View Request"));
        clickAndWait(Locator.linkWithText("Originating Location Specimen Lists"));
        // Ordering of locations is nondeterministic
        if (isPresentInThisOrder("The McMichael Lab, Oxford, UK", "KCMC, Moshi, Tanzania") == null)
        {
            checkCheckbox("notify"); // MCMichael - SLG
            _specimen_McMichael = getText(Locator.xpath("//tr[@class = 'labkey-alternate-row']/td[3]//td"));
            checkCheckbox("notify", 4); // KCMC - IRB, Aurum Health KOSH
            _specimen_KCMC = getText(Locator.xpath("//tr[@class = 'labkey-row']/td[3]//td"));
        }
        else
        {
            checkCheckbox("notify", 1); // KCMC - IRB, Aurum Health KOSH
            _specimen_KCMC = getText(Locator.xpath("//tr[@class = 'labkey-alternate-row']/td[3]//td"));
            checkCheckbox("notify", 3); // MCMichael - SLG
            _specimen_McMichael = getText(Locator.xpath("//tr[@class = 'labkey-row']/td[3]//td"));
        }
        checkCheckbox("sendXls");
        checkCheckbox("sendTsv");
        clickButton("Send Email");
        _requestId = Integer.parseInt(getUrlParam(getURL().toString(), "id", false));
        clickAndWait(Locator.linkWithText("Providing Location Specimen Lists"));
        assertTextPresent(SOURCE_SITE);
        clickButton("Cancel");
    }

    @LogMethod
    private void verifyAdditionalRequestFields()
    {
        log("verifying addtional freezer fields from the exports");
        clickAndWait(Locator.linkWithText("Originating Location Specimen Lists"));
        addUrlParameter("exportAsWebPage=true");
        refresh();

        pushLocation();
         clickAndWait(Locator.linkContainingText("Export to text file"));

        // verify the additional columns
        assertTextPresent("Freezer", "Fr Container", "Fr Position", "Fr Level1", "Fr Level2");
        popLocation();

        // customize the locationSpecimenListTable then make sure changes are propogated to the exported lists
        log("customizing the locationSpecimenList default view");
        pushLocation();
        goToSchemaBrowser();
        selectQuery("study", "LocationSpecimenList");
        waitForText("view data");
         clickAndWait(Locator.linkContainingText("view data"));

        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.removeCustomizeViewColumn("Freezer");
        _customizeViewsHelper.removeCustomizeViewColumn("Fr_Container");
        _customizeViewsHelper.removeCustomizeViewColumn("Fr_Position");
        _customizeViewsHelper.removeCustomizeViewColumn("Fr_Level1");
        _customizeViewsHelper.saveCustomView();
        popLocation();

        log("verifying column changes");
        clickAndWait(Locator.linkWithText(getStudyLabel()));
        clickAndWait(Locator.linkWithText("Specimen Data"));
        waitAndClick(Locator.linkWithText("Specimen Requests")); // expand node in Specimens webpart
        clickAndWait(Locator.xpath("//a[text() = 'View Current Requests']"));

        clickButton("Details");
        clickAndWait(Locator.linkWithText("Originating Location Specimen Lists"));
        addUrlParameter("exportAsWebPage=true");
        refresh();
        pushLocation();
         clickAndWait(Locator.linkContainingText("Export to text file"));

        // verify the additional columns
        assertTextNotPresent("Freezer", "Fr Container", "Fr Position", "Fr Level1");
        assertTextPresent("Fr Level2");
        popLocation();

        clickButton("Cancel");
        clickAndWait(Locator.linkWithText("Providing Location Specimen Lists"));
        addUrlParameter("exportAsWebPage=true");
        refresh();
        pushLocation();
         clickAndWait(Locator.linkContainingText("Export to text file"));

        // verify the additional columns
        assertTextNotPresent("Freezer", "Fr Container", "Fr Position", "Fr Level1");
        assertTextPresent("Fr Level2");
        popLocation();

        clickButton("Cancel");
    }

    private String _specimen_McMichael;
    private String _specimen_KCMC;
    private final static String ATTACHMENT1 = "KCMC_Moshi_Ta_to_Aurum_Health_.tsv";
    private final static String ATTACHMENT2 = "KCMC_Moshi_Ta_to_Aurum_Health_KO_%s.xls"; // Params: date(yyyy-MM-dd)
    private final String NOTIFICATION_TEMPLATE = // Params: Study Name, requestId, Study Name, requestId, Username, Date(yyyy-MM-dd)
            "%s: Specimen Request Notification\n" +
            "\n" +
            "\n" +
            "Specimen request #%s was updated in %s.\n" +
            "\n" +
            "Request Details\n" +
            "Specimen Request %s\n" +
            "Destination " + DESTINATION_SITE+"\n" +
            "Status New Request\n" +
            "Modified by %s\n" +
            "Action Originating location notification of specimen shipment to "+DESTINATION_SITE+"\n" +
            "Attachments KCMC_Moshi_Ta_to_Aurum_Health_.tsv\n" +
            "KCMC_Moshi_Ta_to_Aurum_Health_KO_%s.xls\n" +
            "Assay Plan:\n" +
            "Assay Plan\n" +
            "\n" +
            "Shipping Information:\n" +
            "Shipping\n" +
            "\n" +
            "Comments:\n" +
            "Comments\n" +
            "\n" +
            "Last One:\n" +
            "sample last one input\n" +
            "Specimen List (Request Link)\n" +
            "\n" +
            "  Participant Id Global Unique Id Visit Description Sequence Num Visit Volume Volume Units Primary Type Derivative Type Additive Type Derivative Type2 Sub Additive Derivative Draw Timestamp Clinic Processing Location First Processed By Initials Sal Receipt Date Class Id Protocol Number Primary Volume Primary Volume Units Total Cell Count Tube Type Comments Locked In Request Requestable Site Name Site Ldms Code At Repository Available Availability Reason Quality Control Flag Quality Control Comments Collection Cohort Vial Count Locked In Request Count At Repository Count Available Count Expected Available Count\n" +
            "1 999320824 BAA07XNP-01 Vst 501.0 1.0 ML Blood (Whole) Plasma, Unknown Processing EDTA N/A 2005-12-23 10:05:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2005-12-23 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 2 1 2 0 1\n" +
            "2 999320087 CAA07XN8-01 Vst 301.0 1.0 ML Vaginal Swab Swab None N/A 2005-12-22 12:50:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2005-12-22 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0\n" +
            "3 999320706 DAA07YGW-01 Vst 301.0 1.0 ML Vaginal Swab Swab None N/A 2006-01-05 10:00:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-05 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0\n" +
            "4 999320898 FAA07XLJ-01 Vst 301.0 1.0 ML Vaginal Swab Swab None N/A 2005-12-20 12:05:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2005-12-20 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0\n" +
            "5 999320264 FAA07YSC-01 Vst 201.0 1.0 ML Vaginal Swab Swab None N/A 2006-01-13 12:10:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-13 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0\n" +
            "6 999320520 FAA07YXY-01 Vst 501.0 1.0 ML Vaginal Swab Swab None N/A 2005-12-15 10:30:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-15 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0\n" +
            "7 999320498 JAA07YJB-01 Vst 501.0 1.0 ML Vaginal Swab Swab None N/A 2006-01-05 09:30:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-11 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0\n" +
            "8 999320476 JAA07YSQ-01 Vst 301.0 1.0 ML Vaginal Swab Swab None N/A 2006-01-11 10:20:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-11 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0\n" +
            "9 999320980 KAA07YV1-01 Vst 301.0 1.0 ML Vaginal Swab Swab None N/A 2006-01-17 08:30:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-17 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0\n" +
            "10 999320520 KAA07YY0-01 Vst 501.0 1.0 ML Blood (Whole) Plasma, Unknown Processing EDTA N/A 2005-12-15 10:30:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-15 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 2 2 2 0 0\n" +
            "11 999320520 KAA07YY0-02 Vst 501.0 1.0 ML Blood (Whole) Plasma, Unknown Processing EDTA N/A 2005-12-15 10:30:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-15 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 2 2 2 0 0";
    @LogMethod
    private void verifyNotificationEmails()
    {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String notification = String.format(NOTIFICATION_TEMPLATE, getStudyLabel(), _requestId, getStudyLabel(), _requestId, PasswordUtil.getUsername(), date);

        log("Check notification emails");
        goToHome();
        goToModule("Dumbster");
        assertTextPresent("Specimen Request Notification", 8);
        assertTextPresent(USER1, 4);
        assertTextPresent(USER2, 4);

        log("Check for correct data in notification emails");
        if (getTableCellText("dataregion_EmailRecord", 2, 0).equals(USER1))
        {
            click(Locator.linkContainingText("Specimen Request Notification"));
            _shortWait.until(LabKeyExpectedConditions.emailIsExpanded(1));
            String bodyText = getText(Locator.id("dataregion_EmailRecord"));
            Assert.assertTrue(bodyText.contains(_specimen_McMichael));
            Assert.assertTrue(!bodyText.contains(_specimen_KCMC));
            click(Locator.linkContainingText("Specimen Request Notification").index(1));
            _shortWait.until(LabKeyExpectedConditions.emailIsExpanded(2));
            sleep(500); // Avoid WebDriver timeout
            bodyText = getText(Locator.id("dataregion_EmailRecord"));
            Assert.assertTrue(bodyText.contains(_specimen_KCMC));
            DataRegionTable mailTable = new DataRegionTable("EmailRecord", this, false, false);
            Assert.assertEquals("Notification was not as expected", notification, mailTable.getDataAsText(1, "Message"));
        }
        else
        {
            click(Locator.linkContainingText("Specimen Request Notification"));
            _shortWait.until(LabKeyExpectedConditions.emailIsExpanded(1));
            String bodyText = getText(Locator.id("dataregion_EmailRecord"));
            Assert.assertTrue(bodyText.contains(_specimen_KCMC));
            Assert.assertTrue(!bodyText.contains(_specimen_McMichael));
            click(Locator.linkContainingText("Specimen Request Notification").index(1));
            _shortWait.until(LabKeyExpectedConditions.emailIsExpanded(2));
            sleep(500); // Avoid WebDriver timeout
            bodyText = getText(Locator.id("dataregion_EmailRecord"));
            Assert.assertTrue(bodyText.contains(_specimen_McMichael));
            DataRegionTable mailTable = new DataRegionTable("EmailRecord", this, false, false);
            Assert.assertEquals("Notification was not as expected", notification, mailTable.getDataAsText(0, "Message"));
        }

        String attachment1 = getAttribute(Locator.linkWithText(ATTACHMENT1), "href");
        String attachment2 = getAttribute(Locator.linkWithText(String.format(ATTACHMENT2, date)), "href");

        try
        {
            Assert.assertEquals("Bad link to attachment: " + ATTACHMENT1, HttpStatus.SC_OK, WebTestHelper.getHttpGetResponse(attachment1));
            Assert.assertEquals("Bad link to attachment: " + String.format(ATTACHMENT2, date), HttpStatus.SC_OK, WebTestHelper.getHttpGetResponse(attachment2));
        }
        catch (HttpException e)
        {
            Assert.fail("Failed to get HTTP client: "+e.getMessage());
        }
        catch (IOException e)
        {
            Assert.fail("Failed to perform HTTP GET: "+e.getMessage());
        }

        clickAndWait(Locator.linkWithText("Request Link"));
        assertTextPresent("Specimen Request " + _requestId);
    }

    @LogMethod
    private void verifyRequestCancel()
    {
        clickFolder(getProjectName());
        clickFolder(getFolderName());

        waitAndClick(Locator.linkWithText("Specimen Requests"));
        click(Locator.linkWithText("View Current Requests"));

        waitForElement(Locator.id("dataregion_SpecimenRequest"));
        clickButton("Details");

        clickAndWait(Locator.linkWithText("Update Request"));
        selectOptionByText(Locator.name("status"), "Not Yet Submitted");
        clickButton("Save Changes and Send Notifications");
        clickButton("Cancel Request", 0);
        assertAlert("Canceling will permanently delete this pending request.  Continue?");
        waitForText("No data to show.");
        clickAndWait(Locator.linkWithText(getStudyLabel()));
        clickAndWait(Locator.linkWithText("Specimen Data"));
        click(Locator.linkWithText("Vials by Derivative Type"));
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Swab"), WAIT_FOR_PAGE);
        checkCheckbox(".toggle");
        clickMenuButton("Request Options", "Create New Request");
        clickButton("Cancel");
    }

    @LogMethod
    private void verifyReports()
    {
        log("check reports by participant group");
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("Specimen Data"));
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Blood (Whole)"), WAIT_FOR_PAGE);
        pushLocation();
        clickAndWait(Locator.linkWithText("Reports"));
        clickButton("View"); // Summary Report
        //Verify by vial count
        assertElementPresent(Locator.xpath("//a[number(text()) > 0]"), 36);
        selectOptionByText(Locator.name("participantGroupFilter"), "Category1");
        clickButton("Refresh");
        assertElementNotPresent(Locator.xpath("//a[number(text()) > 6]"));
        assertElementPresent(Locator.xpath("//a[number(text()) <= 6]"), 8);
        selectOptionByText(Locator.name("participantGroupFilter"), "All Groups");
        clickButton("Refresh");
        assertElementPresent(Locator.xpath("//a[number(text()) > 0]"), 36);
        //Verify by ptid list
        checkCheckbox("viewPtidList");
        uncheckCheckbox("viewVialCount");
        clickButton("Refresh");
        assertLinkPresentWithTextCount(PTIDS[0], 3);
        assertLinkPresentWithTextCount(PTIDS[1], 5);
        selectOptionByText(Locator.name("participantGroupFilter"), "Category1");
        clickButton("Refresh");
        assertLinkPresentWithTextCount(PTIDS[0], 3);
        assertLinkPresentWithTextCount(PTIDS[1], 5);
    }

    @LogMethod
    private void verifyRequestingLocationRestriction()
    {
        clickFolder(getProjectName());
        clickFolder(getFolderName());

        verifyRequestingLocationCounts(StudyLocationType.values()); // All locations should be enabled by default

        enableAndVerifyRequestingLocationTypes();
        enableAndVerifyRequestingLocationTypes(StudyLocationType.CLINIC);
        enableAndVerifyRequestingLocationTypes(StudyLocationType.ENDPOINT);
        enableAndVerifyRequestingLocationTypes(StudyLocationType.REPOSITORY);
        enableAndVerifyRequestingLocationTypes(StudyLocationType.SAL);
        enableAndVerifyRequestingLocationTypes(StudyLocationType.REPOSITORY, StudyLocationType.CLINIC);
        enableAndVerifyRequestingLocationTypes(StudyLocationType.values());
    }

    @LogMethod
    private void verifySpecimenTableAttachments()
    {
        clickFolder(getProjectName());
        clickFolder(getFolderName());

        log("Setup Excel specimen attachment");
        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Manage Notifications"));
        checkCheckbox(Locator.checkboxById("newRequestNotifyCheckbox"));
        setFormElement(Locator.id("newRequestNotify"), PasswordUtil.getUsername());
        checkRadioButton(Locator.radioButtonByNameAndValue("specimensAttachment", "ExcelAttachment"));
        clickButton("Save");

        log("Create request with excel specimen attachment");
        clickTab("Specimen Data");
        clickAndWait(Locator.linkWithText("Urine"));
        checkDataRegionCheckbox("SpecimenDetail", 0);
        _extHelper.clickMenuButton(true, "Request Options", "Create New Request");
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input1"), "Shipping");
        setFormElement(Locator.id("input3"), "Comments");
        clickButton("Create and View Details");
        clickButton("Submit Request", 0);
        getAlert();
        waitForElement(Locator.css("h3").withText("Your request has been successfully submitted."));

        log("Setup text specimen attachment");
        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Manage Notifications"));
        checkRadioButton(Locator.radioButtonByNameAndValue("specimensAttachment", "TextAttachment"));
        clickButton("Save");

        log("Create request with text specimen attachment");
        clickTab("Specimen Data");
        clickAndWait(Locator.linkWithText("Urine"));
        checkDataRegionCheckbox("SpecimenDetail", 1);
        _extHelper.clickMenuButton(true, "Request Options", "Create New Request");
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input1"), "Shipping");
        setFormElement(Locator.id("input3"), "Comments");
        clickButton("Create and View Details");
        clickButton("Submit Request", 0);
        getAlert();
        waitForElement(Locator.css("h3").withText("Your request has been successfully submitted."));

        log("Verify specimen list attachments");
        goToModule("Dumbster");

        click(Locator.linkContainingText("Specimen Request Notification"));
        waitForElement(Locator.linkWithText("SpecimenDetail.tsv"));
        click(Locator.linkContainingText("Specimen Request Notification").index(1));
        waitForElement(Locator.linkWithText("SpecimenDetail.xls"));

        // Each notification should be have only the specimen request details, no specimen list
        assertElementPresent(Locator.css("#email_body_1 > table"), 1);
        assertElementPresent(Locator.css("#email_body_2 > table"), 1);
    }

    @LogMethod
    private void verifyInactiveUsersInRequests()
    {
        enableEmailRecorder(); // clear email recorder
        goToSiteUsers();
        DataRegionTable usersTable = new DataRegionTable("Users", this, true, true);
        int row = usersTable.getRow("Email", USER2);
        usersTable.checkCheckbox(row);
        clickButton("Deactivate");
        clickButton("Deactivate");
        assertTextNotPresent(USER2);

        clickFolder(getProjectName());
        clickFolder(getFolderName());

        waitAndClick(Locator.linkWithText("Specimen Requests"));
        click(Locator.linkWithText("View Current Requests"));

        waitForElement(Locator.id("dataregion_SpecimenRequest"));
        clickButton("Details");

        waitAndClick(Locator.linkWithText("Update Request"));

        waitForElement(Locator.pageHeader("Update Request"));
        checkCheckbox(Locator.checkboxByName("notificationIdPairs"));
        click(Locator.css(".labkey-help-pop-up"));
        waitForElement(Locator.xpath("id('helpDivBody')").containing(USER1));

        checkCheckbox(Locator.checkboxByName("notificationIdPairs").index(1));
        click(Locator.css(".labkey-help-pop-up").index(1));
        waitForElement(Locator.xpath("id('helpDivBody')/del").withText(USER2));

        setFormElement(Locator.name("requestDescription"), "Just one notification.");
        pushLocation();
        clickButton("Save Changes and Send Notifications");

        popLocation();

        waitForElement(Locator.pageHeader("Update Request"));
        checkCheckbox(Locator.checkboxByName("notificationIdPairs"));
        checkCheckbox(Locator.checkboxByName("notificationIdPairs").index(1));
        checkCheckbox(Locator.checkboxByName("emailInactiveUsers"));
        setFormElement(Locator.name("requestDescription"), "Two notifications.");
        clickButton("Save Changes and Send Notifications");

        goToModule("Dumbster");
    }

    /**
     * Allow all provided location types to make requests, disallow all others
     * @param types List of location types to allow to be requesting locations
     */
    @LogMethod
    private void enableAndVerifyRequestingLocationTypes(@LoggedParam StudyLocationType... types)
    {
        clickTab("Manage");
        waitAndClick(Locator.linkWithText("Manage Location Types"));
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Manage Location Types"));

        for (StudyLocationType type : StudyLocationType.values())
        {
            if (Arrays.asList(types).contains(type))
                _ext4Helper.checkCheckbox(type.toString());
            else
                _ext4Helper.uncheckCheckbox(type.toString());
        }

        clickButton("Save", 0);
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Manage Study"));

        verifyRequestingLocationCounts(types);
    }

    /**
     * Verify that only permitted locations can submit specimen requests
     * Location count algorithm is valid only for locations defined in sample_a.specimens
     */
    private void verifyRequestingLocationCounts(StudyLocationType... types)
    {
        clickTab("Specimen Data");
        click(Locator.linkWithText("Specimen Requests"));
        click(Locator.linkWithText("Create New Request"));
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("New Specimen Request"));

        int expectedLocationCount = StudyLocationType.untypedSites();

        long additionalLocations = Math.round(Math.pow(2, StudyLocationType.values().length - 1));

        for (StudyLocationType type : StudyLocationType.values())
        {
            if (Arrays.asList(types).contains(type))
            {
                assertElementPresent(Locator.xpath("id('destinationLocation')/option").containing(type.toString()), type.siteCount());
                expectedLocationCount += additionalLocations;
                additionalLocations = additionalLocations / 2; // Each additional Location type adds less unique locations
            }
        }

        assertElementPresent(Locator.css("#destinationLocation option"), expectedLocationCount + 1); // +1 for blank select option

        clickButton("Cancel", 0);
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Specimen Requests"));
    }

    /**
     * Verify changing the specimen groupings that appear on the specimen web part
     */
    @LogMethod
    private void verifySpecimenGroupings()
    {
        clickFolder(getProjectName());
        clickFolder(getFolderName());
        clickTab("Manage");
        waitAndClick(Locator.linkWithText("Configure Specimen Groupings"));
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Configure Specimen Web Part"));
        _ext4Helper.selectComboBoxItemById("combo11", "Processing Location");
        _ext4Helper.selectComboBoxItemById("combo12", "Primary Type");
        _ext4Helper.selectComboBoxItemById("combo13", "Site Name");
        _ext4Helper.selectComboBoxItemById("combo21", "Additive Type");
        _ext4Helper.selectComboBoxItemById("combo22", "Derivative Type");
        _ext4Helper.selectComboBoxItemById("combo23", "Tube Type");
        clickButton("Save");
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Manage Study"));
        clickTab("Specimen Data");
        waitForElement(Locator.linkWithText("Vials by Processing Location"));
        assertTextPresent("Vials by Processing Location", "Vials by Additive Type", "The McMichael Lab");
        assertTextPresent("NICD - Joberg", 2);
        clickAndWait(Locator.linkContainingText("The McMichael Lab, Oxford"));
        assertTextPresent("Vials", "(ProcessingLocation = The McMichael Lab, Oxford, UK)");

        // Put groupings back for other tests
        clickTab("Manage");
        waitAndClick(Locator.linkWithText("Configure Specimen Groupings"));
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Configure Specimen Web Part"));
        _ext4Helper.selectComboBoxItemById("combo11", "Primary Type");
        _ext4Helper.selectComboBoxItemById("combo12", "Derivative Type");
        _ext4Helper.selectComboBoxItemById("combo13", "Additive Type");
        _ext4Helper.selectComboBoxItemById("combo21", "Derivative Type");
        _ext4Helper.selectComboBoxItemById("combo22", "Additive Type");

        clickButton("Save");
    }

    /**
     * Provides info about locations defined in sample_a.specimens
     */
    private enum StudyLocationType
    {
        REPOSITORY("Repository", 8),
        CLINIC("Clinic", 8),
        SAL("Site Affiliated Lab", 8),
        ENDPOINT("Endpoint Lab", 8);

        private String _type;
        private int _count;

        private StudyLocationType(String type, int count)
        {
            _type=type;
            _count=count;
        }

        public String toString()
        {
            return _type;
        }

        public int siteCount()
        {
            return _count;
        }

        public static int untypedSites()
        {
            return 9;
        }
    }

    @LogMethod
    private void searchTest()
    {
        goToProjectHome();
         clickAndWait(Locator.linkContainingText(getFolderName()));
        clickTab("Specimen Data");
        waitForVialSearch();
        Ext4FieldRefWD additiveType = Ext4FieldRefWD.getForLabel(this, "Additive Type");
        additiveType.setValue("Heparin");
        Ext4FieldRefWD.getForLabel(this, "Participant").setValue("999320812");
        clickButtonContainingText("Search");
        assertTextNotPresent("Serum Separator");
        assertTextPresent("(ParticipantId = 999320812) AND (AdditiveType = Heparin)");
        goBack();
        waitForVialSearch();
        additiveType.setValue(new String[] {"Heparin", "Ammounium Heparin"});
        clickButtonContainingText("Search");
        assertTextPresent("ONE OF");
        goBack();
        waitForVialSearch();
        additiveType.setValue(new String[] {"Ammonium Heparin","Cell Preparation Tube Heparin","Cell Preparation Tube SCI","Citrate Phosphate Dextrose","EDTA","Fetal Fibronectin Buffer","Guanidine Isothiocyanate (GITC)","Heparin","Liquid Potassium EDTA","Liquid Sodium EDTA","Lithium Heparin","Lithium Heparin and Gel for Plasma","None","Normal Saline","Optimum Cutting Temperature Medium","Orasure Collection Container","Other","PAXgene Blood RNA tube","Phosphate Buffered Saline","Plasma Preparation Tube","PLP Fixative","Port-a-cul Transport Tube","Potassium EDTA","RNA Later","Serum Separator","Sodium Citrate","Sodium EDTA","Sodium Fluoride","Sodium Fluoride/Potassium Oxalate","Sodium Heparin","Sodium Polyanetholesulfonate","Spray Dried Potassium EDTA","Spray Dried Sodium EDTA","Thrombin","Tissue Freezing Medium","Unknown Additive","Viral Transport Media"});
        clickButtonContainingText("Search");
        assertTextPresent("IS NOT ANY OF ");
    }

    private void waitForVialSearch()
    {
        waitForElement(Locator.css(".specimenSearchLoaded"));
    }

    @LogMethod
    private void exportSpecimenTest()
    {
        popLocation();
        addUrlParameter("&exportType=excelWebQuery");
        assertTextPresent("org.labkey.study.query.SpecimenRequestDisplayColumn");
        goBack();


        goToAuditLog();
        selectOptionByText(Locator.name("view"), "Query events");
        waitForElement(Locator.id("dataregion_audit"));

        DataRegionTable auditTable =  new DataRegionTable("audit", this);
        String[][] columnAndValues = new String[][] {{"Created By", getDisplayName()},
                {"Project", PROJECT_NAME}, {"Container", getFolderName()}, {"SchemaName", "study"},
                {"QueryName", SPECIMEN_DETAIL}, {"Comment", "Exported to Excel Web Query data"}};
        for(String[] columnAndValue : columnAndValues)
        {
            log("Checking column: "+ columnAndValue[0]);
            Assert.assertEquals(columnAndValue[1], auditTable.getDataAsText(0, columnAndValue[0]));
        }
    }
}
