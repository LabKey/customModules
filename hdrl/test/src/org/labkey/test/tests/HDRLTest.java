/*
 * Copyright (c) 2015 LabKey Corporation
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestFileUtils;
import org.labkey.test.categories.CustomModules;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.PostgresOnlyTest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Category({CustomModules.class})
public class HDRLTest extends BaseWebDriverTest implements PostgresOnlyTest
{
    public static final String PROJECT_NAME = "HDRL Verification";
    protected final String TEST_SPECIMEN_UPLOAD_FILE_1 = TestFileUtils.getLabKeyRoot() + "/sampledata/hdrl/sample_upload_01.tsv";
    protected final String TEST_SPECIMEN_UPLOAD_FILE_2 = TestFileUtils.getLabKeyRoot() + "/sampledata/hdrl/sample_upload_02.xlsx";


    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    @BeforeClass
    public static void initProject()
    {
        HDRLTest init = (HDRLTest)getCurrentTest();
        init.setupFolder();
    }

    @Before
    public void preTest()
    {
        goToProjectHome();
    }

    @Test
    public void testNewRequest()
    {
        log("creating a new test request");

        click(Locator.linkContainingText("Create a new test request"));

        // validate required fields
        log("validate required fields");
        clickButton("Save", 0);
        _extHelper.waitForExtDialog("Error");
        clickButtonContainingText("OK", "Error");
        _extHelper.waitForExtDialogToDisappear("Error");

        _ext4Helper.selectComboBoxItem("Request Type","HIV Screening Algorithm");

        // add specimen through the row picker
        List<Map<String, String>> rows = new ArrayList<>();
        Map<String, String> r1 = new HashMap<>();

        r1.put("CustomerBarcode", "1234");
        r1.put("LastName", "Smith");
        r1.put("FirstName", "Jon");
        r1.put("SSN", "543");
        rows.add(r1);
        addSpecimenRequestRow(r1, "Invalid SSN; Required field(s) missing: FMP, Draw Date", true);

        Map<String, String> r2 = new HashMap<>();

        r2.put("CustomerBarcode", "5678");
        r2.put("LastName", "Smith");
        r2.put("FirstName", "Tommy");
        r2.put("SSN", "543221234");
        rows.add(r2);
        addSpecimenRequestRow(r2, "Required field(s) missing: FMP, Draw Date", false);

        log("ensure row editor saves the currently edited row");
        clickButton("Save");

        // verify the inserted row via the data region table
        verifyDataRegionRows("InboundSpecimen", rows, "CustomerBarcode");
    }

    @Test
    public void testFileUpload()
    {
        File file = new File(TEST_SPECIMEN_UPLOAD_FILE_1);
        uploadFile(file);

        log("verify proper SSN formatting");
        waitForElement(Locator.tagContainingText("div", "222-33-4444"));

        log("verify status message update");
        waitForElement(Locator.tagContainingText("div", "Required field(s) missing: FMP, Draw Date, SSN"));
        waitForElement(Locator.tagContainingText("div", "Required field(s) missing: FMP"));

        log("verify submit error");
        clickButton("Submit Requests", 0);
        _extHelper.waitForExtDialog("Error");
        clickButtonContainingText("OK", "Error");
        _extHelper.waitForExtDialogToDisappear("Error");

        clickButton("Save");

        List<Map<String, String>> rows = new ArrayList<>();
        Map<String, String> r1 = new HashMap<>();
        r1.put("CustomerBarcode", "5555");
        r1.put("LastName", "Jones");
        r1.put("FirstName", "Tom");
        r1.put("SSN", "222334444");
        r1.put("DUC", "A13");
        r1.put("SOT", "P");
        rows.add(r1);

        Map<String, String> r2 = new HashMap<>();
        r2.put("CustomerBarcode", "6666");
        r2.put("LastName", "Jones");
        r2.put("FirstName", "Jerry");
        r2.put("DUC", "A14");
        r2.put("SOT", "B");
        rows.add(r2);

        // verify the inserted row via the data region table
        verifyDataRegionRows("InboundSpecimen", rows, "CustomerBarcode");
    }

    @Test
    public void testFileUploadAndSubmit()
    {
        log("creating a new test request by uploading a file");

        click(Locator.linkContainingText("Create a new test request"));
        _ext4Helper.selectComboBoxItem("Request Type", "HIV Screening Algorithm");
        _ext4Helper.selectComboBoxItem("Carrier","FedEx");

        log("upload specimen data from a .xlsx file");
        File file2 = new File(TEST_SPECIMEN_UPLOAD_FILE_2);
        setFormElement(Locator.tagWithName("input", "file"), file2);
        clickButton("upload file", 0);

        log("verify proper SSN formatting");
        waitForElement(Locator.tagContainingText("div", "222-33-4444"));
        waitForElement(Locator.tagContainingText("div", "555-44-3333"));
        clickButton("Save");

        List<Map<String, String>> rows = new ArrayList<>();
        Map<String, String> r1 = new HashMap<>();
        r1.put("CustomerBarcode", "7777");
        r1.put("LastName", "Johnston");
        r1.put("FirstName", "Jack");
        r1.put("SSN", "222334444");
        r1.put("FMP", "01");
        r1.put("DUC", "A13");
        r1.put("SOT", "P");
        rows.add(r1);

        Map<String, String> r2 = new HashMap<>();
        r2.put("CustomerBarcode", "8888");
        r2.put("LastName", "Johnston");
        r2.put("FirstName", "Fred");
        r2.put("SSN", "555443333");
        r2.put("FMP", "02");
        r2.put("DUC", "A14");
        r2.put("SOT", "B");
        rows.add(r2);

        // verify the inserted row via the data region table
        verifyDataRegionRows("InboundSpecimen", rows, "CustomerBarcode");

        log("edit an existing request");
        goToProjectHome();
        click(Locator.linkContainingText("View test requests"));

        DataRegionTable drt = new DataRegionTable("query", this);
        int idx = drt.getRow("ShippingCarrier", "FedEx");
        assertNotEquals(idx, -1);
        clickAndWait(drt.link(idx, 0));
        log("submitting an existing request");
        waitForElement(Locator.tagContainingText("div", "222-33-4444"));
        waitForElement(Locator.tagContainingText("div", "555-44-3333"));
        clickButton("Submit Requests");

        drt = new DataRegionTable("query", this);
        idx = drt.getRow("ShippingCarrier", "FedEx");
        assertNotEquals(idx, -1);
        log("ensure submitted requests are still editable by admins");
        assertEquals("EDIT", drt.getDataAsText(idx, 0));
        clickAndWait(drt.link(idx, 0));

        waitForElement(Locator.tagContainingText("span", "Edit a Test Request"));
        log("verify submitted requests are readonly for non-admins");

        impersonateRole("Reader");
        goToProjectHome();
        click(Locator.linkContainingText("View test requests"));
        drt = new DataRegionTable("query", this);
        idx = drt.getRow("ShippingCarrier", "FedEx");
        assertNotEquals(idx, -1);
        log("ensure submitted requests are still editable by admins");
        assertEquals("VIEW", drt.getDataAsText(idx, -1));
        clickAndWait(drt.link(idx, -1));

        waitForElement(Locator.tagContainingText("td", "Carrier"));
        waitForElement(Locator.tagContainingText("td", "FedEx"));
        assertElementNotPresent(Locator.tagContainingText("span", "Edit a Test Request"));
        stopImpersonatingRole();
    }

    @Test
    public void testDataDeletion()
    {
        File file = new File(TEST_SPECIMEN_UPLOAD_FILE_2);
        uploadFile(file);
        clickButton("Save");
        goToAdminConsole();
        click(Locator.linkWithText("System Maintenance"));
        waitForText("Configure System Maintenance");
        click(Locator.linkWithText("HDRL Request Portal PHI Deletion"));
        //waitForText("System maintenance complete");
        getDriver().close();
        switchToMainWindow();
        goToProjectHome();
        click(Locator.linkWithText("View test requests"));
        DataRegionTable drt = new DataRegionTable("query", this);
        int rowCount = drt.getDataRowCount();
        List<Integer> archivedRows = new ArrayList<>();
        for(int row = 0; row < rowCount; row++)
        {
            List<String> rowData = drt.getRowDataAsText(row);
            //all 'Submitted' requests should have been archived
            org.junit.Assert.assertFalse(rowData.contains("Submitted"));
            if(rowData.contains("Archived"))
            {
                archivedRows.add(row);
            }
        }

    }

    private void uploadFile(File file)
    {
        log("creating a new test request by uploading a file");
        click(Locator.linkContainingText("Create a new test request"));
        _ext4Helper.selectComboBoxItem("Request Type","HIV Screening Algorithm");

        log("upload specimen data from a .tsv file");
        setFormElement(Locator.tagWithName("input", "file"), file);
        clickButton("upload file", 0);
    }

    private void verifyDataRegionRows(String tableName, List<Map<String, String>> expectedRows, String key)
    {
        log("verifying specimen rows in the schema browser");
        goToSchemaBrowser();
        selectQuery("hdrl", tableName);
        waitForText("view data");
        clickAndWait(Locator.linkContainingText("view data"));
        DataRegionTable drt = new DataRegionTable("query", this);

        // find the row to verify
        for (Map<String, String> expectedRow : expectedRows)
        {
            int idx = drt.getRow(key, expectedRow.get(key));
            assertNotEquals(String.format("Didn't find row with %s = %s", key, expectedRow.get(key)), idx, -1);

            Map<String, String> actualRow = new HashMap<>();
            for (Map.Entry<String, String> field : expectedRow.entrySet())
            {
                actualRow.put(field.getKey(), drt.getDataAsText(idx, field.getKey()));
            }
            assertEquals("Bad row data", expectedRow, actualRow);
        }
    }

    private void addSpecimenRequestRow(Map<String, String> values, String statusText, boolean updateRow)
    {
        clickButton("add specimen", 0);

        log("adding a specimen through the row picker");
        for (Map.Entry<String, String> field : values.entrySet())
        {
            Locator loc = Locator.xpath("//input[contains(@class, 'form-field') and @name='" + field.getKey() + "']");
            waitForElement(loc);
            log("setting " + field.getKey());
            setFormElement(loc, field.getValue());
        }

        if (updateRow)
        {
            clickButton("Update", 0);
            waitForElement(Locator.tagWithClass("div", "x4-grid-cell-inner").withText(statusText));
        }
    }

    @LogMethod
    protected void setupFolder()
    {
        _containerHelper.createProject(getProjectName(), "HDRL Request Portal");
    }

    @Override
    public BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("HDRL");
    }
}
