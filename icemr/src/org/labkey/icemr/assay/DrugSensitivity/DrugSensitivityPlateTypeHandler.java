/*
 * Copyright (c) 2013-2016 LabKey Corporation
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
package org.labkey.icemr.assay.DrugSensitivity;

import org.labkey.api.assay.dilution.DilutionManager;
import org.labkey.api.data.Container;
import org.labkey.api.study.AbstractPlateTypeHandler;
import org.labkey.api.study.PlateService;
import org.labkey.api.study.PlateTemplate;
import org.labkey.api.study.WellGroup;
import org.labkey.api.util.Pair;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User: klum
 * Date: 5/17/13
 */
public class DrugSensitivityPlateTypeHandler extends AbstractPlateTypeHandler
{
    public static final String DEFAULT_PLATE = "default";

    @Override
    public String getAssayType()
    {
        return DrugSensitivityAssayProvider.NAME;
    }

    @Override
    public List<String> getTemplateTypes(Pair<Integer, Integer> size)
    {
        return Arrays.asList(DEFAULT_PLATE);
    }

    @Override
    public PlateTemplate createPlate(String templateTypeName, Container container, int rowCount, int colCount) throws SQLException
    {
        PlateTemplate template = PlateService.get().createPlateTemplate(container, getAssayType(), rowCount, colCount);

/*
        template.addWellGroup(DilutionManager.CELL_CONTROL_SAMPLE, WellGroup.Type.CONTROL,
                PlateService.get().createPosition(container, 0, template.getColumns() - 1),
                PlateService.get().createPosition(container, template.getRows() - 1, template.getColumns() - 1));
*/
        template.addWellGroup(DilutionManager.VIRUS_CONTROL_SAMPLE, WellGroup.Type.CONTROL,
                PlateService.get().createPosition(container, 0, template.getColumns() - 3),
                PlateService.get().createPosition(container, template.getRows() - 1, template.getColumns() - 2));

        if (DEFAULT_PLATE.equals(templateTypeName))
        {
            for (int sample = 0; sample < 3; sample++)
            {
                int firstCol = (sample * 3);

                template.addWellGroup("Drug " + (sample + 1), WellGroup.Type.SPECIMEN,
                        PlateService.get().createPosition(container, 0, firstCol),
                        PlateService.get().createPosition(container, template.getRows() - 1, firstCol + 2));

                for (int replicate = 0; replicate < template.getRows(); replicate++)
                {
                    String specimenName = ("Drug " + (sample + 1));

                    template.addWellGroup(specimenName + ", Replicate " + (replicate + 1), WellGroup.Type.REPLICATE,
                            PlateService.get().createPosition(container, replicate, firstCol),
                            PlateService.get().createPosition(container, replicate, firstCol + 2));
                }
            }

            // add the control replicate groups
            for (int replicate = 0; replicate < template.getRows(); replicate++)
            {
                template.addWellGroup("Control, Replicate " + (replicate + 1), WellGroup.Type.REPLICATE,
                        PlateService.get().createPosition(container, replicate, 9),
                        PlateService.get().createPosition(container, replicate, 10));
            }
        }
        return template;
    }

    @Override
    public List<Pair<Integer, Integer>> getSupportedPlateSizes()
    {
        return Collections.singletonList(new Pair<>(8, 12));
    }

    @Override
    public List<WellGroup.Type> getWellGroupTypes()
    {
        return Arrays.asList(WellGroup.Type.CONTROL, WellGroup.Type.SPECIMEN, WellGroup.Type.REPLICATE);
    }
}
