/*
 * Copyright (c) 2014-2017 LabKey Corporation
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
import org.labkey.test.Locator;
import org.labkey.test.TestFileUtils;
import org.labkey.test.categories.DailyC;
import org.labkey.test.components.CustomizeView;
import org.labkey.test.components.PropertiesEditor;
import org.labkey.test.params.FieldDefinition;
import org.labkey.test.params.FieldDefinition.ColumnType;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.StudyHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Category({DailyC.class})
public class SpecimenCustomizeTest extends SpecimenBaseTest
{
    protected static final String PROJECT_NAME = "SpecimenCustomizeProject";
    protected static final String SPECIMEN_ARCHIVE = StudyHelper.getStudySampleDataPath() + "specimens/Rollup.specimens";
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
    protected void doVerifySteps() throws Exception
    {
        configureSpecimenProperties();

        setPipelineRoot(StudyHelper.getPipelinePath());
        startSpecimenImport(2, SPECIMEN_ARCHIVE);
        waitForSpecimenImport();

        addFieldsToSpecimenView();

        verifySpecimenDetailContents();
    }

    private void configureSpecimenProperties()
    {
        goToManageStudy();
        clickAndWait(Locator.linkContainingText("Edit specimen properties"));

        PropertiesEditor specimenEventFields = PropertiesEditor.PropertiesEditor(getDriver()).withTitle("SpecimenEvent").waitFor();
        specimenEventFields.addField(new FieldDefinition("Tally").setType(ColumnType.Integer));
        specimenEventFields.addField(new FieldDefinition("Note").setType(ColumnType.String));
        specimenEventFields.addField(new FieldDefinition("Minutes").setType(ColumnType.Double));
        specimenEventFields.addField(new FieldDefinition("Flag").setType(ColumnType.Boolean));

        PropertiesEditor vialFields = PropertiesEditor.PropertiesEditor(getDriver()).withTitle("Vial").find();
        vialFields.addField(new FieldDefinition("Tally").setType(ColumnType.Integer));
        vialFields.addField(new FieldDefinition("FirstTally").setType(ColumnType.Integer));
        vialFields.addField(new FieldDefinition("LatestTally").setType(ColumnType.Integer));
        vialFields.addField(new FieldDefinition("LatestNonBlankTally").setType(ColumnType.Integer));
        vialFields.addField(new FieldDefinition("CombineTally").setType(ColumnType.Integer));
        vialFields.addField(new FieldDefinition("FirstNote").setType(ColumnType.String));
        vialFields.addField(new FieldDefinition("LatestNote").setType(ColumnType.String));
        vialFields.addField(new FieldDefinition("LatestNonBlankNote").setType(ColumnType.String));
        vialFields.addField(new FieldDefinition("CombineNote").setType(ColumnType.String));
        vialFields.addField(new FieldDefinition("FirstMinutes").setType(ColumnType.Double));
        vialFields.addField(new FieldDefinition("LatestMinutes").setType(ColumnType.Double));
        vialFields.addField(new FieldDefinition("LatestNonBlankMinutes").setType(ColumnType.Double));
        vialFields.addField(new FieldDefinition("CombineMinutes").setType(ColumnType.Double));
        vialFields.fieldProperties().selectFormatTab().propertyFormat.set("0.####");
        vialFields.addField(new FieldDefinition("FirstFlag").setType(ColumnType.Boolean));
        vialFields.addField(new FieldDefinition("LatestFlag").setType(ColumnType.Boolean));
        vialFields.addField(new FieldDefinition("LatestNonBlankFlag").setType(ColumnType.Boolean));
        vialFields.addField(new FieldDefinition("LatestDrawTimestamp").setType(ColumnType.DateTime));
        vialFields.addField(new FieldDefinition("FirstDrawTimestamp").setType(ColumnType.DateTime));

        PropertiesEditor specimenFields = PropertiesEditor.PropertiesEditor(getDriver()).withTitle("Specimen").find();
        specimenFields.addField(new FieldDefinition("TotalLatestNonBlankTally").setType(ColumnType.Integer));
        specimenFields.addField(new FieldDefinition("SumOfLatestNonBlankMinutes").setType(ColumnType.Double));
        specimenFields.fieldProperties().selectFormatTab().propertyFormat.set("0.####");
        specimenFields.addField(new FieldDefinition("SumOfCombineMinutes").setType(ColumnType.Double));
        specimenFields.fieldProperties().selectFormatTab().propertyFormat.set("0.####");
        specimenFields.addField(new FieldDefinition("CountLatestNonBlankFlag").setType(ColumnType.Integer));
        specimenFields.addField(new FieldDefinition("MaxAvailabilityReason").setType(ColumnType.String));
        specimenFields.addField(new FieldDefinition("MinAvailabilityReason").setType(ColumnType.String));

        clickAndWait(Locator.lkButton("Save & Close"));
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
