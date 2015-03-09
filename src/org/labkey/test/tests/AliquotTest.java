/*
 * Copyright (c) 2013-2015 LabKey Corporation
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

import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.DailyA;
import org.labkey.test.categories.Specimen;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;

@Category({DailyA.class, Specimen.class})
public class AliquotTest extends SpecimenBaseTest
{
    protected static final String PROJECT_NAME = "AliquotVerifyProject";
    protected static final String SPECIMEN_ARCHIVE_148 = getStudySampleDataPath() + "specimens/lab148.specimens";

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        deleteUsersIfPresent(USER1, USER2);
        super.doCleanup(afterTest);
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
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
        setupRequestStatuses();
        setupActorsAndGroups();
        setupDefaultRequirements();
        setupRequestForm();
        setupActorNotification();
    }

    @Override
    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    protected void doVerifySteps()
    {
        createRequests();
        verifyEditableSpecimens();
    }

    @Override
    @LogMethod
    protected void setupRequestabilityRules()
    {
        // Create custom query to test requestability rules.
        goToSchemaBrowser();
        selectQuery("study", SPECIMEN_DETAIL);
        createNewQuery("study");
        setFormElement(Locator.name("ff_newQueryName"), REQUESTABILITY_QUERY);
        clickAndWait(Locator.linkWithText("Create and Edit Source"));
        setCodeEditorValue("queryText",
                "SELECT \n" +
                        SPECIMEN_DETAIL + ".GlobalUniqueId AS GlobalUniqueId\n" +
                        "FROM " + SPECIMEN_DETAIL + "\n" +
                        "WHERE " + SPECIMEN_DETAIL + ".GlobalUniqueId='" + UNREQUESTABLE_SAMPLE + "'");
        clickButton("Save", 0);
        waitForText(WAIT_FOR_JAVASCRIPT, "Saved");

        clickFolder(getFolderName());
        waitAndClick(Locator.linkWithText("Manage Study"));
        waitAndClick(Locator.linkWithText("Manage Requestability Rules"));
        waitForElement(Locator.xpath("//div[contains(@class, 'x-grid3-row')]//div[text()='Locked In Request Check']"));

        clickButton("Add Rule", 0);
        click(Locator.menuItem("Custom Query"));
        _extHelper.selectComboBoxItem(Locator.xpath("//div[@id='x-form-el-userQuery_schema']"), "study" );
        _extHelper.selectComboBoxItem(Locator.xpath("//div[@id='x-form-el-userQuery_query']"), REQUESTABILITY_QUERY );
        _extHelper.selectComboBoxItem(Locator.xpath("//div[@id='x-form-el-userQuery_action']"), "Unavailable" );
        clickButton("Submit", 0);

        clickButton("Add Rule", 0);
        click(Locator.menuItem("Locked While Processing Check"));

        // Remove Locked In Request
        waitForElement(Locator.xpath("//div[contains(@class, 'x-grid3-row')]//div[text()='Locked In Request Check']"));
        click(Locator.xpath("//div[contains(@class, 'x-grid3-row')]//div[text()='Locked In Request Check']"));
        clickButton("Remove Rule", 0);

        clickButton("Save");
    }

    public static final String ALIQUOT_ONE = "AAA07XK5-03";
    public static final String ALIQUOT_TWO = "EBG002K4-25";
    public static final String ALIQUOT_THREE = "AAA07XK5-01";
    public static final String ALIQUOT_FOUR = "AAA07XK5-04";
    public static final String ALIQUOT_ONE_CHECKBOX = "//input[@id='check_" + ALIQUOT_ONE + "']";
    public static final String ALIQUOT_ONE_SPECIMEN_DETAIL_CHECKBOX = "//td[contains(text(), '" + ALIQUOT_ONE + "')]/../td/input[@type='checkbox' and @title='Select/unselect row']";
    public static final String ALIQUOT_ONE_EDITLINK = "//input[@id='check_" + ALIQUOT_ONE + "']/../../td/a[contains(text(), 'edit')]";
    public static final String ALIQUOT_THREE_CHECKBOX = "//input[@id='check_" + ALIQUOT_THREE + "']";
    public static final String ALIQUOT_THREE_SPECIMEN_DETAIL_CHECKBOX = "//td[contains(text(), '" + ALIQUOT_THREE + "')]/../td/input[@type='checkbox' and @title='Select/unselect row']";
    public static final String ALIQUOT_FOUR_CHECKBOX = "//input[@id='check_" + ALIQUOT_FOUR + "']";
    public static final String ALIQUOT_FOUR_EDITLINK = "//input[@id='check_" + ALIQUOT_FOUR + "']/../../td/a[contains(text(), 'edit')]";
    public static final String ALIQUOT_ONE_CHECKBOX_DISABLED = "//input[@id='check_" + ALIQUOT_ONE + "' and @disabled]";
    public static final String UNAVAILABLE_ALIQUOT = "AAQ00032-02";
    public static final String ALIQUOT_TWO_CHECKBOX = "//input[@id='check_" + ALIQUOT_TWO + "']";
    public static final String UNAVAILABLE_ALIQUOT_DISABLED = "//input[@id='check_" + UNAVAILABLE_ALIQUOT + "' and @disabled]";

    @LogMethod
    private void createRequests()
    {
        clickTab("Specimen Data");
        waitForVialSearch();
        clickAndWait(Locator.linkWithText("Blood (Whole)"));

        assertElementPresent(Locator.xpath(UNAVAILABLE_ALIQUOT_DISABLED));
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX));
        checkCheckbox(Locator.xpath(ALIQUOT_ONE_CHECKBOX));

        createNewRequestFromQueryView();
        assertTextPresent(ALIQUOT_ONE);
        assertTextNotPresent("Complete");

        // Check that aliquot we added is not available
        clickTab("Specimen Data");
        waitForVialSearch();
        clickAndWait(Locator.linkWithText("Blood (Whole)"));
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX_DISABLED));
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX + "/../../td[contains(text(), 'This vial is unavailable because it is being processed')]"));

        // Now submit that request
        _extHelper.clickMenuButton("Request Options", "View Existing Requests");
        clickButton("Submit", 0);
        assertAlert("Once a request is submitted, its specimen list may no longer be modified.  Continue?");
        waitForElement(Locator.css("h3").withText("Your request has been successfully submitted."));

        clickAndWait(Locator.linkWithText("Update Request"));
        selectOptionByText(Locator.name("status"), "Completed");
        clickButton("Save Changes and Send Notifications");

        // Now verify that that aliquot is available again
        clickTab("Specimen Data");
        waitForVialSearch();
        clickAndWait(Locator.linkWithText("Blood (Whole)"));

        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX));
        checkCheckbox(Locator.xpath(ALIQUOT_ONE_CHECKBOX));

        createNewRequestFromQueryView();
        assertTextPresent(ALIQUOT_ONE);
        assertTextNotPresent("Complete");

        clickAndWait(Locator.linkWithText("Upload Specimen Ids"));
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), ALIQUOT_TWO);     // add specimen
        clickButton("Submit");    // Submit button
        assertTextPresent(ALIQUOT_ONE, ALIQUOT_TWO);
        checkCheckbox(Locator.checkboxByTitle("Select/unselect row"));      // all individual item checkboxes have same name/title; should be first one
        clickButton("Remove Selected");
        assertTextNotPresent(ALIQUOT_ONE);
        assertTextPresent(ALIQUOT_TWO);
        clickAndWait(Locator.linkWithText("Upload Specimen Ids"));
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), ALIQUOT_ONE);     // add specimen
        clickButton("Submit");    // Submit button
        assertTextPresent(ALIQUOT_ONE, ALIQUOT_TWO);
    }

    @LogMethod
    private void verifyEditableSpecimens()
    {
        // Change repository to editable
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Change Repository Type"));
        Locator enableEditableSpecimens = Locator.radioButtonByNameAndValue("specimenDataEditable", "true");
        waitForElement(enableEditableSpecimens);
        checkRadioButton(enableEditableSpecimens);
        clickButton("Submit");

        verifyDeletingSpecimens();
        verifyEditingSpecimens();
        verifyInsertingSpecimens();

        verifyIllegalImporting();
    }

    @LogMethod
    private void verifyDeletingSpecimens()
    {
        clickAndWait(Locator.linkWithText("Specimen Data"));
        waitAndClickAndWait(Locator.linkWithText("Blood (Whole)").notHidden());

        // Check that ALIQUOT_ONE cannot be selected for Delete
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX_DISABLED));

        // There's a request from createRequest(); Submit it and make it completed to free up aliqouts
        _extHelper.clickMenuButton("Request Options", "View Existing Requests");
        clickButton("Submit", 0);
        assertAlert("Once a request is submitted, its specimen list may no longer be modified.  Continue?");
        waitForElement(Locator.css("h3").withText("Your request has been successfully submitted."));
        clickAndWait(Locator.linkWithText("Update Request"));
        selectOptionByText(Locator.name("status"), "Completed");
        clickButton("Save Changes and Send Notifications");

        // Now try to delete ALIQUOT_ONE
        navigateToQuery("study", "SpecimenDetail");
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_SPECIMEN_DETAIL_CHECKBOX));
        checkCheckbox(Locator.xpath(ALIQUOT_ONE_SPECIMEN_DETAIL_CHECKBOX));
        click(Locator.linkWithText("Delete"));
        assertAlert("Are you sure you want to delete the selected row?");
        waitForText("Specimen may not be deleted because it has been used in a request.");
        clickButton("Back");

        // Now delete a different aliquot
        checkCheckbox(Locator.xpath(ALIQUOT_THREE_SPECIMEN_DETAIL_CHECKBOX));
        click(Locator.linkWithText("Delete"));
        assertAlert("Are you sure you want to delete the selected row?");
        waitForElementToDisappear(Locator.xpath(ALIQUOT_THREE_SPECIMEN_DETAIL_CHECKBOX));
        clickFolder(getFolderName());
    }

    @LogMethod
    private void verifyEditingSpecimens()
    {
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("Specimen Data"));
        waitAndClickAndWait(Locator.linkWithText("Blood (Whole)").notHidden());

        // Create request with ALIQUOT_FOUR
        checkCheckbox(Locator.xpath(ALIQUOT_FOUR_CHECKBOX));
        createNewRequestFromQueryView();
        assertTextPresent(ALIQUOT_FOUR);

        // Attempt to edit, which should be error
        clickAndWait(Locator.linkWithText("Specimen Data"));
        waitAndClickAndWait(Locator.linkWithText("Blood (Whole)").notHidden());
        clickAndWait(Locator.xpath(ALIQUOT_FOUR_EDITLINK));
        assertTextNotPresent("Specimen may not be edited when it's in a non-final request.");
        clickButton("Submit");
        waitForText("Specimen may not be edited when it's in a non-final request.");
        clickButton("Cancel");

        // Edit another specimen
        waitAndClickAndWait(Locator.xpath(ALIQUOT_ONE_EDITLINK));
        assertTextNotPresent("Specimen may not be edited when it's in a non-final request.");
        setFormElement(Locator.xpath("//input[@name='quf_VisitDescription']"), "VisitVisit");
        clickButton("Submit");
        waitForText("VisitVisit");
    }

    @LogMethod
    private void verifyInsertingSpecimens()
    {
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("Specimen Data"));
        waitAndClickAndWait(Locator.linkWithText("Blood (Whole)").notHidden());

        // verify insert new here
        clickAndWait(Locator.linkWithText("Insert New"));
        setFormElement(Locator.xpath("//input[@name='quf_GlobalUniqueId']"), "Global");
        setFormElement(Locator.xpath("//input[@name='quf_VisitDescription']"), "NewVisit");
        setFormElement(Locator.xpath("//input[@name='quf_SequenceNum']"), "001");
        selectOptionByText(Locator.name("quf_ParticipantId"), "618005775");
        clickButton("Submit");
        assertElementNotPresent(Locator.tagWithClass("*", "labkey-error").withText());
        setFilter("SpecimenDetail", "VisitDescription", "Equals", "NewVisit");
        assertTextPresent("NewVisit");
    }

    @LogMethod
    private void verifyIllegalImporting()
    {
        checkErrors();
        clickProject("AliquotVerifyProject");

        clickFolder(getFolderName());
        startSpecimenImport(2, SPECIMEN_ARCHIVE_148);
        setExpectSpecimenImportError(true);
        waitForSpecimenImport();

        // Make sure the expected errors have been logged and will not hang up the test later on.
        checkExpectedErrors(1);
    }

    private void createNewRequestFromQueryView()
    {
        DataRegionTable specimenTable = new DataRegionTable("SpecimenDetail", this);
        specimenTable.clickHeaderButton("Request Options", "Create New Request");
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input2"), "Comments");
        setFormElement(Locator.id("input1"), "Shipping");
        setFormElement(Locator.id("input3"), "sample last one input");
        clickButton("Create and View Details");
        assertTextPresent(DESTINATION_SITE);
    }
}
