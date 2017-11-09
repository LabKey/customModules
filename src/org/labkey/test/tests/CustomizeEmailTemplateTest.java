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

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.categories.DailyC;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.PortalHelper;
import org.labkey.test.util.StudyHelper;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Category(DailyC.class)
public class CustomizeEmailTemplateTest extends SpecimenBaseTest
{
    private static final String _projectName = "EmailTemplateProject";
    private final PortalHelper _portalHelper = new PortalHelper(this);
    private static final String _assayPlan = "assay plan";
    private static final String _shipping = "123 main street";
    private static final String _comments = "this is my comment";
    private static final String _delim = "::";
    private static final String _emailBody1 = "<Div id=\"body\">" +
            "action==^action^"  + _delim + "\n" +
            "attachments==^attachments^" + _delim + "\n" +
            "comments==^comments^" + _delim + "\n" +
            "contextPath==^contextPath^" + _delim + "\n" +
            "currentDateTime==^currentDateTime^" + _delim + "\n" +
            "destinationLocation==^destinationLocation^" + _delim + "\n" +
            "folderName==^folderName^" + _delim + "\n" +
            "folderPath==^folderPath^" + _delim + "\n" +
            "folderURL==^folderURL^" + _delim + "\n" +
            "homePageURL==^homePageURL^" + _delim + "\n" +
            "modifiedBy==^modifiedBy^" + _delim + "\n" +
            "organizationName==^organizationName^" + _delim + "\n" +
            "simpleStatus==^simpleStatus^" + _delim + "\n" +
            "siteShortName==^siteShortName^" + _delim + "\n" +
            "specimenList==^specimenList^" + _delim + "\n" +
            "specimenRequestNumber==^specimenRequestNumber^" + _delim + "\n" +
            "status==^status^" + _delim + "\n" +
            "studyName==^studyName^" + _delim + "\n" +
            "subjectSuffix==^subjectSuffix^" + _delim + "\n" +
            "supportLink==^supportLink^" + _delim + "\n" +
            "systemDescription==^systemDescription^" + _delim + "\n" +
            "systemEmail==^systemEmail^" +
            "<Div>";

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
        click(Locator.radioButtonByNameAndValue("simpleRepository", "false"));
        clickButton("Create Study");

        setPipelineRoot(StudyHelper.getPipelinePath());
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
        setFormElement(waitForElement(Locator.id("newRequestNotify").notHidden()), "notify@emailtemplate.test");
        clickButton("Save");
        clickAndWait(Locator.linkWithText("Manage Notifications"));
        clickAndWait(Locator.linkWithText("Edit Email Template"));
        selectOptionByText(Locator.name("templateClass"), "Specimen request notification");
        setFormElement(Locator.id("emailSubject"), _projectName);
        setFormElement(Locator.id("emailMessage"), _emailBody1);
        clickButton("Save");
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
        clickButton("Submit Request", 0);
        assertAlert("Once a request is submitted, its specimen list may no longer be modified.  Continue?");
    }

    @Override
    protected void doVerifySteps() throws Exception
    {
        goToModule("Dumbster");
        Locator.linkWithText(_projectName).waitForElement(getDriver(), WAIT_FOR_JAVASCRIPT).click();
        String emailBody= getText(Locator.xpath("//Div[@id='body']"));
        String[] bodyContents = emailBody.split(_delim);
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
        Assert.assertEquals("New Request", emailNVPs.get("status"));
        Assert.assertEquals("New Request Created", emailNVPs.get("action"));
        Assert.assertEquals("/labkey", emailNVPs.get("contextPath"));
        Assert.assertEquals("My Study", emailNVPs.get("folderName"));
        Assert.assertEquals("/EmailTemplateProject/My Study", emailNVPs.get("folderPath"));
        Assert.assertEquals(getBaseURL(), emailNVPs.get("homePageURL"));
    }


    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("study");
    }
}
