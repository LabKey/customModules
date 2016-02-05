/*
 * Copyright (c) 2016 LabKey Corporation
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
package org.labkey.sequencenum;

import org.labkey.api.data.TableCustomizer;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.FieldKey;
import org.labkey.api.study.Dataset;
import org.labkey.api.study.DatasetTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: jeckels
 * Date: 10/20/15
 */
public class DatasetVisitColumnCustomizer implements TableCustomizer
{
    @Override
    public void customize(TableInfo tableInfo)
    {
        if (tableInfo instanceof DatasetTable)
        {
            Dataset dataset = ((DatasetTable)tableInfo).getDataset();
            List<FieldKey> defaultCols = new ArrayList<>(tableInfo.getDefaultVisibleColumns());

            // First, try to swap out the ParticipantVisit/Visit column
            FieldKey visitFK = FieldKey.fromParts("ParticipantVisit", "Visit");
            int visitIndex = defaultCols.indexOf(visitFK);
            if (visitIndex >= 0)
            {
                if (!dataset.isDemographicData())
                {
                    defaultCols.add(visitIndex, FieldKey.fromParts("SequenceNum"));
                }
                defaultCols.remove(visitFK);
            }
            else if (!dataset.isDemographicData())
            {
                // If that's not found, add SequenceNum right after ParticipantID
                int ptidIndex = defaultCols.indexOf(FieldKey.fromParts("ParticipantID"));
                if (ptidIndex >= 0)
                {
                    defaultCols.add(ptidIndex + 1, FieldKey.fromParts("SequenceNum"));
                }
                else
                {
                    // Fall through - add to the end of the list
                    defaultCols.add(FieldKey.fromParts("SequenceNum"));
                }
            }
            tableInfo.setDefaultVisibleColumns(defaultCols);
        }
    }
}
