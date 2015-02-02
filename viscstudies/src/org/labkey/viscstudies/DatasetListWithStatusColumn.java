/*
 * Copyright (c) 2012-2015 LabKey Corporation
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
package org.labkey.viscstudies;

import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.DataColumn;
import org.labkey.api.data.RenderContext;
import org.labkey.api.data.views.DataViewProvider;
import org.labkey.api.reports.model.ReportPropsManager;
import org.labkey.api.settings.AppProps;
import org.labkey.api.study.Dataset;
import org.labkey.api.study.Study;
import org.labkey.api.study.StudyService;
import org.labkey.api.util.PageFlowUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Renders a table with all the study's datasets, one per row, including the dataset's status 
 * User: jeckels
 * Date: May 22, 2012
*/
public class DatasetListWithStatusColumn extends DataColumn
{
    /** The known set of dataset status and their icons */
    private static final Map<String, String> ICON_PATHS;

    static
    {
        Map<String, String> paths = new CaseInsensitiveHashMap<>();
        paths.put("Final", AppProps.getInstance().getContextPath() + "/reports/icon_final.png");
        paths.put("Draft", AppProps.getInstance().getContextPath() + "/reports/icon_draft.png");
        paths.put("Locked", AppProps.getInstance().getContextPath() + "/reports/icon_locked.png");
        paths.put("Unlocked", AppProps.getInstance().getContextPath() + "/reports/icon_unlocked.png");
        ICON_PATHS = Collections.unmodifiableMap(paths);
    }

    public DatasetListWithStatusColumn(ColumnInfo colInfo)
    {
        super(colInfo);
        setWidth("350");
    }

    /** Figure out the datasets associated with the study for this row */
    private List<? extends Dataset> getDatasets(RenderContext ctx)
    {
        Object value = getBoundColumn().getValue(ctx);
        if (value != null)
        {
            Container container = ContainerManager.getForId(value.toString());
            if (container != null)
            {
                Study study = StudyService.get().getStudy(container);
                if (study != null)
                {
                    return study.getDatasets();
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
    {
        // Show an icon for the dataset status (if set) and the dataset name, one per row, in a table
        out.write("<table>");
        for (Dataset dataset : getDatasets(ctx))
        {
            Object status = ReportPropsManager.get().getPropertyValue(dataset.getEntityId(), dataset.getContainer(), DataViewProvider.EditInfo.Property.status.toString());
            out.write("<tr><td style=\"width: 16px; border-style: none\">");
            if (status == null || "None".equalsIgnoreCase(status.toString()))
            {
                out.write("&nbsp;");
            }
            else
            {
                String iconPath = ICON_PATHS.get(status.toString());
                if (iconPath != null)
                {
                    out.write("<img src=\"" + PageFlowUtil.filter(iconPath) + "\" height=\"16px\" width=\"16px\" />");
                }
                else
                {
                    out.write(PageFlowUtil.filter(status));
                }
            }
            out.write("</td><td style=\"border-style: none\">");
            out.write(PageFlowUtil.filter(dataset.getLabel()));
            out.write("</td></tr>\n");
        }
        out.write("</table>");
    }

    @Override
    public Object getValue(RenderContext ctx)
    {
        // Create a string with one dataset/status pair per line, suitable for Excel or TSV export
        StringBuilder sb = new StringBuilder();
        String separator = "";
        for (Dataset dataset : getDatasets(ctx))
        {
            sb.append(separator);
            separator = "\n";
            Object status = ReportPropsManager.get().getPropertyValue(dataset.getEntityId(), dataset.getContainer(), DataViewProvider.EditInfo.Property.status.toString());
            if (status != null && !status.toString().isEmpty())
            {
                // Just emit the first character as an abbreviation
                sb.append(status.toString().charAt(0));
                sb.append(": ");
            }
            sb.append(dataset.getLabel());
        }
        return sb.toString();
    }

    @Override
    public Object getDisplayValue(RenderContext ctx)
    {
        return getValue(ctx);
    }
}
