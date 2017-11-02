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
package org.labkey.test.tests.icemr;

import org.apache.http.HttpStatus;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.Locators;
import org.labkey.test.TestFileUtils;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.WebTestHelper;
import org.labkey.test.categories.CustomModules;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.ExcelHelper;
import org.labkey.test.util.Ext4Helper;
import org.labkey.test.util.ListHelper;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.LoggedParam;
import org.labkey.test.util.PortalHelper;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.labkey.test.util.DataRegionTable.DataRegion;

@Category({CustomModules.class})
public class ICEMRModuleTest extends BaseWebDriverTest
{
    public static final String ID = "myid";
    public static final String DIAGNOSTICS_ASSAY_DESIGN = "ICEMR Diagnostics";
    public static final String TRACKING_ASSAY_DESIGN = "ICEMR Flask Tracking";
    public static final String SPECIES_ASSAY_DESIGN = "ICEMR Species-specific PCR";
    public static final String DIAGNOSTIC_ASSAY_NAME = "Diagnostics Assay";
    public static final String ADAPTATION_ASSAY_NAME = "Culture Adaptation";
    public static final String SELECTION_ASSAY_NAME = "Drug Selection";
    public static final String SPECIES_ASSAY_NAME = "Species";
    public static final String FOLD_INCREASE_DEFAULT = "4";
    public static final String ADAPTATION_CRITERIA_DEFAULT = "2";
    public static final String ADAPTATION_FLASKS_NAME = "Adaptation Flasks";
    public static final String SELECTION_FLASKS_NAME = "Selection Flasks";
    public static final String GEL_IMAGE_FIELD = "GelImage";

    /* Sample data files */
    public static final String ADAPTATION_FLASK_FILE = "icemr/adaptFlaskFields.txt";
    public static final String GEL_IMAGE_FILE = "icemr/piggy.JPG";
    public static final String SELECTION_FLASK_FILE = "icemr/selectFlaskFields.txt";
    public static final String MISSING_COLUMNS_FILE = "icemr/missingColumns.xls";
    public static final String BAD_FLASKS_FILE = "icemr/badFlasks.xls";
    public static final String DAILY_UPLOAD_FILLED_FILE = "icemr/dailyUploadFilled.xls";

    protected static final String ICEMR_AUTHOR_USER = "maverick@labkey.test";
    protected static final String ICEMR_AUTHOR_USER_DISPLAY = "maverick";
    protected static final String ICEMR_EDITOR_USER = "goose@labkey.test";
    protected static final String ICEMR_EDITOR_USER_DISPLAY = "goose";

    protected static final String EXPERIMENT1_ID = "Exp1234";
    protected static final int EXPERIMENT1_NUM_FLASKS = 2;
    protected static final String EXPERIMENT2_ID = "Exp6789";
    protected static final int EXPERIMENT2_NUM_FLASKS = 4;

    @Override
    protected String getProjectName()
    {
        return "ICEMR assay test";
    }

    @Test
    public void testSteps()
    {
        setupAssays();
        doVerification();
    }

    @LogMethod
    private void doVerification()
    {
        testJavaScript();
        recreateSampleSets();

        // make sure we can do everything as an author
        impersonate(ICEMR_AUTHOR_USER);
        goToProjectHome();

        // test diagnostics
        enterDiagnosticsData();
        verifyDataInAssay();

        // test species
        enterSpeciesData(null);
        verifyDataInAssay();
        enterSpeciesData(GEL_IMAGE_FILE);
        verifyDataInAssay();

        // test tracking assays, ensure they work with both sample sets
        verifyTrackingAssay(ADAPTATION_ASSAY_NAME);
        goToProjectHome();
        // track drug selection flavor of tracking assay
        verifyTrackingAssay(SELECTION_ASSAY_NAME);
        goToProjectHome();

        // next test involves deleting data so go back to our admin user
        stopImpersonating();
        goToProjectHome();

        verifyTrackingIndependence();
    }

    private void recreateSampleSets()
    {
        DataRegionTable sampleSetsTable = new DataRegionTable("SampleSet", getDriver());
        sampleSetsTable.checkCheckbox(sampleSetsTable.getRowIndex("Name", SELECTION_FLASKS_NAME));
        sampleSetsTable.checkCheckbox(sampleSetsTable.getRowIndex("Name", ADAPTATION_FLASKS_NAME));
        sampleSetsTable.clickHeaderButton("Delete");
        clickButton("Confirm Delete");

        createFlasksSampleSet(ADAPTATION_FLASKS_NAME, ADAPTATION_FLASK_FILE);
        createFlasksSampleSet(SELECTION_FLASKS_NAME, SELECTION_FLASK_FILE);
    }

    @LogMethod
    private void verifyTrackingIndependence()
    {
        // issues 18050 and 18040 - verify that the adaptation and drug selection assays work
        // with only the appropriate sample set available (Adaptation Flasks for Culture Adaptation
        // or Selection Flasks for Drug Selection.  Just check adaptation in our automated tests.
        DataRegionTable samplesTable = new DataRegionTable("SampleSet", getDriver());
        samplesTable.checkCheckbox(samplesTable.getRowIndex("Name", SELECTION_FLASKS_NAME));
        samplesTable.checkCheckbox(samplesTable.getRowIndex("Name", ADAPTATION_FLASKS_NAME));
        samplesTable.clickHeaderButton("Delete");
        clickButton("Confirm Delete");

        createFlasksSampleSet(ADAPTATION_FLASKS_NAME, ADAPTATION_FLASK_FILE);
        verifyTrackingAssay(ADAPTATION_ASSAY_NAME);
        goToProjectHome();

        // recreate the selection flasks so that query validation will succeed.
        createFlasksSampleSet(SELECTION_FLASKS_NAME, SELECTION_FLASK_FILE);
    }

    @LogMethod
    private void verifyTrackingAssay(@LoggedParam String assayName)
    {
        enterDataPointTracking(assayName);
        enterDailyTrackingData();
        checkResultsPage();
        checkVisualization();
    }

    @LogMethod
    private void checkVisualization()
    {
        goBack();
        Locator.XPathLocator visButton = Locator.lkButtonContainingText("Visualization");
        waitAndClick(visButton);
        waitForText("ICEMR Visualization");
        Locator.CssLocator datapointLoc = Locator.css("svg g a.point");
        WebElement datapoint = datapointLoc.waitForElement(longWait());
        String datapointData = datapoint.getAttribute("title");
        for (String s : new String[] {"Parasitemia", EXPERIMENT1_ID+"100101", "SampleID"})
            assertTrue("Datapoint data ['" + datapointData + "'] doesn't contain ['" + s + "']", datapointData.contains(s));
    }

    @LogMethod
    private void setupUsers()
    {
        createUserWithPermissions(ICEMR_AUTHOR_USER, getProjectName(), "Author");
        clickButton("Save and Finish");
        createUserWithPermissions(ICEMR_EDITOR_USER, getProjectName(), "Editor");
        clickButton("Save and Finish");
    }

    @LogMethod
    private void setupAssays()
    {
        log("Create ICEMR assays and samplesets");
        _containerHelper.createProject(getProjectName(), "ICEMR");
        _containerHelper.enableModule(getProjectName(), "Study");

        setupUsers();

        PortalHelper ph = new PortalHelper(this);
        ph.addWebPart("Assay List");
        ph.addWebPart("Sample Sets");
        ph.addWebPart("ICEMR Upload Tests");

        _assayHelper.createAssayWithDefaults(DIAGNOSTICS_ASSAY_DESIGN, DIAGNOSTIC_ASSAY_NAME);
        _assayHelper.createAssayWithDefaults(TRACKING_ASSAY_DESIGN, ADAPTATION_ASSAY_NAME);
        _assayHelper.createAssayWithDefaults(TRACKING_ASSAY_DESIGN, SELECTION_ASSAY_NAME);
        _assayHelper.createAssayWithDefaults(SPECIES_ASSAY_DESIGN, SPECIES_ASSAY_NAME);
        createFlasksSampleSet(ADAPTATION_FLASKS_NAME, ADAPTATION_FLASK_FILE);
        createFlasksSampleSet(SELECTION_FLASKS_NAME, SELECTION_FLASK_FILE);
        goToProjectHome();
    }

    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        _userHelper.deleteUsers(false, ICEMR_AUTHOR_USER, ICEMR_EDITOR_USER);
        _containerHelper.deleteProject(getProjectName(), afterTest);
    }

    @LogMethod
    private void verifyDataInAssay()
    {
        DataRegionTable table = new DataRegionTable("Data", this);
        int row = table.getDataRowCount() - 1;
        for (Map.Entry<String, String> field : fieldAndValue.entrySet())
        {
            assertEquals("Wrong value for " + field.getKey(), field.getValue(), table.getDataAsText(row, field.getKey()).trim());
        }

        // make sure we can download the uploaded image
        if (fieldAndValue.containsKey(GEL_IMAGE_FIELD))
        {
            String src = table.link(row, GEL_IMAGE_FIELD).getAttribute("href");
            assertEquals("Bad response from uploaded image.", HttpStatus.SC_OK, WebTestHelper.getHttpResponse(src).getResponseCode());
        }

        goToProjectHome();
    }

    private void startAssayImport(String assayName)
    {
        waitAndClickAndWait(Locator.linkContainingText(assayName));
        waitAndClickAndWait(Locator.lkButtonContainingText("Import Data"));
    }

    @LogMethod
    private void enterDataPointTracking(String assayName)
    {

        if (assayName.equalsIgnoreCase(SELECTION_ASSAY_NAME))
            enterSelectionData(assayName);
        else
            enterAdaptationData(assayName);
    }

    @LogMethod
    private void prepareDay0Upload(String assayName, boolean firstUpload)
    {
        if (firstUpload)
        {
            waitAndClickAndWait(Locator.linkContainingText(assayName));
        }
        waitAndClickAndWait(Locator.lkButtonContainingText("New Experiment"));
        waitForElement(Locator.id("SampleID1"));
    }

    @LogMethod
    private void enterAdaptationData(String assayName)
    {
        prepareDay0Upload(assayName, true);
        enterAdaptationExperiment(EXPERIMENT1_ID, EXPERIMENT1_NUM_FLASKS);
        prepareDay0Upload(assayName, false);
        enterAdaptationExperiment(EXPERIMENT2_ID, EXPERIMENT2_NUM_FLASKS);
    }

    @LogMethod
    private void enterSelectionData(String assayName)
    {
        prepareDay0Upload(assayName, true);
        enterSelectionExperiment(EXPERIMENT1_ID, 2);
        prepareDay0Upload(assayName, false);
        enterSelectionExperiment(EXPERIMENT2_ID, 4);
    }

    @LogMethod
    private void enterAdaptationExperiment(String experimentId, int numFlasks)
    {
        clickButton("Submit", 0);
        verifyError(7);

        // verify our default values for fold increase and adaptation criteria are correct
        for (int i = 1; i < 4; i++)
        {
            assertFormElementEquals(Locator.name("FoldIncrease" + i + "1"), FOLD_INCREASE_DEFAULT);
        }
        assertFormElementEquals(Locator.name("AdaptationCriteria1"), ADAPTATION_CRITERIA_DEFAULT);

        fieldAndValue = new HashMap<>();

        fieldAndValue.put("PatientID", experimentId + "100101");
        fieldAndValue.put("ExperimentID", experimentId);
        fieldAndValue.put("SampleID1", experimentId + "Flask1");
        fieldAndValue.put("Scientist1", ICEMR_EDITOR_USER_DISPLAY);
        fieldAndValue.put("Gametocytemia1", "20");
        fieldAndValue.put("Hematocrit1", "24");
        fieldAndValue.put("Parasitemia1", "");
        fieldAndValue.put("SerumBatchID1", "00123");
        fieldAndValue.put("AlbumaxBatchID1", "10213");
        fieldAndValue.put("FoldIncrease11", "10");
        fieldAndValue.put("FoldIncrease21", "11");
        fieldAndValue.put("FoldIncrease31", "12");
        fieldAndValue.put("AdaptationCriteria1", "24");
        fieldAndValue.put("Comments1", "Lorem ipsum");

        for (String field : fieldAndValue.keySet())
        {
            setICEMRField(field, fieldAndValue.get(field));
        }

        //Verify a missing field (parasitemia)
        verifyError(1);

        //Verify fields out of acceptable bounds
        setICEMRField("Hematocrit1", "101");
        setICEMRField("Gametocytemia1", "101");
        setICEMRField("Parasitemia1", "101");
        verifyError(3);

        setICEMRField("Hematocrit1", "20");
        setICEMRField("Gametocytemia1", "22");
        setICEMRField("Parasitemia1", "25");

        for (int i = 2; i <= numFlasks; i++)
        {
            clickButton("Add Flask", "Flask " + String.valueOf(i));
            makeAdaptationFlask(experimentId, i);
        }

        clickButtonContainingText("Submit");
        waitForElement(Locators.bodyTitle(ADAPTATION_ASSAY_NAME + " Runs"));
    }

    @LogMethod
    private void enterSelectionExperiment(String experimentId, int numFlasks)
    {
        clickButton("Submit", 0);
        verifyError(8);

        // verify our default values for fold increase and adaptation criteria are correct
        for (int i = 1; i < 4; i++)
        {
            assertFormElementEquals(Locator.name("FoldIncrease" + i + "1"), FOLD_INCREASE_DEFAULT);
        }

        fieldAndValue = new HashMap<>();

        fieldAndValue.put("PatientID", experimentId + "100101");
        fieldAndValue.put("ExperimentID", experimentId);
        fieldAndValue.put("SampleID1", experimentId + "Flask1");
        fieldAndValue.put("Scientist1", ICEMR_EDITOR_USER_DISPLAY);

        fieldAndValue.put("InitialPopulation1", "7");
        fieldAndValue.put("Concentration1", "3");
        fieldAndValue.put("AlbumaxBatchID1", "10213");
        fieldAndValue.put("FoldIncrease11", "10");
        fieldAndValue.put("FoldIncrease21", "11");
        fieldAndValue.put("FoldIncrease31", "12");
        fieldAndValue.put("ResistanceNumber1", "2");
        fieldAndValue.put("Comments1", "Lorem ipsum");

        for (String field : fieldAndValue.keySet())
        {
            setICEMRField(field, fieldAndValue.get(field));
        }

        //Verify missing fields (MinimumParasitemia)
        verifyError(1);

        //Verify fields out of acceptable bounds
        setICEMRField("MinimumParasitemia1", "101");
        verifyError(1);

        setICEMRField("MinimumParasitemia1", ".5");

        for (int i = 2; i <= numFlasks; i++)
        {
            clickButton("Add Flask", "Flask " + String.valueOf(i));
            makeSelectionFlask(experimentId, i);
        }

        clickButtonContainingText("Submit");
        waitForElement(Locators.bodyTitle(SELECTION_ASSAY_NAME + " Runs"));
    }


    @LogMethod
    private void makeAdaptationFlask(String experimentId, int flaskNum)
    {
        clickButton("Submit", 0);
        verifyError(5);
        fieldAndValue = new HashMap<>();
        fieldAndValue.put("SampleID" + flaskNum, experimentId + "Flask" + String.valueOf(flaskNum));
        fieldAndValue.put("Scientist" + flaskNum, ICEMR_EDITOR_USER_DISPLAY);
        fieldAndValue.put("Gametocytemia" + flaskNum, "24");
        fieldAndValue.put("Hematocrit" + flaskNum, "28");
        fieldAndValue.put("Parasitemia" + flaskNum, "27");
        fieldAndValue.put("SerumBatchID"+ flaskNum, "00123");
        fieldAndValue.put("AlbumaxBatchID"+ flaskNum, "10213");
        fieldAndValue.put("FoldIncrease1"+ flaskNum, "12");
        fieldAndValue.put("FoldIncrease2"+ flaskNum, "13");
        fieldAndValue.put("FoldIncrease3"+ flaskNum, "14");
        fieldAndValue.put("AdaptationCriteria"+ flaskNum, "20");
        fieldAndValue.put("Comments"+ flaskNum, "Lorem ipsum");

        for (String field : fieldAndValue.keySet())
        {
            setICEMRField(field, fieldAndValue.get(field));
        }
        sleep(1000);
    }

    @LogMethod
    private void makeSelectionFlask(String experimentId, int flaskNum)
    {
        clickButton("Submit", 0);
        verifyError(6);

        fieldAndValue = new HashMap<>();

        fieldAndValue.put("SampleID" + flaskNum, experimentId + "Flask" + String.valueOf(flaskNum));
        fieldAndValue.put("Scientist" + flaskNum, ICEMR_EDITOR_USER_DISPLAY);
        fieldAndValue.put("SerumBatchID"+ flaskNum, "00123");
        fieldAndValue.put("AlbumaxBatchID"+ flaskNum, "10213");
        fieldAndValue.put("InitialPopulation"+ flaskNum, "7");
        fieldAndValue.put("Concentration"+ flaskNum, "3");
        fieldAndValue.put("FoldIncrease1"+ flaskNum, "10");
        fieldAndValue.put("FoldIncrease2"+ flaskNum, "11");
        fieldAndValue.put("FoldIncrease3"+ flaskNum, "12");
        fieldAndValue.put("ResistanceNumber"+ flaskNum, "2");
        fieldAndValue.put("MinimumParasitemia" + flaskNum, ".5");
        fieldAndValue.put("Comments"+ flaskNum, "Lorem ipsum");

        for (String field : fieldAndValue.keySet())
        {
            setICEMRField(field, fieldAndValue.get(field));
        }
        sleep(1000);
    }

    @LogMethod
    private void enterDailyTrackingData()
    {
        //Navigate to Daily Upload page
        checkCheckbox(Locator.checkboxByTitle("Select/unselect all on current page"));      // all individual item checkboxes have same name/title; should be first one
        clickButton("Daily Maintenance");

        String firstExp = checkTemplate();
        waitForElement(Locator.name("dailyUpload"));

        //Try to upload a form with bad columns
        setFormElement(Locator.name("dailyUpload"), TestFileUtils.getSampleData(MISSING_COLUMNS_FILE));
        clickButtonContainingText("Upload", "The data file header row does not match the daily results schema");
        _extHelper.waitForExtDialog("Daily Upload Failed");
        clickButtonContainingText("OK", "Daily Upload");
        _extHelper.waitForExtDialogToDisappear("Daily Upload Failed");

        //Try to upload a form with bad flasks (invalid IDs)
        setFormElement(Locator.name("dailyUpload"), TestFileUtils.getSampleData(BAD_FLASKS_FILE));
        completeUpload(firstExp);
        clickButtonContainingText("Submit", "Invalid flask specified");
        _extHelper.waitForExtDialog("Daily Maintenance Error");
        clickButtonContainingText("OK", "Result");
        _extHelper.waitForExtDialogToDisappear("Daily Maintenance Error");

        //Upload test
        refresh();
        waitForElement(Locator.name("dailyUpload"));
        setFormElement(Locator.name("dailyUpload"), TestFileUtils.getSampleData(DAILY_UPLOAD_FILLED_FILE));
        completeUpload(null);
        waitAndClickAndWait(Ext4Helper.Locators.ext4Button("Submit"));

        //Ensure that you can't add flasks if maintenance has been stopped on that flask.
        clickButton("Daily Maintenance");
        waitForElement(Locator.name("dailyUpload"));
        setFormElement(Locator.name("dailyUpload"), TestFileUtils.getSampleData(DAILY_UPLOAD_FILLED_FILE));
        completeUpload(null);
        clickButtonContainingText("Submit", "Invalid flask specified");
        _extHelper.waitForExtDialog("Daily Maintenance Error");
        clickButtonContainingText("OK", "Result");
        _extHelper.waitForExtDialogToDisappear("Daily Maintenance Error");
        click(Ext4Helper.Locators.ext4Button("Cancel"));
    }

    @LogMethod
    private void clickFieldSet(String experimentId)
    {
        Locator fieldSet = Locator.xpath("//fieldset").withClass("x4-fieldset-collapsed").withDescendant(Locator.xpath("//div").withClass("x4-fieldset-header-text").containing(experimentId)).append("//div/img");
        click(fieldSet);
    }

    @LogMethod
    private void completeUpload(String firstExp)
    {
        clickButtonContainingText("Upload", "Submit");
        sleep(500);

        // if only one fieldset is on the page then click it otherwise click both - this can happen
        // when testing some malformed template files
        if (null != firstExp)
        {
            clickFieldSet(firstExp);
        }
        else
        {
            clickFieldSet(EXPERIMENT1_ID);
            clickFieldSet(EXPERIMENT2_ID);
        }
    }

    @LogMethod
    private void checkTemplateFlaskHeader(Sheet sheet, int row)
    {
        List<String> expectedColumns = Arrays.asList(
                "SampleID",
                "MeasurementDate",
                "Scientist",
                "Parasitemia",
                "Gametocytemia",
                "Stage",
                "Removed",
                "RBCBatchID",
                "SerumBatchID",
                "AlbumaxBatchID",
                "GrowthFoldTestInitiated",
                "GrowthFoldTestFinished",
                "Contamination",
                "MycoTestResult",
                "FreezerProIDs",
                "FlaskMaintenanceStopped",
                "InterestingResult",
                "Comments");
        assertEquals("Wrong flask headers", expectedColumns, ExcelHelper.getRowData(sheet, row));
    }

    @LogMethod
    private int checkTemplateExperiment(Sheet sheet, int baseRow, String expId)
    {
        int row = baseRow;
        // experiment header row
        assertEquals("ExperimentID: " + expId, String.valueOf(ExcelHelper.getCell(sheet, 0, row)));
        row++;
        checkTemplateFlaskHeader(sheet, row);
        row ++;
        int numFlasks = (expId.equals(EXPERIMENT1_ID)) ? EXPERIMENT1_NUM_FLASKS : EXPERIMENT2_NUM_FLASKS;
        for (int i = 1; i <= numFlasks; i++)
        {
            assertEquals(expId + "Flask" + i, String.valueOf(ExcelHelper.getCell(sheet, 0, row)));
            row++;
        }
        // skip blank row
        row++;
        return row;
    }

    @LogMethod
    private String checkTemplate()
    {
        waitForElement(Locator.name("dailyUpload"));
        File templateFile = clickAndWaitForDownload(Ext4Helper.Locators.ext4Button("Get Template"));
        assertTrue("Wrong file name", templateFile.getName().matches("dailyUpload( ?\\()?[0-9]?\\)?\\.xls"));
        String firstExp;
        try
        {
            Workbook template = ExcelHelper.create(templateFile);
            Sheet sheet = template.getSheetAt(0);
            // sheet looks like:
            //
            // Experiment ID: <experiment id>
            // column header row
            // 1 or more flask rows
            //
            // Experimeent ID: ...

            int row = 0;
            // find the order first (it doesn't appear to be guaranteed)
            firstExp = String.valueOf(ExcelHelper.getCell(sheet, 0, row));
            if (firstExp.contains(EXPERIMENT1_ID))
            {
                firstExp = EXPERIMENT1_ID;
                row = checkTemplateExperiment(sheet, row, EXPERIMENT1_ID);
                checkTemplateExperiment(sheet,row, EXPERIMENT2_ID);
            }
            else
            {
                firstExp = EXPERIMENT2_ID;
                row = checkTemplateExperiment(sheet, row, EXPERIMENT2_ID);
                checkTemplateExperiment(sheet,row, EXPERIMENT1_ID);
            }
        }
        catch (IOException | InvalidFormatException e)
        {
            throw new RuntimeException(e);
        }

        return firstExp;
    }

    @LogMethod
    private void checkResultsPage()
    {
        Locator experimentRunsLink = Locator.linkWithText(EXPERIMENT1_ID + "100101");
        waitAndClickAndWait(experimentRunsLink);
        //Make sure the header is there and we are in the right place
        waitForText("Showing data for " + EXPERIMENT1_ID);
        //Make sure the flasks we'd expect are there
        DataRegionTable results = DataRegion(getDriver()).waitFor(getDriver());
        final String flask1 = EXPERIMENT1_ID + "Flask1";
        final String flask2 = EXPERIMENT1_ID + "Flask2";

        assertEquals("Wrong samples",
                new TreeSet<>(Arrays.asList(flask1, flask2)),
                new TreeSet<>(results.getColumnDataAsText("Sample ID")));
        waitForElement(Locator.linkWithText(flask1));
        assertElementPresent(Locator.linkWithText(flask2));

        //Hop into one of the flasks to make sure that they have data
        clickAndWait(Locator.linkWithText(flask1));
        DataRegionTable flaskSummary = DataRegion(getDriver()).waitFor(getDriver());
        assertEquals("Should only be one row in flask summary", 1, flaskSummary.getDataRowCount());
        assertEquals("Wrong data for " + flask1, Arrays.asList(EXPERIMENT1_ID + "100101", flask1, "goose"), flaskSummary.getRowDataAsText(0, "Patient ID", "Sample ID", "Scientist"));
    }

    private void verifyError(int errorCount)
    {
        waitForElementToDisappear(Locator.css(".x4-form-invalid-field").index(errorCount), WAIT_FOR_JAVASCRIPT);
        waitForElement(Locator.css(".x4-form-invalid-field").index(errorCount - 1), WAIT_FOR_JAVASCRIPT);
        assertElementPresent(Locator.id("error-div").withText("> Errors in your submission. See below."));
    }

    private Map<String, String> fieldAndValue = new HashMap<>();
    @LogMethod
    private void enterDiagnosticsData()
    {
        startAssayImport(DIAGNOSTIC_ASSAY_NAME);
        clickButton("Submit", 0);
        verifyError(12);

        fieldAndValue = new HashMap<>();

        fieldAndValue.put("Scientist", ICEMR_AUTHOR_USER_DISPLAY);
        fieldAndValue.put("ParticipantID", ID);
        fieldAndValue.put("ProcessingProtocol", "1");
        fieldAndValue.put("InitPvParasitemia", "0.3");
        fieldAndValue.put("InitPfParasitemia", "0.01");
        fieldAndValue.put("InitPmParasitemia", "0.02");
        fieldAndValue.put("ParasiteDensity", "-34"); // invalid: can't have negative number
        fieldAndValue.put("InitGametocytemia", "3.5");
        fieldAndValue.put("GametocyteDensity", "3.4"); // invalid: can't have a float for an int
        fieldAndValue.put("PatientHemoglobin", "300.4");
        fieldAndValue.put("Hematocrit", "500"); // invalid: can't have percentage > 100
        fieldAndValue.put("ThinBloodSmear", "Pv");
        fieldAndValue.put("RDT", "3.4"); //this should be ignored
        fieldAndValue.put("FreezerProID", "3.4");

        for (String field : fieldAndValue.keySet())
        {
            setICEMRField(field, fieldAndValue.get(field));
        }

        fieldAndValue.put("RDT", "Pf"); // Expected default value

        if (getFormElement(Locator.name("GametocyteDensity")).equals("3.4"))
            verifyError(3); // Some browsers allow the decimal; if so, there should be an error
        else
            assertFormElementEquals(Locator.name("GametocyteDensity"), "3"); // Other browsers round the decimal value (3.4 -> 3)
        setICEMRField("GametocyteDensity", "34"); // update value

        // we have 2 errors total, fix one at a time
        // Issue 16876: need to screen invalid entries in ICEMR module
        verifyError(2);

        // correct negative number error
        setICEMRField("ParasiteDensity", "34");
        verifyError(1);

        // correct > 100 percent error
        // the form should submit now
        setICEMRField("Hematocrit", "5.0");
        clickButton("Submit");
        waitForElement(Locators.bodyTitle(DIAGNOSTIC_ASSAY_NAME + " Results"));
    }

    @LogMethod
    private void enterSpeciesData(String fileUploadField)
    {
        startAssayImport(SPECIES_ASSAY_NAME);
        clickButton("Submit", 0);
        verifyError(7);

        fieldAndValue = new HashMap<>();
        String expId = "4321A";

        if (fileUploadField != null)
            expId = expId + "/" + GEL_IMAGE_FIELD;

        fieldAndValue.put("ExpID", expId);
        fieldAndValue.put("ParticipantID", ID);
        fieldAndValue.put("Scientist", ICEMR_AUTHOR_USER_DISPLAY);
        fieldAndValue.put("FreezerProID", "2543");
        fieldAndValue.put("Attempt", "2");
        fieldAndValue.put("Sample", "4.0");
        fieldAndValue.put("DNADilution", "20.0");
        fieldAndValue.put("PfBand", "500");

        for (String field : fieldAndValue.keySet())
        {
            setICEMRField(field, fieldAndValue.get(field));
        }

        // verify unset checkboxes default to false
        fieldAndValue.put("PvPresent", "false");
        fieldAndValue.put("PfPresent", "false");

        if (fileUploadField != null)
        {
            File f = TestFileUtils.getSampleData(fileUploadField);
            setFormElement(Locator.name(GEL_IMAGE_FIELD), f);
            // verify that a "GelImage" field exists and that its value is the file name without the path
            fieldAndValue.put(GEL_IMAGE_FIELD, f.getName());
        }

        waitForElementToDisappear(Locator.css(".x4-form-invalid-field"));
        clickButton("Submit");
        waitForElement(Locators.bodyTitle(SPECIES_ASSAY_NAME + " Results"));
    }

    @LogMethod
    private void createFlasksSampleSet(String samplesetName, String samplesetFilename)
    {
        clickProject(getProjectName());
        clickButton("Import Sample Set");
        setFormElement(Locator.id("name"), samplesetName);
        setFormElement(Locator.name("data"), "SampleID\n" + "1");
        clickButton("Submit");
        deleteSample("1");

        // now add our real fields with rich metadata
        clickButton("Edit Fields");



        ListHelper listHelper = new ListHelper(this).withEditorTitle("Field Properties");
        listHelper.deleteField("Field Properties", 0);
        clickButton("Save");
        clickButton("Edit Fields");
        String samplesetCols = TestFileUtils.getFileContents(TestFileUtils.getSampleData(samplesetFilename));
        listHelper.addFieldsNoImport(samplesetCols);

      // waitForElement(Locator.xpath("//input[@name='ff_label3']"), WAIT_FOR_JAVASCRIPT);
        // set the scientist column type to a user instead of just an int
        // this will make it be a combobox in the drop down.
        setFormElement(Locator.xpath("//input[@name='ff_type2']"), "User");
        clickButton("Save");
        clickProject(getProjectName());
    }


    @LogMethod
    private void testJavaScript()
    {
        WebElement results = Locator.id("log-info").findElement(getDriver());

        // run the test script
        clickButton("Start Test", 0);

        waitFor(() -> Locator.tag("div").startsWith("DONE:").findElementOrNull(results) != null,
                "Test did not finish!", WAIT_FOR_PAGE * 4);

        List<WebElement> errors = Locators.labkeyError.findElements(results);

        if (!errors.isEmpty())
            fail("JavaScript test failed: " + errors.get(0).getText());
    }

    @LogMethod
    private void deleteSample(String sample)
    {
        if (isTextPresent(sample))
        {
            DataRegionTable dataRegionTable = new DataRegionTable("Material", getDriver());
            dataRegionTable.checkCheckbox(dataRegionTable.getRowIndex("Sample ID", sample));
            dataRegionTable.clickHeaderButton("Delete");
            clickButton("Confirm Delete");
        }
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("icemr");
    }

    @LogMethod(quiet = true)
    protected void setICEMRField(@LoggedParam String field, @LoggedParam String value)
    {
        if (field.startsWith("Scientist"))
        {
            Locator.XPathLocator comboBox = Locator.xpath("//tr["+Locator.NOT_HIDDEN+" and ./td/label[@id='"+field+"-labelEl']]");
            waitForElement(comboBox);
            _ext4Helper.selectComboBoxItem(comboBox, Ext4Helper.TextMatchTechnique.CONTAINS, value);
        }
        else
        {
            Locator surveyFieldLoc = Locator.name(field);
            WebElement surveyField = surveyFieldLoc.waitForElement(shortWait());
            setFormElement(surveyField, value);
            fireEvent(surveyField, SeleniumEvent.blur);
        }

        fieldAndValue.put(field, value);
    }

    @Override public BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }
}
