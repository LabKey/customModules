/*
 * Copyright (c) 2014-2019 LabKey Corporation
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
import org.junit.experimental.categories.Category;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestFileUtils;
import org.labkey.test.categories.Daily;
import org.labkey.test.categories.Specimen;
import org.labkey.test.components.CustomizeView;
import org.labkey.test.components.DomainDesignerPage;
import org.labkey.test.params.FieldDefinition;
import org.labkey.test.params.FieldDefinition.ColumnType;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.StudyHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Category({Daily.class, Specimen.class})
@BaseWebDriverTest.ClassTimeout(minutes = 7)
public class SpecimenCustomizeTest extends SpecimenBaseTest
{
    protected static final String PROJECT_NAME = "SpecimenCustomizeProject";
    protected static final File SPECIMEN_ARCHIVE = StudyHelper.getSpecimenArchiveFile("Rollup.specimens");
    protected static final String SPECIMEN_AVAILABLE_REASON = "This vial's availability status was set by an administrator. Please contact an administrator for more information.";
    protected static final String SPECIMEN_UNAVAILABLE_REASON = "This vial is unavailable because it is not currently held by a repository.";

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    protected void doCreateSteps()
    {
        initializeFolder();
        importFolderFromZip(TestFileUtils.getSampleData("studies/SpecimenCustomizeStudy.folder.zip"));
    }

    @Override
    @LogMethod
    protected void doVerifySteps()
    {
        configureSpecimenProperties();

        setPipelineRoot(StudyHelper.getStudySubfolderPath());
        startSpecimenImport(2, SPECIMEN_ARCHIVE);
        waitForSpecimenImport();

        addFieldsToSpecimenView();

        verifySpecimenDetailContents();
    }

    private void configureSpecimenProperties()
    {
        goToManageStudy();
        clickAndWait(Locator.linkContainingText("Edit Specimen Event fields"));

        DomainDesignerPage designerPage = new DomainDesignerPage(getDriver());
        designerPage.fieldsPanel().addField(new FieldDefinition("Tally", ColumnType.Integer));
        // Set a couple import aliases to test them below: Issue 39672
        designerPage.fieldsPanel().addField(new FieldDefinition("Note", ColumnType.String).setImportAliases("Comment,Notables"));
        designerPage.fieldsPanel().addField(new FieldDefinition("Minutes", ColumnType.Decimal));
        designerPage.fieldsPanel().addField(new FieldDefinition("Flag", ColumnType.Boolean));
        designerPage.clickFinish();

        clickAndWait(Locator.linkContainingText("Edit Vial fields"));
        designerPage = new DomainDesignerPage(getDriver());
        designerPage.fieldsPanel().addField(new FieldDefinition("Tally", ColumnType.Integer));
        designerPage.fieldsPanel().addField(new FieldDefinition("FirstTally", ColumnType.Integer));
        designerPage.fieldsPanel().addField(new FieldDefinition("LatestTally", ColumnType.Integer));
        designerPage.fieldsPanel().addField(new FieldDefinition("LatestNonBlankTally", ColumnType.Integer));
        designerPage.fieldsPanel().addField(new FieldDefinition("CombineTally", ColumnType.Integer));
        designerPage.fieldsPanel().addField(new FieldDefinition("FirstNote", ColumnType.String));
        designerPage.fieldsPanel().addField(new FieldDefinition("LatestNote", ColumnType.String));
        designerPage.fieldsPanel().addField(new FieldDefinition("LatestNonBlankNote", ColumnType.String));
        designerPage.fieldsPanel().addField(new FieldDefinition("CombineNote", ColumnType.String));
        designerPage.fieldsPanel().addField(new FieldDefinition("FirstMinutes", ColumnType.Decimal));
        designerPage.fieldsPanel().addField(new FieldDefinition("LatestMinutes", ColumnType.Decimal));
        designerPage.fieldsPanel().addField(new FieldDefinition("LatestNonBlankMinutes", ColumnType.Decimal));
        designerPage.fieldsPanel().addField(new FieldDefinition("CombineMinutes", ColumnType.Decimal).setFormat("0.####"));
        designerPage.fieldsPanel().addField(new FieldDefinition("FirstFlag", ColumnType.Boolean));
        designerPage.fieldsPanel().addField(new FieldDefinition("LatestFlag", ColumnType.Boolean));
        designerPage.fieldsPanel().addField(new FieldDefinition("LatestNonBlankFlag", ColumnType.Boolean));
        designerPage.fieldsPanel().addField(new FieldDefinition("LatestDrawTimestamp", ColumnType.DateAndTime));
        designerPage.fieldsPanel().addField(new FieldDefinition("FirstDrawTimestamp", ColumnType.DateAndTime));
        designerPage.clickFinish();

        clickAndWait(Locator.linkContainingText("Edit Specimen fields"));
        designerPage = new DomainDesignerPage(getDriver());
        designerPage.fieldsPanel().addField(new FieldDefinition("TotalLatestNonBlankTally", ColumnType.Integer));
        designerPage.fieldsPanel().addField(new FieldDefinition("SumOfLatestNonBlankMinutes", ColumnType.Decimal).setFormat("0.####"));
        designerPage.fieldsPanel().addField(new FieldDefinition("SumOfCombineMinutes", ColumnType.Decimal).setFormat("0.####"));
        designerPage.fieldsPanel().addField(new FieldDefinition("CountLatestNonBlankFlag", ColumnType.Integer));
        designerPage.fieldsPanel().addField(new FieldDefinition("MaxAvailabilityReason", ColumnType.String));
        designerPage.fieldsPanel().addField(new FieldDefinition("MinAvailabilityReason", ColumnType.String));
        designerPage.clickFinish();
    }

    private ArrayList<PropertyRow> getRowsFromTable(WebElement table)
    {
        ArrayList<PropertyRow> rows = new ArrayList<>();
        int i = 1;
        for(WebElement row : table.findElements(By.xpath(".//tr")))
        {
            rows.add(new PropertyRow(row, i));
            i++;
        }
        return rows;
    }

    class SpecimenPropertyTable
    {
        List<PropertyRow> _propertyRows;
        String _label;

        public SpecimenPropertyTable(String label)
        {
            _label = label;
            _propertyRows = new ArrayList<>();
            String xPath = "//td[.='" + _label + "']/../..//table[@class='labkey-pad-cells']";
            Locator locator = Locator.xpath(xPath);
            WebElement table = locator.findElement(getDriver());
            int i = 1;
            for(WebElement row : table.findElements(By.xpath(".//tr")))
            {
                _propertyRows.add(new PropertyRow(row, i));
                i++;
            }
        }

        public void refresh()
        {
            _propertyRows = new ArrayList<>();
            String xPath = "//td[.='" + _label + "']/../..//table[@class='labkey-pad-cells']";
            Locator locator = Locator.xpath(xPath);
            waitForElement(locator);
            WebElement table = locator.findElement(getDriver());
            int i = 1;
            for(WebElement row : table.findElements(By.xpath(".//tr")))
            {
                _propertyRows.add(new PropertyRow(row, i));
                i++;
            }
        }

        public PropertyRow getPropertyRowByName(String name)
        {
            PropertyRow theRow = null;
            for(int i = 1; i <= _propertyRows.size(); i++)
            {
                if(name.equals(_propertyRows.get(i).getName()))
                {
                    theRow = _propertyRows.get(i);
                    return theRow;
                }
            }
            return theRow;
        }

        public PropertyRow getPropertyRowByLabel(String label)
        {
            PropertyRow theRow = null;
            for(int i = 1; i < _propertyRows.size(); i++)
            {
                if(label.equals(_propertyRows.get(i).getLabel()))
                {
                    theRow = _propertyRows.get(i);
                }
            }
            return theRow;
        }
    }

    //wrapper for <tr> element in specimen tables
    private class PropertyRow
    {
        protected WebElement _tr;
        protected int _index;

        public PropertyRow(WebElement tr, int index)
        {
         _tr = tr;
        }

        private String getEditableValue()
        {
            String value = _tr.findElement(By.xpath(".//div//input")).getAttribute("value");
            return _tr.findElement(By.xpath(".//div//input")).getAttribute("value");

        }

        public String getName()
        {
            WebElement nameElement = _tr.findElement(By.xpath(".//td[6]/div"));
            if(isEditable())
            {
                return getEditableValue();
            }
            else
            {
                return nameElement.getText();
            }
        }

        public String getLabel()
        {
            WebElement labelElement = _tr.findElement(By.xpath(".//td[7]/div"));
            if(isEditable())
            {
                return getEditableValue();
            }
            else
            {
                return labelElement.getText();
            }
        }

        public String getType()
        {
            WebElement typeElement = _tr.findElement(By.xpath(".//td[8]/div"));
            if(isEditable())
            {
                return getEditableValue();
            }
            else
            {
                return typeElement.getAttribute("value");
            }
        }

        public Boolean isEditable()
        {
            return _tr.findElements(By.xpath(".//div[contains(@id,'name')]/input")).size() > 0;
        }

        public void editName(String value)
        {
            //WebElement editableNameElement = _tr.findElement(By.xpath(".//div[contains(@id,'name')]/input"));
            WebElement editableNameElement = _tr.findElement(By.xpath(".//td[6]/div/input"));
            editableNameElement.click();
            editableNameElement.sendKeys(value);
        }

        public void editLabel(String value)
        {
            _tr.findElement(By.xpath(".//div[contains(@id,'label')]/input")).sendKeys(value);
        }

        public void editType(String value)
        {
            _tr.findElement(By.xpath(".//div[contains(@id,'auto')]/div[contains(@class,'trigger')]")).click();
            waitForElement(Locator.xpath("//div[@class=' x-window x-component ']"));
            click(Locator.xpath("//label[text()='" + value + "']"));
            click(Locator.buttonContainingText("Apply"));
        }

        public void delete()
        {
            _tr.findElement(By.xpath("//div[contains(@id, 'partdelete']/input")).click();
        }
    }

    public void setFormat(String where, String value)
    {
        String prefix = "//td[text() = '" + where + "']/../..";
        click(Locator.xpath(prefix + "//span[contains(@class,'x-tab-strip-text') and text()='Format']"));
        Locator formatLoc = Locator.tagWithId("input", "propertyFormat");
        setFormElement(formatLoc, value);
    }

    private void addFieldsToSpecimenView()
    {
        clickTab("Specimen Data");
        shortWait().until(ExpectedConditions.elementToBeClickable(Locator.linkWithText("By Individual Vial")));
        click(Locator.linkWithText("By Individual Vial"));
        waitForText("Vials");
        DataRegionTable specimenTable = new DataRegionTable("SpecimenDetail", this);
        CustomizeView specimenTableCustomizer = specimenTable.getCustomizeView();
        specimenTableCustomizer.openCustomizeViewPanel();
        waitForText("Available Fields");
        specimenTableCustomizer.addColumn("MaxAvailabilityReason");
        specimenTableCustomizer.addColumn("MinAvailabilityReason");
        specimenTableCustomizer.addColumn("LatestDrawTimestamp");
        specimenTableCustomizer.addColumn("FirstDrawTimestamp");
        specimenTableCustomizer.saveDefaultView();
    }

    private void importSpecimenArchives()
    {
        startSpecimenImport(2, SPECIMEN_ARCHIVE);
        waitForSpecimenImport();
    }

    private void verifySpecimenDetailContents()
    {
        clickTab("Specimen Data");
        waitAndClickAndWait(Locator.linkWithText("By Individual Vial"));
        DataRegionTable table = new DataRegionTable("SpecimenDetail", this);

        // This tests import aliases, since the "Note" column in Rollup.specimens has the column header "Notables": Issue 39672
        List<String> actual = table.getColumnDataAsText("CombineNote");
        List<String> expected = Arrays.asList("novials1002", "novials1001, novials1003", " ", " ", " ", " ", " ",
                "unavailable2", "unavailable1, unavailable22", " ", "note1, note101", "note2", " ", " ", " ", " ", " ");
        Assert.assertEquals("CombineNote values incorrect", expected, actual);

        actual = table.getColumnDataAsText("CombineTally");
        expected = Arrays.asList("1002", "2004", " ", " ", " ", " ", " ",
                "12", "33", " ", "102", "2", " ", " ", " ", " ", " ");
        Assert.assertEquals("CombineTally values incorrect", expected, actual);

        actual = table.getColumnDataAsText("SumOfLatestNonBlankMinutes");
        expected = Arrays.asList("100.2", "100.3", " ", " ", " ", " ", "47.12",
                "47.12", "47.12", "47.12", "5.794", "5.794", "5.794", "5.794", " ", " ", " ");
        Assert.assertEquals("SumOfLatestNonBlankMinutes values incorrect", expected, actual);

        actual = table.getColumnDataAsText("SumOfCombineMinutes");
        expected = Arrays.asList("100.2", "200.4", " ", " ", " ", " ", "59.46",
                "59.46", "59.46", "59.46", "7.024", "7.024", "7.024", "7.024", " ", " ", " ");
        Assert.assertEquals("SumOfCombineMinutes values incorrect", expected, actual);

        /*
            Because text sorting differs, slightly, on Postgres Windows vs. Mac/Linux, this ordering is not consistent, (Because Vial's vs. Vial is)
            We can't do this comparison in the tests. If we want to add back a Max/Min test, we need to use a new or different text field and control the text.

        actual = table.getColumnDataAsText("MaxAvailabilityReason");
        expected = Arrays.asList(SPECIMEN_AVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, SPECIMEN_UNAVAILABLE_REASON, SPECIMEN_UNAVAILABLE_REASON,
                SPECIMEN_UNAVAILABLE_REASON, SPECIMEN_UNAVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON,
                SPECIMEN_AVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, " ", " ",
                SPECIMEN_UNAVAILABLE_REASON);
        Assert.assertEquals("MaxAvailabilityReason values incorrect", expected, actual);

        actual = table.getColumnDataAsText("MinAvailabilityReason");
        expected = Arrays.asList(SPECIMEN_AVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, SPECIMEN_UNAVAILABLE_REASON, SPECIMEN_UNAVAILABLE_REASON,
                SPECIMEN_UNAVAILABLE_REASON, SPECIMEN_UNAVAILABLE_REASON, SPECIMEN_UNAVAILABLE_REASON, SPECIMEN_UNAVAILABLE_REASON, SPECIMEN_UNAVAILABLE_REASON,
                SPECIMEN_UNAVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, SPECIMEN_AVAILABLE_REASON, " ", " ",
                SPECIMEN_UNAVAILABLE_REASON);
        Assert.assertEquals("MinAvailabilityReason values incorrect", expected, actual);
        */

        actual = table.getColumnDataAsText("LatestDrawTimestamp");
        expected = Arrays.asList("2009-01-07 00:00", "2009-01-06 00:00", "2008-09-17 00:00", "2008-09-17 00:00", "2008-09-17 00:00", "2008-09-17 00:00", "2008-07-30 00:00", "2008-07-30 00:00", "2008-07-30 00:00",
                "2008-07-30 00:00", "2009-03-16 00:00", "2009-03-16 00:00", "2009-03-16 00:00", "2009-03-16 00:00", "2009-06-15 00:00", "2009-06-15 00:00", "2009-05-09 00:00");
        Assert.assertEquals("LatestDrawTimestamp values incorrect", expected, actual);

        actual = table.getColumnDataAsText("FirstDrawTimestamp");
        expected = Arrays.asList("2009-01-06 00:00", "2009-01-06 00:00", "2008-09-17 00:00", "2008-09-17 00:00", "2008-09-17 00:00", "2008-09-17 00:00", "2008-07-30 00:00", "2008-07-30 00:00", "2008-07-30 00:00",
                "2008-07-30 00:00", "2009-03-16 00:00", "2009-03-16 00:00", "2009-03-16 00:00", "2009-03-16 00:00", "2009-06-15 00:00", "2009-06-15 00:00", "2009-05-09 00:00");
        Assert.assertEquals("FirstDrawTimestamp values incorrect", expected, actual);
    }
}
