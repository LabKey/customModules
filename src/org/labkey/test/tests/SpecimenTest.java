/*
 * Copyright (c) 2007-2012 LabKey Corporation
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

import org.junit.Assert;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.labkey.test.Locator;
import org.labkey.test.WebTestHelper;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.ExtHelper;
import org.labkey.test.util.PasswordUtil;
import org.labkey.test.util.ext4cmp.Ext4FieldRef;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * User: brittp
 * Date: Mar 9, 2006
 * Time: 1:54:57 PM
 */
public class SpecimenTest extends StudyBaseTest
{
    protected static final String PROJECT_NAME = "SpecimenVerifyProject";
    public static final String SPECIMEN_DETAIL = "SpecimenDetail";
    private static final String DESTINATION_SITE = "Aurum Health KOSH Lab, Orkney, South Africa (Repository)";
    private static final String USER1 = "user1@specimen.test";
    private static final String USER2 = "user2@specimen.test";
    private static final String REQUESTABILITY_QUERY = "RequestabilityRule";
    private static final String UNREQUESTABLE_SAMPLE = "BAA07XNP-02";
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
    protected void doVerifySteps()
    {
        verifyActorDetails();
        createRequest();
        verifyViews();
        verifyNotificationEmails();
        verifyRequestCancel();
        verifyReports();
        exportSpecimenTest();
        searchTest();
    }

    private void setupRequestabilityRules()
    {
        // Create custom query to test requestability rules.
        goToSchemaBrowser();
        selectQuery("study", SPECIMEN_DETAIL);
        clickButton("Create New Query");
        setFormElement("ff_newQueryName", REQUESTABILITY_QUERY);
        clickLinkWithText("Create and Edit Source");        
        setQueryEditorValue("queryText",
                "SELECT \n" +
                SPECIMEN_DETAIL + ".GlobalUniqueId AS GlobalUniqueId\n" +
                "FROM " + SPECIMEN_DETAIL + "\n" +
                "WHERE " + SPECIMEN_DETAIL + ".GlobalUniqueId='" + UNREQUESTABLE_SAMPLE + "'");
        clickButton("Save", 0);
        waitForText("Saved", WAIT_FOR_JAVASCRIPT);

        clickLinkWithText(getStudyLabel());
        clickLinkWithText("Manage Study");
        clickLinkWithText("Manage Requestability Rules");
        // Verify that LOCKED_IN_REQUEST is the last rule
        assertElementPresent(Locator.xpath("//div[contains(@class, 'x-grid3-row-last')]//div[text()='Locked In Request Check']"));
        mouseDown(Locator.xpath("//div[contains(@class, 'x-grid3-row-last')]//div[text()='Locked In Request Check']"));
        // Verify that LOCKED_IN_REQUEST rule cannot be moved or deleted
        assertElementPresent(Locator.xpath("//table[@id='btn_deleteEngine' and contains(@class, 'x-item-disabled')]"));
        assertElementPresent(Locator.xpath("//table[@id='btn_moveUp' and contains(@class, 'x-item-disabled')]"));
        assertElementPresent(Locator.xpath("//table[@id='btn_moveDown' and contains(@class, 'x-item-disabled')]"));
        mouseDown(Locator.xpath("//div[contains(@class, 'x-grid3-col-numberer') and text()='2']"));
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

    private void checkTubeType()
    {
        // Field check for Tube Type column (including conflict)
        clickLinkWithText(getStudyLabel());
        addWebPart("Specimens");
        clickLinkWithText("By Individual Vial");
        setFilter(SPECIMEN_DETAIL, "PrimaryType", "Is Blank");
        // Verify that there's only one vial of unknown type:
        assertLinkPresentWithTextCount("[history]", 1);
        // There's a conflict in TubeType for this vial's events; verify that no TubeType is populated at the vial level
        assertTextNotPresent("Cryovial");
        clickLinkWithText("[history]");
        // This vial has three events, each of which list a different tube type:
        assertTextPresent("15ml Cryovial");
        assertTextPresent("20ml Cryovial");
        assertTextPresent("25ml Cryovial");
        clickLinkWithText("Specimen Overview");
        clickLinkWithText("Vials by Derivative", false);
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Tear Flo Strips"), WAIT_FOR_PAGE);
        // For these three vials, there should be no conflict in TubeType, so we should see the text once for each of three vials:
        assertLinkPresentWithTextCount("[history]", 3);
        assertTextPresent("15ml Cryovial", 3);
    }

    private void setupSpecimenManagement()
    {
        clickLinkWithText(getStudyLabel());
        clickLinkWithText("Manage Study");
        clickLinkWithText("Manage Request Statuses");
        setFormElement("newLabel", "New Request");
        clickButton("Save");
        setFormElement("newLabel", "Processing");
        clickButton("Save");
        setFormElement("newLabel", "Completed");
        checkCheckbox("newFinalState");
        clickButton("Save");
        setFormElement("newLabel", "Rejected");
        checkCheckbox("newFinalState");
        uncheckCheckbox("newSpecimensLocked");
        clickButton("Done");
    }

    private void setupActorsAndGroups()
    {
        clickLinkWithText("Manage Actors and Groups");
        setFormElement("newLabel", "SLG");
        selectOptionByText("newPerSite", "One Per Study");
        clickButton("Save");
        clickLinkWithText("Update Members");
        setText("names", USER1);
        uncheckCheckbox("sendEmail");
        clickButton("Update Members");
        setFormElement("newLabel", "IRB");
        selectOptionByText("newPerSite", "Multiple Per Study (Location Affiliated)");
        clickButton("Save");
        clickLinkWithText("Update Members", 1);
        clickLinkWithText(DESTINATION_SITE);
        setText("names", USER2);
        uncheckCheckbox("sendEmail");
        clickLinkWithText("Update Members");
        clickLinkWithText(getStudyLabel());
    }

    private void setupDefaultRequirements()
    {
        clickLinkWithText("Manage Study");
        clickLinkWithText("Manage Default Requirements");
        selectOptionByText("originatorActor", "IRB");
        setFormElement("originatorDescription", "Originating IRB Approval");
        clickButton("Add Requirement");
        selectOptionByText("providerActor", "IRB");
        setFormElement("providerDescription", "Providing IRB Approval");
        clickLink(Locator.xpath("//input[@name='providerDescription']/../.." + Locator.navButton("Add Requirement").getPath()));
        selectOptionByText("receiverActor", "IRB");
        setFormElement("receiverDescription", "Receiving IRB Approval");
        clickLink(Locator.xpath("//input[@name='receiverDescription']/../.." + Locator.navButton("Add Requirement").getPath()));
        selectOptionByText("generalActor", "SLG");
        setFormElement("generalDescription", "SLG Approval");
        clickLink(Locator.xpath("//input[@name='generalDescription']/../.." + Locator.navButton("Add Requirement").getPath()));
        clickTab("Manage");
    }

    private void setupRequestForm()
    {
        clickLinkWithText("Manage New Request Form");
        clickButton("Add New Input", 0);
        setFormElement("//descendant::input[@name='title'][4]", "Last One");
        setFormElement("//descendant::input[@name='helpText'][4]", "A test input");
        click(Locator.xpath("//descendant::input[@name='required'][4]"));
        clickButton("Save");
        clickLinkWithText(getStudyLabel());
    }

    private void setupActorNotification()
    {
        log("Check Configure Defaults for Actor Notification");
        clickLinkWithText(getStudyLabel());
        clickLinkWithText("Manage");
        clickLinkWithText("Manage Notifications");
        assertTextPresent("Default Email Recipients");
        checkRadioButton("defaultEmailNotify", "All");
        clickButton("Save");
    }

    private void uploadSpecimensFromFile()
    {
        log("Check Upload Specimen List from file");
        clickLinkWithText(getStudyLabel());
        clickLinkWithText("Specimen Data");
        clickLinkWithText("Create New Request");
        selectOptionByText("destinationSite", "Aurum Health KOSH Lab, Orkney, South Africa (Repository)");
        setFormElement("input0", "Assay Plan");
        setFormElement("input2", "Comments");
        setFormElement("input1", "Shipping");
        setFormElement("input3", "Last one");
        clickButton("Create and View Details");
        clickLinkWithText("Upload Specimen Ids");
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-01");     // add specimen
        clickButton("Submit");    // Submit button

        clickLinkWithText("Upload Specimen Ids");
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-01");     // try to add again
        clickButton("Submit", 0);    // Submit button
        waitForText("Specimen AAA07XK5-01 not available", 20000);
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-02");     // try to add one that doesn't exist
        clickButton("Submit", 0);    // Submit button
        waitForText("Specimen AAA07XK5-02 not available", 20000);
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-04\nAAA07XK5-06\nAAA07XSF-03");     // add different one
        clickButton("Submit");    // Submit button
    }

    private void verifyActorDetails()
    {
        // Check each Actor's Details for "Default Actor Notification" feature;
        // In Details, for each actor the ether Notify checkbox should be set or disabled, because we set Notifications to All
        List<Locator> detailsLinks = findAllMatches(Locator.xpath("//td[a='Details']/a"));
        for (Locator link : detailsLinks)
        {
            clickLink(link);
            List<Locator> allCheckBoxes = findAllMatches(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs']"));
            List<Locator> checkedCheckBoxes = findAllMatches(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs' and @checked]"));
            List<Locator> disabledCheckBoxes = findAllMatches(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs' and @disabled]"));
            Assert.assertTrue("Actor Notification: All actors should be notified if addresses configured.", allCheckBoxes.size() == checkedCheckBoxes.size() + disabledCheckBoxes.size());
            clickButton("Cancel");
        }

        assertTextPresent("Associated Specimens");
        assertTextPresent("AAA07XK5-01", "AAA07XK5-04", "AAA07XK5-06", "AAA07XSF-03");


        clickButton("Cancel Request");
        Assert.assertTrue(getConfirmationAndWait().matches("^Canceling will permanently delete this pending request\\.  Continue[\\s\\S]$"));
    }

    private void createRequest()
    {
        clickLinkWithText(getStudyLabel());
        clickLinkWithText("Specimen Data");
        clickLinkWithText("Vials by Derivative", false);
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Plasma, Unknown Processing"), WAIT_FOR_PAGE);
        // Verify unavailable sample
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "' and @disabled]"));
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "']/../../td[contains(text(), 'This vial is unavailable because it was found in the set called \"" + REQUESTABILITY_QUERY + "\".')]"));
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "']/../a[contains(@onmouseover, 'This vial is unavailable because it was found in the set called \\\"" + REQUESTABILITY_QUERY + "\\\".')]"));
        checkCheckbox(".toggle");

        clickMenuButton("Page Size", "Show All");
        clickLinkContainingText("history");
        assertTextPresent("Vial History");
        goBack();

        clickMenuButton("Request Options", "Create New Request");
        selectOptionByText("destinationSite", "Aurum Health KOSH Lab, Orkney, South Africa (Repository)");
        setFormElement("input0", "Assay Plan");
        setFormElement("input2", "Comments");
        setFormElement("input1", "Shipping");
        clickButton("Create and View Details");
        assertTextPresent("Please provide all required input.");
        setFormElement("input3", "sample last one input");
        clickButton("Create and View Details");
        assertTextPresent("sample last one input");
        assertTextPresent("IRB");
        assertTextPresent("KCMC, Moshi, Tanzania");
        assertTextPresent("Originating IRB Approval");
        assertTextPresent("Contract Lab Services, Johannesburg, South Africa (Repository)");
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
        clickLinkWithText(getStudyLabel());

        // add additional specimens
        clickLinkWithText("Specimen Data");
        clickLinkWithText("Vials by Derivative", false);
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
        assertTextPresent("Contract Lab Services, Johannesburg, South Africa (Repository)");
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
        Assert.assertTrue(getConfirmationAndWait().matches("^Once a request is submitted, its specimen list may no longer be modified\\.  Continue[\\s\\S]$"));
        assertTextNotPresent("Not Yet Submitted");
        assertTextPresent("New Request");

        // modify request
        selectOptionByText("newActor", "SLG");
        setFormElement("newDescription", "Other SLG Approval");
        clickButton("Add Requirement");
        clickLinkWithText("Details");
        checkCheckbox("complete");
        checkCheckbox("notificationIdPairs");
        checkCheckbox("notificationIdPairs", 1);
        clickButton("Save Changes and Send Notifications");
        assertTextPresent("Complete");
    }

    private void verifyViews()
    {
        clickLinkWithText("View History");
        assertTextPresent("Request submitted for processing.");
        assertTextPresent("Notification Sent", 2);
        assertTextPresent(USER1);
        assertTextPresent(USER2);
        clickLinkWithText("View Request");
        clickLinkWithText("Originating Location Specimen Lists");
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
        clickLinkWithText("Providing Location Specimen Lists");
        assertTextPresent("Contract Lab Services, Johannesburg, South Africa (Repository)");
        clickButton("Cancel");
    }

    private String _specimen_McMichael;
    private String _specimen_KCMC;
    private final static String ATTACHMENT1 = "KCMC_Moshi_Ta_to_Aurum_Health_.tsv";
    private final static String ATTACHMENT2 = "KCMC_Moshi_Ta_to_Aurum_Health_KO_%s.xls"; // Params: date(yyyy-MM-dd)
    private final String NOTIFICATION_TEMPLATE = // Params: Study Name, requestId, Study Name, requestId, Username, Date(yyyy-MM-dd)
            "%s: Specimen Request Notification \n" +
            " \n" +
            " Specimen request #%s was updated in %s. \n" +
            " \n" +
            " \n" +
            " Request Details Specimen Request %s Destination Aurum Health KOSH Lab, Orkney, South Africa (Repository) Status New Request Modified by %s Action Originating location notification of specimen shipment to Aurum Health KOSH Lab, Orkney, South Africa (Repository) Attachments KCMC_Moshi_Ta_to_Aurum_Health_.tsv \n" +
            " KCMC_Moshi_Ta_to_Aurum_Health_KO_%s.xls \n" +
            " Assay Plan:\n" +
            " Assay Plan\n" +
            " \n" +
            " Shipping Information:\n" +
            " Shipping\n" +
            " \n" +
            " Comments:\n" +
            " Comments\n" +
            " \n" +
            " Last One:\n" +
            " sample last one input \n" +
            " Specimen List (Request Link)\n" +
            "\n" +
            " \n" +
            "   Participant Id Global Unique Id Visit Description Visit Volume Volume Units Primary Type Derivative Type Additive Type Derivative Type2 Sub Additive Derivative Draw Timestamp Clinic Processing Location First Processed By Initials Sal Receipt Date Class Id Protocol Number Primary Volume Primary Volume Units Total Cell Count Tube Type Comments Locked In Request Requestable Site Name Site Ldms Code At Repository Available Availability Reason Quality Control Flag Quality Control Comments Collection Cohort Vial Count Locked In Request Count At Repository Count Available Count Expected Available Count 1 999320824 BAA07XNP-01 Vst 1.0 ML Blood (Whole) Plasma, Unknown Processing EDTA N/A 2005-12-23 10:05:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2005-12-23 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 2 1 2 0 1 2 999320087 CAA07XN8-01 Vst 1.0 ML Vaginal Swab Swab None N/A 2005-12-22 12:50:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2005-12-22 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0 3 999320706 DAA07YGW-01 Vst 1.0 ML Vaginal Swab Swab None N/A 2006-01-05 10:00:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-05 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0 4 999320898 FAA07XLJ-01 Vst 1.0 ML Vaginal Swab Swab None N/A 2005-12-20 12:05:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2005-12-20 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0 5 999320264 FAA07YSC-01 Vst 1.0 ML Vaginal Swab Swab None N/A 2006-01-13 12:10:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-13 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0 6 999320520 FAA07YXY-01 Vst 1.0 ML Vaginal Swab Swab None N/A 2005-12-15 10:30:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-15 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0 7 999320498 JAA07YJB-01 Vst 1.0 ML Vaginal Swab Swab None N/A 2006-01-05 09:30:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-11 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0 8 999320476 JAA07YSQ-01 Vst 1.0 ML Vaginal Swab Swab None N/A 2006-01-11 10:20:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-11 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0 9 999320980 KAA07YV1-01 Vst 1.0 ML Vaginal Swab Swab None N/A 2006-01-17 08:30:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-17 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 1 1 1 0 0 10 999320520 KAA07YY0-01 Vst 1.0 ML Blood (Whole) Plasma, Unknown Processing EDTA N/A 2005-12-15 10:30:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-15 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 2 2 2 0 0 11 999320520 KAA07YY0-02 Vst 1.0 ML Blood (Whole) Plasma, Unknown Processing EDTA N/A 2005-12-15 10:30:00.0 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-15 00:00:00.0 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false 2 2 2 0 0";
    private void verifyNotificationEmails()
    {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String notification = String.format(NOTIFICATION_TEMPLATE, getStudyLabel(), _requestId, getStudyLabel(), _requestId, PasswordUtil.getUsername(), date);

        log("Check notification emails");
        goToModule("Dumbster");
        assertTextPresent("Specimen Request Notification", 8);
        assertTextPresent(USER1, 4);
        assertTextPresent(USER2, 4);

        log("Check for correct data in notification emails");
        if (getTableCellText("dataregion_EmailRecord", 2, 0).equals(USER1))
        {
            clickLinkContainingText("Specimen Request Notification", false);
            assertTextPresent(_specimen_McMichael);
            assertTextNotPresent(_specimen_KCMC);
            clickLinkContainingText("Specimen Request Notification", 1, false);
            assertTextPresent(_specimen_KCMC);
            DataRegionTable mailTable = new DataRegionTable("EmailRecord", this, false, false);
            Assert.assertEquals("Notification was not as expected", notification, mailTable.getDataAsText(1, "Message"));
        }
        else
        {
            clickLinkContainingText("Specimen Request Notification", false);
            assertTextPresent(_specimen_KCMC);
            assertTextNotPresent(_specimen_McMichael);
            clickLinkContainingText("Specimen Request Notification", 1, false);
            assertTextPresent(_specimen_McMichael);
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

        clickLinkWithText("Request Link");
        assertTextPresent("Specimen Request " + _requestId);
    }

    private void verifyRequestCancel()
    {
        clickLinkWithText("Update Request");
        selectOptionByText("status", "Not Yet Submitted");
        clickButton("Save Changes and Send Notifications");
        clickButton("Cancel Request", 0);
        Assert.assertTrue(getConfirmationAndWait().matches("^Canceling will permanently delete this pending request\\.  Continue[\\s\\S]$"));
        assertTextPresent("No data to show.");
        clickLinkWithText(getStudyLabel());
        clickLinkWithText("Specimen Data");
        clickLinkWithText("Vials by Derivative", false);
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Swab"), WAIT_FOR_PAGE);
        checkCheckbox(".toggle");
        clickMenuButton("Request Options", "Create New Request");
        clickButton("Cancel");
    }

    private void verifyReports()
    {
        log("check reports by participant group");
        clickLinkWithText(getFolderName());
        clickLinkWithText("Specimen Data");
        clickLinkWithText("Vials by Primary Type", false);
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Blood (Whole)"), WAIT_FOR_PAGE);
        pushLocation();
        clickLinkWithText("Reports");
        clickButton("View"); // Summary Report
        //Verify by vial count
        assertElementPresent(Locator.xpath("//a[number(text()) > 0]"), 36);
        selectOptionByText("participantGroupFilter", "Category1");
        clickButton("Refresh");
        assertElementNotPresent(Locator.xpath("//a[number(text()) > 6]"));
        assertElementPresent(Locator.xpath("//a[number(text()) <= 6]"), 8);
        selectOptionByText("participantGroupFilter", "All Groups");
        clickButton("Refresh");
        assertElementPresent(Locator.xpath("//a[number(text()) > 0]"), 36);
        //Verify by ptid list
        checkCheckbox("viewPtidList");
        uncheckCheckbox("viewVialCount");
        clickButton("Refresh");
        assertLinkPresentWithTextCount(PTIDS[0], 3);
        assertLinkPresentWithTextCount(PTIDS[1], 5);
        selectOptionByText("participantGroupFilter", "Category1");
        clickButton("Refresh");
        assertLinkPresentWithTextCount(PTIDS[0], 3);
        assertLinkPresentWithTextCount(PTIDS[1], 5);
    }

    private void searchTest()
    {
        goToProjectHome();
        clickLinkContainingText(getFolderName());
        clickTab("Specimen Data");
        waitForPageToLoad();
        waitForVialSearch();
        Ext4FieldRef additiveType = Ext4FieldRef.getForLabel(this, "Additive Type");
        additiveType.setValue("Heparin");
        Ext4FieldRef.getForLabel(this, "Participant").setValue("999320812");
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

    private void exportSpecimenTest()
    {
        popLocation();
        addUrlParameter("&exportType=excelWebQuery");
        assertTextPresent("org.labkey.study.query.SpecimenRequestDisplayColumn");
        goBack();


        goToAuditLog();
        selectOptionByText("view", "Query events");
        waitForPageToLoad();

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
