<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.ms2extensions.FilterView" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.util.GUID" %>
<%@ page import="org.labkey.ms2extensions.MS2ExtensionsController" %>
<%@ page import="org.labkey.api.view.ViewContext" %>
<%
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
%>
<%@ page extends="org.labkey.api.jsp.JspBase" %>

<%
    ViewContext context = HttpView.currentContext();
    FilterView peptideView = new FilterView(context);
    String peptideCustomizeViewId = GUID.makeGUID();
    String targetProteinId = GUID.makeGUID();
%>
<fieldset>
    <legend>Comparison and Export Filters</legend>
    <table>
        <tr>
            <td class="labkey-form-label"><label for="<%= text(targetProteinId) %>">Target protein</label></td>
            <td><input type="text" id="<%= text(targetProteinId) %>" name="targetProtein" value="<%= h(MS2ExtensionsController.getTargetProteinPreference(context)) %>"/></td>
        </tr>
        <tr>
            <td class="labkey-form-label">Peptide filter</td>
            <td>
                <%
                    String peptideViewSelectId = peptideView.renderViewList(request, out, MS2ExtensionsController.getPeptideFilterPreference(context));
                %>

                <%= PageFlowUtil.textLink("Create or Edit View", (ActionURL)null, "showViewDesigner('PeptidesFilter', '" + peptideCustomizeViewId + "', " + PageFlowUtil.jsString(peptideViewSelectId) + ", viewSavedCallback); return false;", "editPeptidesViewLink") %>
            </td>
        </tr>
        <tr>
            <td></td>
            <td><span id="<%= h(peptideCustomizeViewId) %>"></span></td>
        </tr>
    </table>
</fieldset>

<script type="text/javascript">
    function viewSavedCallback(arg1, viewInfo)
    {
        // Get the name of the newly saved view
        var viewName = viewInfo.views[0].name;
        // Make sure we're set to use the custom view
        var viewNamesSelect = document.getElementById(<%= PageFlowUtil.jsString(peptideViewSelectId) %>);
        if (!viewNamesSelect)
        {
            window.location.reload();
        }
        else
        {
            // Check if it already exists in our list
            for (var i = 0; i < viewNamesSelect.options.length; i++)
            {
                if (viewNamesSelect.options[i].value == viewName)
                {
                    // If so, select it
                    viewNamesSelect.options[i].selected = true;
                    return;
                }
            }
            // Otherwise, add it as a new option
            viewNamesSelect.options[viewNamesSelect.options.length] = new Option(viewName, viewName, false, true);
        }
    }

    function comparePeptides(dataRegionName)
    {
        handleRunGridButtonClick(dataRegionName, "comparePeptideQuery");
    }

    function handleRunGridButtonClick(dataRegionName, actionTarget)
    {
        var dataRegion = LABKEY.DataRegions[dataRegionName];
        var runIds = dataRegion.getChecked();
        var viewSelectElement = document.getElementById(<%= PageFlowUtil.jsString(peptideViewSelectId)%>);
        var viewName = viewSelectElement ? viewSelectElement.value : '';
        var targetProtein = document.getElementById(<%= PageFlowUtil.jsString(targetProteinId)%>).value;

        // Fire off an AJAX request so that we repopulate with the last used values
        var preferencesURL = <%= PageFlowUtil.jsString(new ActionURL(MS2ExtensionsController.SetPreferencesAction.class, context.getContainer()).toString())%>;
        Ext4.Ajax.request({ url: preferencesURL, jsonData:
        {
            peptideFilter : viewName,
            targetProtein : targetProtein
        }});

        LABKEY.Experiment.createHiddenRunGroup({runIds: runIds, success: function(runGroup, response)
        {
            var urlParams =
            {
                runList: runGroup.id,
                peptideFilterType: 'customView',
                'PeptidesFilter.viewName': viewName,
                targetProtein: targetProtein
            };

            window.location = LABKEY.ActionURL.buildURL("ms2", actionTarget, LABKEY.ActionURL.getContainer(), urlParams);
        }});
    }

    function exportPeptideBluemap(dataRegionName)
    {
        handleRunGridButtonClick(dataRegionName, "exportComparisonProteinCoverageMap");
    }
</script>