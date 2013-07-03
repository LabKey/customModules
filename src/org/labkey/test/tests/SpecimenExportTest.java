/*
 * Copyright (c) 2013 LabKey Corporation
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

import junit.framework.Assert;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.util.LogMethod;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * User: cnathe
 * Date: 6/26/13
 */
public class SpecimenExportTest extends SpecimenBaseTest
{
    protected static final String PROJECT_NAME = "SpecimenExportVerifyProject";

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
        startSpecimenImport(1);
        waitForSpecimenImport();
        setPipelineRootToDefault();

        setupRepositoryType();
        setupRequestStatuses();
        setupActorsAndGroups();
        setupDefaultRequirements();
        setupWebpartGroupings();
        setupLocationTypes();
        // UNDONE: enable this once settings included in study archive - setupRequestabilityRules();
        // UNDONE: enable this once settings included in study archive - setupRequestForm();
        // UNDONE: enable this once settings included in study archive - setupActorNotification();
    }

    @Override
    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    protected void doVerifySteps()
    {
        // verify the specimen settings roundtrip by 1. exporting folder zip, 2. deleting the study,
        // 3. importing folder zip, 4. exporting folder expanded, and verifying settings in the XML (instead of in the UI)
        exportStudy(true, true);
        deleteStudy();
        importFromZipExport();
        exportStudy(true, false);
        verifySpecimenSettingsInArchive();
    }

    private void setupRepositoryType()
    {
        log("Setup specimen repository type settings");
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Change Repository Type"));
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Manage Repository Settings"));
        checkRadioButton(Locator.radioButtonByName("simple").index(1)); // Advanced repository type
        checkRadioButton(Locator.radioButtonByName("specimenDataEditable").index(1)); // Editable specimen data
        checkRadioButton(Locator.radioButtonByName("enableRequests").index(0)); // Enabled specimen requests
        clickButton("Submit");
    }

    private void setupWebpartGroupings()
    {
        log("Setup specimen webpart groupings");
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Configure Specimen Groupings"));
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Configure Specimen Web Part"));
        _ext4Helper.selectComboBoxItemById("combo11", "Processing Location");
        _ext4Helper.selectComboBoxItemById("combo12", "Processing Location");
        _ext4Helper.selectComboBoxItemById("combo13", "Processing Location");
        _ext4Helper.selectComboBoxItemById("combo21", "Tube Type");
        _ext4Helper.selectComboBoxItemById("combo22", "Tube Type");
        _ext4Helper.selectComboBoxItemById("combo23", "Tube Type");
        clickButton("Save");
    }

    private void setupLocationTypes()
    {
        log("Setup location types");
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Location Types"));
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Manage Location Types"));
        _ext4Helper.checkCheckbox("Repository");
        _ext4Helper.uncheckCheckbox("Clinic");
        _ext4Helper.uncheckCheckbox("Site Affiliated Lab");
        _ext4Helper.checkCheckbox("Endpoint Lab");
        clickButton("Save");
    }

    private void importFromZipExport()
    {
        log("Import folder zip archive from pipeline");
        goToFolderManagement();
        clickAndWait(Locator.linkWithText("Import"));
        clickButtonContainingText("Import Folder Using Pipeline");
        _extHelper.expandFileBrowserRootNode();
        _extHelper.selectFileBrowserItem("export/");
        click(Locator.xpath("//div[contains(@class, 'x-grid3-cell-inner') and starts-with(text(), 'My Study_')]"));
        selectImportDataAction("Import Folder");
        waitForPipelineJobsToComplete(2, "Folder import", false);
    }

    private void verifySpecimenSettingsInArchive()
    {
        log("verify specimen settings in study.xml");
        _extHelper.selectFileBrowserItem("export/study/study.xml");
        doubleClick(Locator.xpath("//div[contains(@class, 'x-grid3-cell-inner') and text()='study.xml']"));
        waitForText("<specimens dir=\"specimens\" settings=\"specimen_settings.xml\" file=\"Study.specimens\"/>");

        log("verify specimen_settings.xml");
        clickAndWait(Locator.linkWithText("Manage Files"));
        click(Locator.css("button.iconFolderTree"));
        _shortWait.until(ExpectedConditions.visibilityOf(Locator.xpath("id('fileBrowser')//div[contains(@id, 'xsplit')]").findElement(_driver)));
        _extHelper.selectFileBrowserItem("export/study/specimens/specimen_settings.xml");
        doubleClick(Locator.xpath("//div[contains(@class, 'x-grid3-cell-inner') and text()='specimen_settings.xml']"));
        waitForText("<specimens repositoryType=\"ADVANCED\" enableRequests=\"true\" editableRepository=\"true\"");
        assertTextPresentInThisOrder(
            "<webPartGroupings>",
                "<groupBy>Processing Location</groupBy>",
                "<groupBy>Tube Type</groupBy>",
            "</webPartGroupings>");
        assertTextPresentInThisOrder(
            "<locationTypes>",
                "<repository allowRequests=\"true\"/>",
                "<clinic allowRequests=\"false\"/>",
                "<siteAffiliatedLab allowRequests=\"false\"/>",
                "<endpointLab allowRequests=\"true\"/>",
            "</locationTypes>");
        assertTextPresentInThisOrder(
            "<requestStatuses>",
                "<status label=\"New Request\" finalState=\"false\" lockSpecimens=\"true\"/>",
                "<status label=\"Processing\" finalState=\"false\" lockSpecimens=\"true\"/>",
                "<status label=\"Completed\" finalState=\"true\" lockSpecimens=\"true\"/>",
                "<status label=\"Rejected\" finalState=\"true\" lockSpecimens=\"false\"/>",
            "</requestStatuses>");
        assertTextPresentInThisOrder(
            "<requestActors>",
                "<actor label=\"SLG\" type=\"study\">",
                "<group name=\"SLG\" type=\"project\">",
                "<ns:user name=\"user1@specimen.test\"/>",
                "<actor label=\"IRB\" type=\"location\">",
                "<group name=\"Aurum Health KOSH Lab, Orkney, South Africa\" type=\"project\">",
                "<ns:user name=\"user2@specimen.test\"/>",
            "</requestActors>");
        assertTextPresentInThisOrder(
            "<defaultRequirements",
                "<requirement actor=\"IRB\">",
                "<description>Originating IRB Approval</description>",
                "<description>Providing IRB Approval</description>",
                "<description>Receiving IRB Approval</description>",
                "<requirement actor=\"SLG\">",
                "<description>SLG Approval</description>",
            "</defaultRequirements");
    }
}
