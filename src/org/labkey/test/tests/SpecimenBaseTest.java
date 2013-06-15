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

import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.util.LogMethod;

/**
 * User: davebradlee
 * Date: 5/16/13
 */

// Base class for SpecimenTest and AliquotTest
public abstract class SpecimenBaseTest extends StudyBaseTestWD
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
        deleteUsers(afterTest, USER1, USER2);
        super.doCleanup(afterTest);
    }


    @LogMethod
    protected void setupActorsAndGroups()
    {
        clickAndWait(Locator.linkWithText("Manage Actors and Groups"));
        setFormElement(Locator.name("newLabel"), "SLG");
        selectOptionByText(Locator.name("newPerSite"), "One Per Study");
        clickButton("Save");
        clickAndWait(Locator.linkWithText("Update Members"));
        setFormElement(Locator.name("names"), USER1);
        uncheckCheckbox(Locator.checkboxByName("sendEmail"));
        clickButton("Update Members");
        setFormElement(Locator.name("newLabel"), "IRB");
        selectOptionByText(Locator.name("newPerSite"), "Multiple Per Study (Location Affiliated)");
        clickButton("Save");
        clickAndWait(Locator.linkWithText("Update Members").index(1));
        clickAndWait(Locator.linkWithText(DESTINATION_SITE));
        setFormElement(Locator.name("names"), USER2);
        uncheckCheckbox(Locator.checkboxByName("sendEmail"));
        clickAndWait(Locator.linkWithText("Update Members"));
        clickFolder(getStudyLabel());
    }

    @LogMethod (quiet = true)
    protected void setupDefaultRequirements()
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
    protected void setupRequestForm()
    {
        clickAndWait(Locator.linkWithText("Manage New Request Form"));
        clickButton("Add New Input", 0);
        setFormElement(Locator.xpath("//descendant::input[@name='title'][4]"), "Last One");
        setFormElement(Locator.xpath("//descendant::input[@name='helpText'][4]"), "A test input");
        click(Locator.xpath("//descendant::input[@name='required'][4]"));
        clickButton("Save");
        clickFolder(getStudyLabel());
    }

    @LogMethod
    protected void setupActorNotification()
    {
        log("Check Configure Defaults for Actor Notification");
        clickFolder(getStudyLabel());
        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Manage Notifications"));
        assertTextPresent("Default Email Recipients");
        checkRadioButton(Locator.radioButtonByNameAndValue("defaultEmailNotify", "All"));
        clickButton("Save");
    }

}
