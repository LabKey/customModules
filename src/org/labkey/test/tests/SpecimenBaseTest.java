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

import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.components.html.RadioButton;
import org.labkey.test.util.LogMethod;

import static org.labkey.test.components.html.RadioButton.RadioButton;

public abstract class SpecimenBaseTest extends StudyBaseTest
{
    public static final String SPECIMEN_DETAIL = "SpecimenDetail";
    protected static final String DESTINATION_SITE = "Aurum Health KOSH Lab, Orkney, South Africa (Endpoint Lab, Repository)";
    protected static final String SOURCE_SITE = "Contract Lab Services, Johannesburg, South Africa (Repository, Clinic)";
    protected static final String USER1 = "user1@specimen.test";
    protected static final String USER2 = "user2@specimen.test";
    protected static final String REQUESTABILITY_QUERY = "RequestabilityRule";
    protected static final String UNREQUESTABLE_SAMPLE = "BAA07XNP-02";
    protected static final String[] PTIDS = {"999320396","999320812"};
    protected int _requestId;

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        super.doCleanup(afterTest);
    }

    @LogMethod
    protected void setupRequestabilityRules()
    {
        // Create custom query to test requestability rules.
        goToSchemaBrowser();
        createNewQuery("study", SPECIMEN_DETAIL);
        setFormElement(Locator.name("ff_newQueryName"), REQUESTABILITY_QUERY);
        clickAndWait(Locator.lkButton("Create and Edit Source"));
        setCodeEditorValue("queryText",
                "SELECT \n" +
                        SPECIMEN_DETAIL + ".GlobalUniqueId AS GlobalUniqueId\n" +
                        "FROM " + SPECIMEN_DETAIL + "\n" +
                        "WHERE " + SPECIMEN_DETAIL + ".GlobalUniqueId='" + UNREQUESTABLE_SAMPLE + "'");
        clickButton("Save", 0);
        waitForElement(Locator.css(".labkey-status-info").withText("Saved"));

        clickFolder(getFolderName());
        waitAndClick(Locator.linkWithText("Manage Study"));
        waitAndClick(Locator.linkWithText("Manage Requestability Rules"));
        // Verify that LOCKED_IN_REQUEST is the last rule
        waitForElement(Locator.xpath("//div[contains(@class, 'x-grid3-row-last')]//div[text()='Locked In Request Check']"));
        click(Locator.xpath("//div[contains(@class, 'x-grid3-row-last')]//div[text()='Locked In Request Check']"));

        click(Locator.xpath("//div[contains(@class, 'x-grid3-col-numberer') and text()='2']"));

        clickButton("Add Rule", 0);
        click(Locator.menuItem("Custom Query"));
        _extHelper.selectComboBoxItem(Locator.xpath("//div[@id='x-form-el-userQuery_schema']"), "study" );
        _extHelper.selectComboBoxItem(Locator.xpath("//div[@id='x-form-el-userQuery_query']"), REQUESTABILITY_QUERY );
        _extHelper.selectComboBoxItem(Locator.xpath("//div[@id='x-form-el-userQuery_action']"), "Unavailable" );
        clickButton("Submit",0);
        clickButton("Save");
    }

    @LogMethod
    protected void setupActorsAndGroups()
    {
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Actors and Groups"));
        setFormElement(Locator.name("newLabel"), "SLG");
        selectOptionByText(Locator.name("newPerSite"), "One Per Study");
        clickButton("Save");
        clickAndWait(Locator.linkWithText("Update Members"));
        waitForElement(Locator.name("names"));
        setFormElement(Locator.name("names"), USER1);
        uncheckCheckbox(Locator.checkboxByName("sendEmail"));
        clickButton("Update Members");
        setFormElement(Locator.name("newLabel"), "IRB");
        selectOptionByText(Locator.name("newPerSite"), "Multiple Per Study (Location Affiliated)");
        clickButton("Save");
        clickAndWait(Locator.linkWithText("Update Members").index(1));
        clickAndWait(Locator.linkWithText(DESTINATION_SITE));
        waitForElement(Locator.name("names"));
        setFormElement(Locator.name("names"), USER2);
        uncheckCheckbox(Locator.checkboxByName("sendEmail"));
        clickAndWait(Locator.linkWithText("Update Members"));
    }

    @LogMethod (quiet = true)
    protected void setupDefaultRequirements()
    {
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Default Requirements"));
        selectOptionByText(Locator.name("originatorActor"), "IRB");
        setFormElement(Locator.name("originatorDescription"), "Originating IRB Approval");
        clickButton("Add Requirement");
        selectOptionByText(Locator.name("providerActor"), "IRB");
        setFormElement(Locator.name("providerDescription"), "Providing IRB Approval");
        clickAndWait(Locator.xpath("//input[@name='providerDescription']/../.." + Locator.lkButton("Add Requirement").toXpath()));
        selectOptionByText(Locator.name("receiverActor"), "IRB");
        setFormElement(Locator.name("receiverDescription"), "Receiving IRB Approval");
        clickAndWait(Locator.xpath("//input[@name='receiverDescription']/../.." + Locator.lkButton("Add Requirement").toXpath()));
        selectOptionByText(Locator.name("generalActor"), "SLG");
        setFormElement(Locator.name("generalDescription"), "SLG Approval");
        clickAndWait(Locator.xpath("//input[@name='generalDescription']/../.." + Locator.lkButton("Add Requirement").toXpath()));
    }

    @LogMethod (quiet = true)
    protected void setupRequestForm()
    {
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage New Request Form"));
        clickButton("Add New Input", 0);
        setFormElement(Locator.xpath("//descendant::input[@name='title'][4]"), "Last One");
        setFormElement(Locator.xpath("//descendant::input[@name='helpText'][4]"), "A test input");
        click(Locator.xpath("//descendant::input[@name='required'][4]"));
        clickButton("Save");
    }

    @LogMethod
    protected void setupActorNotification()
    {
        log("Check Configure Defaults for Actor Notification");
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Notifications"));
        assertTextPresent("Default Email Recipients");
        RadioButton notifyAllRadioButton = RadioButton(Locator.radioButtonByNameAndValue("defaultEmailNotify", "All")).find(getDriver());
        waitFor(() -> {
            notifyAllRadioButton.check();
            return notifyAllRadioButton.isChecked();
        }, "Failed to check radio button", WAIT_FOR_JAVASCRIPT);
        clickButton("Save");

        // TODO: Remove check below. This is temporary to investigate intermittent test failures where actors with emails
        // aren't checked on the manage requirement page. Verify that setting actually got set.
        clickAndWait(Locator.linkWithText("Manage Notifications"));
        assertTextPresent("Default Email Recipients");
        assertRadioButtonSelected(Locator.radioButtonByNameAndValue("defaultEmailNotify", "All"));
        clickButton("Cancel");
    }
}
