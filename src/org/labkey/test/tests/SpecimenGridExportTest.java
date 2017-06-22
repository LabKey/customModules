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

import org.jetbrains.annotations.Nullable;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.labkey.api.data.ColumnHeaderType;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.DailyB;
import org.labkey.test.categories.Specimen;

import java.util.Arrays;
import java.util.List;

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
        SpecimenGridExportTest initTest = (SpecimenGridExportTest)getCurrentTest();

        initTest._containerHelper.createProject(initTest.getProjectName(), "Study");
        initTest.importSpecimens();
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
    protected boolean hasSelectors()
    {
        return true;
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
    protected String getExportedXlsTestColumnHeader(ColumnHeaderType exportType)
    {
        switch (exportType)
        {
            case FieldKey:
                return "ParticipantId";
            default:
                return "Participant Id";
        }
    }

    @Override
    protected String getExportedTsvTestColumnHeader(ColumnHeaderType exportType)
    {
        switch (exportType)
        {
            case FieldKey:
                return "ParticipantId";
            default:
                return "Participant Id";
        }
    }

    @Override
    protected String getDataRegionColumnName()
    {
        return "ProtocolNumber";
    }

    @Override
    protected String getDataRegionSchemaName()
    {
        return "study";
    }

    @Override
    protected String getDataRegionQueryName()
    {
        return "SpecimenSummary";
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
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("study");
    }
}
