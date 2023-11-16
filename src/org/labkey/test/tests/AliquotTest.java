/*
 * Copyright (c) 2013-2019 LabKey Corporation
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
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.Locators;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.Daily;
import org.labkey.test.categories.Specimen;
import org.labkey.test.components.html.BootstrapMenu;
import org.labkey.test.pages.ImportDataPage;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.StudyHelper;

import java.io.File;

@Category({Daily.class, Specimen.class})
@BaseWebDriverTest.ClassTimeout(minutes = 8)
public class AliquotTest extends SpecimenBaseTest
{
    protected static final String PROJECT_NAME = "AliquotVerifyProject";
    protected static final File SPECIMEN_ARCHIVE_148 = StudyHelper.getSpecimenArchiveFile("lab148.specimens");

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        _userHelper.deleteUsers(false, USER1, USER2);
        super.doCleanup(afterTest);
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    @LogMethod
    protected void doCreateSteps()
    {
        enableEmailRecorder();
        initializeFolder();

        clickButton("Create Study");
        setFormElement(Locator.name("label"), getStudyLabel());
        clickButton("Create Study");
        _studyHelper.setupAdvancedRepositoryType();
        setupRequestabilityRules();

        setPipelineRoot(StudyHelper.getStudySubfolderPath());
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
    @LogMethod
    protected void doVerifySteps()
    {
        createRequests();
        verifyEditableSpecimens();
    }

    @Override
    @LogMethod
    protected void setupRequestabilityRules()
    {
        super.setupRequestabilityRules();

        clickButton("Add Rule", 0);
        click(Locator.menuItem("Locked While Processing Check"));

        // Remove "Locked In Request" rule
        Locator lockedInRequestRow = Locator.xpath("//tr[contains(@class, 'x4-grid-row')]//div[text()='Locked In Request Check']");
        waitForElement(lockedInRequestRow);
        click(lockedInRequestRow);
        clickButton("Remove Rule", 0);

        clickButton("Save");
    }

    public static final String ALIQUOT_ONE = "AAA07XK5-03";
    public static final String ALIQUOT_TWO = "EBG002K4-25";
    public static final String ALIQUOT_THREE = "AAA07XK5-01";
    public static final String ALIQUOT_FOUR = "AAA07XK5-04";
    public static final String ALIQUOT_ONE_CHECKBOX = "//input[@id='check_" + ALIQUOT_ONE + "']";
    public static final String ALIQUOT_ONE_SPECIMEN_DETAIL_CHECKBOX = "//td[contains(text(), '" + ALIQUOT_ONE + "')]/../td/input[@type='checkbox' and @title='Select/unselect row']";
    public static final String ALIQUOT_THREE_CHECKBOX = "//input[@id='check_" + ALIQUOT_THREE + "']";
    public static final String ALIQUOT_THREE_SPECIMEN_DETAIL_CHECKBOX = "//td[contains(text(), '" + ALIQUOT_THREE + "')]/../td/input[@type='checkbox' and @title='Select/unselect row']";
    public static final String ALIQUOT_FOUR_CHECKBOX = "//input[@id='check_" + ALIQUOT_FOUR + "']";
    public static final String ALIQUOT_ONE_CHECKBOX_DISABLED = "//input[@id='check_" + ALIQUOT_ONE + "' and @disabled]";
    public static final String UNAVAILABLE_ALIQUOT = "AAQ00032-02";
    public static final String ALIQUOT_TWO_CHECKBOX = "//input[@id='check_" + ALIQUOT_TWO + "']";
    public static final String UNAVAILABLE_ALIQUOT_DISABLED = "//input[@id='check_" + UNAVAILABLE_ALIQUOT + "' and @disabled]";

    @LogMethod
    private void createRequests()
    {
        goToSpecimenData();
        clickAndWait(Locator.linkWithText("Blood (Whole)"));

        assertElementPresent(Locator.xpath(UNAVAILABLE_ALIQUOT_DISABLED));
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX));
        checkCheckbox(Locator.xpath(ALIQUOT_ONE_CHECKBOX));

        createNewRequestFromQueryView();
        assertTextPresent(ALIQUOT_ONE);
        assertTextNotPresent("Complete");

        // Check that aliquot we added is not available
        goToSpecimenData();
        clickAndWait(Locator.linkWithText("Blood (Whole)"));
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX_DISABLED));
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX + "/../../td[contains(text(), 'This vial is unavailable because it is being processed')]"));

        // Now submit that request
        viewExistingRequests();
        clickButton("Submit", 0);
        assertAlertIgnoreCaseAndSpaces("Once a request is submitted, its specimen list may no longer be modified.  Continue?");
        waitForElement(Locator.tag("h3").withText("Your request has been successfully submitted."));

        clickAndWait(Locator.linkWithText("Update Request"));
        selectOptionByText(Locator.name("status"), "Completed");
        clickButton("Save Changes and Send Notifications");

        // Now verify that that aliquot is available again
        goToSpecimenData();
        clickAndWait(Locator.linkWithText("Blood (Whole)"));

        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX));
        checkCheckbox(Locator.xpath(ALIQUOT_ONE_CHECKBOX));

        createNewRequestFromQueryView();
        assertTextPresent(ALIQUOT_ONE);
        assertTextNotPresent("Complete");

        clickAndWait(Locator.linkWithText("Upload Specimen Ids"));
        new ImportDataPage(getDriver())
                .setText(ALIQUOT_TWO)
                .submit();
        assertTextPresent(ALIQUOT_ONE, ALIQUOT_TWO);
        checkCheckbox(Locator.checkboxByTitle("Select/unselect row"));      // all individual item checkboxes have same name/title; should be first one
        clickButton("Remove Selected");
        assertTextNotPresent(ALIQUOT_ONE);
        assertTextPresent(ALIQUOT_TWO);
        clickAndWait(Locator.linkWithText("Upload Specimen Ids"));
        new ImportDataPage(getDriver())
                .setText(ALIQUOT_ONE)
                .submit();
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
        goToSpecimenData();
        waitAndClickAndWait(Locator.linkWithText("Blood (Whole)").notHidden());

        // Check that ALIQUOT_ONE cannot be selected for Delete
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX_DISABLED));

        // There's a request from createRequest(); Submit it and make it completed to free up aliqouts
        viewExistingRequests();
        doAndWaitForPageToLoad(() -> {
            clickButton("Submit", 0);
            assertAlertIgnoreCaseAndSpaces("Once a request is submitted, its specimen list may no longer be modified.  Continue?");
        });
        waitForElement(Locator.css("h3").withText("Your request has been successfully submitted."));
        clickAndWait(Locator.linkWithText("Update Request"));
        selectOptionByText(Locator.name("status"), "Completed");
        clickButton("Save Changes and Send Notifications");

        // Now try to delete ALIQUOT_ONE
        DataRegionTable dataRegion = navigateToQuery("study", "SpecimenDetail").getDataRegion();
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_SPECIMEN_DETAIL_CHECKBOX));
        dataRegion.doAndWaitForUpdate(() -> checkCheckbox(Locator.xpath(ALIQUOT_ONE_SPECIMEN_DETAIL_CHECKBOX)));
        doAndWaitForPageToLoad(() -> {
            dataRegion.clickHeaderButton("Delete");
            assertAlertIgnoreCaseAndSpaces("Are you sure you want to delete the selected row?");
        });
        waitForElement(Locators.labkeyError.withText("Specimen may not be deleted because it has been used in a request."));
        clickButton("Back");

        // Now delete a different aliquot
        dataRegion.doAndWaitForUpdate(() -> checkCheckbox(Locator.xpath(ALIQUOT_THREE_SPECIMEN_DETAIL_CHECKBOX)));
        dataRegion.deleteSelectedRows();
        waitForElementToDisappear(Locator.xpath(ALIQUOT_THREE_SPECIMEN_DETAIL_CHECKBOX));
        clickFolder(getFolderName());
    }

    @LogMethod
    private void verifyEditingSpecimens()
    {
        clickFolder(getFolderName());
        goToSpecimenData();
        waitAndClickAndWait(Locator.linkWithText("Blood (Whole)").notHidden());

        // Create request with ALIQUOT_FOUR
        checkCheckbox(Locator.xpath(ALIQUOT_FOUR_CHECKBOX));
        createNewRequestFromQueryView();
        assertTextPresent(ALIQUOT_FOUR);

        // Attempt to edit, which should be error
        goToSpecimenData();
        waitAndClickAndWait(Locator.linkWithText("Blood (Whole)").notHidden());
        DataRegionTable table = new DataRegionTable("SpecimenDetail", getDriver());
        clickAndWait(table.updateLink(table.getRowIndex("GlobalUniqueId", ALIQUOT_FOUR)));
        assertTextNotPresent("Specimen may not be edited when it's in a non-final request.");
        clickButton("Submit");
        waitForText("Specimen may not be edited when it's in a non-final request.");
        clickButton("Cancel");

        // Edit another specimen
        clickAndWait(table.updateLink(table.getRowIndex("GlobalUniqueId", ALIQUOT_ONE)));
        assertTextNotPresent("Specimen may not be edited when it's in a non-final request.");
        setFormElement(Locator.xpath("//input[@name='quf_VisitDescription']"), "VisitVisit");
        clickButton("Submit");
        waitForText("VisitVisit");
    }

    @LogMethod
    private void verifyInsertingSpecimens()
    {
        clickFolder(getFolderName());
        goToSpecimenData();
        waitAndClickAndWait(Locator.linkWithText("Blood (Whole)").notHidden());

        // verify insert new here
        DataRegionTable detail = new DataRegionTable("SpecimenDetail", this);
        detail.clickInsertNewRow();
        setFormElement(Locator.xpath("//input[@name='quf_GlobalUniqueId']"), "Global");
        setFormElement(Locator.xpath("//input[@name='quf_VisitDescription']"), "NewVisit");
        setFormElement(Locator.xpath("//input[@name='quf_SequenceNum']"), "001");
        selectOptionByText(Locator.name("quf_ParticipantId"), "618005775");
        clickButton("Submit");
        assertElementNotPresent(Locator.tagWithClass("*", "labkey-error").withText());
        detail.setFilter("VisitDescription", "Equals", "NewVisit");
        assertTextPresent("NewVisit");
    }

    @LogMethod
    private void verifyIllegalImporting()
    {
        checkErrors();
        navigateToFolder("AliquotVerifyProject", getFolderName());
        startSpecimenImport(2, SPECIMEN_ARCHIVE_148);
        setExpectSpecimenImportError(true);
        waitForSpecimenImport();

        // Check there was an error in the specimen merge.
        clickAndWait(Locator.linkWithText("ERROR"));
        assertTextPresent("With an editable specimen repository, importing may not reference any existing specimen. " +
                "8 imported specimen events refer to existing specimens.");

        // Make sure the expected errors have been logged and will not hang up the test later on.
        checkExpectedErrors(1);
    }

    private void createNewRequestFromQueryView()
    {
        DataRegionTable specimenTable = new DataRegionTable("SpecimenDetail", this);
        specimenTable.clickHeaderMenu("Request Options", "Create New Request");
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input2"), "Comments");
        setFormElement(Locator.id("input1"), "Shipping");
        setFormElement(Locator.id("input3"), "sample last one input");
        clickButton("Create and View Details");
        assertTextPresent(DESTINATION_SITE);
    }

    private void viewExistingRequests()
    {
        new BootstrapMenu(getDriver(), Locator.tagWithClass("div", "lk-menu-drop")
                .withDescendant(Locator.tag("span").withText("Request Options")).findElement(getDriver())
        ).clickSubMenu(true, "View Existing Requests");
    }
}
