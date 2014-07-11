/*
 * Copyright (c) 2014 LabKey Corporation
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
import org.labkey.test.categories.DailyA;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.ListHelper;
import org.labkey.test.util.LogMethod;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: RyanS
 * Date: 2/7/14
 */
@Category({DailyA.class})
public class SpecimenCustomizeTest extends SpecimenBaseTest
{
    protected static final String PROJECT_NAME = "SpecimenCustomizeProject";

    protected static final String SPECIMEN_ARCHIVE = getStudySampleDataPath() + "specimens/Rollup.specimens";

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
        importFolderFromZip(new File(TestFileUtils.getSampledataPath(), "/study/SpecimenCustomizeStudy.folder.zip"));
    }

    @Override
    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    protected void doVerifySteps() throws Exception
    {
        addSpecimenEventFields();
        addVialFields();
        addSpecimenFields();

        setPipelineRoot(getPipelinePath());
        startSpecimenImport(2, SPECIMEN_ARCHIVE);
        waitForSpecimenImport();

        verifySpecimenDetailContents();
    }

    private void addSpecimenEventFields()
    {
        goToEditSpecimenProperties();
        ListHelper propertiesHelper = new ListHelper(this);
        propertiesHelper.addField("SpecimenEvent", "Tally", null, ListHelper.ListColumnType.Integer);
        propertiesHelper.addField("SpecimenEvent", "Note", null, ListHelper.ListColumnType.String);
        propertiesHelper.addField("SpecimenEvent", "Minutes", null, ListHelper.ListColumnType.Double);
        propertiesHelper.addField("SpecimenEvent", "Flag", null, ListHelper.ListColumnType.Boolean);
        save();
        saveAndClose();
    }

    private void addVialFields()
    {
        goToEditSpecimenProperties();
        ListHelper propertiesHelper = new ListHelper(this);
        propertiesHelper.addField("Vial", "Tally", null, ListHelper.ListColumnType.Integer);
        propertiesHelper.addField("Vial", "FirstTally", null, ListHelper.ListColumnType.Integer);
        propertiesHelper.addField("Vial", "LatestTally", null, ListHelper.ListColumnType.Integer);
        propertiesHelper.addField("Vial", "LatestNonBlankTally", null, ListHelper.ListColumnType.Integer);
        propertiesHelper.addField("Vial", "CombineTally", null, ListHelper.ListColumnType.Integer);
        propertiesHelper.addField("Vial", "FirstNote", null, ListHelper.ListColumnType.String);
        propertiesHelper.addField("Vial", "LatestNote", null, ListHelper.ListColumnType.String);
        propertiesHelper.addField("Vial", "LatestNonBlankNote", null, ListHelper.ListColumnType.String);
        propertiesHelper.addField("Vial", "CombineNote", null, ListHelper.ListColumnType.String);
        propertiesHelper.addField("Vial", "FirstMinutes", null, ListHelper.ListColumnType.Double);
        propertiesHelper.addField("Vial", "LatestMinutes", null, ListHelper.ListColumnType.Double);
        propertiesHelper.addField("Vial", "LatestNonBlankMinutes", null, ListHelper.ListColumnType.Double);
        propertiesHelper.addField("Vial", "CombineMinutes", null, ListHelper.ListColumnType.Double);
        setFormat("Vial", "0.####");
        propertiesHelper.addField("Vial", "FirstFlag", null, ListHelper.ListColumnType.Boolean);
        propertiesHelper.addField("Vial", "LatestFlag", null, ListHelper.ListColumnType.Boolean);
        propertiesHelper.addField("Vial", "LatestNonBlankFlag", null, ListHelper.ListColumnType.Boolean);
        save();
        saveAndClose();
    }

    private void addSpecimenFields()
    {
        goToEditSpecimenProperties();
        ListHelper propertiesHelper = new ListHelper(this);
        propertiesHelper.addField("Specimen", "TotalLatestNonBlankTally", null, ListHelper.ListColumnType.Integer);
        propertiesHelper.addField("Specimen", "SumOfLatestNonBlankMinutes", null, ListHelper.ListColumnType.Double);
        setFormat("Specimen", "0.####");
        propertiesHelper.addField("Specimen", "SumOfCombineMinutes", null, ListHelper.ListColumnType.Double);
        setFormat("Specimen", "0.####");
        propertiesHelper.addField("Specimen", "CountLatestNonBlankFlag", null, ListHelper.ListColumnType.Integer);
        save();
        saveAndClose();
    }

    private void goToEditSpecimenProperties()
    {
        goToManageStudy();
        click(Locator.linkContainingText("Edit specimen properties"));
        waitForElement(Locator.xpath("//td[.='SpecimenEvent']//../..//span[@id='button_Add Field']/a"));
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

    private void saveAndClose()
    {
        click(Locator.xpath("//span[@id='button_Save & Close']/a"));
    }

    private void save()
    {
        Locator saveButtonLocator = Locator.xpath("//span[@id='button_Save']/a");
        shortWait().until(ExpectedConditions.elementToBeClickable(saveButtonLocator.toBy()));
        click(saveButtonLocator);
//        waitForElementToDisappear(Locator.xpath("//span[@id='button_Save']/a[@class='labkey-disabled-button']"), 60000);
        waitForText("Save successful");
    }

    private void cancel()
    {
        click(Locator.xpath("//span[@id='button_Cancel']/a"));
    }

    //type is Specimen, Vial or SpecimenEvent
    private void clickAddField(String label)
    {
        click(Locator.xpath("//td[.='" + label + "']//../..//span[@id='button_Add Field']/a"));
        //td[.='SpecimenEvent']/../../../..//table[@class='gwt-ButtonBar']//span[@id='button_Save & Close']
    }

    private void clickImportFields(String type)
    {
        click(Locator.xpath("//td[.='" + type + "']/../..//span[@id='button_Import Fields']/a"));
    }

    private void clickExportFields(String type)
    {
        click(Locator.xpath("//td[.='" + type + "']/../..//span[@id='button_Export Fields']/a"));
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
            WebElement table = getElement(locator);
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
            WebElement table = getElement(locator);
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

    private void verifySpecimenDetailContents()
    {
        clickTab("Specimen Data");
        waitAndClickAndWait(Locator.linkWithText("By Individual Vial"));
        DataRegionTable table = new DataRegionTable("SpecimenDetail", this);

        List<String> actual = table.getColumnDataAsText("CombineNote");
        List<String> expected = Arrays.asList("novials1002", "novials1001, novials1003", " ", " ", " ", " ", " ",
                "unavailable2", "unavailable1, unavailable22", " ", "note1, note101", "note2", " ", " ", " ", " ", " ");
        if (!compareLists(actual, expected))
            Assert.fail("CombineNote values incorrect");

        actual = table.getColumnDataAsText("CombineTally");
        expected = Arrays.asList("1002", "2004", " ", " ", " ", " ", " ",
                "12", "33", " ", "102", "2", " ", " ", " ", " ", " ");
        if (!compareLists(actual, expected))
            Assert.fail("CombineTally values incorrect");

        actual = table.getColumnDataAsText("SumOfLatestNonBlankMinutes");
        expected = Arrays.asList("200.5", "200.5", " ", " ", " ", " ", "47.12",
                "47.12", "47.12", "47.12", "5.794", "5.794", "5.794", "5.794", " ", " ", " ");
        if (!compareLists(actual, expected))
            Assert.fail("SumOfLatestNonBlankMinutes values incorrect");

        actual = table.getColumnDataAsText("SumOfCombineMinutes");
        expected = Arrays.asList("300.6", "300.6", " ", " ", " ", " ", "59.46",
                "59.46", "59.46", "59.46", "7.024", "7.024", "7.024", "7.024", " ", " ", " ");
        if (!compareLists(actual, expected))
            Assert.fail("SumOfCombineMinutes values incorrect");
    }

    private boolean compareLists(List<String> actual, List<String> expected)
    {
        if (actual.size() != expected.size())
            return false;
        for (int i = 0; i < actual.size(); i += 1)
            if (!actual.get(i).equals(expected.get(i)))
                return false;
        return true;
    }
}
