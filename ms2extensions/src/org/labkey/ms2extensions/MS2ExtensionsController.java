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
package org.labkey.ms2extensions;

import org.json.JSONObject;
import org.labkey.api.action.ApiResponse;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.MutatingApiAction;
import org.labkey.api.action.SimpleApiJsonForm;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.admin.AdminUrls;
import org.labkey.api.data.Container;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.AdminOperationsPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.util.DOM;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.ViewContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        public ModelAndView getView(Object o, BindException errors)
        {
            if (isPost())
            {
                List<DOM.Renderable> warnings = new ArrayList<>();
                updatePeptideCounts(warnings, getContainer());
                if (warnings.size() == 0)
                {
                    return new HtmlView(DOM.DIV("Success!"));
                }

                warnings.add(0, DOM.DIV("Success, with warnings:"));
                return new HtmlView(DOM.DIV(warnings.toArray(new Object[0])));
            }
            return new HtmlView(DOM.LK.FORM(
                    DOM.at(DOM.Attribute.method, "POST"),
                    DOM.BUTTON("Update peptide counts")));
        }

        private void updatePeptideCounts(List<DOM.Renderable> warnings, Container container)
        {
            String warning = new PeptideCountUpdater().update(container, getUser());
            if (warning != null)
            {
                warnings.add(DOM.DIV(warning));
            }
            for (Container child : container.getChildren())
            {
                updatePeptideCounts(warnings, child);
            }
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            urlProvider(AdminUrls.class).addAdminNavTrail(root, "Update Peptide Counts", getClass(), getContainer());
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class SetPreferencesAction extends MutatingApiAction<SimpleApiJsonForm>
    {
        @Override
        public ApiResponse execute(SimpleApiJsonForm simpleApiJsonForm, BindException errors)
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

            JSONObject jsonObject = simpleApiJsonForm.getNewJsonObject();
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
