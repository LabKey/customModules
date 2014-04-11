package org.labkey.test.tests;

import org.jetbrains.annotations.Nullable;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.DailyB;
import org.labkey.test.categories.Specimen;

/**
 * Test exporting rows from a specimen grid (not folder/study specimen export.)
 */
@Category({DailyB.class, Specimen.class})
public class SpecimenGridExportTest extends AbstractExportTest
{
    public static final String SPECIMEN_DATA =
            "Vial Id\tDraw Date\tParticipant\tVolume\tUnits\tSpecimen Type\tDerivative Type\tAdditive Type\n" +
            "Sample_001\t11/13/12\tP7310\t200\tml\t\t\t\n" +
            "Sample_002\t11/13/12\tP7311\t200\tml\t\t\t\n" +
            "Sample_003\t11/13/12\tP7312\t200\tml\t\t\t\n" +
            "Sample_004\t11/13/12\tP7313\t200\tml\t\t\t\n" +
            "Sample_005\t11/13/12\tP7314\t200\tml\t\t\t\n" +
            "Sample_006\t11/13/12\tP7315\t200\tml\t\t\t\n";

    @BeforeClass
    public static void doSetup() throws Exception
    {
        SpecimenGridExportTest initTest = new SpecimenGridExportTest();
        initTest.doCleanup(false);

        initTest._containerHelper.createProject(initTest.getProjectName(), "Study");
        initTest.importSpecimens();

        currentTest = initTest;
    }

    private void importSpecimens()
    {
        clickButton("Create Study");
        //use date-based study
        click(Locator.xpath("(//input[@name='timepointType'])[1]"));
        setFormElement(Locator.xpath("//input[@name='startDate']"), "2012-01-01");
        clickButton("Create Study");

        log("** Import specimens");
        clickTab("Specimen Data");
        waitAndClickAndWait(Locator.linkWithText("Import Specimens"));
        setFormElementJS(Locator.id("tsv"), SPECIMEN_DATA);
        clickButton("Submit");
        assertTextPresent("Specimens uploaded successfully");
    }

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        super.doCleanup(afterTest);
    }

    @Nullable
    @Override
    protected String getProjectName()
    {
        return SpecimenGridExportTest.class.getSimpleName();
    }

    @Override
    protected String getTestColumnTitle()
    {
        return "Participant Id";
    }

    @Override
    protected int getTestColumnIndex()
    {
        return 0;
    }

    @Override
    protected String getExportedTsvTestColumnHeader()
    {
        return "Participant Id";
    }

    @Override
    protected String getExportedFilePrefixRegex()
    {
        return "SpecimenSummary";
    }

    @Override
    protected String getDataRegionId()
    {
        return "SpecimenSummary";
    }

    @Override
    protected void goToDataRegionPage()
    {
        beginAt("/" + getProjectName() + "/study-samples-samples.view?showVials=false");
    }

    @Override
    public String getAssociatedModuleDirectory()
    {
        return "server/modules/study";
    }
}
