/*
 * Copyright (c) 2012-2017 LabKey Corporation
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

package org.labkey.test.tests.viscstudies;

import org.junit.experimental.categories.Category;
import org.labkey.api.reader.TabLoader;
import org.labkey.test.Locator;
import org.labkey.test.TestFileUtils;
import org.labkey.test.categories.CustomModules;
import org.labkey.test.tests.StudyBaseTest;
import org.labkey.test.util.DataRegionExportHelper;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.Ext4Helper;
import org.labkey.test.util.ListHelper;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.LoggedParam;
import org.labkey.test.util.PortalHelper;
import org.labkey.test.util.StudyHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category({CustomModules.class})
public class CAVDStudyTest extends StudyBaseTest
{
    private static final String PROJECT_NAME = "CAVDStudyTest Project";
    private static final String FOLDER_NAME = "CAVDStudyTest Folder";
    private static final String STUDY_NAME = FOLDER_NAME + " Study";
    private static final String FOLDER_NAME2 = "CAVDStudy2";
    private static final String FOLDER_NAME3 = "CAVDStudy3";
    private static final String FOLDER_NAME4 = "VerifyStudyList";
    private static final File CAVD_TEST_STUDY_ZIP = TestFileUtils.getSampleData("studies/CAVDTestStudy.folder.zip");
    private static Map<Integer, String> DATASETS = new TreeMap<>();
    private ArrayList<String> _expectedVaccineDesignText = new ArrayList<>();
    private ArrayList<String> _expectedImmunizationText = new ArrayList<>();
    private ArrayList<String> _expectedAssayDesignText = new ArrayList<>();

    private static final String[] IMMUNOGEN_TYPES = {"Canarypox", "Subunit Protein", "Fowlpox"};
    private static final String[] GENES = {"Gag", "Env", "Tat", "Nef"};
    private static final String[] SUB_TYPES = {"Clade B", "Clade C", "Clade D"};
    private static final String[] ROUTES = {"Intramuscular (IM)"};
    private static final String[] ASSAYS = {"ELISPOT", "Neutralizing Antibodies Panel 1", "ICS", "CAVDTestAssay"};
    private static final String[] LABS = {"Schmitz", "Seaman", "McElrath", "CAVDLab1"};
    private static final String[] UNITS = {"ul"};
    private static final String[] SAMPLE_TYPES = {"Platelets"};

    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("viscstudies");
    }

    @Override @LogMethod
    protected void doCreateSteps()
    {
        _containerHelper.createProject(PROJECT_NAME, "None");
        _containerHelper.createSubfolder(PROJECT_NAME, PROJECT_NAME, FOLDER_NAME, "CAVD Study", null);

        // used for doVerifyCrossContainerDatasetStatus
        configureStudy(FOLDER_NAME2);
        configureStudy(FOLDER_NAME3);
        _containerHelper.createSubfolder(PROJECT_NAME, PROJECT_NAME, FOLDER_NAME4, "Collaboration", null);
    }

    @Override @LogMethod
    protected void  doVerifySteps()
    {
        doVerifyEmptyStudy();
        doVerifyStudyDesign();
        doVerifyAssaySchedule();
        doVerifyDatasets();
        doVerifyCrossContainerDatasetStatus();
    }

    @LogMethod
    public void configureStudy(String folderName)
    {
        _containerHelper.createSubfolder(PROJECT_NAME, PROJECT_NAME, folderName, "Collaboration", null);
        importFolderFromZip(CAVD_TEST_STUDY_ZIP);
        waitForPipelineJobsToComplete(1, "Folder import", false);
    }

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    @LogMethod
    private void doVerifyEmptyStudy()
    {
        log("Verifying that the study is empty.");
        clickFolder(FOLDER_NAME);

        // Make sure a study was made.
        assertTextNotPresent("No study is active in the current container.");
        assertTextPresent(STUDY_NAME + " tracks data in");

        // Change timepoint type.
        clickAndWait(Locator.linkWithText("Edit"));
        waitForElement(Ext4Helper.Locators.radiobutton(this, "DATE"));
        _ext4Helper.selectRadioButton("DATE");

        clickButton("Submit");

        //Check to see if date is checked.
        clickAndWait(Locator.linkWithText("Edit"));
        waitForElement(Ext4Helper.Locators.radiobutton(this, "DATE"));
        assertTrue(_ext4Helper.isChecked(Ext4Helper.Locators.ext4Radio("DATE")));
    }

    @LogMethod
    private void doVerifyStudyDesign()
    {
        populateStudyDesignLookups();

        clickFolder(FOLDER_NAME);

        goToGWTStudyDesignerPanel(FOLDER_NAME, "VACCINE");
        clickEditDesign();

        addStudyDesignRow(RowType.Immunogen, "Immunogen1", IMMUNOGEN_TYPES[0], "1.5e10 Ad vg", ROUTES[0]);
        addStudyDesignRow(RowType.Immunogen, "gp120", IMMUNOGEN_TYPES[1], "1.6e8 Ad vg", ROUTES[0]);
        saveRevision();

        addStudyDesignRow(RowType.Adjuvant, "Adjuvant1", "1cc");
        finishRevision();

        goToGWTStudyDesignerPanel(FOLDER_NAME, "VACCINE");
        waitForText("Immunogens");
        assertTextPresent(_expectedVaccineDesignText);

        clickEditDesign();
        addStudyDesignRow(RowType.Immunogen, "Immunogen3", IMMUNOGEN_TYPES[2], "1.9e8 Ad vg", ROUTES[0]);
        addStudyDesignRow(RowType.Adjuvant, "Adjuvant2");
        addAntigen(1, GENES[0], SUB_TYPES[0]);
        addAntigen(2, GENES[1]);
        addAntigen(3, GENES[2], SUB_TYPES[1]);
        addAntigen(3, GENES[3], SUB_TYPES[2]);

        finishRevision();
        waitForText("Immunogens");
        assertTextPresent(_expectedVaccineDesignText);

        goToGWTStudyDesignerPanel(FOLDER_NAME, "IMMUNIZATIONS");
        clickEditDesign();
        addGroup("Vaccine", false, "1", 1);
        addGroup("Placebo", false, "2", 2);
        finishRevision();

        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Cohorts"));
        DataRegionTable.DataRegion(getDriver()).find().clickInsertNewRow();
        setFormElement(Locator.name("quf_label"), "Vaccine2");
        clickButton("Submit");
        waitForText("Placebo");
        assertTextPresentInThisOrder("Vaccine", "Placebo", "Vaccine2");

        goToGWTStudyDesignerPanel(FOLDER_NAME, "IMMUNIZATIONS");
        clickEditDesign();
        addGroup("Vaccine2", true, "3", 3);
        addTimepoint("CAVDImmTimepoint", "0", TimeUnit.Days);
        finishRevision();
        waitForText("Immunization Schedule", 4, defaultWaitForPage); // WebPart title and schedule table header
        assertTextPresent(_expectedImmunizationText);
        assertElementNotPresent(Locator.tagWithText("div", "30")); // From deleted rows

        //
        // Test 'inactive' study design option
        //
        // 1. inactivate the Canarypox type
        goToGWTStudyDesignerPanel(FOLDER_NAME, "VACCINE");
        waitForText("Adjuvants");
        clickAndWait(Locator.linkContainingText("Edit"));
        waitForText("Configure Dropdown Options");
        click(Locator.linkContainingText("Configure Dropdown Options"));
        clickAndWait(Locator.linkWithText("folder").index(0));  // configure the first type 'Immunogen Types'
        DataRegionTable dt = DataRegionTable.DataRegion(getDriver()).find();
        dt.clickEditRow(dt.getRowIndex("Name", "Canarypox"));
        click(Locator.checkboxByName("quf_Inactive"));
        clickAndWait(Locator.linkWithText("Submit"));
        // 2. verify that the Canarypox option, although inactive is still present.
        goToGWTStudyDesignerPanel(FOLDER_NAME, "VACCINE");
        clickButton("Edit", defaultWaitForPage);
        waitForElement(Locator.lkButton("Finished"));
        selectOptionByText(Locator.xpath("//select[@title='Immunogen 1 type']"), "Canarypox");
    }

    @LogMethod(quiet = true)
    private void goToGWTStudyDesignerPanel(String folderName, @LoggedParam String panel)
    {
        beginAt("/study-designer/" + getProjectName() + "/" + folderName + "/designer.view?panel=" + panel);
    }

    @LogMethod
    private void doVerifyAssaySchedule()
    {
        goToGWTStudyDesignerPanel(FOLDER_NAME, "ASSAYS");
        clickEditDesign();
        addStudyDesignRow(RowType.Assay, ASSAYS[0], LABS[0]);
        addStudyDesignRow(RowType.Assay, ASSAYS[1], LABS[1]);
        addStudyDesignRow(RowType.Assay, ASSAYS[2], LABS[2]);
        saveRevision();

        clickButton("Create Study Timepoints", 0);
        assertAlert("No timepoints are defined in the assay schedule.");
        
        addTimepoint("CAVDTestTimepoint", "13", TimeUnit.Days);
        waitForText(WAIT_FOR_JAVASCRIPT,"CAVDTestTimepoint: 13 days");

        addStudyDesignRow(RowType.Assay, ASSAYS[3], LABS[3]);

        finishRevision();
    }

    @LogMethod
    private void populateStudyDesignLookups()
    {
        // StudyDesignAssays
        goToAssayConfigureLookupValues(true, 0);
        importLookupRecords(ASSAYS);

        // StudyDesignLabs
        goToAssayConfigureLookupValues(false, 1);
        importLookupRecords(LABS);

        // StudyDesignRoutes
        goToVaccineConfigureLookupValues(true, 1);
        importLookupRecords(ROUTES);

        // StudyDesignImmunogenTypes
        goToVaccineConfigureLookupValues(false, 0);
        importLookupRecords(IMMUNOGEN_TYPES);

        // StudyDesignGenes
        goToVaccineConfigureLookupValues(true, 2);
        importLookupRecords(GENES);

        // StudyDesignSubTypes
        goToVaccineConfigureLookupValues(false, 3);
        importLookupRecords(SUB_TYPES);

        // StudyDesignUnits
        goToAssayConfigureLookupValues(true, 2);
        importLookupRecords(UNITS);

        // StudyDesignSampleTypes
        goToAssayConfigureLookupValues(false, 3);
        for (String sampleType : SAMPLE_TYPES)
        {
            DataRegionTable.findDataRegion(this).clickInsertNewRow();
            setFormElement(Locator.name("quf_Name"), sampleType);
            setFormElement(Locator.name("quf_PrimaryType"), "Blood");
            setFormElement(Locator.name("quf_ShortSampleCode"), sampleType.substring(0, 1).toUpperCase());
            clickButton("Submit");
        }
    }

    private void goToAssayConfigureLookupValues(boolean project, int index)
    {
        // project true - project
        // project false - folder
        // index 0 - Assays
        // index 1 - Labs
        // index 2 - Units
        // index 3 - SampleTypes

        String projectStr = project ? "project" : "folder";

        clickFolder(FOLDER_NAME);
        goToGWTStudyDesignerPanel(FOLDER_NAME, "ASSAYS");
        clickButton("Edit", defaultWaitForPage);
        waitAndClick(Locator.linkContainingText("Configure Dropdown Options"));
        waitAndClickAndWait(Locator.linkWithText(projectStr).index(index));
    }

    private void goToVaccineConfigureLookupValues(boolean project, int index)
    {
        // project true - project
        // project false - folder
        // index 0 - Immunogen Types
        // index 1 - Routes
        // index 2 - Genes
        // index 3 - SubTypes

        String projectStr = project ? "project" : "folder";

        clickFolder(FOLDER_NAME);
        goToGWTStudyDesignerPanel(FOLDER_NAME, "VACCINE");
        clickButton("Edit", defaultWaitForPage);
        waitAndClick(Locator.linkContainingText("Configure Dropdown Options"));
        waitAndClickAndWait(Locator.linkWithText(projectStr).index(index));
    }

    private void importLookupRecords(String... names)
    {
        DataRegionTable.findDataRegion(this).clickImportBulkData();
        StringBuilder tsvBuilder = new StringBuilder("Name\tLabel");
        for (String name : names)
        {
            tsvBuilder.append("\n");
            tsvBuilder.append(name);
            tsvBuilder.append("\t");
            tsvBuilder.append(name);
            tsvBuilder.append(" Label");
        }
        setFormElement(Locator.id("tsv3"), tsvBuilder.toString());
        clickButton("Submit");
    }

    @LogMethod
    private void doVerifyDatasets()
    {
        clickFolder(FOLDER_NAME);
        goToGWTStudyDesignerPanel(FOLDER_NAME, "ASSAYS");

        clickButton("Create Assay Datasets", 0);
        waitForAlert("Placeholder datasets created. Use Manage/Study Schedule to define datasets or link to assay data.", WAIT_FOR_JAVASCRIPT);

        clickButton("Create Study Timepoints", 0);
        waitForAlert("2 timepoints created.", WAIT_FOR_JAVASCRIPT);

        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Manage Datasets"));

        assertTextPresent("Placeholder", 4);
        assertElementPresent(Locator.linkWithText("ELISPOT"));
        assertElementPresent(Locator.linkWithText("Neutralizing Antibodies Panel 1"));
        assertElementPresent(Locator.linkWithText("ICS"));
        assertElementPresent(Locator.linkWithText("CAVDTestAssay"));

        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Manage Timepoints"));

        assertTextPresent("Study Start: 0 days", "CAVDTestTimepoint: 13 days");
//
//        addDataset();
//
//        clickAndWait(Locator.linkWithText("Overview"));
//
//        assertTabPresent("Data");
//
//        clickAndWait(Locator.linkWithText("Edit"));
//
//        waitForText("Timepoint Type");
//        assertEquals(2, getXpathCount(Locator.xpath("//input[@type='radio'][@name='TimepointType'][@disabled]")));
    }

    @LogMethod
    private void doVerifyCrossContainerDatasetStatus()
    {
        final String myStudyNameCol = "MyStudyName";
        final String studyNameCol = "Study Name";
        final String statusCol = "Dataset Status";

        // setup the dataset map (ID > Name)
        DATASETS.put(5001, "NAbTest");
        DATASETS.put(5002, "FlowTest");
        DATASETS.put(5003, "LuminexTest");
        DATASETS.put(5004, "ELISATest");

        String[][] statuses = {
                {"Draft", "/labkey/reports/icon_draft.png", "D"},
                {"Final", "/labkey/reports/icon_final.png", "F"},
                {"Locked", "/labkey/reports/icon_locked.png", "L"},
                {"Unlocked", "/labkey/reports/icon_unlocked.png", "U"}
        };

        String study2name = FOLDER_NAME2 + " Study";
        String study3name = FOLDER_NAME3 + " Study";

        log("Set study name for " + FOLDER_NAME2 + " and verify datasets exist.");
        navigateToFolder(PROJECT_NAME, FOLDER_NAME2);
        // workaround for issue 15023: go to manage views page to initialize study dataset properties
        goToManageViews();
        waitForText("Manage Views");
        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Change Study Properties"));
        waitForElement(Locator.name("Label"), WAIT_FOR_JAVASCRIPT);
        setFormElement(Locator.name("Label"), study2name);
        clickButton("Submit");
        waitForText("General Study Settings");
        assertTextPresent(study2name);
        clickAndWait(Locator.linkWithText("Manage Datasets"));
        for (Map.Entry<Integer, String> dataset : DATASETS.entrySet())
        {
            assertTextPresentInThisOrder(dataset.getKey().toString(), dataset.getValue());
        }

        log("Set study name for " + FOLDER_NAME3 + " and verify datasets exist.");
        navigateToFolder(PROJECT_NAME, FOLDER_NAME3);
        // workaround for issue 15023: go to manage views page to initialize study dataset properties
        goToManageViews();
        waitForText("Manage Views");
        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Change Study Properties"));
        waitForElement(Locator.name("Label"), WAIT_FOR_JAVASCRIPT);
        setFormElement(Locator.name("Label"), study3name);
        clickButton("Submit");
        waitForText("General Study Settings");
        assertTextPresent(study3name);
        clickAndWait(Locator.linkWithText("Manage Datasets"));
        for (Map.Entry<Integer, String> dataset : DATASETS.entrySet())
        {
            assertTextPresentInThisOrder(dataset.getKey().toString(), dataset.getValue());
        }

        log("Verify study list query from sibling folder contains studies and dataset status.");
        clickFolder(FOLDER_NAME4);
        _containerHelper.enableModule("ViscStudies");
        goToViscStudiesQuery(FOLDER_NAME4);
        assertElementPresent(Locator.linkWithText(study2name));
        assertElementPresent(Locator.linkWithText(study3name));
        for (String datasetName : DATASETS.values())
        {
            assertElementPresent(Locator.xpath("//td[text()='" + datasetName + "']"), 2);
        }
        // verify that there are no status icons to start
        for (String[] status : statuses)
        {
            assertElementNotPresent("Status icon not expected in studies query at this time.", Locator.tagWithAttribute("img", "src", status[1]));
        }

        log("Change study dataset status for " + FOLDER_NAME2 + " and verify changes in study list query.");
        navigateToFolder(PROJECT_NAME, FOLDER_NAME2);
        waitForText("Study Schedule");
        // wait for the study schedule grid to load, any dataset name will do
        waitForText(DATASETS.values().iterator().next());
        int statusCounter = 0;
        for (String dataset : DATASETS.values())
        {
            setDatasetStatus(dataset, statuses[statusCounter][0]);
            statusCounter++;
        }

        log("Verify each status icon appears once in the studies list.");
        goToViscStudiesQuery(FOLDER_NAME4);
        for (String[] status : statuses)
        {
            assertElementPresent(Locator.tagWithAttribute("img", "src", status[1]), 1);
        }

        log("Create list in " + FOLDER_NAME4 + " with lookup to the studies list query.");
        navigateToFolder(PROJECT_NAME, FOLDER_NAME4);
        new PortalHelper(this).addWebPart("Lists");
        ListHelper.ListColumn[] columns = new ListHelper.ListColumn[]{
                new ListHelper.ListColumn(myStudyNameCol, myStudyNameCol, ListHelper.ListColumnType.String, ""),
                new ListHelper.ListColumn("StudyLookup", "StudyLookup", ListHelper.ListColumnType.String, "", new ListHelper.LookupInfo(null, "viscstudies", "studies"))
        };
        _listHelper.createList(PROJECT_NAME + "/" + FOLDER_NAME4, "AllStudiesList", ListHelper.ListColumnType.AutoInteger, "Key", columns);
        clickButton("Done");

        log("Add records to list for each study.");
        navigateToFolder(PROJECT_NAME, FOLDER_NAME4);
        clickAndWait(Locator.linkWithText("AllStudiesList"));
        DataRegionTable.findDataRegion(this).clickInsertNewRow();
        setFormElement(Locator.name("quf_" + myStudyNameCol), "Something");
        selectOptionByText(Locator.name("quf_StudyLookup"), study2name);
        clickButton("Submit");
        DataRegionTable.findDataRegion(this).clickInsertNewRow();
        setFormElement(Locator.name("quf_" + myStudyNameCol), "TheOtherOne");
        selectOptionByText(Locator.name("quf_StudyLookup"), study3name);
        clickButton("Submit");

        log("Verify that the list lookup displays dataset status values.");
        navigateToFolder(PROJECT_NAME, FOLDER_NAME4);
        clickAndWait(Locator.linkWithText("AllStudiesList"));
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.removeColumn("StudyLookup");
        _customizeViewsHelper.addColumn("StudyLookup/Dataset Status");
        _customizeViewsHelper.addColumn("StudyLookup/Label");
        _customizeViewsHelper.applyCustomView();
        // verify each status icon appears once originally
        for (String[] status : statuses)
        {
            assertElementPresent(Locator.tagWithAttribute("img", "src", status[1]), 1);
        }
        log("Verify that you can navigate to study and set status from study list.");
        clickAndWait(Locator.linkWithText(study3name));
        // wait for the study schedule grid to load, any dataset name will do
        waitForText(DATASETS.values().iterator().next());
        // set the status to Locked for all of the datasets
        for (String dataset : DATASETS.values())
        {
            setDatasetStatus(dataset, "Locked");
        }
        assertElementPresent(Locator.tagWithAttribute("img", "src", "/labkey/reports/icon_locked.png"), DATASETS.size());
        clickButton("Save Changes", defaultWaitForPage);
        // verify that we are back on the list view
        assertTextPresent("AllStudiesList", statusCol);
        // locked icon should now appear once for study2 and for all datasets in study3
        assertElementPresent(Locator.tagWithAttribute("img", "src", "/labkey/reports/icon_locked.png"), DATASETS.size() + 1);

        log("Verify data status exports to text as expected.");

        DataRegionTable drt = new DataRegionTable("query", getDriver());
        DataRegionExportHelper drtHelper = new DataRegionExportHelper(drt);
        File exportTextFile = drtHelper.exportText();

        String[][] tsvData;
        try (TabLoader tsvLoader = new TabLoader(exportTextFile, true))
        {
            tsvData = tsvLoader.getFirstNLines(4);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        assertEquals("Wrong number of rows in exported study list.", 3, tsvData.length);

        String[] columnLabels = tsvData[0];
        assertEquals("Wrong columns in exported study list.",
                Arrays.asList(myStudyNameCol, studyNameCol, statusCol), Arrays.asList(columnLabels));

        log("verify first study values");
        List<String> expectedDatasetStatuses = new ArrayList<>();
        statusCounter = 0;
        for (String dataset : DATASETS.values())
        {
            expectedDatasetStatuses.add(statuses[statusCounter][2] + ": " + dataset);
            statusCounter++;
        }
        assertEquals("Wrong data for first study.", Arrays.asList("Something", study2name, String.join("\n", expectedDatasetStatuses)), Arrays.asList(tsvData[1]));

        log("verify second study values");
        expectedDatasetStatuses = new ArrayList<>();
        for (String dataset : DATASETS.values())
        {
            expectedDatasetStatuses.add("L: " + dataset);
        }
        assertEquals("Wrong data for second study.", Arrays.asList("TheOtherOne", study3name, String.join("\n", expectedDatasetStatuses)), Arrays.asList(tsvData[2]));
    }

    private void setDatasetStatus(String dataset, String status)
    {
        clickEditDatasetIcon(dataset);
        Locator.XPathLocator comboParent = Locator.xpath("//label[contains(text(), 'Status')]/../..");
        _ext4Helper.selectComboBoxItem("Status", status);
        clickButton("Save", 0);

        // verify that the status icon appears
        Locator statusLink = Locator.xpath("//div[contains(@class, 'x4-grid-cell-inner')]//div[contains(text(), '" + dataset + "')]/../../..//img[@alt='" + status + "']");
        waitForElement(statusLink, WAIT_FOR_JAVASCRIPT);
    }

    private void goToViscStudiesQuery(String folderName)
    {
        navigateToFolder(PROJECT_NAME, folderName);
        goToSchemaBrowser();
        selectQuery("viscstudies", "studies");
        waitForText("view data");
        clickAndWait(Locator.linkContainingText("view data"));
    }

    private void clickEditDatasetIcon(String dataset)
    {
        Locator editLink = Locator.xpath("//div[contains(@class, 'x4-grid-cell-inner')]//div[contains(text(), '" + dataset + "')]/../../..//span[contains(@class, 'edit-views-link')]");
        waitForElement(editLink, WAIT_FOR_JAVASCRIPT);
        click(editLink);

        _extHelper.waitForExtDialog(dataset);
    }

    private void deleteStudyDesignRow(RowType type, int row)
    {
        int count = getElementCount(Locator.xpath("//table[@id='" + type + "Grid']//div[starts-with(@title, 'Click to delete ')]"));
        click(Locator.xpath("//table[@id='"+type+"Grid']//div[starts-with(@title, 'Click to delete ')][text()='"+row+"']"));
        waitAndClick(Locator.xpath("//td[@role='menuitem'][starts-with(text(), 'Delete ')]"));
        waitForElementToDisappear(Locator.xpath("(//table[@id='"+type+"Grid']//div[starts-with(@title, 'Click to delete ')])["+count+"]"), WAIT_FOR_JAVASCRIPT);
        click(Locator.xpath("/html/body"));
    }

    private void addStudyDesignRow(RowType type, String... values)
    {
        waitForElement(Locator.lkButton("Finished"));
        int rowCount = getElementCount(Locator.xpath("//table[@id='" + type + "Grid']//div[starts-with(@title, 'Click to delete ')]"));
        int rowOffset = getElementCount(Locator.xpath("//table[@id='" + type + "Grid']/tbody/tr[not(.//input or .//select)]")) + 1;
        String tablePath = "//table[@id='"+type+"Grid']";
        assertElementPresent(Locator.xpath(tablePath));
        String rowPath = tablePath + "/tbody/tr["+(rowCount+rowOffset)+"]";
        assertElementPresent(Locator.xpath(rowPath));
        for(int i = 0; i < values.length; i++)
        {
            String cellPath = rowPath + "/td["+(i+2)+"]";
            if(isElementPresent(Locator.xpath(cellPath+"/select")))
                selectOptionByText(Locator.xpath(cellPath+"/select"), values[i]);
            else if(isElementPresent(Locator.xpath(cellPath+"/input")))
                setFormElement(Locator.xpath(cellPath+"/input"), values[i]);
            else
                fail("Non input/select cell found when adding new " + type);
            switch(type)
            {
                case Adjuvant:
                    _expectedVaccineDesignText.add(values[i]);
                    break;
                case Immunogen:
                    _expectedVaccineDesignText.add(values[i]);
                    break;
                case Immunization:
                    _expectedImmunizationText.add(values[i]);
                    break;
                case Assay:
                    _expectedAssayDesignText.add(values[i]);
                    break;
            }
        }

        waitForElement(Locator.xpath("(//div[starts-with(@title, 'Click to delete ')])["+(rowCount+1)+"]"), WAIT_FOR_JAVASCRIPT);
    }

    private void addAntigen(int immunogenNumber, String... values)
    {
        String tablePath = "(//table[starts-with(@id, 'AntigenGrid')])["+immunogenNumber+"]";
        assertElementPresent(Locator.xpath(tablePath));
        int rowCount = getElementCount(Locator.xpath(tablePath + "//div[@title='Click to delete antigen']"));
        String rowPath = tablePath + "/tbody/tr["+(rowCount+2)+"]";
        assertElementPresent(Locator.xpath(rowPath));
        for(int i = 0; i < values.length; i++)
        {
            String cellPath = rowPath + "/td["+(i+2)+"]";
            if(isElementPresent(Locator.xpath(cellPath+"/select")))
                selectOptionByText(Locator.xpath(cellPath+"/select"), values[i]);
            else if(isElementPresent(Locator.xpath(cellPath+"/input")))
                setFormElement(Locator.xpath(cellPath+"/input"), values[i]);
            else
                fail("Non input/select cell found when adding new antigen");
            _expectedVaccineDesignText.add(values[i]);
        }

        waitForElement(Locator.xpath("(//div[@title='Click to delete antigen'])["+(rowCount+1)+"]"), WAIT_FOR_JAVASCRIPT);

    }

    enum RowType
    {
        Immunogen,
        Adjuvant,
        Immunization,
        Antigen,
        Assay
    }

    enum TimeUnit
    {
        Days,
        Weeks
    }

    private void clickEditDesign()
    {
        clickButton("Edit", defaultWaitForPage * 2);
        waitForElement(Locator.lkButton("Finished"));
    }

    private void addGroup(String name, boolean cohortExists, String count, int groupCount)
    {
        click(Locator.xpath("//div[text() = 'Add New']"));
        waitForElement(Locator.id("DefineGroupDialog"));
        if (cohortExists)
        {
            checkRadioButton(Locator.name("cohortType").index(1));
            selectOptionByText(Locator.name("existName"), name);
        }
        else
        {
            setFormElement(Locator.name("newName"), name);
        }
        clickButton("OK", 0);
        waitForElementToDisappear(Locator.id("DefineGroupDialog"), WAIT_FOR_JAVASCRIPT);

        Locator loc = Locator.xpath("//table[@id='ImmunizationGrid']/tbody/tr[" + (groupCount+2) + "]/td[3]/input");
        waitForElement(loc);
        setFormElement(loc, count);
        _expectedImmunizationText.add(name);
    }

    private void addTimepoint(String name, String count, TimeUnit unit)
    {
        click(Locator.xpath("//div[text() = 'Add Timepoint']"));
        waitForElement(Locator.id("DefineTimepointDialog"));
        setFormElement(Locator.name("timepointName"), name);
        setFormElement(Locator.name("timepointCount"), count);
        selectOptionByText(Locator.name("timepointUnit"), unit.toString());
        clickButton("OK", 0);
        waitForElementToDisappear(Locator.id("DefineTimepointDialog"), WAIT_FOR_JAVASCRIPT);
    }

    private void addDataset()
    {
        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Study Schedule"));

        log("adding dataset: " + "ImportedDataset");

        clickButton("Add Dataset", 0);
        waitForElement(Locator.xpath("//span[text() = 'New Dataset']"), WAIT_FOR_JAVASCRIPT);
        setFormElement(Locator.xpath("//label[text() = 'Name:']/..//input"), "ImportedDataset");


        click(Ext4Helper.Locators.ext4Radio("Import data from file"));
        clickButton("Next");

        String datasetFileName = StudyHelper.getStudySampleDataPath() + "/datasets/plate001.tsv";
        File file = new File(TestFileUtils.getLabKeyRoot(), datasetFileName);

        if (file.exists())
        {
            Locator fileUpload = Locator.xpath("//input[@name = 'uploadFormElement']");
            waitForElement(fileUpload, WAIT_FOR_JAVASCRIPT);
            setFormElement(fileUpload, file.getAbsolutePath());

            waitForElement(Locator.xpath("//div[@class = 'gwt-HTML' and contains(text(), 'Showing first 5 rows')]"), WAIT_FOR_JAVASCRIPT);
            clickButton("Import");
        }
        else
            fail("The dataset import .tsv file (plate001.tsv) does not exist");
    }

    private int revision = 1;
    private void saveRevision()
    {
        waitForElementToDisappear(Locator.tag("a").withText("Save").withClass("labkey-disabled-button"));
        clickButton("Save", 0);
        Locator successText = Locator.tag("div").withClass("gwt-Label").withText("Revision "+(++revision)+" saved successfully.");
        if (!waitForElement(successText, WAIT_FOR_JAVASCRIPT, false))
        {
            clickButton("Save", 0); // GWT retry
        }
        waitForElement(successText);
        waitForElement(Locator.tag("a").withText("Save").withClass("labkey-disabled-button"));
    }

    private void finishRevision()
    {
        clickButton("Finished");
        revision++;
    }
}
