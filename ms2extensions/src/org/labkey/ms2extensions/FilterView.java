/*
 * Copyright (c) 2013 LabKey Corporation
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

package org.labkey.ms2extensions;

import org.labkey.api.ms2.MS2Service;
import org.labkey.api.query.CustomView;
import org.labkey.api.query.QueryParam;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.util.GUID;
import org.labkey.api.view.ViewContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * User: jeckels
 * Date: Mar 3, 2008
 */
public class FilterView extends QueryView
{
    public FilterView(ViewContext context)
    {
        super(MS2Service.get().createSchema(context.getUser(), context.getContainer()));
        setSettings(createSettings(context));
    }

    private QuerySettings createSettings(ViewContext context)
    {
        return getSchema().getSettings(context, "PeptidesFilter", "PeptidesFilter");
    }

    public String renderViewList(HttpServletRequest request, Writer out, String viewName) throws IOException
    {
        Map<String, CustomView> customViews = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, CustomView> savedViews = getQueryDef().getCustomViews(getUser(), request, false, false);
        savedViews.remove(null);
        customViews.putAll(savedViews);
        Map<String, String> options = new LinkedHashMap<>();
        options.put("", "<default>");
        for (CustomView view : customViews.values())
        {
            String label = view.getName();
            if (label == null)
                continue;
            options.put(view.getName(), label);
        }

        if (options.size() <= 1)
            return null;
        String id = GUID.makeGUID();
        out.write("<select id=\"" + id + "\" name=\"" + h(param(QueryParam.viewName)) + "\"");
        out.write(">");
        for (Map.Entry<?, String> entry : options.entrySet())
        {
            out.write("\n<option");
            if (Objects.equals(entry.getKey(), viewName))
            {
                out.write(" selected");
            }
            out.write(" value=\"");
            out.write(h(entry.getKey()));
            out.write("\">");
            out.write(h(entry.getValue()));
            out.write("</option>");
        }
        out.write("</select>");
        return id;
    }
}
