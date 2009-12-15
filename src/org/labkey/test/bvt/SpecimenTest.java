/*
 * Copyright (c) 2007-2009 LabKey Corporation
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

package org.labkey.test.bvt;

import org.labkey.test.BaseSeleniumWebTest;
import org.labkey.test.Locator;

import java.io.File;

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
    private String _studyDataRoot = null;


    public String getAssociatedModuleDirectory()
    {
        return "study";
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

        createProject(PROJECT_NAME);
        createSubfolder(PROJECT_NAME, PROJECT_NAME, FOLDER_NAME, "Study", null);
        clickNavButton("Create Study");
        click(Locator.radioButtonByNameAndValue("simpleRepository", "false"));
        clickNavButton("Create Study");
        clickLinkWithText("My Study");

        clickLinkWithText("Data Pipeline");
        clickNavButton("Setup");
        setPipelineRoot(_studyDataRoot);
        clickLinkWithText("Pipeline");

        SpecimenImporter importer = new SpecimenImporter(new File(_studyDataRoot), new File(getLabKeyRoot(), SPECIMEN_ARCHIVE), new File(getLabKeyRoot(), SPECIMEN_TEMP_DIR), FOLDER_NAME, 1);
        importer.importAndWaitForComplete();

        // specimen management setup
        selenium.click("link=My Study Study");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Manage Statuses");
        selenium.waitForPageToLoad("30000");
        selenium.type("newLabel", "New Request");
        clickNavButton("Save");
        selenium.waitForPageToLoad("30000");
        selenium.type("newLabel", "Processing");
        clickNavButton("Save");
        selenium.waitForPageToLoad("30000");
        selenium.type("newLabel", "Completed");
        selenium.click("newFinalState");
        clickNavButton("Save");
        selenium.waitForPageToLoad("30000");
        selenium.type("newLabel", "Rejected");
        selenium.click("newFinalState");
        selenium.click("newSpecimensLocked");
        clickNavButton("Done");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Manage Actors and Groups");
        selenium.waitForPageToLoad("30000");
        selenium.type("newLabel", "SLG");
        selenium.select("newPerSite", "label=One Per Study");
        clickNavButton("Save");
        selenium.waitForPageToLoad("30000");
        selenium.type("newLabel", "IRB");
        selenium.select("newPerSite", "label=Multiple Per Study (Location Affiliated)");
        clickNavButton("Save");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Update Members");
        selenium.waitForPageToLoad("30000");
        clickNavButton("Update Members");
        selenium.waitForPageToLoad("30000");
        selenium.click("//a[contains(@href, 'manageActors.view?showMemberSites')]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=My Study Study");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Manage Default Requirements");
        selenium.waitForPageToLoad("30000");
        selenium.select("originatorActor", "label=IRB");
        selenium.type("originatorDescription", "Originating IRB Approval");
        clickNavButton("Add Requirement");
        selenium.waitForPageToLoad("30000");
        selenium.select("providerActor", "label=IRB");
        selenium.type("providerDescription", "Providing IRB Approval");
        selenium.click("//input[@name='providerDescription']/../.." + Locator.navButton("Add Requirement").getPath());
        selenium.waitForPageToLoad("30000");
        selenium.select("receiverActor", "label=IRB");
        selenium.type("receiverDescription", "Receiving IRB Approval");
        selenium.click("//input[@name='receiverDescription']/../.." + Locator.navButton("Add Requirement").getPath());
        selenium.waitForPageToLoad("30000");
        selenium.select("generalActor", "label=SLG");
        selenium.type("generalDescription", "SLG Approval");
        selenium.click("//input[@name='generalDescription']/../.." + Locator.navButton("Add Requirement").getPath());
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Manage Study");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Manage New Request Form");
        selenium.waitForPageToLoad("30000");
        clickNavButton("Add New Input", 0);
        selenium.type("document.forms[0].title[3]", "Last One");
        selenium.type("document.forms[0].helpText[3]", "A test input");
        selenium.click("document.forms[0].required[3]");
        clickNavButton("Save");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=My Study Study");
        selenium.waitForPageToLoad("30000");

        // create request
        selenium.click("link=Plasma, Unknown Processing");
        selenium.waitForPageToLoad("30000");
        selenium.click(".toggle");
        clickNavButton("Request Options", 0);
        clickLinkWithText("Create New Request");
        selenium.waitForPageToLoad("30000");
        selenium.select("destinationSite", "label=Aurum Health KOSH Lab, Orkney, South Africa (Repository)");
        selenium.type("input0", "Assay Plan");
        selenium.type("input2", "Comments");
        selenium.type("input1", "Shipping");
        clickNavButton("Create and View Details");
        selenium.waitForPageToLoad("30000");
        assertTextPresent("Please provide all required input.");
        selenium.type("input3", "sample last one input");
        clickNavButton("Create and View Details");
        selenium.waitForPageToLoad("30000");
        assertTextPresent("sample last one input");
        assertTextPresent("IRB");
        assertTextPresent("KCMC, Moshi, Tanzania");
        assertTextPresent("Originating IRB Approval");
        assertTextPresent("Contract Lab Services, Johannesburg, South Africa (Repository)");
        assertTextPresent("Providing IRB Approval");
        assertTextPresent("Aurum Health KOSH Lab, Orkney, South Africa (Repository)");
        assertTextPresent("Receiving IRB Approval");
        assertTextPresent("SLG");
        assertTextPresent("SLG Approval");
        assertTextPresent("BAA07XNP-01");
        // verify that the swab specimen isn't present yet
        assertTextNotPresent("DAA07YGW-01");
        assertTextNotPresent("Complete");
        selenium.click("link=My Study Study");
        selenium.waitForPageToLoad("30000");

        // add additional specimens
        selenium.click("link=Swab");
        selenium.waitForPageToLoad("30000");
        selenium.click(".toggle");
        clickNavButton("Request Options", 0);
        clickLinkWithText("Add To Existing Request", false);
        sleep(15000);
        clickNavButton("Add 8 Vials to Request", 0);
        sleep(15000);
        clickNavButton("OK", 0);
        sleep(15000);
        clickNavButton("Request Options", 0);
        clickLinkWithText("View Existing Requests");
        clickNavButton("Details");
        selenium.waitForPageToLoad("30000");
        assertTextPresent("sample last one input");
        assertTextPresent("IRB");
        assertTextPresent("KCMC, Moshi, Tanzania");
        assertTextPresent("Originating IRB Approval");
        assertTextPresent("Contract Lab Services, Johannesburg, South Africa (Repository)");
        assertTextPresent("Providing IRB Approval");
        assertTextPresent("Aurum Health KOSH Lab, Orkney, South Africa (Repository)");
        assertTextPresent("Receiving IRB Approval");
        assertTextPresent("SLG");
        assertTextPresent("SLG Approval");
        assertTextPresent("BAA07XNP-01");
        assertTextPresent("DAA07YGW-01");

        // submit request
        assertTextPresent("Not Yet Submitted");
        assertTextNotPresent("New Request");
        clickNavButton("Submit Request");
        assertTrue(selenium.getConfirmation().matches("^Once a request is submitted, its specimen list may no longer be modified\\.  Continue[\\s\\S]$"));
        assertTextNotPresent("Not Yet Submitted");
        assertTextPresent("New Request");

        // modify request
        selenium.select("newActor", "label=SLG");
        selenium.type("newDescription", "Other SLG Approval");
        clickNavButton("Add Requirement");
        selenium.waitForPageToLoad("30000");
        selenium.click("//a[contains(@href, 'manageRequirement.view')]");
        selenium.waitForPageToLoad("30000");
        selenium.click("complete");
        clickNavButton("Save Changes and Send Notifications");
        selenium.waitForPageToLoad("30000");
        assertTextPresent("Complete");

        // verify views
        selenium.click("link=View History");
        selenium.waitForPageToLoad("30000");
        assertTextPresent("Request submitted for processing.");
        selenium.click("link=View Request");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Originating Location Specimen Lists");
        selenium.waitForPageToLoad("30000");
        assertTextPresent("KCMC, Moshi, Tanzania");
        clickNavButton("Cancel");
        selenium.click("link=Providing Location Specimen Lists");
        selenium.waitForPageToLoad("30000");
        assertTextPresent("Contract Lab Services, Johannesburg, South Africa (Repository)");
        clickNavButton("Cancel");
        // cancel request
        selenium.click("link=Update Status");
        selenium.waitForPageToLoad("30000");
        selenium.select("status", "label=Not Yet Submitted");
        clickNavButton("Save Changes and Send Notifications");
        clickNavButton("Cancel Request");
        assertTrue(selenium.getConfirmation().matches("^Canceling will permanently delete this pending request\\.  Continue[\\s\\S]$"));
        assertTextPresent("No data to show.");
        clickLinkWithText("My Study Study");
        clickLinkWithText("Swab");
        selenium.click(".toggle");
        clickNavButton("Request Options", 0);
        clickLinkWithText("Create New Request");
        clickNavButton("Cancel");
    }
}
