/*
 * Copyright (c) 2013-2017 LabKey Corporation
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

import org.json.JSONObject;
import org.labkey.api.action.ApiAction;
import org.labkey.api.action.ApiResponse;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.SimpleApiJsonForm;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.data.Container;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.AdminOperationsPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.ViewContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * User: jeckels
 * Date: 1/18/13
 */
public class MS2ExtensionsController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(MS2ExtensionsController.class);

    public static final String PREFERENCES_KEY_NAME = MS2ExtensionsController.class.getName();

    public static final String TARGET_PROTEIN_PREFERENCE_NAME = "targetProtein";
    public static final String PEPTIDE_FILTER_PREFERENCE_NAME = "peptideFilter";
    public static final String TARGET_PROTEIN_PREFERENCE_MATCH_CRITERIA = "targetProteinMatchCriteria";

    public MS2ExtensionsController()
    {
        setActionResolver(_actionResolver);
    }

    public static String getTargetProteinPreference(ViewContext viewContext)
    {
        return getPreference(viewContext, TARGET_PROTEIN_PREFERENCE_NAME);
    }

    public static String getPeptideFilterPreference(ViewContext viewContext)
    {
        return getPreference(viewContext, PEPTIDE_FILTER_PREFERENCE_NAME);
    }

    public static String getTargetProteinMatchCriteria(ViewContext viewContext)
    {
        return getPreference(viewContext, TARGET_PROTEIN_PREFERENCE_MATCH_CRITERIA);
    }

    private static String getPreference(ViewContext viewContext, String preferenceName)
    {
        Map<String, String> props;
        if (viewContext.getUser().isGuest())
        {
            HttpSession session = viewContext.getRequest().getSession(true);
            props = (Map<String, String>)session.getAttribute(PREFERENCES_KEY_NAME);
            if (props == null)
            {
                return null;
            }
        }
        else
        {
            props = PropertyManager.getProperties(viewContext.getUser(), viewContext.getContainer(), PREFERENCES_KEY_NAME);
        }
        return props.get(preferenceName);
    }

    @RequiresPermission(AdminOperationsPermission.class)
    public class UpdatePeptideCountsAction extends SimpleViewAction<Object>
    {
        @Override
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            StringBuilder sb = new StringBuilder();
            updatePeptideCounts(sb, getContainer());
            if (sb.length() == 0)
            {
                sb.append("Success!");
            }
            else
            {
                sb.insert(0, "<div>Success, with warnings:</div>");
            }
            return new HtmlView(sb.toString());
        }

        private void updatePeptideCounts(StringBuilder sb, Container container)
        {
            String error = new PeptideCountUpdater().update(container, getUser());
            if (error != null)
            {
                sb.append("<div>");
                sb.append(PageFlowUtil.filter(error));
                sb.append("</div>");
            }
            for (Container child : container.getChildren())
            {
                updatePeptideCounts(sb, child);
            }
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild("Update Peptide Counts");
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class SetPreferencesAction extends ApiAction<SimpleApiJsonForm>
    {
        @Override
        public ApiResponse execute(SimpleApiJsonForm simpleApiJsonForm, BindException errors) throws Exception
        {
            Map<String, String> props;
            PropertyManager.PropertyMap mapToSave = null;
            if (getUser().isGuest())
            {
                // Use session for guests
                HttpSession session = getViewContext().getRequest().getSession(true);
                props = (Map<String, String>)session.getAttribute(PREFERENCES_KEY_NAME);
                if (props == null)
                {
                    props = new HashMap<>();
                    session.setAttribute(PREFERENCES_KEY_NAME, props);
                }
            }
            else
            {
                mapToSave = PropertyManager.getWritableProperties(getUser(), getContainer(), PREFERENCES_KEY_NAME, true);
                props = mapToSave;
            }

            JSONObject jsonObject = simpleApiJsonForm.getJsonObject();
            if (jsonObject.has(TARGET_PROTEIN_PREFERENCE_NAME))
            {
                props.put(TARGET_PROTEIN_PREFERENCE_NAME, jsonObject.getString(TARGET_PROTEIN_PREFERENCE_NAME));
            }
            if (jsonObject.has(PEPTIDE_FILTER_PREFERENCE_NAME))
            {
                props.put(PEPTIDE_FILTER_PREFERENCE_NAME, jsonObject.getString(PEPTIDE_FILTER_PREFERENCE_NAME));
            }
            if (jsonObject.has(TARGET_PROTEIN_PREFERENCE_MATCH_CRITERIA))
            {
                props.put(TARGET_PROTEIN_PREFERENCE_MATCH_CRITERIA, jsonObject.getString(TARGET_PROTEIN_PREFERENCE_MATCH_CRITERIA));
            }

            if (mapToSave != null)
            {
                mapToSave.save();
            }
            return new ApiSimpleResponse();
        }
    }
}
