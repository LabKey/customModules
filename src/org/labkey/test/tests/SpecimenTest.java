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

import org.labkey.test.BaseSeleniumWebTest;
import org.labkey.test.Locator;
import org.labkey.test.util.ComponentQuery;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.Ext4Helper;
import org.labkey.test.util.ExtHelper;
import org.labkey.test.util.PasswordUtil;
import org.labkey.test.util.ext4cmp.Ext4CmpRef;
import org.labkey.test.util.ext4cmp.Ext4FieldRef;

import java.io.File;
import java.util.List;

/**
 * User: brittp
 * Date: Mar 9, 2006
 * Time: 1:54:57 PM
 */
public class SpecimenTest extends BaseSeleniumWebTest
{
    protected static final String PROJECT_NAME = "SpecimenVerifyProject";
    protected static final String FOLDER_NAME = "My Study";
    private static final String SPECIMEN_ARCHIVE = "/sampledata/study/specimens/sample_a.specimens";
    private static final String SPECIMEN_TEMP_DIR = "/sampledata/study/drt_temp";
    public static final String SPECIMEN_DETAIL = "SpecimenDetail";
    private String _studyDataRoot = null;
    private static final String STUDY_NAME = "My Study Study";
    private static final String DESTINATION_SITE = "Aurum Health KOSH Lab, Orkney, South Africa (Repository)";
    private static final String USER1 = "user1@specimen.test";
    private static final String USER2 = "user2@specimen.test";
    private static final String REQUESTABILITY_QUERY = "RequestabilityRule";
    private static final String UNREQUESTABLE_SAMPLE = "BAA07XNP-02";
    private static final String[] PTIDS = {"999320396","999320812"};


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

    protected void doCleanup() throws Exception
    {
        _studyDataRoot = getLabKeyRoot() + "/sampledata/study";
        File tempDir = new File(getLabKeyRoot() + SPECIMEN_TEMP_DIR);
        if (tempDir.exists())
        {
            for (File file : tempDir.listFiles())
                file.delete();
            tempDir.delete();
        }
        try { deleteProject(PROJECT_NAME); } catch (Throwable e) {}
    }


    protected void doTestSteps()
    {
        _studyDataRoot = getLabKeyRoot() + "/sampledata/study";

        _containerHelper.createProject(PROJECT_NAME, null);

        enableEmailRecorder();

        createSubfolder(PROJECT_NAME, PROJECT_NAME, FOLDER_NAME, "Study", null);
        clickNavButton("Create Study");
        click(Locator.radioButtonByNameAndValue("simpleRepository", "false"));
        clickNavButton("Create Study");

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

        clickLinkWithText(STUDY_NAME);
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
        ExtHelper.selectComboBoxItem(this, Locator.xpath("//div[@id='x-form-el-userQuery_schema']"), "study" );
        ExtHelper.selectComboBoxItem(this, Locator.xpath("//div[@id='x-form-el-userQuery_query']"), REQUESTABILITY_QUERY );
        ExtHelper.selectComboBoxItem(this, Locator.xpath("//div[@id='x-form-el-userQuery_action']"), "Unavailable" );
        clickButton("Submit",0);
        clickButton("Save");
        

        clickLinkWithText("My Study");

        setPipelineRoot(_studyDataRoot);
        clickLinkWithText("My Study");
        clickLinkWithText("Manage Files");

        SpecimenImporter importer = new SpecimenImporter(new File(_studyDataRoot), new File(getLabKeyRoot(), SPECIMEN_ARCHIVE), new File(getLabKeyRoot(), SPECIMEN_TEMP_DIR), FOLDER_NAME, 1);
        importer.importAndWaitForComplete();

        clickLinkWithText("My Study");
        clickLinkWithText("Manage Study");
        clickLinkWithText("Manage Participant Groups");
        log("Set up participant groups");
        clickNavButton("Create", 0);
        ExtHelper.waitForExtDialog(this, "Define Participant Group");
        setFormElement("groupLabel", "Category1");
        setFormElement("categoryIdentifiers", PTIDS[0] + "," + PTIDS[1]);
        ExtHelper.clickExtButton(this, "Define Participant Group", "Save", 0);

        // Field check for Tube Type column (including conflict)
        clickLinkWithText(STUDY_NAME);
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

        // specimen management setup
        clickLinkWithText(STUDY_NAME);
        clickLinkWithText("Manage Study");
        clickLinkWithText("Manage Request Statuses");
        setFormElement("newLabel", "New Request");
        clickNavButton("Save");
        setFormElement("newLabel", "Processing");
        clickNavButton("Save");
        setFormElement("newLabel", "Completed");
        checkCheckbox("newFinalState");
        clickNavButton("Save");
        setFormElement("newLabel", "Rejected");
        checkCheckbox("newFinalState");
        uncheckCheckbox("newSpecimensLocked");
        clickNavButton("Done");

        clickLinkWithText("Manage Actors and Groups");
        setFormElement("newLabel", "SLG");
        selectOptionByText("newPerSite", "One Per Study");
        clickNavButton("Save");
        clickLinkWithText("Update Members");
        setText("names", USER1);
        uncheckCheckbox("sendEmail");
        clickNavButton("Update Members");
        setFormElement("newLabel", "IRB");
        selectOptionByText("newPerSite", "Multiple Per Study (Location Affiliated)");
        clickNavButton("Save");
        clickLinkWithText("Update Members", 1);
        clickLinkWithText(DESTINATION_SITE);
        setText("names", USER2);
        uncheckCheckbox("sendEmail");
        clickLinkWithText("Update Members");
        clickLinkWithText(STUDY_NAME);

        clickLinkWithText("Manage Study");
        clickLinkWithText("Manage Default Requirements");
        selectOptionByText("originatorActor", "IRB");
        setFormElement("originatorDescription", "Originating IRB Approval");
        clickNavButton("Add Requirement");
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
        
        clickLinkWithText("Manage New Request Form");
        clickNavButton("Add New Input", 0);
        setFormElement("//descendant::input[@name='title'][4]", "Last One");
        setFormElement("//descendant::input[@name='helpText'][4]", "A test input");
        click(Locator.xpath("//descendant::input[@name='required'][4]"));
        clickNavButton("Save");
        clickLinkWithText(STUDY_NAME);

        // Test "Configure Defaults for Actor Notification"
        log("Check Configure Defaults for Actor Notification");
        clickLinkWithText(STUDY_NAME);
        clickLinkWithText("Manage");
        clickLinkWithText("Manage Notifications");
        assertTextPresent("Default Email Recipients");
        checkRadioButton("defaultEmailNotify", "All");
        clickNavButton("Save");

        // Test Upload Specimen List from file/paste
        log("Check Upload Specimen List from file");
        clickLinkWithText(STUDY_NAME);
        clickLinkWithText("Specimen Data");
        clickLinkWithText("Create New Request");
        selectOptionByText("destinationSite", "Aurum Health KOSH Lab, Orkney, South Africa (Repository)");
        setFormElement("input0", "Assay Plan");
        setFormElement("input2", "Comments");
        setFormElement("input1", "Shipping");
        setFormElement("input3", "Last one");
        clickNavButton("Create and View Details");
        clickLinkWithText("Upload Specimen Ids");
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-01");     // add specimen
        clickNavButton("Submit");    // Submit button

        clickLinkWithText("Upload Specimen Ids");
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-01");     // try to add again
        clickNavButton("Submit", 0);    // Submit button
        waitForText("Specimen AAA07XK5-01 not available", 20000);
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-02");     // try to add one that doesn't exist
        clickNavButton("Submit", 0);    // Submit button
        waitForText("Specimen AAA07XK5-02 not available", 20000);
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-04\nAAA07XK5-06\nAAA07XSF-03");     // add different one
        clickNavButton("Submit");    // Submit button

        // Check each Actor's Details for "Default Actor Notification" feature;
        // In Details, for each actor the ether Notify checkbox should be set or disabled, because we set Notifications to All
        List<Locator> detailsLinks = findAllMatches(Locator.xpath("//td[a='Details']/a"));
        for (Locator link : detailsLinks)
        {
            clickLink(link);
            List<Locator> allCheckBoxes = findAllMatches(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs']"));
            List<Locator> checkedCheckBoxes = findAllMatches(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs' and @checked]"));
            List<Locator> disabledCheckBoxes = findAllMatches(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs' and @disabled]"));
            assertTrue("Actor Notification: All actors should be notified if addresses configured.", allCheckBoxes.size() == checkedCheckBoxes.size() + disabledCheckBoxes.size());
            clickNavButton("Cancel");
        }

        assertTextPresent("Associated Specimens");
        assertTextPresent("AAA07XK5-01", "AAA07XK5-04", "AAA07XK5-06", "AAA07XSF-03");


        clickNavButton("Cancel Request");
        assertTrue(getConfirmationAndWait().matches("^Canceling will permanently delete this pending request\\.  Continue[\\s\\S]$"));

        // create request
        clickLinkWithText(STUDY_NAME);
        clickLinkWithText("Specimen Data");
        clickLinkWithText("Vials by Derivative", false);
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Plasma, Unknown Processing"), WAIT_FOR_PAGE);
        // Verify unavailable sample
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "' and @disabled]"));
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "']/../../td[contains(text(), 'This vial is unavailable because it was found in the set called \"" + REQUESTABILITY_QUERY + "\".')]"));
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "']/../a[contains(@onmouseover, 'This vial is unavailable because it was found in the set called \\\"" + REQUESTABILITY_QUERY + "\\\".')]"));
        checkCheckbox(".toggle");
        clickMenuButton("Request Options", "Create New Request");
        selectOptionByText("destinationSite", "Aurum Health KOSH Lab, Orkney, South Africa (Repository)");
        setFormElement("input0", "Assay Plan");
        setFormElement("input2", "Comments");
        setFormElement("input1", "Shipping");
        clickNavButton("Create and View Details");
        assertTextPresent("Please provide all required input.");
        setFormElement("input3", "sample last one input");
        clickNavButton("Create and View Details");
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
        clickLinkWithText(STUDY_NAME);

        // add additional specimens
        clickLinkWithText("Specimen Data");
        clickLinkWithText("Vials by Derivative", false);
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Swab"), WAIT_FOR_PAGE);
        checkCheckbox(".toggle");
        clickMenuButtonAndContinue("Request Options", "Add To Existing Request");
        ExtHelper.waitForExtDialog(this, "Request Vial", WAIT_FOR_JAVASCRIPT);
        sleep(10000); //TODO: Determine which specific element to wait for.
        clickNavButton("Add 8 Vials to Request", 0);
        ExtHelper.waitForExtDialog(this, "Success", WAIT_FOR_JAVASCRIPT * 5);
        clickNavButton("OK", 0);
        clickMenuButton("Request Options", "View Existing Requests");
        clickNavButton("Details");
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
        clickNavButton("Submit Request", 0);
        assertTrue(getConfirmationAndWait().matches("^Once a request is submitted, its specimen list may no longer be modified\\.  Continue[\\s\\S]$"));
        assertTextNotPresent("Not Yet Submitted");
        assertTextPresent("New Request");

        // modify request
        selectOptionByText("newActor", "SLG");
        setFormElement("newDescription", "Other SLG Approval");
        clickNavButton("Add Requirement");
        clickLinkWithText("Details");
        checkCheckbox("complete");
        checkCheckbox("notificationIdPairs");
        checkCheckbox("notificationIdPairs", 1);
        clickNavButton("Save Changes and Send Notifications");
        assertTextPresent("Complete");

        // verify views
        clickLinkWithText("View History");
        assertTextPresent("Request submitted for processing.");
        assertTextPresent("Notification Sent", 2);
        assertTextPresent(USER1);
        assertTextPresent(USER2);
        clickLinkWithText("View Request");
        clickLinkWithText("Originating Location Specimen Lists");
        assertTextPresent("KCMC, Moshi, Tanzania");
        checkCheckbox("notify");
        String specimen1 = getText(Locator.xpath("//tr[@class = 'labkey-alternate-row']/td[3]//td"));
        checkCheckbox("notify", 4);
        String specimen2 = getText(Locator.xpath("//tr[@class = 'labkey-row']/td[3]//td"));
        checkCheckbox("sendXls");
        checkCheckbox("sendTsv");
        clickNavButton("Send Email");
        clickLinkWithText("Providing Location Specimen Lists");
        assertTextPresent("Contract Lab Services, Johannesburg, South Africa (Repository)");
        clickNavButton("Cancel");
        // cancel request
        clickLinkWithText("Update Request");
        selectOptionByText("status", "Not Yet Submitted");
        clickNavButton("Save Changes and Send Notifications");
        clickNavButton("Cancel Request", 0);
        assertTrue(getConfirmationAndWait().matches("^Canceling will permanently delete this pending request\\.  Continue[\\s\\S]$"));
        assertTextPresent("No data to show.");
        clickLinkWithText(STUDY_NAME);
        clickLinkWithText("Specimen Data");
        clickLinkWithText("Vials by Derivative", false);
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Swab"), WAIT_FOR_PAGE);
        checkCheckbox(".toggle");
        clickMenuButton("Request Options", "Create New Request");
        clickNavButton("Cancel");

        log("check reports by participant group");
        clickLinkWithText("My Study");
        clickLinkWithText("Specimen Data");
        clickLinkWithText("Vials by Primary Type", false);
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.linkWithText("Blood (Whole)"), WAIT_FOR_PAGE);
        pushLocation();
        clickLinkWithText("Reports");
        clickNavButton("View"); // Summary Report
        //Verify by vial count
        assertElementPresent(Locator.xpath("//a[number(text()) > 0]"), 36);
        selectOptionByText("participantGroupFilter", "Category1");
        clickNavButton("Refresh");
        assertElementNotPresent(Locator.xpath("//a[number(text()) > 6]"));
        assertElementPresent(Locator.xpath("//a[number(text()) <= 6]"), 8);
        selectOptionByText("participantGroupFilter", "All Groups");
        clickNavButton("Refresh");
        assertElementPresent(Locator.xpath("//a[number(text()) > 0]"), 36);
        //Verify by ptid list
        checkCheckbox("viewPtidList");
        uncheckCheckbox("viewVialCount");
        clickNavButton("Refresh");
        assertLinkPresentWithTextCount(PTIDS[0], 3);
        assertLinkPresentWithTextCount(PTIDS[1], 5);
        selectOptionByText("participantGroupFilter", "Category1");
        clickNavButton("Refresh");
        assertLinkPresentWithTextCount(PTIDS[0], 3);
        assertLinkPresentWithTextCount(PTIDS[1], 5);

        log("Check notification emails");
        goToModule("Dumbster");
        assertTextPresent("Specimen Request Notification", 8);
        assertTextPresent(USER1, 4);
        assertTextPresent(USER2, 4);

        log("Check for correct data in notification emails");
        if ( getTableCellText("dataregion_EmailRecord", 2, 0).equals(USER1))
        {
            clickLinkContainingText("Specimen Request Notification", false);
            assertTextPresent(specimen1);
            assertTextNotPresent(specimen2);
            clickLinkContainingText("Specimen Request Notification", 1, false);
            assertTextPresent(specimen2);
        }
        else
        {
            clickLinkContainingText("Specimen Request Notification", false);
            assertTextPresent(specimen2);
            assertTextNotPresent(specimen1);
            clickLinkContainingText("Specimen Request Notification", 1, false);
            assertTextPresent(specimen1);
        }

        exportSpecimenTest();
        searchTest();
    }

    private void searchTest()
    {
        goToProjectHome();
        clickLinkContainingText(FOLDER_NAME);
        clickTab("Specimen Data");
        waitForPageToLoad();
        waitForExt4();
        Ext4FieldRef additiveType = Ext4FieldRef.getForLabel(this, "Additive Type");
        additiveType.setValue("Heparin");
        Ext4FieldRef.getForLabel(this, "Participant").setValue("999320812");
        clickButtonContainingText("Search");
        assertTextNotPresent("Serum Separator");
        assertTextPresent("(ParticipantId = 999320812) AND (AdditiveType = Heparin)");
        goBack();
        waitForExt4();
        additiveType.setValue(new String[] {"Heparin", "Ammounium Heparin"});
        clickButtonContainingText("Search");
        assertTextPresent("ONE OF");
        goBack();
        waitForExt4();
        additiveType.setValue(new String[] {"Ammonium Heparin","Cell Preparation Tube Heparin","Cell Preparation Tube SCI","Citrate Phosphate Dextrose","EDTA","Fetal Fibronectin Buffer","Guanidine Isothiocyanate (GITC)","Heparin","Liquid Potassium EDTA","Liquid Sodium EDTA","Lithium Heparin","Lithium Heparin and Gel for Plasma","None","Normal Saline","Optimum Cutting Temperature Medium","Orasure Collection Container","Other","PAXgene Blood RNA tube","Phosphate Buffered Saline","Plasma Preparation Tube","PLP Fixative","Port-a-cul Transport Tube","Potassium EDTA","RNA Later","Serum Separator","Sodium Citrate","Sodium EDTA","Sodium Fluoride","Sodium Fluoride/Potassium Oxalate","Sodium Heparin","Sodium Polyanetholesulfonate","Spray Dried Potassium EDTA","Spray Dried Sodium EDTA","Thrombin","Tissue Freezing Medium","Unknown Additive","Viral Transport Media"});
        clickButtonContainingText("Search");
        assertTextPresent("IS NOT ANY OF ");
    }

    private void waitForExt4()
    {
        sleep(2000); //TODO:  this needs something better
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
                {"Project", PROJECT_NAME}, {"Container", FOLDER_NAME}, {"SchemaName", "study"},
                {"QueryName", SPECIMEN_DETAIL}, {"Comment", "Exported to Excel Web Query data"}};
        for(String[] columnAndValue : columnAndValues)
        {
            log("Checking column: "+ columnAndValue[0]);
            assertEquals(columnAndValue[1], auditTable.getDataAsText(0, columnAndValue[0]));
        }
        //To change body of created methods use File | Settings | File Templates.
    }
}
