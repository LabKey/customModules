package org.labkey.test.tests.trialshare;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.remoteapi.Connection;
import org.labkey.remoteapi.SimplePostCommand;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.WebTestHelper;
import org.labkey.test.categories.Git;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Category({Git.class})
public class TrialShareExportTest extends BaseWebDriverTest
{
    @BeforeClass
    public static void setupProject()
    {
        TrialShareExportTest init = (TrialShareExportTest) getCurrentTest();

        init.doSetup();
    }

    private void doSetup()
    {
        _containerHelper.createProject(getProjectName(), null);
    }

    @Test
    public void testTrialShareExportActionDefault() throws Exception
    {
        String subfolder = "exportDefault";
        _containerHelper.createSubfolder(getProjectName(), subfolder);

        Connection connection = WebTestHelper.getRemoteApiConnection();
        SimplePostCommand command = new SimplePostCommand("trialShare", "trialShareExport");
        command.execute(connection, getProjectName() + "/" + subfolder);

        goToModule("FileContent");
        _fileBrowserHelper.selectFileBrowserItem("/export/folder.xml");
        List<String> fileList = _fileBrowserHelper.getFileList();
        List<String> expectedFiles = Arrays.asList("data-classes", "etls", "inventory", "sample-types", "wikis", "xar", "data_states.xml", "folder.xml", "pages.xml");
        assertEquals("Default export should include several folder objects", expectedFiles, fileList);
    }

    @Test
    public void testTrialShareExportActionCustom() throws Exception
    {
        String subfolder = "exportCustom";
        _containerHelper.createSubfolder(getProjectName(), subfolder);

        Connection connection = WebTestHelper.getRemoteApiConnection();
        SimplePostCommand command = new SimplePostCommand("trialShare", "trialShareExport");
        Map<String, Object> params = new HashMap<>();
        params.put("webpartPropertiesAndLayout", true); // Add one custom data type to override default
        command.setParameters(params);
        command.execute(connection, getProjectName() + "/" + subfolder);

        goToModule("FileContent");
        _fileBrowserHelper.selectFileBrowserItem("/export/folder.xml");
        List<String> fileList = _fileBrowserHelper.getFileList();
        List<String> expectedFiles = Arrays.asList("folder.xml", "pages.xml");
        assertEquals("Custom export should include only the specified objects", expectedFiles, fileList);
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    protected String getProjectName()
    {
        return "TrialShareExportTest Project";
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList();
    }
}
