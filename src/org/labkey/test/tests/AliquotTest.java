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

import org.labkey.test.Locator;
import org.labkey.test.util.LogMethod;

/**
 * User: davebradlee
 * Date: 5/14/13
 * Time: 1:44 PM
 */
public class AliquotTest extends SpecimenBaseTest
{
    protected static final String PROJECT_NAME = "AliquotVerifyProject";

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
        setupSpecimenManagement();
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
    }

    @LogMethod
    protected void setupRequestabilityRules()
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
    public static final String ALIQUOT_ONE_CHECKBOX = "//input[@id='check_" + ALIQUOT_ONE + "']";
    public static final String ALIQUOT_ONE_CHECKBOX_DISABLED = "//input[@id='check_" + ALIQUOT_ONE + "' and @disabled]";
    public static final String UNAVAILABLE_ALIQUOT = "AAQ00032-02";
    public static final String ALIQUOT_TWO_CHECKBOX = "//input[@id='check_" + ALIQUOT_TWO + "']";
    public static final String UNAVAILABLE_ALIQUOT_DISABLED = "//input[@id='check_" + UNAVAILABLE_ALIQUOT + "' and @disabled]";

    @LogMethod
    private void createRequests()
    {
        clickFolder(getStudyLabel());
        clickAndWait(Locator.linkWithText("Specimen Data"));
        clickAndWait(Locator.linkWithText("Blood (Whole)"));

        assertElementPresent(Locator.xpath(UNAVAILABLE_ALIQUOT_DISABLED));
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX));
        checkCheckbox(Locator.xpath(ALIQUOT_ONE_CHECKBOX));

        _extHelper.clickMenuButton("Request Options", "Create New Request");
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input2"), "Comments");
        setFormElement(Locator.id("input1"), "Shipping");
        setFormElement(Locator.id("input3"), "sample last one input");
        clickButton("Create and View Details");
        assertTextPresent(DESTINATION_SITE);
        assertTextPresent(ALIQUOT_ONE);
        assertTextNotPresent("Complete");

        // Check that aliquot we added is not available
        clickFolder(getStudyLabel());
        clickAndWait(Locator.linkWithText("Specimen Data"));
        clickAndWait(Locator.linkWithText("Blood (Whole)"));
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX_DISABLED));
        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX + "/../../td[contains(text(), 'This vial is unavailable because it is being processed')]"));

        // Now submit that request
        _extHelper.clickMenuButton("Request Options", "View Existing Requests");
        clickButton("Submit", 0);
        assertAlert("Once a request is submitted, its specimen list may no longer be modified.  Continue?");
        assertTextPresent("Your request has been successfully submitted");

        clickAndWait(Locator.linkWithText("Update Request"));
        selectOptionByText(Locator.name("status"), "Completed");
        clickButton("Save Changes and Send Notifications");

        // Now verify that that aliquot is available again
        clickFolder(getStudyLabel());
        clickAndWait(Locator.linkWithText("Specimen Data"));
        clickAndWait(Locator.linkWithText("Blood (Whole)"));

        assertElementPresent(Locator.xpath(ALIQUOT_ONE_CHECKBOX));
        checkCheckbox(Locator.xpath(ALIQUOT_ONE_CHECKBOX));

        _extHelper.clickMenuButton("Request Options", "Create New Request");
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input2"), "Comments");
        setFormElement(Locator.id("input1"), "Shipping");
        setFormElement(Locator.id("input3"), "sample last one input");
        clickButton("Create and View Details");
        assertTextPresent(DESTINATION_SITE);
        assertTextPresent(ALIQUOT_ONE);
        assertTextNotPresent("Complete");

        clickAndWait(Locator.linkWithText("Upload Specimen Ids"));
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), ALIQUOT_TWO);     // add specimen
        clickButton("Submit");    // Submit button
        assertTextPresent(ALIQUOT_ONE);
        assertTextPresent(ALIQUOT_TWO);
        checkCheckbox(Locator.checkboxByTitle("Select/unselect row"));      // all individual item checkboxes have same name/title; should be first one
        clickButton("Remove Selected");
        assertTextNotPresent(ALIQUOT_ONE);
        assertTextPresent(ALIQUOT_TWO);
        clickAndWait(Locator.linkWithText("Upload Specimen Ids"));
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), ALIQUOT_ONE);     // add specimen
        clickButton("Submit");    // Submit button
        assertTextPresent(ALIQUOT_ONE);
        assertTextPresent(ALIQUOT_TWO);
    }
}
