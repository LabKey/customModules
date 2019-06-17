/*
 * Copyright (c) 2013-2019 LabKey Corporation
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

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.Connection;
import org.labkey.remoteapi.query.SelectRowsCommand;
import org.labkey.remoteapi.query.SelectRowsResponse;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.WebTestHelper;
import org.labkey.test.categories.DailyC;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.Ext4Helper;
import org.labkey.test.util.LogMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@Category({DailyC.class})
@BaseWebDriverTest.ClassTimeout(minutes = 5)
public class SpecimenImportTest extends SpecimenBaseTest
{
    private final String noVisitIdTSV =
            "Global Unique Id\tSample Id\tDraw Timestamp\tVisit\tParticipant Id\n" +
                    "1\t1\t\t\t1\n" +
                    "2\t2\t\t\t2\n" +
                    "3\t3\t\t\t\n" +
                    "4\t4\t\t\t";

    private final String noPtidsTSV =
            "Global Unique Id\tSample Id\tDraw Timestamp\tVisit\tParticipant\n" +
                    "\t\t\t1\t\n" +
                    "\t\t\t2\t\n" +
                    "\t\t\t3\t\n" +
                    "\t\t\t4\t";
    private final String cleanEntry =
            "Global Unique Id\tSample Id\tDraw Timestamp\tVisit\tParticipant Id\n" +
                    "1\t1\t\t1\t1\n" +
                    "2\t2\t\t2\t2\n" +
                    "3\t3\t\t3\t\n" +
                    "4\t4\t\t4\t";

    @Override
    @LogMethod
    protected void doCreateSteps()
    {
        initializeFolder();

        checkRequiredFields(false);
        changeTimepointType();

        waitForText("General Study Settings");
        checkRequiredFields(true);
        doUploads();

        assertIdsSet();

        assertSampleSetData();
    }

    @Override
    protected void doVerifySteps()
    {
        //Do Nothing
    }

    protected void goToImport()
    {
        clickTab("Data");
        waitForText("No specimens found");
        waitForText("Import Specimens");
        sleep(100);
        clickAndWait(Locator.linkContainingText("Import Specimens"));
    }

    protected void checkRequiredFields(boolean visit)
    {
        goToImport();
        waitAndClick(Locator.linkContainingText("Show Expected Data Fields"));
        assertTextPresent("Global Unique Id","Sample Id","Participant Id", "Volume", "Volume Units", "Primary Type", "Derivative Type", "Additive Type");
        if(!visit)
        {
            assertTextNotPresent("Visit");
            assertTextPresent("Draw Timestamp");
        }
        else
        {
            assertTextPresent("Visit");
        }
    }

    protected void changeTimepointType()
    {
        clickTab("Manage");
        waitAndClick(Locator.linkContainingText("Change Study Properties"));
        waitForText("Timepoint Type:");
        _ext4Helper.selectRadioButton("Timepoint Type:", "VISIT");
        clickAndWait(Ext4Helper.Locators.ext4Button("Submit"));
    }
    protected void doUploads()
    {
        setFormElementJS(Locator.name("tsv"), noVisitIdTSV);
        clickButton("Submit");
        waitForText("Error: row 1 does not contain a value for field Visit");
        setFormElementJS(Locator.name("tsv"), noPtidsTSV);
        clickButton("Submit");
        waitForText("Error in row 1: required field Participant was not supplied");
        assertTextPresent("Error in row 1: missing Global Unique Id");
        setFormElementJS(Locator.name("tsv"), cleanEntry);
        clickButton("Submit");
        waitForText("Specimens uploaded successfully.");
    }

    protected void assertIdsSet()
    {
        clickTab("Data");
        waitAndClick(Locator.linkContainingText("By Individual Vial"));
        DataRegionTable region = new DataRegionTable("SpecimenDetail", this);
        assertTrue("Failed to find Participant ID 1", region.getRow("Participant Id", "1") == 0);
        assertTrue("Failed to find Global Unique ID 1", region.getRow("Global Unique Id", "1") == 0);
        assertTrue("Failed to find Participant ID 3", region.getRow("Participant Id", "3") == 2);
        assertTrue("Failed to find Global Unique ID 3", region.getRow("Global Unique Id", "3") == 2);
    }

    // Checking for regression like the one that occurred in issue 36863: specimen importer doesn't create rows in provisioned table.
    protected  void assertSampleSetData()
    {
        String folderPath = "/" + getProjectName() + "/" + getFolderName();
        List<String> fields = Arrays.asList("Name", "Run", "Flag/Comment");

        List<Map<String, String>> sampleSetData = getSampleDataFromDB(folderPath, "Study Specimens", fields);

        Assert.assertNotEquals("There are no rows in the \"Study Specimens\" sample set.", sampleSetData.size(), 0);

        List<String> expectedNames = Arrays.asList("1", "2", "3", "4");

        boolean pass = true;
        if(sampleSetData.size() != expectedNames.size())
        {
            pass = false;
            log("\n*************** ERROR ***************\nThe number of records returned is not as expected. Expected: " + expectedNames.size() + " found: " + sampleSetData.size() + "\n*************** ERROR ***************");
        }

        for(int i = 0; i < expectedNames.size(); i++)
        {
            boolean found = false;
            for(int j = 0; j < sampleSetData.size(); j++)
            {
                if(expectedNames.get(i).trim().equalsIgnoreCase(sampleSetData.get(j).get("Name").trim()))
                {
                    found = true;
                    break;
                }
            }

            if(!found)
            {
                pass = false;
                log("\n*************** ERROR ***************\nDid not find the expected name '" + expectedNames.get(i).trim() + "' in the returned data.\n*************** ERROR ***************");
            }
        }

        if(!pass)
        {
            log("\n*************** ERROR ***************\nExpected values: " + expectedNames + "\nValues returned: " + sampleSetData + "\n*************** ERROR ***************");
            Assert.fail("Sample Set data not as expected.");
        }
    }

    protected List<Map<String, String>> getSampleDataFromDB(String folderPath, String sampleSetName, List<String> fields)
    {
        List<Map<String, String>> results = new ArrayList<>(6);
        Map<String, String> tempRow;

        Connection cn = WebTestHelper.getRemoteApiConnection();
        SelectRowsCommand cmd = new SelectRowsCommand("samples", sampleSetName);
        cmd.setColumns(fields);

        try
        {
            SelectRowsResponse response = cmd.execute(cn, folderPath);

            for (Map<String, Object> row : response.getRows())
            {

                tempRow = new HashMap<>();

                for(String key : row.keySet())
                {

                    if (fields.contains(key))
                    {

                        String tmpFlag = key;

                        if(key.equalsIgnoreCase("Flag/Comment"))
                            tmpFlag = "Flag";

                        if (null == row.get(key))
                        {
                            tempRow.put(tmpFlag, "");
                        }
                        else
                        {
                            tempRow.put(tmpFlag, row.get(key).toString());
                        }

                    }

                }

                results.add(tempRow);

            }

        }
        catch(CommandException | IOException excp)
        {
            Assert.fail(excp.getMessage());
        }

        return results;
    }

    @Override
    protected void initializeFolder()
    {
        int response = WebTestHelper.getHttpResponse(WebTestHelper.buildURL("project", getProjectName(), "begin")).getResponseCode();

        if (HttpStatus.SC_OK != response)
        {
            _containerHelper.createProject(getProjectName(), null);
        }

        _containerHelper.createSubfolder(getProjectName(), getProjectName(), getFolderName(), "CAVD Study", null, true);
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

}
