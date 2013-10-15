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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.categories.CustomModules;
import org.labkey.test.util.ExcelHelper;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.LoggedParam;
import org.labkey.test.util.PortalHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: elvan
 * Date: 12/27/12
 * Time: 7:02 PM
 */
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
    public static final String ADAPTATION_FLASK_FILE = "sampledata/icemr/adaptFlaskFields.txt";
    public static final String ADAPTATION_FLASKS_NAME = "Adaptation Flasks";
    public static final String SELECTION_FLASK_FILE = "sampledata/icemr/selectFlaskFields.txt";
    public static final String SELECTION_FLASKS_NAME = "Selection Flasks";
    public static final String SCIENTIST = "Torruk";
    public static final String GEL_IMAGE_FIELD = "GelImage";
    public static final String GEL_IMAGE_FILE = "sampledata/icemr/piggy.JPG";

    @Override
//    @LogMethod(category = LogMethod.MethodType.SETUP)
    protected String getProjectName()
    {
        return "ICEMR assay test";
    }

    @Override
    protected void doTestSteps() throws Exception
    {
        setupAssays();
        doVerification();
    }

    @LogMethod
    private void doVerification()
    {
        testJavaScript();

        // test diagnostics
        enterDataPoint(DIAGNOSTIC_ASSAY_NAME, Locator.id("upload-diagnostic-form-body"), null);
        verifyDataInAssay();

        // test species
        enterDataPoint(SPECIES_ASSAY_NAME, Locator.id("upload-speciesSpecific-form-body"), null);
        verifyDataInAssay();
        enterDataPoint(SPECIES_ASSAY_NAME, Locator.id("upload-speciesSpecific-form-body"), GEL_IMAGE_FILE);
        verifyDataInAssay();

        // test tracking assays, ensure they work with both sample sets
        verifyTrackingAssay(ADAPTATION_ASSAY_NAME);
        goToProjectHome();
        // track drug selection flavor of tracking assay
        verifyTrackingAssay(SELECTION_ASSAY_NAME);
        goToProjectHome();

        verifyTrackingIndependence();
    }

    @LogMethod
    private void verifyTrackingIndependence()
    {
        // issues 18050 and 18040 - verify that the adaptation and drug selection assays work
        // with only the appropriate sample set available (Adaptation Flasks for Culture Adaptation
        // or Selection Flasks for Drug Selection.  Just check adaptation in our automated tests.
        deleteSample(SELECTION_FLASKS_NAME);
        deleteSample(ADAPTATION_FLASKS_NAME);

        createFlasksSampleSet(ADAPTATION_FLASKS_NAME, ADAPTATION_FLASK_FILE);
        verifyTrackingAssay(ADAPTATION_ASSAY_NAME);
        goToProjectHome();

        // recreate the selection flasks so that query validation will succeed.
        createFlasksSampleSet(SELECTION_FLASKS_NAME, SELECTION_FLASK_FILE);
    }

    @LogMethod
    private void verifyTrackingAssay(String assayName)
    {
        enterDataPointTracking(assayName);
        enterDailyTrackingData();
        checkResultsPage();
        checkVisualization();
    }

    @LogMethod(category = LogMethod.MethodType.SETUP)
    private void checkVisualization()
    {
        goBack();
        Locator.XPathLocator visButton = Locator.navButtonContainingText("Visualization");
        waitAndClick(visButton);
        waitForText("ICEMR Visualization");
        Locator.CssLocator datapoint = Locator.css("svg a");
        waitForElement(datapoint);
        String datapointData = getAttribute(datapoint, "title");
        for(String s : new String[] {"Parasitemia", "100101", "SampleID"})
            Assert.assertTrue(datapointData.contains(s));
    }

    @LogMethod
    private void setupAssays()
    {
        log("Create ICEMR assays and samplesets");
        _containerHelper.createProject(getProjectName(), "ICEMR");
        enableModule(getProjectName(), "Study");

        PortalHelper ph = new PortalHelper(this);
        ph.addWebPart("Assay List");
        ph.addWebPart("Sample Sets");

        _assayHelper.createAssayWithDefaults(DIAGNOSTICS_ASSAY_DESIGN, DIAGNOSTIC_ASSAY_NAME);
        _assayHelper.createAssayWithDefaults(TRACKING_ASSAY_DESIGN, ADAPTATION_ASSAY_NAME);
        _assayHelper.createAssayWithDefaults(TRACKING_ASSAY_DESIGN, SELECTION_ASSAY_NAME);
        _assayHelper.createAssayWithDefaults(SPECIES_ASSAY_DESIGN, SPECIES_ASSAY_NAME);
        createFlasksSampleSet(ADAPTATION_FLASKS_NAME, ADAPTATION_FLASK_FILE);
        createFlasksSampleSet(SELECTION_FLASKS_NAME, SELECTION_FLASK_FILE);
    }

    @LogMethod
    private void verifyDataInAssay()
    {
        waitForElement(Locator.id("dataregion_Data"));
        for (String value: fieldAndValue.values())
        {
            assertElementPresent(Locator.css("#dataregion_Data td").withText(value));
        }

        // make sure we can download the uploaded image
        if (fieldAndValue.containsKey(GEL_IMAGE_FIELD))
        {
            Locator.XPathLocator link = Locator.linkContainingText(fieldAndValue.get(GEL_IMAGE_FIELD));
            waitAndClick(link);
            goBack();
        }

        goToProjectHome();
    }

    private void enterDataPoint(String assayName, Locator.IdLocator locator, String fileUploadField)
    {
        Locator.XPathLocator link = Locator.linkContainingText(assayName);
        waitAndClick(link);
        link = Locator.navButtonContainingText("Import Data");
        waitAndClick(link);
        waitForElement(locator);
        enterData(assayName, fileUploadField);
    }

    private void enterDataPointTracking(String assayName)
    {
        Locator.XPathLocator link = Locator.linkContainingText(assayName);
        waitAndClick(link);
        link = Locator.navButtonContainingText("New Experiment");
        waitAndClick(link);
        waitForElement(Locator.id("SampleID1"));

        if (assayName.equalsIgnoreCase(SELECTION_ASSAY_NAME))
            enterSelectionData();
        else
            enterAdaptationData();
    }

    @LogMethod
    private void enterAdaptationData()
    {
        verifyError(7);

        // verify our default values for fold increase and adaptation criteria are correct
        for (int i = 1; i < 4; i++)
        {
            assertFormElementEquals(Locator.name("FoldIncrease" + i + "1"), FOLD_INCREASE_DEFAULT);
        }
        assertFormElementEquals(Locator.name("AdaptationCriteria1"), ADAPTATION_CRITERIA_DEFAULT);

        fieldAndValue = new HashMap<>();

        fieldAndValue.put("PatientID", "100101");
        fieldAndValue.put("ExperimentID", "12345");
        fieldAndValue.put("SampleID1", "15243");
        fieldAndValue.put("Scientist1", "Dr. Helvetica");
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

        for(String field : fieldAndValue.keySet())
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

        clickButton("Add Flask", "Flask 2");

        makeAdaptationFlask(2);

        clickButtonContainingText("Submit");
        waitForElement(Locator.css(".labkey-nav-page-header").withText(ADAPTATION_ASSAY_NAME + " Runs"));
    }

    @LogMethod
    private void enterSelectionData()
    {
        verifyError(8);

        // verify our default values for fold increase and adaptation criteria are correct
        for (int i = 1; i < 4; i++)
        {
            assertFormElementEquals(Locator.name("FoldIncrease" + i + "1"), FOLD_INCREASE_DEFAULT);
        }

        fieldAndValue = new HashMap<>();

        fieldAndValue.put("PatientID", "100101");
        fieldAndValue.put("ExperimentID", "12345");
        fieldAndValue.put("SampleID1", "15243");
        fieldAndValue.put("Scientist1", "Dr. Helvetica");

        fieldAndValue.put("InitialPopulation1", "7");
        fieldAndValue.put("Concentration1", "3");
        fieldAndValue.put("AlbumaxBatchID1", "10213");
        fieldAndValue.put("FoldIncrease11", "10");
        fieldAndValue.put("FoldIncrease21", "11");
        fieldAndValue.put("FoldIncrease31", "12");
        fieldAndValue.put("ResistanceNumber1", "2");
        fieldAndValue.put("Comments1", "Lorem ipsum");

        for(String field : fieldAndValue.keySet())
        {
            setICEMRField(field, fieldAndValue.get(field));
        }

        //Verify missing fields (MinimumParasitemia)
        verifyError(1);

        //Verify fields out of acceptable bounds
        setICEMRField("MinimumParasitemia1", "101");
        verifyError(1);

        setICEMRField("MinimumParasitemia1", ".5");

        clickButton("Add Flask", "Flask 2");

        makeSelectionFlask(2);

        clickButtonContainingText("Submit");
        waitForElement(Locator.css(".labkey-nav-page-header").withText(SELECTION_ASSAY_NAME + " Runs"));
    }


    @LogMethod
    private void makeAdaptationFlask(int flaskNum)
    {
        verifyError(5);

        fieldAndValue = new HashMap<>();

        fieldAndValue.put("SampleID" + flaskNum, "15258");
        fieldAndValue.put("Scientist" + flaskNum, "Dr. Helvetica");
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

        for(String field : fieldAndValue.keySet())
        {
            setICEMRField(field, fieldAndValue.get(field));
        }
        sleep(1000);
    }

    @LogMethod
    private void makeSelectionFlask(int flaskNum)
    {
        verifyError(6);

        fieldAndValue = new HashMap<>();

        fieldAndValue.put("SampleID" + flaskNum, "15258");
        fieldAndValue.put("Scientist" + flaskNum, "Dr. Helvetica");
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

        for(String field : fieldAndValue.keySet())
        {
            setICEMRField(field, fieldAndValue.get(field));
        }
        sleep(1000);
    }


    @LogMethod
    private void enterDailyTrackingData()
    {
        //Navigate to Daily Upload page
        Locator.XPathLocator link = Locator.linkContainingText("Daily Maintenance");
        waitAndClick(link);

        checkTemplate();
        waitForElement(Locator.name("dailyUpload"));

        //Try to upload a form with bad columns
        setFormElement(Locator.name("dailyUpload"), new File(getLabKeyRoot(), "sampledata/icemr/missingColumns.xls"));
        clickButtonContainingText("Upload", "The data file header row does not match the daily results schema");
        _extHelper.waitForExtDialog("Daily Upload Failed");
        clickButtonContainingText("OK", "Daily Upload");
        _extHelper.waitForExtDialogToDisappear("Daily Upload Failed");

        //Try to upload a form with bad flasks (invalid IDs)
        setFormElement(Locator.name("dailyUpload"), new File(getLabKeyRoot(), "sampledata/icemr/badFlasks.xls"));
        clickButtonContainingText("Upload", "Submit");
        sleep(500);
        clickButtonContainingText("Submit", "Invalid flask specified");
        _extHelper.waitForExtDialog("Daily Maintenance Error");
        clickButtonContainingText("OK", "Result");
        _extHelper.waitForExtDialogToDisappear("Daily Maintenance Error");

        //Upload test
        refresh();
        waitForElement(Locator.name("dailyUpload"));
        setFormElement(Locator.name("dailyUpload"), new File(getLabKeyRoot(), "sampledata/icemr/dailyUploadFilled.xls"));
        clickButtonContainingText("Upload", "Scientist Name");
        sleep(500);
        waitAndClick(Locator.ext4Button("Submit"));

        //Ensure that you can't add flasks if maintenance has been stopped on that flask.
        waitAndClick(link);
        waitForElement(Locator.name("dailyUpload"));
        setFormElement(Locator.name("dailyUpload"), new File(getLabKeyRoot(), "sampledata/icemr/dailyUploadFilled.xls"));
        clickButtonContainingText("Upload", "Scientist Name");
        sleep(500);
        clickButtonContainingText("Submit", "Invalid flask specified");
        _extHelper.waitForExtDialog("Daily Maintenance Error");
        clickButtonContainingText("OK", "Result");
        _extHelper.waitForExtDialogToDisappear("Daily Maintenance Error");
        click(Locator.ext4Button("Cancel"));
    }

    @LogMethod
    private void checkTemplate()
    {
        waitForElement(Locator.name("dailyUpload"));
        clickButtonContainingText("Get Template", "Daily Upload");
        File templateFile = new File(getDownloadDir(), "dailyUpload.xls");
        try
        {
            Workbook template = ExcelHelper.create(templateFile);
            Sheet sheet = template.getSheetAt(0);
            //Warnings about possible null pointers can be ignored, as all cells in question are tested for null before loading them.
            if(sheet != null){
                for(int i = 0; i < 18; i++)
                {
                    Assert.assertNotNull(ExcelHelper.getCell(sheet, i, 0));
                }
                Assert.assertEquals(ExcelHelper.getCell(sheet, 0, 0).toString(), "SampleID");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 1, 0).toString(), "MeasurementDate");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 2, 0).toString(), "Scientist");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 3, 0).toString(), "Parasitemia");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 4, 0).toString(), "Gametocytemia");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 5, 0).toString(), "Stage");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 6, 0).toString(), "Removed");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 7, 0).toString(), "RBCBatchID");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 8, 0).toString(), "SerumBatchID");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 9, 0).toString(), "AlbumaxBatchID");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 10, 0).toString(), "GrowthFoldTestInitiated");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 11, 0).toString(), "GrowthFoldTestFinished");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 12, 0).toString(), "Contamination");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 13, 0).toString(), "MycoTestResult");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 14, 0).toString(), "FreezerProIDs");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 15, 0).toString(), "FlaskMaintenanceStopped");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 16, 0).toString(), "InterestingResult");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 17, 0).toString(), "Comments");


                Assert.assertNotNull(ExcelHelper.getCell(sheet, 0, 1));
                Assert.assertNotNull(ExcelHelper.getCell(sheet, 0, 2));
                Assert.assertEquals(ExcelHelper.getCell(sheet, 0, 1).toString(), "15243");
                Assert.assertEquals(ExcelHelper.getCell(sheet, 0, 2).toString(), "15258");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("IOException creating the template file", e);
        }
        catch (InvalidFormatException e)
        {
            throw new RuntimeException("Template file has invalid format.", e);
        }
    }

    @LogMethod
    private void checkResultsPage(){
        Locator.XPathLocator link = Locator.xpath("//a[text()='100101']");
        waitAndClick(link);
        //Make sure the header is there and we are in the right place
        waitForText("12345");
        //Make sure the flasks we'd expect are there
        waitForText("15243");
        waitForText("15258");
        //Hop into one of the flasks to make sure that they have data
        link = Locator.xpath("//a[text()='15243']");
        waitAndClick(link);
        waitForText("100101");
        waitForText("15243");
    }

    private void verifyError(int errorCount)
    {
        clickButton("Submit", 0);
        waitForElementToDisappear(Locator.css(".x4-form-invalid-field").index(errorCount), WAIT_FOR_JAVASCRIPT);
        waitForElement(Locator.css(".x4-form-invalid-field").index(errorCount - 1), WAIT_FOR_JAVASCRIPT);
        assertElementPresent(Locator.id("error-div").withText("> Errors in your submission. See below."));
    }

    private Map<String, String> fieldAndValue = new HashMap<>();
    @LogMethod
    private void enterDiagnosticsData()
    {
        verifyError(10);

        fieldAndValue = new HashMap<>();

        fieldAndValue.put("Scientist", SCIENTIST);
        fieldAndValue.put("ParticipantID", ID);
        fieldAndValue.put("ProcessingProtocol", "1");
        fieldAndValue.put("InitParasitemia", "0.3");
        fieldAndValue.put("ParasiteDensity", "-34"); // invalid: can't have negative number
        fieldAndValue.put("InitGametocytemia", "3.5");
        fieldAndValue.put("GametocyteDensity", "3.4"); // invalid: can't have a float for an int
        fieldAndValue.put("PatientHemoglobin", "300.4");
        fieldAndValue.put("Hematocrit", "500"); // invalid: can't have percentage > 100
//        fieldAndValue.put("thinbloodsmear", "3.4");
        fieldAndValue.put("RDT", "3.4"); //this should be ignored
        fieldAndValue.put("FreezerProID", "3.4");

        for(String field : fieldAndValue.keySet())
        {
            setICEMRField(field, fieldAndValue.get(field));
        }

        // Issue 16875: decimals in certain icemr module fields causes js exception
        assertFormElementEquals(Locator.name("GametocyteDensity"), "3.4"); // '.' can't be entered, The value must be a positive integer
        fieldAndValue.put("GametocyteDensity", "34"); // update value

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
        waitForElement(Locator.css(".labkey-nav-page-header").withText(DIAGNOSTIC_ASSAY_NAME + " Results"));
    }

    @LogMethod
    private void enterSpeciesData(String fileUploadField)
    {
        verifyError(7);

        fieldAndValue = new HashMap<>();
        String expId = "4321A";

        if (fileUploadField != null)
            expId = expId + "/" + GEL_IMAGE_FIELD;

        fieldAndValue.put("ExpID", expId);
        fieldAndValue.put("ParticipantID", ID);
        fieldAndValue.put("Scientist", SCIENTIST);
        fieldAndValue.put("FreezerProID", "2543");
        fieldAndValue.put("Attempt", "2");
        fieldAndValue.put("Sample", "4.0");
        fieldAndValue.put("DNADilution", "20.0");
        fieldAndValue.put("PfBand", "500");

        for(String field : fieldAndValue.keySet())
        {
            setICEMRField(field, fieldAndValue.get(field));
        }

        // verify unset checkboxes default to false
        fieldAndValue.put("PvPresent", "false");
        fieldAndValue.put("PfPresent", "false");

        if (fileUploadField != null)
        {
            File f = new File(getLabKeyRoot(), fileUploadField);
            setFormElement(Locator.name(GEL_IMAGE_FIELD), f);
            // verify that a "GelImage" field exists and that its value is the file name without the path
            fieldAndValue.put(GEL_IMAGE_FIELD, f.getName());
        }

        waitForElementToDisappear(Locator.css(".x4-form-invalid-field"));
        clickButton("Submit");
        waitForElement(Locator.css(".labkey-nav-page-header").withText(SPECIES_ASSAY_NAME + " Results"));
    }

    private void enterData(String assayName, String fileUploadField)
    {
        if (assayName.equals(DIAGNOSTIC_ASSAY_NAME))
            enterDiagnosticsData();
        else
            enterSpeciesData(fileUploadField);
    }

    @LogMethod
    private void createFlasksSampleSet(String samplesetName, String samplesetFilename)
    {
        clickProject(getProjectName());
        clickButton("Import Sample Set");
        setFormElement("name", samplesetName);
        setFormElement("data", "SampleID\n" + "1");
        clickButton("Submit");
        deleteSample("1");

        // now add our real fields with rich metadata
        clickButton("Edit Fields");
        waitAndClickButton("Import Fields", 0);
        waitForElement(Locator.xpath("//textarea[@id='schemaImportBox']"), WAIT_FOR_JAVASCRIPT);
        String samplesetCols = getFileContents(new File(getLabKeyRoot(), samplesetFilename));
        setFormElement("schemaImportBox", samplesetCols);

        clickButton("Import", 0);
        waitForElement(Locator.xpath("//input[@name='ff_label3']"), WAIT_FOR_JAVASCRIPT);
        clickButton("Save");
        clickProject(getProjectName());
    }

    @LogMethod
    private void testJavaScript()
    {
        PortalHelper ph = new PortalHelper(this);
        ph.addWebPart("ICEMR Upload Tests");
        // run the test script
        clickButton("Start Test", 0);

        waitFor(new Checker()
        {
            @Override
            public boolean check()
            {
                String s = Locator.id("log-info").findElement(getDriver()).getText();
                return s.contains("DONE:");
            }
        }, "Test did not finish!", WAIT_FOR_PAGE * 2);

        Assert.assertFalse("At least one of the javascript tests failed", Locator.id("log-info").findElement(getDriver()).getText().contains("FAILED"));
    }

    @LogMethod
    private void deleteSample(String sample)
    {
        if (isTextPresent(sample))
        {
            checkCheckbox(Locator.xpath("//td/a[contains(text(), '" + sample + "')]/../../td/input"));
            clickButton("Delete");
            clickButton("Confirm Delete");
        }
    }

    @Override
    public String getAssociatedModuleDirectory()
    {
        return "server/customModules/icemr";
    }

    @LogMethod(quiet = true)
    protected void setICEMRField(@LoggedParam String field, @LoggedParam String value)
    {
        setFormElement(Locator.name(field), value);
        fieldAndValue.put(field, value);
    }
}
