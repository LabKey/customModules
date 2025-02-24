/*
 * Copyright (c) 2013-2015 LabKey Corporation
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
package org.labkey.test.tests.pepdb;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.Connection;
import org.labkey.remoteapi.query.ContainerFilter;
import org.labkey.remoteapi.query.DeleteRowsCommand;
import org.labkey.remoteapi.query.Row;
import org.labkey.remoteapi.query.RowMap;
import org.labkey.remoteapi.query.SelectRowsCommand;
import org.labkey.remoteapi.query.SelectRowsResponse;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestFileUtils;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.WebTestHelper;
import org.labkey.test.categories.External;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.PostgresOnlyTest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category({External.class})
public class PepDBModuleTest extends BaseWebDriverTest implements PostgresOnlyTest
{

    public static final String FOLDER_TYPE = "Custom";
    // Folder type defined in customFolder.foldertype.xml
    public static final String MODULE_NAME = "PepDB";
    public static final String USER_SCHEMA_NAME = "pepdb";
    public static final String FOLDER_NAME = "PepDB";
    private int peptideStartIndex = 0;

    @Override
    protected String getProjectName()
    {
        return getClass().getSimpleName() + " Project";
    }

    // Setup duplicates the folder structure and webparts used in production.
    private void setupProject()
    {
        log("Starting setupProject()");
        assertModuleDeployed(MODULE_NAME);
        _containerHelper.createProject(getProjectName(), FOLDER_TYPE);

        _containerHelper.enableModule(getProjectName(), MODULE_NAME);
        _containerHelper.createSubfolder(getProjectName(), "Labs", FOLDER_TYPE);
        _containerHelper.createSubfolder(getProjectName() + "/Labs", "Test", FOLDER_TYPE);
        _containerHelper.createSubfolder(getProjectName() + "/Labs/Test", FOLDER_NAME, FOLDER_TYPE);
        clickFolder("Labs");
        clickFolder("Test");
        clickFolder(FOLDER_NAME);

        _containerHelper.enableModules(Arrays.asList("Issues", "Wiki", "PepDB"));
        _containerHelper.disableModules("Portal");
        setDefaultModule("PepDB");
        log("Finished setupProject()");
    }

    /**
     * Set which view in this folder should be the default
     *
     * @param moduleName
     */
    void setDefaultModule(String moduleName)
    {
        goToFolderManagement();
        clickAndWait(Locator.linkWithText("Folder Type"));
        selectOptionByText(Locator.name("defaultModule"), moduleName);
        clickButton("Update Folder");
    }

    @Test
    public void testSteps() throws Exception
    {
        setupProject();
        assertModuleEnabled("Issues");
        assertModuleEnabled("Wiki");
        assertModuleEnabled("PepDB");
        log("Expected modules enabled.");

        beginAt(WebTestHelper.buildURL("pepdb", getProjectName() + "/Labs/Test/" + FOLDER_NAME, "begin"));

        /*  Insert the Peptide Group" */
        clickAndWait(Locator.linkWithText("Insert a New Group"));
        setFormElement(Locator.name("peptide_group_name"), "gagptegprac");
        selectOptionByText(Locator.name("pathogen_id"), "Other");
        selectOptionByText(Locator.name("clade_id"), "Other");
        selectOptionByText(Locator.name("group_type_id"), "Other");
        clickAndWait(Locator.css("a.labkey-button > span"));
        clickFolder(FOLDER_NAME);

        // Import some test Peptides from a file.
        clickAndWait(Locator.linkWithText("Import Peptides"));
        selectOptionByText(Locator.id("actionType"), "Peptides");
        setFormElement(Locator.name("pFile"), getSampleData("/peptide_file/gagptegprac.txt"));
        clickButton("Import Peptides");

        /*  Import Peptide Pool 'Pool Descriptions' file.
        *     ./test_import_files/pool_description_file/pool_description.txt
        */
        clickFolder(FOLDER_NAME);
        clickAndWait(Locator.linkWithText("Import Peptide Pools"));
        selectOptionByText(Locator.name("actionType"), "Pool Descriptions");

        setFormElement(Locator.name("pFile"), getSampleData("/pool_description_file/pool_description.txt"));
        clickAndWait(Locator.css("a.labkey-button > span"));

        /* Import Peptide Pool 'Peptides in Pool' file.
          ./test_import_files/pool_detail_file/pool_details.txt
        */
        clickFolder(FOLDER_NAME);
        clickAndWait(Locator.linkWithText("Import Peptide Pools"));
        selectOptionByText(Locator.name("actionType"), "Peptides in Pool");

        setFormElement(Locator.name("pFile"), getSampleData("/pool_detail_file/pool_details.txt"));
        clickAndWait(Locator.css("a.labkey-button > span"));

        /* Search for the Peptides belonging to our just-imported pool */
        clickFolder(FOLDER_NAME);
        clickAndWait(Locator.linkWithText("Search for Peptides by Criteria"));
        selectOptionByText(Locator.name("queryKey"), "Peptides in a Peptide Pool");
        selectOptionByTextContaining(getDriver().findElement(Locator.name("queryValue")), "Prac_Pool");

        clickAndWait(Locator.name("action_type"));

        // Identify the index at which our peptide IDs start.
        findPeptideStartIndex();
        // List the peptides in the the 'gagptegprac' Peptide Group. We expect there to be 16 of them.
        clickFolder(FOLDER_NAME);
        clickAndWait(Locator.linkWithText("Search for Peptides by Criteria"));
        waitForText("Search for Peptides using different criteria : ");
        selectOptionByText(Locator.id("queryKey"), "Peptides in a Peptide Group");
        selectOptionByText(Locator.id("queryValue"), "gagptegprac");

        clickAndWait(Locator.name("action_type"));

        assertTextPresentInThisOrder("There are (16) peptides in the 'gagptegprac' peptide group.");
        // Select a newly uploaded peptide, #3 and edit it to have a storage location of 'Kitchen Sink'
        clickAndWait(Locator.linkWithText(pepString(4)));
        // Verify the expected record's content
        assertTrue(getDriver().findElement(Locator.xpath("//form[@lk-region-form='peptides']/table/tbody")).getText().matches("^[\\s\\S]*Peptide Id:\\s*" + pepString(4) + "\nPeptide Sequence:\\s*REPRGSDIAGTTSTL\nProtein Category:\\s*p24\nSequence Length:\\s*15\nAAStart:\\s*97\nAAEnd:\\s*111\nIs Child:\\s*false\nIs Parent:\\s*false[\\s\\S]*$"));

        clickAndWait(Locator.linkWithText("Edit Peptide"));
        setFormElement(Locator.name("storage_location"), "Kitchen Sink");
        clickAndWait(Locator.xpath("//span[text()='Save Changes']"));

        // Assert that the Storage Location now contains "Kitchen Sink"
        assertTrue(getDriver().findElement(Locator.xpath("//form[@lk-region-form='peptides']/table/tbody")).getText().matches("^[\\s\\S]*Peptide Id:\\s*" + pepString(4) + "\nPeptide Sequence:\\s*REPRGSDIAGTTSTL\nProtein Category:\\s*p24\nSequence Length:\\s*15\nAAStart:\\s*97\nAAEnd:\\s*111\nIs Child:\\s*false\nIs Parent:\\s*false[\\s\\S]*$"));
        clickAndWait(Locator.css("a.labkey-button > span"));

        //  Search for a single, newly-uploaded peptide and verify it displays as expected.
        setFormElement(Locator.name("peptide_id"), Integer.toString(peptideStartIndex + 9));
        clickButton("Find");
        // Verify the expected record's content
        assertTrue(getDriver().findElement(Locator.tagWithAttribute("form", "lk-region-form", "peptides")).getText()
                .matches("^[\\s\\S]*Peptide Id:\\s*" + pepString(9) + "\nPeptide Sequence:\\s*KCGKEGHQMKDCTER\nProtein Category:\\s*p2p7p1p6\nSequence Length:\\s*15\nAAStart:\\s*52\nAAEnd:\\s*66\nIs Child:\\s*false\nIs Parent:\\s*false\nStorage Location:\\s*\n[\\s\\S]*$"));

        clickAndWait(Locator.css("a.labkey-button > span"));


        /*
         *
         *  'Search for Peptides by Criteria' using  'Peptides in a Peptide Pool'
         *   using the newly imported Peptide Pool.
         *
         *   Verify that the peptides found match what was expected (by the pool import), and their values include the
         *   columns imported earlier during the importing of the peptides.
         *
        */
        clickFolder(FOLDER_NAME);
        clickAndWait(Locator.linkWithText("Search for Peptides by Criteria"));
        selectOptionByText(Locator.id("queryKey"), "Peptides in a Peptide Pool");

        selectOptionByTextContaining(getDriver().findElement(Locator.name("queryValue")), "Prac_Pool");
        clickAndWait(Locator.name("action_type"));
        clickAndWait(Locator.linkWithText(pepString(4)));

        checker().verifyEquals("", "Peptide Sequence:", getDriver().findElement(Locator.xpath("//form[@lk-region-form='peptides']/table/tbody/tr[2]/td")).getText());
        checker().verifyEquals("", "REPRGSDIAGTTSTL", getDriver().findElement(Locator.xpath("//form[@lk-region-form='peptides']/table/tbody/tr[2]/td[2]")).getText());
        checker().verifyEquals("", "gagptegprac (PEPTIDE NUMBER =GAG1-4)", getDriver().findElement(Locator.css(".lk-body-ct div:nth-of-type(2) > table > tbody > tr > td")).getText());
    }

    @LogMethod
    private void cleanupSchema() throws IOException, CommandException
    {
        Connection cn = createDefaultConnection();

        cleanupTable(cn, "pepdb", "peptide_pool_assignment");
        cleanupTable(cn, "pepdb", "peptide_group_assignment");
        cleanupTable(cn, "pepdb", "peptides");
        cleanupTable(cn, "pepdb", "peptide_pool");
        cleanupTable(cn, "pepdb", "peptide_group");

    }

    @LogMethod
    private void cleanupTable(Connection cn, String schemaName, String tableName) throws IOException, CommandException
    {
        SelectRowsCommand selectCmd = new SelectRowsCommand(schemaName, tableName);
        selectCmd.setMaxRows(-1);
        selectCmd.setContainerFilter(ContainerFilter.AllFolders);
        selectCmd.setColumns(Arrays.asList("*"));
        SelectRowsResponse selectResp = selectCmd.execute(cn, getProjectName());
        if (selectResp.getRowCount().intValue() > 0)
        {
            DeleteRowsCommand deleteCmd = new DeleteRowsCommand(schemaName, tableName);
            deleteCmd.setRows(selectResp.getRows());
            deleteCmd.execute(cn, getProjectName());
            assertEquals("Expected no rows remaining", 0, selectCmd.execute(cn, getProjectName()).getRowCount().intValue());
        }
    }

    // We have to expose our schema as an external schema in order to run the selectRow and deleteRow commands.
    void ensureExternalSchema(String containerPath)
    {
        log("** Ensure ExternalSchema: " + USER_SCHEMA_NAME);

        beginAt(WebTestHelper.buildURL("query", containerPath, "admin"));

        if (!isTextPresent("reload"))
        {
            assertTextPresent("new external schema");
            log("** Creating ExternalSchema: " + USER_SCHEMA_NAME);
            clickAndWait(Locator.linkWithText("new external schema"));
            checkCheckbox(Locator.name("includeSystem"));
            setFormElement(Locator.name("userSchemaName"), USER_SCHEMA_NAME);
            setFormElement(Locator.name("sourceSchemaName"), USER_SCHEMA_NAME);
            pressEnter(Locator.name("sourceSchemaName"));
            checkCheckbox(Locator.name("editable"));
            uncheckCheckbox(Locator.name("indexable"));
            clickButton("Create");
        }
        assertTextPresent(USER_SCHEMA_NAME);
        assertTextNotPresent("reload all schemas");  // Present only for external schemas > 1
    }

    // Identify the index at which our peptide IDs start.
    private void findPeptideStartIndex() throws IOException
    {
        Connection cn = createDefaultConnection();
        try
        {
            ensureExternalSchema(getProjectName());
            SelectRowsCommand selectCmd = new SelectRowsCommand("pepdb", "peptides");
            selectCmd.setMaxRows(1);
            selectCmd.setContainerFilter(ContainerFilter.AllFolders);
            selectCmd.setColumns(Arrays.asList("*"));
            SelectRowsResponse selectResp = selectCmd.execute(cn, getProjectName());

            if (selectResp.getRowCount().intValue() > 0)
            {
                Row convertedRow = new RowMap(selectResp.getRows().get(0));
                peptideStartIndex = ((int) convertedRow.getValue("peptide_id")) - 1;
            }
        }
        catch (CommandException e)
        {
            log("** Error during findPeptideStartIndex:");
            e.printStackTrace(System.out);
        }
    }

    // Convert the peptide ID integer into its string representation.
    private String pepString(int peptideIdOffset)
    {
        //return String.format("P%06d", peptideStartIndex + peptideIdOffset);
        return String.format("P%d", peptideStartIndex + peptideIdOffset);
    }

    protected void assertModuleDeployed(String moduleName)
    {
        log("Ensuring that that '" + moduleName + "' module is deployed");
        goToAdminConsole();
        assertTextPresent(moduleName);
    }

    protected void assertModuleEnabled(String moduleName)
    {
        log("Ensuring that that '" + moduleName + "' module is enabled");
        goToFolderManagement();
        clickAndWait(Locator.linkWithText("Folder Type"));
        assertElementPresent(Locator.xpath("//input[@type='checkbox' and @checked and @title='" + moduleName + "']"));
    }

    protected void assertModuleEnabledByDefault(String moduleName)
    {
        log("Ensuring that that '" + moduleName + "' module is enabled");
        goToFolderManagement();
        clickAndWait(Locator.linkWithText("Folder Type"));
        assertElementPresent(Locator.xpath("//input[@type='checkbox' and @checked and @disabled and @title='" + moduleName + "']"));
    }

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        if (afterTest)
        {
            try
            {
                cleanupSchema();
            }
            catch (IOException | CommandException e)
            {
                throw new RuntimeException(e);
            }
        }
        _containerHelper.deleteProject(getProjectName(), afterTest);
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("pepdb");
    }

    public static File getSampleData(String sampleDataPath)
    {
        return TestFileUtils.getSampleData("test_import_files" + sampleDataPath);
    }
}
