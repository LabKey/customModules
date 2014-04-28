package org.labkey.test.tests;

import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.categories.DailyA;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: RyanS
 * Date: 2/7/14
 */
@Category({DailyA.class})
public class SpecimenCustomizeTest extends SpecimenBaseTest
{
    protected static final String PROJECT_NAME = "SpecimenCustomizeProject";

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
        importStudyFromZip(new File(getSampledataPath(), "/study/LabkeyDemoStudy.zip"));
    }

    @Override
    protected void doVerifySteps() throws Exception
    {
        goToEditSpecimenProperties();
        clickAddField("SpecimenEvent");
        specimenPropertyTable specimenEventTable = new specimenPropertyTable("SpecimenEvent");
        propertyRow row = specimenEventTable.getPropertyRowByName("");
        row.editName("newValue");
        row.editType("Text (String)");
        save();
        specimenEventTable.refresh();
        clickAddField("Vial");
        specimenPropertyTable specimenVialTable = new specimenPropertyTable("Vial");
        row = specimenVialTable.getPropertyRowByName("");
        row.editName("FirstComments");
        save();
        clickAddField("Vial");
        specimenVialTable = new specimenPropertyTable("Vial");
        row = specimenVialTable.getPropertyRowByName("");
        row.editName("LatestNonBlankComments");
        save();
        specimenPropertyTable specimenTable = new specimenPropertyTable("Specimen");
        clickAddField("Specimen");
        propertyRow specimenRow = specimenTable.getPropertyRowByName("");
        specimenRow.editName("CountComments");
        save();
        specimenTable = new specimenPropertyTable("Specimen");
        clickAddField("Specimen");
        specimenRow = specimenTable.getPropertyRowByName("");
        specimenRow.editName("CombineComments");
        save();
        setPipelineRoot(getPipelinePath());
        startSpecimenImport(2);
        waitForSpecimenImport();
    }

    private void goToEditSpecimenProperties()
    {
        goToManageStudy();
        click(Locator.linkContainingText("Edit specimen properties"));
        waitForElement(Locator.xpath("//td[.='SpecimenEvent']//../..//span[@id='button_Add Field']/a"));
    }

    private ArrayList<propertyRow> getRowsFromTable(WebElement table)
    {
        ArrayList<propertyRow> rows = new ArrayList<>();
        int i = 1;
        for(WebElement row : table.findElements(By.xpath(".//tr")))
        {
            rows.add(new propertyRow(row, i));
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
        waitForElementToDisappear(Locator.xpath("//span[@id='button_Save']/a[@class='labkey-disabled-button']"), 60000);
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

    class specimenPropertyTable
    {
        List<propertyRow> _propertyRows;
        String _label;

        public specimenPropertyTable (String label)
        {
            _label = label;
            _propertyRows = new ArrayList<>();
            String xPath = "//td[.='" + _label + "']/../..//table[@class='labkey-pad-cells']";
            Locator locator = Locator.xpath(xPath);
            WebElement table = getElement(locator);
            int i = 1;
            for(WebElement row : table.findElements(By.xpath(".//tr")))
            {
                _propertyRows.add(new propertyRow(row, i));
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
                _propertyRows.add(new propertyRow(row, i));
                i++;
            }
        }

        public propertyRow getPropertyRowByName(String name)
        {
            propertyRow theRow = null;
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

        public propertyRow getPropertyRowByLabel(String label)
        {
            propertyRow theRow = null;
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
    private class propertyRow
    {
        protected WebElement _tr;
        protected int _index;

        public propertyRow(WebElement tr, int index)
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
}
