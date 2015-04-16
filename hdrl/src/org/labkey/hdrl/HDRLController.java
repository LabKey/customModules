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
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.RequiresPermissionClass;
import org.labkey.api.security.permissions.InsertPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.VBox;
import org.labkey.api.view.ViewContext;
import org.labkey.hdrl.query.HDRLSchema;
import org.labkey.hdrl.view.InboundRequestBean;
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
            HtmlView submitView = new HtmlView("New Test Request", PageFlowUtil.textLink("Submit new test request", new ActionURL(EditRequestAction.class, getViewContext().getContainer())));
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
            SQLFragment sql = new SQLFragment("SELECT r.RequestId, r.ShippingNumber, r.Title, s.Name as RequestStatus, c.Name as ShippingCarrier, t.Name as TestType FROM ");
            sql.append("(SELECT * FROM hdrl.InboundRequest WHERE (Container = ?) AND (RequestId = ?)) r ");
            sql.add(getViewContext().getContainer());
            sql.add(Integer.parseInt(getViewContext().getRequest().getParameter("query.InboundRequestId~eq")));
            sql.append("JOIN hdrl.ShippingCarrier c on r.ShippingCarrierId = c.RowId ")
                    .append("JOIN hdrl.TestType t on r.TestTypeId = t.RowId ")
                    .append("JOIN hdrl.RequestStatus s on r.RequestStatusId = s.RowId ");
//            DbSchema dbSchema = schema.getDbSchema();
//            SQLFragment sql = new SQLFragment("SELECT r.RequestId, r.ShippingNumber, r.Title, s.Name as RequestStatus, c.Name as ShippingCarrier, t.Name as TestType FROM ");
//            sql.append("(SELECT * FROM ")
//                    .append(dbSchema.getTable("InboundRequest").getFromSQL())
//                    .append(" WHERE (Container = ?) AND (RequestId = ?)) r ");
//            sql.add(getViewContext().getContainer());
//            sql.add(Integer.parseInt(getViewContext().getRequest().getParameter("query.InboundRequestId~eq")));
//            sql.append("JOIN ").append(dbSchema.getTable("ShippingCarrier").getFromSQL("c")).append(" ON r.ShippingCarrierId = c.RowId ")
//                    .append("JOIN ").append(dbSchema.getTable("TestType").getFromSQL("t")).append(" ON r.TestTypeId = t.RowId ")
//                    .append("JOIN ").append(dbSchema.getTable("RequestStatus").getFromSQL("s")).append(" ON r.RequestStatusId = s.RowId ");


            SqlSelector sqlSelector = new SqlSelector(schema.getDbSchema(), sql);
            InboundRequestBean bean = sqlSelector.getObject(InboundRequestBean.class);
            JspView jsp = new JspView("/org/labkey/hdrl/view/requestDetails.jsp", bean);
            jsp.setTitle("Test Request");

            QuerySettings settings = schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, HDRLSchema.TABLE_SPECIMEN);
            QueryView queryView = schema.createView(getViewContext(), settings, errors);

            jsp.setView("queryView", queryView);

            return jsp;
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return root;
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class EditRequestAction extends SimpleViewAction<RequestForm>
    {
        private String _navLabel = "Create a new Test Request";

        @Override
        public ModelAndView getView(RequestForm form, BindException errors) throws Exception
        {
            if (form.getRequestId() != -1)
            {
                _navLabel = "Edit a Test Request";
            }
            return new JspView("/org/labkey/hdrl/view/editRequest.jsp", form, errors);
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }

    public static class RequestForm
    {
        private int _requestId = -1;

        public int getRequestId()
        {
            return _requestId;
        }

        public void setRequestId(int requestId)
        {
            _requestId = requestId;
        }
    }
}