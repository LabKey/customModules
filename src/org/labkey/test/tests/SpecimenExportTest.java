/*
 * Copyright (c) 2013-2017 LabKey Corporation
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
import org.labkey.test.TestFileUtils;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.DailyC;
import org.labkey.test.categories.Specimen;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.StudyHelper;

import java.io.File;

import static org.junit.Assert.assertTrue;

@Category({DailyC.class, Specimen.class})
public class SpecimenExportTest extends SpecimenBaseTest
{
    protected static final String PROJECT_NAME = "SpecimenExportVerifyProject";

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
    @LogMethod
    protected void doCreateSteps()
    {
        enableEmailRecorder();
        initializeFolder();

        clickButton("Create Study");
        setFormElement(Locator.name("label"), getStudyLabel());
        click(Locator.radioButtonByNameAndValue("simpleRepository", "false"));
        clickButton("Create Study");

        setPipelineRoot(StudyHelper.getPipelinePath());
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
    @LogMethod
    protected void doVerifySteps()
    {
        // verify the specimen settings roundtrip by 1. exporting folder zip, 2. deleting the study,
        // 3. importing folder zip, 4. exporting folder expanded, and verifying settings in the XML (instead of in the UI)
        exportStudy(true);
        deleteStudy();
        importFromZipExport();
        exportStudy(false);
        verifySpecimenSettingsInArchive();
    }

    private void setupRepositoryType()
    {
        log("Setup specimen repository type settings");
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Change Repository Type"));
        waitForElement(Locator.tagContainingText("h3","Manage Repository Settings"));
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
        waitForElement(Locator.tagContainingText("h3","Configure Specimen Web Part"));
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
        waitForElement(Locator.tagContainingText("h3","Manage Location Types"));
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
        clickButtonContainingText("Use Pipeline");
        _fileBrowserHelper.selectFileBrowserItem("/export/");
        waitAndClick(Locator.tag("tr").withClass("x4-grid-data-row").withAttributeContaining("data-recordid", "My Study_"));
        _fileBrowserHelper.selectImportDataAction("Import Folder");
        clickButton("Start Import"); // Validate queries page
        waitForPipelineJobsToComplete(2, "Folder import", false);
    }

    private void verifySpecimenSettingsInArchive()
    {
        log("verify specimen settings in study.xml");
        File studyXml = new File(TestFileUtils.getDefaultFileRoot(getProjectName() + "/" + getFolderName()), "export/study/study.xml");
        waitFor(studyXml::exists, "Couldn't find exported study: " + studyXml, WAIT_FOR_JAVASCRIPT);
        String studyXmlText = TestFileUtils.getFileContents(studyXml).replaceAll(" *\r*\n *", "\n").replaceAll(" +", " ");
        assertTrue(studyXmlText.contains("<specimens dir=\"specimens\" settings=\"specimen_settings.xml\" file=\"Study.specimens\"/>"));

        log("verify specimen_settings.xml");
        File specimenSettingsXml = new File(TestFileUtils.getDefaultFileRoot(getProjectName() + "/" + getFolderName()), "export/study/specimens/specimen_settings.xml");
        String specimenSettingsXmlText = TestFileUtils.getFileContents(specimenSettingsXml).replaceAll(" *\r*\n *", "\n").replaceAll(" +", " ");

        assertTrue(specimenSettingsXmlText.contains("<specimens repositoryType=\"ADVANCED\" enableRequests=\"true\" editableRepository=\"true\""));
        assertTrue(specimenSettingsXmlText.contains(
                "<webPartGroupings>\n"+
                    "<grouping>\n"+
                        "<groupBy>Processing Location</groupBy>\n"+
                        "<groupBy>Processing Location</groupBy>\n"+
                        "<groupBy>Processing Location</groupBy>\n"+
                    "</grouping>\n"+
                    "<grouping>\n"+
                        "<groupBy>Tube Type</groupBy>\n"+
                        "<groupBy>Tube Type</groupBy>\n"+
                        "<groupBy>Tube Type</groupBy>\n"+
                    "</grouping>\n"+
                "</webPartGroupings>"));
        assertTrue(specimenSettingsXmlText.contains(
                "<locationTypes>\n" +
                    "<repository allowRequests=\"true\"/>\n" +
                    "<clinic allowRequests=\"false\"/>\n" +
                    "<siteAffiliatedLab allowRequests=\"false\"/>\n" +
                    "<endpointLab allowRequests=\"true\"/>\n" +
                "</locationTypes>"));
        assertTrue(specimenSettingsXmlText.contains(
                "<requestStatuses>\n" +
                    "<status label=\"New Request\" finalState=\"false\" lockSpecimens=\"true\"/>\n" +
                    "<status label=\"Processing\" finalState=\"false\" lockSpecimens=\"true\"/>\n" +
                    "<status label=\"Completed\" finalState=\"true\" lockSpecimens=\"true\"/>\n" +
                    "<status label=\"Rejected\" finalState=\"true\" lockSpecimens=\"false\"/>\n" +
                "</requestStatuses>"));
        assertTrue(specimenSettingsXmlText.contains(
                "<requestActors>\n" +
                    "<actor label=\"SLG\" type=\"study\">\n" +
                        "<groups>\n" +
                            "<ns:group name=\"SLG\" type=\"project\">\n" +
                                "<ns:users>\n" +
                                    "<ns:user name=\"user1@specimen.test\"/>\n" +
                                "</ns:users>\n" +
                            "</ns:group>\n" +
                        "</groups>\n" +
                    "</actor>\n" +
                    "<actor label=\"IRB\" type=\"location\">\n" +
                        "<groups>\n" +
                            "<ns:group name=\"Aurum Health KOSH Lab, Orkney, South Africa\" type=\"project\">\n" +
                                "<ns:users>\n" +
                                    "<ns:user name=\"user2@specimen.test\"/>\n" +
                                "</ns:users>\n" +
                            "</ns:group>\n" +
                        "</groups>\n" +
                    "</actor>\n" +
                "</requestActors>"));
        assertTrue(specimenSettingsXmlText.contains(
                "<defaultRequirements>\n" +
                    "<originatingLab>\n" +
                        "<requirement actor=\"IRB\">\n" +
                            "<description>Originating IRB Approval</description>\n" +
                        "</requirement>\n" +
                    "</originatingLab>\n" +
                    "<providingLab>\n" +
                        "<requirement actor=\"IRB\">\n" +
                            "<description>Providing IRB Approval</description>\n" +
                        "</requirement>\n" +
                    "</providingLab>\n" +
                    "<receivingLab>\n" +
                        "<requirement actor=\"IRB\">\n" +
                            "<description>Receiving IRB Approval</description>\n" +
                        "</requirement>\n" +
                    "</receivingLab>\n" +
                    "<general>\n" +
                        "<requirement actor=\"SLG\">\n" +
                            "<description>SLG Approval</description>\n" +
                        "</requirement>\n" +
                    "</general>\n" +
                "</defaultRequirements>"));
    }
}
