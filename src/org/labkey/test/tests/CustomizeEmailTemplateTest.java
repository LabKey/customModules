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

import org.jetbrains.annotations.Nullable;
import org.junit.experimental.categories.Category;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.Locators;
import org.labkey.test.WebTestHelper;
import org.labkey.test.categories.Daily;
import org.labkey.test.components.dumbster.EmailRecordTable;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.StudyHelper;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Category(Daily.class)
@BaseWebDriverTest.ClassTimeout(minutes = 5)
public class CustomizeEmailTemplateTest extends SpecimenBaseTest
{
    private static final String _projectName = "EmailTemplateProject";
    private static final String _recipient = "notify@emailtemplate.test";
    private static final String _assayPlan = "assay plan";
    private static final String _shipping = "123 main street";
    private static final String _comments = "this is my comment";
    private static final String _studyName = "Study 001";
    private static final String _notificationDivName = "params";
    private static final List<String> replacementParams = Arrays.asList(
            "action",
            "attachments",
            "comments",
            "contextPath",
            "currentDateTime",
            "destinationLocation",
            "folderName",
            "folderPath",
            "folderURL",
            "homePageURL",
            "modifiedBy",
            "organizationName",
            "simpleStatus",
            "siteShortName",
            "specimenRequestNumber",
            "status",
            "studyName",
            "subjectSuffix",
            "supportLink",
            "systemDescription",
            "systemEmail",
            "specimenList");

    @Nullable
    @Override
    protected String getProjectName()
    {
        return _projectName;
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

        setPipelineRoot(StudyHelper.getStudySubfolderPath());
        startSpecimenImport(1);
        waitForSpecimenImport();
        setupRequestStatuses();
        setupActorsAndGroups();
        setupRequestForm();
        setupActorNotification();
        setCustomNotificationTemplate();
        createSpecimenRequest();
    }

    private void setCustomNotificationTemplate()
    {
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Notifications"));
        WebElement newRequestNotifyCheckbox = Locator.checkboxById("newRequestNotifyCheckbox").findElement(getDriver());
        checkCheckbox(newRequestNotifyCheckbox);
        checkCheckbox(newRequestNotifyCheckbox); // First try just doesn't stick sometimes
        setFormElement(waitForElement(Locator.id("newRequestNotify").notHidden()), _recipient);
        clickButton("Save");
        clickAndWait(Locator.linkWithText("Manage Notifications"));
        clickAndWait(Locator.linkWithText("Edit Email Template"));
        selectOptionByText(Locator.name("templateClass"), "Specimen request notification");
        setFormElement(Locator.id("emailSubject"), "^studyName^");
        setFormElement(Locator.id("emailMessage"), buildTemplateBody());
        clickButton("Save");
        assertElementNotPresent(Locators.labkeyError);
    }

    private void createSpecimenRequest()
    {
        clickTab("Specimen Data");
        waitAndClickAndWait(Locator.linkWithText("By Individual Vial"));
        DataRegionTable specimenDetail = new DataRegionTable("SpecimenDetail", this);
        specimenDetail.checkCheckbox(0);
        specimenDetail.clickHeaderMenu("Request Options", "Create New Request");
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), _assayPlan);
        setFormElement(Locator.id("input2"), _comments);
        setFormElement(Locator.id("input1"), _shipping);
        setFormElement(Locator.id("input3"), "sample last one input");
        clickButton("Create and View Details");

        doAndWaitForPageToLoad(()-> {
            clickButton("Submit Request", 0);       // don't wait for the page load; an assert will come
            assertAlertIgnoreCaseAndSpaces("Once a request is submitted, its specimen list may no longer be modified.  Continue?"); // dismiss the alert; /then/ wait
        });
    }

    @Override
    protected void doVerifySteps() throws Exception
    {
        EmailRecordTable emailRecordTable = goToEmailRecord();
        EmailRecordTable.EmailMessage message = emailRecordTable.getEmailAtTableIndex(3);
        emailRecordTable.clickMessage(message);
        String[] bodyContents = Locator.name(_notificationDivName).findElement(getDriver()).getText().split("\n");
        Map<String, String> emailNVPs = new HashMap<>();
        for (String line : bodyContents)
        {
            String[] nvp = line.split("==");
            if(nvp.length==2)
            {
                emailNVPs.put(nvp[0].trim(), nvp[1]);
            }
            if(nvp.length==1)
            {
                emailNVPs.put(nvp[0].trim(), "");
            }
        }
        assertEquals(_studyName, message.getSubject());
        assertEquals("New Request", emailNVPs.get("status"));
        assertEquals("New Request Created", emailNVPs.get("action"));
        assertEquals(WebTestHelper.getContextPath(), emailNVPs.getOrDefault("contextPath", ""));
        assertEquals("My Study", emailNVPs.get("folderName"));
        assertEquals("/EmailTemplateProject/My Study", emailNVPs.get("folderPath"));
        assertEquals(WebTestHelper.getBaseURL(), emailNVPs.get("homePageURL"));
    }

    private String buildTemplateBody()
    {
        String delimiter = "";
        StringBuilder templateBuilder = new StringBuilder();
        templateBuilder.append("<div name=\"").append(_notificationDivName).append("\">\n");
        for (String param : replacementParams)
        {
            templateBuilder.append(delimiter);
            templateBuilder.append(String.format("^%s|%s==%%s^", param, param));
            delimiter = "<br>\n";
        }
        templateBuilder.append("\n</div>");
        return templateBuilder.toString();
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("study");
    }
}
