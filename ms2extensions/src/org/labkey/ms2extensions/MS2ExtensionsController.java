package org.labkey.ms2extensions;

import org.json.JSONObject;
import org.labkey.api.action.ApiAction;
import org.labkey.api.action.ApiResponse;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.SimpleApiJsonForm;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.security.RequiresPermissionClass;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.view.ViewContext;
import org.springframework.validation.BindException;

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

    @RequiresPermissionClass(ReadPermission.class)
    public class SetPreferencesAction extends ApiAction<SimpleApiJsonForm>
    {
        @Override
        public ApiResponse execute(SimpleApiJsonForm simpleApiJsonForm, BindException errors) throws Exception
        {
            Map<String, String> props;
            if (getUser().isGuest())
            {
                // Use session for guests
                HttpSession session = getViewContext().getRequest().getSession(true);
                props = (Map<String, String>)session.getAttribute(PREFERENCES_KEY_NAME);
                if (props == null)
                {
                    props = new HashMap<String, String>();
                    session.setAttribute(PREFERENCES_KEY_NAME, props);
                }
            }
            else
            {
                props = PropertyManager.getWritableProperties(getUser(), getContainer(), PREFERENCES_KEY_NAME, true);
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

            if (!getUser().isGuest())
            {
                PropertyManager.saveProperties(props);
            }
            return new ApiSimpleResponse();
        }
    }
}
