<%
/*
 * Copyright (c) 2013-2019 LabKey Corporation
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
<%@ page import="org.labkey.api.util.GUID" %>
<%@ page import="org.labkey.api.view.ViewContext" %>
<%@ page import="org.labkey.ms2extensions.FilterView" %>
<%@ page import="org.labkey.ms2extensions.MS2ExtensionsController" %>
<%@ page import="org.labkey.ms2extensions.MS2ExtensionsController.SetPreferencesAction" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    ViewContext context = getViewContext();
    FilterView peptideView = new FilterView(context);
    String peptideCustomizeViewId = GUID.makeGUID();
    String targetProteinId = GUID.makeGUID();
    String matchCriteriaId = GUID.makeGUID();
    String matchCriteria = MS2ExtensionsController.getTargetProteinMatchCriteria(context);
    if (matchCriteria == null)
        matchCriteria = "Prefix";
%>
<fieldset>
    <legend>Comparison and Export Filters</legend>
    <table>
        <tr>
            <td class="labkey-form-label"><label for="<%= h(targetProteinId) %>">Target protein</label></td>
            <td><input type="text" id="<%= h(targetProteinId) %>" name="targetProtein" value="<%= h(MS2ExtensionsController.getTargetProteinPreference(context)) %>"/></td>
        </tr>
        <tr>
            <td class="labkey-form-label"><label for="<%= h(matchCriteriaId) %>">Match Criteria</label></td>
            <td>
                <select name="<%= h(MS2ExtensionsController.TARGET_PROTEIN_PREFERENCE_MATCH_CRITERIA) %>" id="<%= h(matchCriteriaId) %>" >
                    <option value="exact" <%=selected(("exact").equalsIgnoreCase(matchCriteria))%>>Exact</option>
                    <option value="prefix" <%=selected(("prefix").equalsIgnoreCase(matchCriteria))%>>Prefix</option>
                    <option value="suffix" <%=selected(("suffix").equalsIgnoreCase(matchCriteria))%>>Suffix</option>
                    <option value="substring" <%=selected(("substring").equalsIgnoreCase(matchCriteria))%>>Substring</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="labkey-form-label">Peptide filter</td>
            <td>
                <%
                    String peptideViewSelectId = peptideView.renderViewList(request, out, MS2ExtensionsController.getPeptideFilterPreference(context));
                %>

                <%=link("Create or Edit View").onClick("showViewDesigner('PeptidesFilter', '" + peptideCustomizeViewId + "', " + q(peptideViewSelectId) + ", viewSavedCallback); return false;")%>
            </td>
        </tr>
        <tr>
            <td></td>
            <td><span id="<%= h(peptideCustomizeViewId) %>"></span></td>
        </tr>
    </table>
</fieldset>

<script type="text/javascript" nonce="<%=getScriptNonce()%>">
    // Invoked by text link above
    function viewSavedCallback(arg1, viewInfo)
    {
        // Get the name of the newly saved view
        var viewName = viewInfo.views[0].name;
        // Make sure we're set to use the custom view
        var viewNamesSelect = document.getElementById(<%=q(peptideViewSelectId)%>);
        if (!viewNamesSelect)
        {
            window.location.reload();
        }
        else
        {
            // Check if it already exists in our list
            for (var i = 0; i < viewNamesSelect.options.length; i++)
            {
                if (viewNamesSelect.options[i].value === viewName)
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

    // Invoked from RunGridWebpart
    function viewPeptides(dataRegionName)
    {
        let dataRegion = LABKEY.DataRegions[dataRegionName];
        dataRegion.getSelected({ success:
            function(data) {
                // Translate from experiment run RowIds to MS2 run Ids
                LABKEY.Query.selectRows({
                    schemaName: 'ms2',
                    queryName: 'MS2SearchRuns',
                    containerFilter: dataRegion.containerFilter,
                    columns: 'MS2Details',
                    filterArray: [LABKEY.Filter.create('RowId', data.selected.join(';'), LABKEY.Filter.Types.EQUALS_ONE_OF)],
                    success: function(data) {
                        let runIds = [];
                        for (let x = 0; x < data.rows.length; x++) {
                            runIds.push(data.rows[x].MS2Details);
                        }

                        // Intentionally don't carry forward the target protein and peptide filters, as we only
                        // care about using the selected runs
                        window.location = LABKEY.ActionURL.buildURL('query', 'executeQuery', null, {
                                schemaName: 'ms2',
                                queryName: 'SequestPeptides',
                                'query.containerFilterName': dataRegion.containerFilter,
                                'query.Fraction/Run~in': runIds.join(';')
                            });
                    }
                })
            }
        })
    }

    // Invoked from RunGridWebpart
    function comparePeptides(dataRegionName)
    {
        handleRunGridButtonClick(dataRegionName, "proteinDisambiguationRedirect", { targetURL: LABKEY.ActionURL.buildURL('ms2', 'comparePeptideQuery')});
    }

    // Invoked from RunGridWebpart
    function spectraCount(dataRegionName)
    {
        handleRunGridButtonClick(dataRegionName, "proteinDisambiguationRedirect", { targetURL: LABKEY.ActionURL.buildURL('ms2', 'spectraCount'), spectraConfig: 'SpectraCountPeptideProtein' });
    }

    // Invoked from RunGridWebpart
    function exportPeptideBluemap(dataRegionName)
    {
        handleRunGridButtonClick(dataRegionName, "proteinDisambiguationRedirect", { targetURL: LABKEY.ActionURL.buildURL('ms2', 'exportComparisonProteinCoverageMap')});
    }

    function handleRunGridButtonClick(dataRegionName, actionTarget, urlParams)
    {
        urlParams = urlParams || {};
        var dataRegion = LABKEY.DataRegions[dataRegionName];
        var viewSelectElement = document.getElementById(<%=q(peptideViewSelectId)%>);
        var viewName = viewSelectElement ? viewSelectElement.value : '';
        var targetProtein = document.getElementById(<%=q(targetProteinId)%>).value;
        var matchCriteria = document.getElementById(<%=q(matchCriteriaId)%>).value;

        // Fire off an AJAX request so that we repopulate with the last used values
        var preferencesURL = <%=q(urlFor(SetPreferencesAction.class))%>;
        Ext4.Ajax.request({ url: preferencesURL, method: 'POST', jsonData:
        {
            peptideFilter : viewName,
            targetProtein : targetProtein,
            targetProteinMatchCriteria: matchCriteria
        }});

        LABKEY.Experiment.createHiddenRunGroup({selectionKey: dataRegion.selectionKey, success: function(runGroup, response)
        {
            Ext4.apply(urlParams, {
                runList: runGroup.id,
                peptideFilterType: 'customView',
                'PeptidesFilter.viewName': viewName,
                targetProtein: targetProtein,
                targetProteinMatchCriteria: matchCriteria
            });

            LABKEY.Utils.postToAction(LABKEY.ActionURL.buildURL("ms2", actionTarget, LABKEY.ActionURL.getContainer(), urlParams));
        }});
    }
</script>
