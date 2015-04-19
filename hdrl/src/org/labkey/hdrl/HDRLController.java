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

import org.apache.commons.beanutils.BeanUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.labkey.api.action.ApiAction;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.SimpleApiJsonForm;
import org.labkey.api.action.SimpleResponse;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.RequiresPermissionClass;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.HttpRedirectView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.VBox;
import org.labkey.hdrl.query.HDRLQuerySchema;
import org.labkey.hdrl.query.InboundSpecimenUpdateService;
import org.labkey.hdrl.view.InboundRequestBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

            UserSchema schema = QueryService.get().getUserSchema(getUser(), getContainer(), HDRLQuerySchema.NAME);
            QuerySettings settings = schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, HDRLQuerySchema.TABLE_INBOUND_REQUEST);
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
            UserSchema schema = QueryService.get().getUserSchema(getUser(), getContainer(), HDRLQuerySchema.NAME);
            SQLFragment sql = new SQLFragment("SELECT r.RequestId, r.ShippingNumber, r.Title, s.Name as RequestStatus, c.Name as ShippingCarrier, t.Name as TestType FROM ");
            sql.append("(SELECT * FROM hdrl.InboundRequest WHERE (Container = ?) AND (RequestId = ?)) r ");
            sql.add(getViewContext().getContainer());
            sql.add(Integer.parseInt(getViewContext().getRequest().getParameter("query.InboundRequestId~eq")));
            sql.append("LEFT JOIN hdrl.ShippingCarrier c on r.ShippingCarrierId = c.RowId ")
                    .append("LEFT JOIN hdrl.TestType t on r.TestTypeId = t.RowId ")
                    .append("LEFT JOIN hdrl.RequestStatus s on r.RequestStatusId = s.RowId ");
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

            QuerySettings settings = schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, HDRLQuerySchema.TABLE_SPECIMEN);
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

                TableSelector selector = new TableSelector(org.labkey.hdrl.HDRLSchema.getInstance().getTableInfoInboundRequest());
                BeanUtils.copyProperties(form, selector.getObject(form.getRequestId(), RequestForm.class));
            }

            if (form.getRequestStatusId() >= 2)
                return new HtmlView("This request has been submitted and is locked from editing.");
            else
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
        private Integer _requestStatusId = 1;
        private Integer _shippingCarrierId;
        private String _shippingNumber;
        private Integer _testTypeId;

        public int getRequestId()
        {
            return _requestId;
        }

        public void setRequestId(int requestId)
        {
            _requestId = requestId;
        }

        public Integer getRequestStatusId()
        {
            return _requestStatusId;
        }

        public void setRequestStatusId(Integer requestStatusId)
        {
            _requestStatusId = requestStatusId;
        }

        public Integer getShippingCarrierId()
        {
            return _shippingCarrierId;
        }

        public void setShippingCarrierId(Integer shippingCarrierId)
        {
            _shippingCarrierId = shippingCarrierId;
        }

        public String getShippingNumber()
        {
            return _shippingNumber;
        }

        public void setShippingNumber(String shippingNumber)
        {
            _shippingNumber = shippingNumber;
        }

        public Integer getTestTypeId()
        {
            return _testTypeId;
        }

        public void setTestTypeId(Integer testTypeId)
        {
            _testTypeId = testTypeId;
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class VerifySpecimenAction extends ApiAction<VerifyForm>
    {
        @Override
        public Object execute(VerifyForm form, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            JSONArray rows = form.getJsonObject().getJSONArray("rows");
            List<Map<String, Object>> rowsToValidate = new ArrayList<>();

            for (int idx = 0; idx < rows.length(); ++idx)
            {
                JSONObject jsonObj;
                try
                {
                    jsonObj = rows.getJSONObject(idx);
                }
                catch (JSONException x)
                {
                    throw new IllegalArgumentException("rows[" + idx + "] is not an object.");
                }
                if (null != jsonObj)
                {
                    Map<String, Object> rowMap = new CaseInsensitiveHashMap();
                    rowMap.putAll(jsonObj);

                    rowsToValidate.add(rowMap);
                }
            }

            // validate the rows
            response.put("rows", rowsToValidate);
            for (Map<String, Object> row : rowsToValidate)
            {
                try
                {
                    InboundSpecimenUpdateService.validate(row);
                }
                catch (ValidationException e)
                {
                }
            }
            return response;
        }
    }

    public static class VerifyForm extends SimpleApiJsonForm
    {
    }
}