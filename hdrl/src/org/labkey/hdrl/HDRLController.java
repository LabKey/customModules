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
import org.labkey.api.action.ExportAction;
import org.labkey.api.action.FormViewAction;
import org.labkey.api.action.SimpleApiJsonForm;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.admin.AdminUrls;
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.TableSelector;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.query.ValidationException;
import org.labkey.api.resource.Resource;
import org.labkey.api.security.RequiresPermissionClass;
import org.labkey.api.security.RequiresSiteAdmin;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.InsertPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.settings.AdminConsole;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.util.Path;
import org.labkey.api.util.URLHelper;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.HttpView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.NotFoundException;
import org.labkey.api.view.VBox;
import org.labkey.api.view.template.PageConfig;
import org.labkey.api.view.template.PrintTemplate;
import org.labkey.hdrl.query.HDRLQuerySchema;
import org.labkey.hdrl.query.InboundSpecimenUpdateService;
import org.labkey.hdrl.view.InboundRequestBean;
import org.labkey.hdrl.view.InboundSpecimenBean;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    public static void registerAdminConsoleLinks()
    {
        AdminConsole.addLink(AdminConsole.SettingsLinkType.Management, "HDRL Sensitive Data", new ActionURL(HDRLSensitiveDataAdminAction.class, ContainerManager.getRoot()));
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class BeginAction extends SimpleViewAction
    {
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            VBox vbox = new VBox();

            HtmlView submitView = new HtmlView("New Test Request", PageFlowUtil.textLink("Submit new test request", new ActionURL(EditRequestAction.class, getViewContext().getContainer())));
            vbox.addView(submitView);

            UserSchema schema = QueryService.get().getUserSchema(getUser(), getContainer(), HDRLQuerySchema.NAME);

            if(schema == null)
            {
                throw new NotFoundException("HDRL schema not found in the current folder.");
            }

            QuerySettings settings = schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, HDRLQuerySchema.TABLE_INBOUND_REQUEST);
            QueryView queryView = schema.createView(getViewContext(), settings, errors);
            queryView.setShowInsertNewButton(false);
            queryView.setShowImportDataButton(false);
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
            InboundRequestBean bean = HDRLManager.get().getInboundRequest(getUser(), getContainer(),Integer.parseInt(getViewContext().getRequest().getParameter("query.InboundRequestId~eq")) );
            UserSchema schema = QueryService.get().getUserSchema(getUser(), getContainer(), HDRLQuerySchema.NAME);
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

    @RequiresPermissionClass(InsertPermission.class)
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

            if (form.getRequestStatusId() >= 2 && !getContainer().hasPermission(getUser(), AdminPermission.class))
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

    @RequiresPermissionClass(ReadPermission.class)
    public class DownloadSpecimenTemplateAction extends ExportAction
    {
        @Override
        public void export(Object o, HttpServletResponse response, BindException errors) throws Exception
        {
            // just download the example file checked into the repository
            Module module = ModuleLoader.getInstance().getModule(HDRLModule.NAME);
            Path path = Path.parse("template/").append("specimenTemplate.xlsx");
            Resource r = module.getModuleResource(path);
            if (r != null && r.isFile())
            {
                try
                {
                    PageFlowUtil.streamFile(response, Collections.<String, String>emptyMap(), r.getName(), r.getInputStream(), false);
                }
                catch (IOException ioe)
                {
                    throw new RuntimeException(ioe);
                }
            }
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class PrintPackingListAction extends SimpleViewAction<PackingListBean>
    {
        @Override
        public ModelAndView getView(PackingListBean packingListBean, BindException errors) throws Exception
        {
            List<InboundSpecimenBean> inboundSpecimenRows = HDRLManager.get().getInboundSpecimen(packingListBean.getRequestId());
            InboundRequestBean inboundRequestBean = HDRLManager.get().getInboundRequest(getUser(), getContainer(), packingListBean.getRequestId());

            packingListBean.setInboundSpecimens(inboundSpecimenRows);
            packingListBean.setTotalSamples(inboundSpecimenRows.size());
            packingListBean.setTestType(inboundRequestBean.getTestType());
            packingListBean.setShippingNumber(inboundRequestBean.getShippingNumber());
            packingListBean.setShippingCarrier(inboundRequestBean.getShippingCarrier());

            JspView view = new JspView("/org/labkey/hdrl/view/printPackingList.jsp", packingListBean, errors);
            HttpView template = new PrintTemplate(view, "Shipping Manifest");
            getPageConfig().setTemplate(PageConfig.Template.Print);

            return template;
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return null;
        }
    }

    public static class PackingListBean
    {
        private int _requestId = -1;
        private String _shippingCarrier;
        private int _totalSamples;
        private String _testType;
        private String _shippingNumber;
        private List<InboundSpecimenBean> _inboundSpecimens;

        public int getRequestId()
        {
            return _requestId;
        }

        public void setRequestId(int requestId)
        {
            _requestId = requestId;
        }

        public List<InboundSpecimenBean> getInboundSpecimens()
        {
            return _inboundSpecimens;
        }

        public void setInboundSpecimens(List<InboundSpecimenBean> inboundSpecimens)
        {
            _inboundSpecimens = inboundSpecimens;
        }

        public String getShippingCarrier()
        {
            return _shippingCarrier;
        }

        public void setShippingCarrier(String shippingCarrier)
        {
            _shippingCarrier = shippingCarrier;
        }

        public int getTotalSamples()
        {
            return _totalSamples;
        }

        public void setTotalSamples(int totalSamples)
        {
            _totalSamples = totalSamples;
        }

        public String getTestType()
        {
            return _testType;
        }

        public void setTestType(String testType)
        {
            _testType = testType;
        }
        public String getShippingNumber()
        {
            return _shippingNumber;
        }

        public void setShippingNumber(String shippingNumber)
        {
            _shippingNumber = shippingNumber;
        }
    }

    @RequiresSiteAdmin
    public class HDRLSensitiveDataAdminAction extends FormViewAction<SensitiveDataForm>
    {
        private String _navLabel = "HDRL Sensitive Data Time Window";

        @Override
        public void validateCommand(SensitiveDataForm target, Errors errors)
        {
            if (target.getTimeWindowInDays() < 0)
            {
                errors.reject(ERROR_MSG, "'Number of Days' cannot be negative, it should be greater than or equal to 0.");
            }
        }

        @Override
        public ModelAndView getView(SensitiveDataForm sensitiveDataForm, boolean reshow, BindException errors) throws Exception
        {
            JspView view = new JspView("/org/labkey/hdrl/view/sensitiveData.jsp", sensitiveDataForm, errors);
            return view;
        }

        @Override
        public boolean handlePost(SensitiveDataForm sensitiveDataForm, BindException errors) throws Exception
        {
            HDRLManager.saveProperties(sensitiveDataForm);
            return true;
        }

        @Override
        public URLHelper getSuccessURL(SensitiveDataForm sensitiveDataForm)
        {
            return PageFlowUtil.urlProvider(AdminUrls.class).getAdminConsoleURL();
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }

    public static class SensitiveDataForm
    {
        private int timeWindowInDays = HDRLManager.getNumberOfDays();

        public int getTimeWindowInDays()
        {
            return timeWindowInDays;
        }

        public void setTimeWindowInDays(int timeWindowInDays)
        {
            this.timeWindowInDays = timeWindowInDays;
        }
    }
}