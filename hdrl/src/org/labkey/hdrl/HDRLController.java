/*
 * Copyright (c) 2015 LabKey Corporation
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

package org.labkey.hdrl;

import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.RequiresPermissionClass;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.VBox;
import org.labkey.hdrl.query.HDRLSchema;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class HDRLController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(HDRLController.class);
    public static final String NAME = "hdrl";

    public HDRLController()
    {
        setActionResolver(_actionResolver);
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class BeginAction extends SimpleViewAction
    {
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            VBox vbox = new VBox();

            // TODO change this to edit action when available
            HtmlView submitView = new HtmlView("New Test Request", PageFlowUtil.textLink("Submit new test request", new ActionURL(RequestDetailsAction.class, getViewContext().getContainer())));
            vbox.addView(submitView);

            UserSchema schema = QueryService.get().getUserSchema(getUser(), getContainer(), HDRLSchema.NAME);
            QuerySettings settings = schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, HDRLSchema.TABLE_INBOUND_REQUEST);
            QueryView queryView = schema.createView(getViewContext(), settings, errors);
            vbox.addView(queryView);
            return vbox;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root;
        }
    }


    @RequiresPermissionClass(ReadPermission.class)
    public class RequestDetailsAction extends SimpleViewAction
    {
        @Override
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            UserSchema schema = QueryService.get().getUserSchema(getUser(), getContainer(), HDRLSchema.NAME);
            QuerySettings settings = schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, HDRLSchema.TABLE_SPECIMEN);
            QueryView queryView = schema.createView(getViewContext(), settings, errors);
            return queryView;
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return root;
        }
    }
}