/*
 * Copyright (c) 2014 LabKey Corporation
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

import org.labkey.api.data.ButtonBar;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.exp.ExperimentRunType;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.view.DataView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.ViewContext;
import org.labkey.api.view.template.ClientDependency;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

/**
 * Created by: jeckels
 * Date: 10/22/14
 */
public class MS2ExtensionsExperimentRunType extends ExperimentRunType
{
    public MS2ExtensionsExperimentRunType()
    {
        super("MS2Extensions Runs", "ms2", "MS2SearchRuns");
    }

    @Override
    public Priority getPriority(ExpProtocol object)
    {
        // If the "MS2 Searches" handler matches, claim the object with a higher priority
        ExperimentRunType standardMS2RunType = ExperimentService.get().getExperimentRunType("MS2 Searches", null);
        if (standardMS2RunType != null && standardMS2RunType.getPriority(object) != null)
        {
            return Priority.HIGHEST;
        }
        return null;
    }

    @Override
    public void populateButtonBar(ViewContext context, ButtonBar bar, DataView view, ContainerFilter containerFilter)
    {
        RunGridWebPart.populateButtonBar(bar, view.getDataRegion().getName());
    }

    @Override
    public void renderHeader(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        JspView<ViewContext> v = new JspView<>("/org/labkey/ms2extensions/runGridFilters.jsp");
        v.addClientDependencies(Collections.singleton(ClientDependency.fromPath("/MS2/inlineViewDesigner.js")));
        v.render(request, response);

        response.getWriter().print("<p/>\n");
    }
}
