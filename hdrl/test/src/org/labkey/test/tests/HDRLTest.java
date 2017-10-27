/*
 * Copyright (c) 2015-2017 LabKey Corporation
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.remoteapi.Command;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.Connection;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestFileUtils;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.WebTestHelper;
import org.labkey.test.categories.CustomModules;
import org.labkey.test.etl.ETLHelper;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.LoggedParam;
import org.labkey.test.util.PasswordUtil;
import org.labkey.test.util.PostgresOnlyTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@Category({CustomModules.class})
public class HDRLTest extends BaseWebDriverTest implements PostgresOnlyTest
{
    protected final File TEST_SPECIMEN_UPLOAD_FILE_1 = TestFileUtils.getSampleData("hdrl/sample_upload_01.tsv");
    protected final File TEST_SPECIMEN_UPLOAD_FILE_2 = TestFileUtils.getSampleData("hdrl/sample_upload_02.xlsx");
    protected final File CLINICAL_REPORT_FILE = TestFileUtils.getSampleData("hdrl/clinical_report.pdf");

    private static final String SUBMIT_BUTTON_TEXT = "Submit Request";
    private static final String SAVE_BUTTON_TEXT = "Save";
    private static final String PRINT_PACKING_LIST_TEXT = "Print Packing List";
    private static final String CONFIRM_SAVE_TEXT = "Do you still want to save your changes?";

    private static int CARRIER_COLUMN_INDEX = 2;
    private static int STATUS_COLUMN_INDEX = 13;

    @Override
    protected String getProjectName()
    {
        return "HDRL Verification";
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

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        super.doCleanup(afterTest);
    }

    private void addTestResultData(Map<String, String> requestData, List<Map<String, String>> specimenData)
    {
        addRequestResultData(requestData);
        addSpecimenResultData(specimenData);
    }

    private void addRequestResultData(Map<String, String> data)
    {
        Connection connection = new Connection(WebTestHelper.getBaseURL(), PasswordUtil.getUsername(), PasswordUtil.getPassword());
        Command command = new Command("hdrl", "addLabwareOutboundRequest");
        command.setParameters(data);

        try
        {
            command.execute(connection, getCurrentContainerPath());
        }
        catch (IOException | CommandException e)
        {
            throw new RuntimeException("Error posting request results", e);
        }
    }

    private void addSpecimenResultData(List<Map<String, String>> results)
    {
        Connection connection = new Connection(WebTestHelper.getBaseURL(), PasswordUtil.getUsername(), PasswordUtil.getPassword());
        for (Map<String, String> result : results)
        {
            Command command = new Command("hdrl", "addLabwareOutboundSpecimen");
            command.setParameters(result);

            try
            {
                command.execute(connection, getCurrentContainerPath());
            }
            catch (IOException | CommandException e)
            {
                throw new RuntimeException("Error posting specimen results", e);
            }
        }

    }

    @Test
    public void testRetrievalOfResultsAndArchiving()
    {
        createTestRequest();
        uploadFile(TEST_SPECIMEN_UPLOAD_FILE_2);
        log("File uploaded waiting for content");
        waitForElement(Locator.tagContainingText("div", "555-44-3333"));
        log("setting tracking number");
        doAndWaitForPageSignal(() -> setFormElement(Locator.tagWithName("input", "trackingNumber"), "testRetrievalOfResults"), "pageDirty");
        log("submitting requests");
        clickButton(SUBMIT_BUTTON_TEXT);
        DataRegionTable drt = new DataRegionTable("query", this);
        int idx = drt.getRowIndex("ShippingNumber", "testRetrievalOfResults");
        assertNotEquals(idx, -1);
        String requestId = drt.getDataAsText(idx, "RequestId");

        Map<String, String> result = new HashMap<>();
        result.put("batchId", requestId);
        result.put("dateReceived", "2015-06-01");
        result.put("dateModified", "2015-06-15");
        result.put("dateCompleted", "2015-07-01");
        result.put("hdrlStatus", "Completed");
        result.put("customerNote", "Note for id " + requestId);

        List<String> specimenIds = getSpecimenIds(requestId, "hdrl", "InboundSpecimen", "RowId");

        List<Map<String, String>> lwSpecimens = new ArrayList<>();

        Map<String, String> specimenResult1 = new HashMap<>();
        specimenResult1.put("batchId", String.valueOf(requestId));
        specimenResult1.put("testRequestId", specimenIds.get(0));
        specimenResult1.put("dateReceived", "2015-06-01");
        specimenResult1.put("dateCompleted", "2015-06-03");
        specimenResult1.put("dateModified", "2015-06-03");
        specimenResult1.put("sampleIntegrity", "Hemolyzed");
        specimenResult1.put("testResult", "HIV Negative");
        specimenResult1.put("customerCode", "5B");
        specimenResult1.put("clinicalReportFile", CLINICAL_REPORT_FILE.getAbsolutePath());
        specimenResult1.put("reportFileName", "reportFileName.pdf");
        specimenResult1.put("hdrlStatus", "Completed");
        specimenResult1.put("modifiedResultFlag", "F");
        lwSpecimens.add(specimenResult1);

        Map<String, String> specimenResult2 = new HashMap<>();
        specimenResult2.put("batchId", requestId);
        specimenResult2.put("testRequestId", specimenIds.get(1));
        specimenResult2.put("dateReceived", "2015-06-01");
        specimenResult2.put("dateModified", "2015-06-03");
        specimenResult2.put("hdrlStatus", "Exception");
        specimenResult2.put("modifiedResultFlag", "F");
        lwSpecimens.add(specimenResult2);

        addTestResultData(result, lwSpecimens); // add results for this test request

        // Run the ETL to pick up the results
        ETLHelper _etlHelper = new ETLHelper(this, getProjectName());
        _etlHelper.runETL("{HDRL}/labware");

        Map<String, String> request = new HashMap<>();
        request.put("ShippingNumber", "testRetrievalOfResults");
        result.put("requestId", requestId);
        result.put("received", "2015-06-01");
        result.put("modified", "2015-06-15");
        result.put("Status", "Received");
        result.put("CustomerNote", "Note for id " + requestId);

        // add in the request data for verification
        Map<String, String> specimen1 = new HashMap<>();
        specimen1.put("RequestId", String.valueOf(requestId));
        specimen1.put("SpecimenId", specimenIds.get(0));
        specimen1.put("Received", "2015-06-01 00:00");
        specimen1.put("Completed", "2015-06-03 00:00");
        specimen1.put("SampleIntegrity", "Hemolyzed");
        specimen1.put("TestResult", "HIV Negative");
        specimen1.put("CustomerCode", "5B");
        specimen1.put("RequestStatus", "Completed");
        specimen1.put("ModifiedResultFlag", "F");

        Map<String, String> specimen2 = new HashMap<>();

        specimen2.put("RequestId", requestId);
        specimen2.put("SpecimenId", specimenIds.get(1));
        specimen2.put("Received", "2015-06-01 00:00");
        specimen2.put("RequestStatus", "Exception");
        specimen2.put("ModifiedResultFlag", "F");

        List<Map<String, String>> specimens = new ArrayList<>();
        specimens.add(specimen1);
        specimens.add(specimen2);

        // Verify results data are shown in the grid
        verifyDataRegionRows("InboundRequest", Collections.singletonList(request), "ShippingNumber");
        verifyDataRegionRows("SpecimenResult", specimens, "SpecimenId");

        // Verify download
        goToProjectHome();
        impersonateRole("Reader");
        click(Locator.linkContainingText("View test requests"));
        drt = new DataRegionTable("query", this);
        idx = drt.getRowIndex("RequestId", requestId);
        assertNotEquals(idx, -1);
        log("view test results");
        assertEquals("VIEW", drt.getDataAsText(idx, 0));
        clickAndWait(drt.link(idx, 0));

        waitForElement(Locator.tagContainingText("td", specimenIds.get(0)));
        drt = new DataRegionTable("query", this);
        idx = drt.getRowIndex("SpecimenId", specimenIds.get(0));
        assertEquals("Download", drt.getDataAsText(idx, 0));
        assertEquals( Arrays.asList("Completed","Exception"),drt.getColumnDataAsText("Status"));
        assertEquals( Arrays.asList("F","F"),drt.getColumnDataAsText("ResultModified"));
        assertEquals( Arrays.asList("7777","8888"),drt.getColumnDataAsText("CustomerBarcode"));
        assertEquals( Arrays.asList("Johnston","Johnston"),drt.getColumnDataAsText("LastName"));
        assertEquals( Arrays.asList("Jack","Fred"),drt.getColumnDataAsText("FirstName"));
        assertEquals( Arrays.asList("Sparrow"," "),drt.getColumnDataAsText("MiddleName"));
        assertEquals( Arrays.asList("jsj"," "),drt.getColumnDataAsText("Initials"));
        assertEquals( Arrays.asList("Male","Unknown"),drt.getColumnDataAsText("Gender"));
        assertEquals( Arrays.asList("2000-12-01","1940-06-07"),drt.getColumnDataAsText("BirthDate"));
        assertEquals( Arrays.asList("222334444","555443333"),drt.getColumnDataAsText("SSN"));
        assertEquals( Arrays.asList("01","02"),drt.getColumnDataAsText("FMP"));
        assertEquals( Arrays.asList("A13","A14"),drt.getColumnDataAsText("DUC"));
        assertEquals( Arrays.asList("P","B"),drt.getColumnDataAsText("SOT"));
        assertEquals( Arrays.asList("2015-03-01","1975-03-03"),drt.getColumnDataAsText("DrawDate"));
        assertEquals( Arrays.asList("2015-06-01 00:00","2015-06-01 00:00"),drt.getColumnDataAsText("Received"));
        assertEquals( Arrays.asList("2015-06-03 00:00"," "),drt.getColumnDataAsText("Completed"));
        assertEquals( Arrays.asList("Hemolyzed"," "),drt.getColumnDataAsText("SampleIntegrity"));
        assertEquals( Arrays.asList("HIV Negative"," "),drt.getColumnDataAsText("TestResult"));
        assertEquals( Arrays.asList("5B"," "),drt.getColumnDataAsText("CustomerCode"));
        assertEquals( Arrays.asList("reportFileName.pdf"," "),drt.getColumnDataAsText("ReportFileName"));
        File report = clickAndWaitForDownload(Locator.linkWithSpan("Download"));
        assertTrue(report.getName().contains("reportFileName"));
        stopImpersonating();

        testDataDeletion();
    }

    @Test
    public void testNewRequest()
    {
        log("creating a new test request");
        goToProjectHome();
        clickAndWait(Locator.linkContainingText("Create a new test request"), defaultWaitForPage);

        // we select this even though there is only one option available because there may be other types in the future
        // Also, this test will fail without waiting for the combo box to be populated, so this is an easy way to wait
        // for that.
        _ext4Helper.selectComboBoxItem("Request Type", "HIV Screening Algorithm");
        assertElementPresent("Submit button should not be enabled if no specimen data are available", Locators.disabledSubmit, 1);

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
        clickButton(SAVE_BUTTON_TEXT);

        // verify the inserted row via the data region table
        verifyDataRegionRows("InboundSpecimen", rows, "CustomerBarcode");
    }

    @Test
    public void testFileUpload()
    {
        createTestRequest();
        uploadFile(TEST_SPECIMEN_UPLOAD_FILE_1);

        log("verify proper SSN formatting");
        waitForElement(Locator.tagContainingText("div", "222-33-4444"));

        log("verify status message update");
        waitForElement(Locator.tagContainingText("div", "Required field(s) missing: FMP, Draw Date, SSN"));
        waitForElement(Locator.tagContainingText("div", "Required field(s) missing: FMP"));
        waitForElement(Locator.tagContainingText("div", "Draw date cannot be before birth date; Required field(s) missing: SSN"));

        log("verify submit disabled when there are validation errors");
        assertElementPresent("Submit button should not be enabled if request has been submitted", Locators.disabledSubmit, 1);

        clickButton(SAVE_BUTTON_TEXT);

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
        goToProjectHome();
        clickAndWait(Locator.linkContainingText("Create a new test request"));
        _ext4Helper.selectComboBoxItem("Request Type", "HIV Screening Algorithm");
        _ext4Helper.selectComboBoxItem("Carrier", "FedEx");

        uploadFile(TEST_SPECIMEN_UPLOAD_FILE_2);

        log("verify proper SSN formatting");
        waitForElement(Locator.tagContainingText("div", "222-33-4444"));
        waitForElement(Locator.tagContainingText("div", "555-44-3333"));
        clickButton(SAVE_BUTTON_TEXT);

        List<Map<String, String>> rows = new ArrayList<>();
        Map<String, String> r1 = new HashMap<>();
        r1.put("CustomerBarcode", "7777");
        r1.put("LastName", "Johnston");
        r1.put("FirstName", "Jack");
        r1.put("MiddleName", "Sparrow");
        r1.put("Initials", "jsj");
        r1.put("Gender", "Male");
        r1.put("SSN", "222334444");
        r1.put("FMP", "01");
        r1.put("DUC", "A13");
        r1.put("SOT", "P");
        rows.add(r1);

        Map<String, String> r2 = new HashMap<>();
        r2.put("CustomerBarcode", "8888");
        r2.put("LastName", "Johnston");
        r2.put("FirstName", "Fred");
        r2.put("Gender", "Unknown");
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
        int idx = drt.getRowIndex("ShippingCarrier", "FedEx");
        assertNotEquals(idx, -1);
        clickAndWait(drt.link(idx, 0));
        log("submitting an existing request");
        waitForElement(Locator.tagContainingText("div", "222-33-4444"));
        waitForElement(Locator.tagContainingText("div", "555-44-3333"));
        clickButton(SUBMIT_BUTTON_TEXT);

        drt = new DataRegionTable("query", this);
        idx = drt.getRowIndex("ShippingCarrier", "FedEx");
        assertNotEquals(idx, -1);
        Assert.assertFalse(drt.getDataAsText(idx, "Submitted By").trim().isEmpty()); // "submitted by" field should be filled in
        Assert.assertFalse(drt.getDataAsText(idx, "Submitted").trim().isEmpty()); // submitted date should be filled in

        log("ensure submitted requests are still editable by admins");
        assertEquals("EDIT", drt.getDataAsText(idx, 0));
        clickAndWait(drt.link(idx, 0));

        waitForElement(org.labkey.test.Locators.bodyTitle("Edit a Test Request"));

        testPrintPackingList("Admin", "FedEx");

        log("verify submitted requests are readonly for non-admins");

        impersonateRole("Reader");
        goToProjectHome();
        clickAndWait(Locator.linkContainingText("View test requests"));
        drt = new DataRegionTable("query", this);
        idx = drt.getRowIndex("ShippingCarrier", "FedEx");
        assertNotEquals(idx, -1);
        log("ensure submitted requests are still editable by admins");
        assertEquals("VIEW", drt.getDataAsText(idx, 0));
        clickAndWait(drt.link(idx, 0));

        waitForElement(Locator.tagContainingText("td", "Carrier"));
        waitForElement(Locator.tagContainingText("td", "FedEx"));
        assertElementNotPresent(org.labkey.test.Locators.bodyTitle("Edit a Test Request"));

        testPrintPackingList("Reader", "FedEx");

        stopImpersonating();
    }

    @Test
    public void testEditSubmittedRequest()
    {
        log("creating a new test request by uploading a file");
        goToProjectHome();
        click(Locator.linkContainingText("Create a new test request"));
        log("upload specimen data from a .xlsx file");
        uploadFile(TEST_SPECIMEN_UPLOAD_FILE_2);

        log("submitting new test request");
        waitForElement(Locator.tagContainingText("div", "222-33-4444"));
        waitForElement(Locator.tagContainingText("div", "555-44-3333"));
        setFormElement(Locator.tagWithName("input", "trackingNumber"), "testEditSubmittedRequest");
        clickButton(SUBMIT_BUTTON_TEXT);
        assertTextNotPresent("Create a new test request"); // submit should take us back to the view test requests page

        log("Edit the submitted request as admin");
        DataRegionTable drt = new DataRegionTable("query", this);
        int idx = drt.getRowIndex("ShippingNumber", "testEditSubmittedRequest");
        assertNotEquals(idx, -1);
        assertEquals("Submitted", drt.getDataAsText(idx, STATUS_COLUMN_INDEX));
        String submittedDate = drt.getDataAsText(idx, 6).trim();

        log("ensure submitted requests are still editable by admins");
        assertEquals("EDIT", drt.getDataAsText(idx, 0));
        clickAndWait(drt.link(idx, 0));

        waitForElement(org.labkey.test.Locators.bodyTitle("Edit a Test Request"));
        assertTextPresent("This request has already been submitted");
        assertElementPresent("Submit button should not be enabled if request has been submitted", Locators.disabledSubmit, 1);

        log("Test edit and cancel save of a submitted request");
        _ext4Helper.selectComboBoxItem("Carrier","DHL");
        assertElementPresent("Save button should be enabled if request has been edited", Locators.enabledSave, 1);
        clickButton(SAVE_BUTTON_TEXT, 0);
        waitForText(CONFIRM_SAVE_TEXT);
        clickButton("No", 0);

        log("Test that not saving request does not change anything");
        clickButton("Cancel", 0); // takes you back to the view test requests page
        idx = drt.getRowIndex("ShippingNumber", "testEditSubmittedRequest");
        assertNotEquals(idx, -1);
        assertEquals("Submitted", drt.getDataAsText(idx, STATUS_COLUMN_INDEX));
        assertNotEquals("DHL", drt.getDataAsText(idx, CARRIER_COLUMN_INDEX));
        assertEquals("EDIT", drt.getDataAsText(idx, 0));

        log("Test edit and save of a submitted request");
        clickAndWait(drt.link(idx, 0));
        _ext4Helper.selectComboBoxItem("Carrier","DHL");
        assertElementPresent("Save button should be enabled if request has been edited", Locators.enabledSave, 1);
        clickButton(SAVE_BUTTON_TEXT, 0);
        waitForText(CONFIRM_SAVE_TEXT);
        clickButton("Yes");

        log("Test that saving request does not change the request status");
        idx = drt.getRowIndex("ShippingNumber", "testEditSubmittedRequest");
        assertNotEquals(idx, -1);
        assertEquals("Submitted", drt.getDataAsText(idx, STATUS_COLUMN_INDEX));
        assertEquals("DHL", drt.getDataAsText(idx, CARRIER_COLUMN_INDEX));
        // submitted date should still be the same
        assertEquals(submittedDate, drt.getDataAsText(idx, 6));
    }

    private void testPrintPackingList(String role, String shippingCarrier)
    {
        log("Begin verifying 'Print Packing List' for role: " + role);
        waitForElement(Locators.enabledPrintPackingList);
        clickButton(PRINT_PACKING_LIST_TEXT,0);

        switchToWindow(1);
        waitForElement(Locator.tag("div").withAttributeContaining("class", "barcode"));
        assertTextPresent("Total Samples: 2", shippingCarrier);
        getDriver().close();
        switchToMainWindow();

        log("Finish verifying 'Print Packing List' for role: " + role);

    }

    public void testDataDeletion()
    {
        goToAdminConsole()
                .clickSystemMaintenance();
        waitForText("Configure System Maintenance");
        click(Locator.linkWithText("HDRL Request Portal PHI Deletion"));
        switchToWindow(1);
        waitForText("HDRL Request Portal PHI Deletion complete");
        getDriver().close();
        switchToMainWindow();
        goToProjectHome();
        click(Locator.linkWithText("View test requests"));
        DataRegionTable drt = new DataRegionTable("query", this);
        int idx = drt.getRowIndex("ShippingNumber", "testRetrievalOfResults");
        assertNotEquals(idx, -1);
        String requestId = drt.getDataAsText(idx, "RequestId");
        Assert.assertEquals("Archived", drt.getDataAsText(idx, "Status"));
        List<String> specimenIds = getSpecimenIds(requestId, "hdrl", "InboundSpecimen", "RowId");
        assertTrue("There should be no specimens associated with the archived request", specimenIds.isEmpty());
        specimenIds = getSpecimenIds(requestId, "hdrl", "SpecimenResult", "SpecimenId");
        assertTrue("There should be no specimen results associated with the archived request", specimenIds.isEmpty());
        specimenIds = getSpecimenIds(requestId, "hdrl", "labwareOutboundSpecimens", "test_request_id");
        assertTrue("There should be no specimen results in the labware transfer table associated with the archived request", specimenIds.isEmpty());

    }

    @LogMethod(quiet = true)
    private void createTestRequest()
    {
        log("creating a new test request by uploading a file");
        goToProjectHome();
        clickAndWait(Locator.linkContainingText("Create a new test request"));
        _ext4Helper.selectComboBoxItem("Request Type","HIV Screening Algorithm");
    }

    @LogMethod(quiet = true)
    private void uploadFile(@LoggedParam File file)
    {
        log("upload specimen data from a file");
        setFormElement(Locator.tagWithName("input", "file"), file);
        log("clicking 'upload file'");
        log(doAndWaitForPageSignal(() -> clickButton("upload file", 0), "uploadFile"));
    }

    private List<String> getSpecimenIds(String requestId, String schema, String query, String idField)
    {
        goToSchemaBrowser();
        selectQuery(schema, query);
        waitForText("view data");
        clickAndWait(Locator.linkContainingText("view data"));
        DataRegionTable drt = new DataRegionTable("query", this);
        drt.ensureColumnPresent(idField);
        List<String> specimenIds = new ArrayList<>();
        List<Integer> targetRows = new ArrayList<>();
        List<String> requestIds = drt.getColumnDataAsText(2);
        for(int i = 0; i < requestIds.size(); i++)
        {
            if (requestIds.get(i).equals(requestId))
            targetRows.add(i);
        }
        List<String> requestCol = drt.getColumnDataAsText(15);
        for(int i : targetRows)
        {
            specimenIds.add(requestCol.get(i));
        }
        return specimenIds;
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
            int idx = drt.getRowIndex(key, expectedRow.get(key));
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

    protected void setTimeWindow()
    {
        goToAdminConsole();
        click(Locator.linkWithText("HDRL Sensitive Data"));
        setFormElement(Locator.name("timeWindowInDays"), "0");
        click(Locator.linkWithSpan("Save"));
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

    public static class Locators {
        public static final Locator.XPathLocator disabledSubmit = Locator.xpath("//a[contains(normalize-space(@class),'x4-btn-disabled')]//span[text()='" + SUBMIT_BUTTON_TEXT + "']");
        public static final Locator.XPathLocator enabledSave = Locator.xpath("//a[not(contains(normalize-space(@class), 'x4-btn-disable'))]//span[text()='" + SAVE_BUTTON_TEXT + "']");
        public static final Locator.XPathLocator enabledPrintPackingList = Locator.xpath("//a[not(contains(normalize-space(@class), 'x4-btn-disable'))]//span[text()='" + PRINT_PACKING_LIST_TEXT + "']");
    }
}
