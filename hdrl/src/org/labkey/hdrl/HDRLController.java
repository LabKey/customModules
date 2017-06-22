/*
 * Copyright (c) 2015-2017 LabKey Corporation
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
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.labkey.api.action.ApiAction;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.ExportAction;
import org.labkey.api.action.FormViewAction;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.action.SimpleApiJsonForm;
import org.labkey.api.action.SimpleErrorView;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.admin.AdminUrls;
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.Results;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.TableSelector;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.query.ValidationException;
import org.labkey.api.resource.Resource;
import org.labkey.api.security.AdminConsoleAction;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.InsertPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.settings.AdminConsole;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.util.Path;
import org.labkey.api.util.URLHelper;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.NotFoundException;
import org.labkey.api.view.VBox;
import org.labkey.api.view.template.PageConfig;
import org.labkey.hdrl.query.HDRLQuerySchema;
import org.labkey.hdrl.query.InboundSpecimenUpdateService;
import org.labkey.hdrl.query.LabWareQuerySchema;
import org.labkey.hdrl.view.InboundRequestBean;
import org.labkey.hdrl.view.InboundSpecimenBean;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
        AdminConsole.addLink(AdminConsole.SettingsLinkType.Management, "HDRL Sensitive Data", new ActionURL(HDRLSensitiveDataAdminAction.class, ContainerManager.getRoot()), AdminPermission.class);
    }

    @RequiresPermission(ReadPermission.class)
    public class BeginAction extends SimpleViewAction
    {
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            VBox vbox = new VBox();

            HtmlView submitView = new HtmlView("New Test Request", PageFlowUtil.textLink("Submit new test request", new ActionURL(EditRequestAction.class, getViewContext().getContainer())));
            vbox.addView(submitView);

            UserSchema schema = QueryService.get().getUserSchema(getUser(), getContainer(), HDRLQuerySchema.NAME);

            if (schema == null)
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


    @RequiresPermission(ReadPermission.class)
    public class RequestDetailsAction extends SimpleViewAction
    {
        @Override
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            String requestId = getViewContext().getRequest().getParameter("requestId");
            if (requestId != null)
            {
                InboundRequestBean bean = HDRLManager.get().getInboundRequest(getUser(), getContainer(), Integer.parseInt(requestId));
                JspView jsp = new JspView("/org/labkey/hdrl/view/requestDetails.jsp", bean);
                jsp.setTitle("Test Request");

                UserSchema schema = QueryService.get().getUserSchema(getUser(), getContainer(), HDRLQuerySchema.NAME);
                QuerySettings settings = schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, HDRLQuerySchema.TABLE_SPECIMENS);
                QueryView queryView = schema.createView(getViewContext(), settings, errors);

                jsp.setView("queryView", queryView);
                return jsp;
            }
            else
            {
                errors.reject("RequestId is required");
                return new SimpleErrorView(errors);
            }
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return root;
        }
    }

    @RequiresPermission(InsertPermission.class)
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
            else
            {
                form.setTestTypeId(1); // default to first test type
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

    @RequiresPermission(ReadPermission.class)
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


    @RequiresPermission(ReadPermission.class)
    public class DownloadClinicalReportAction extends ExportAction<SpecimenForm>
    {
        @Override
        public void export(SpecimenForm form, HttpServletResponse response, BindException errors) throws Exception
        {
            LabWareQuerySchema lwSchema = new LabWareQuerySchema(getUser(), getContainer());
            SimpleFilter filter = new SimpleFilter(FieldKey.fromParts("test_request_id"), form.getSpecimenId());
            TableSelector selector = new TableSelector(lwSchema.getSchema().getTable(LabWareQuerySchema.TABLE_OUTBOUND_SPECIMENS), filter, null);

            Results result = selector.getResults(false);
            try
            {
                if (!result.next())
                    throw new NotFoundException("No report available for specimen " + form.getSpecimenId());

                String contentType = "application/pdf";


                byte[] documentBytes = IOUtils.toByteArray(result.getBinaryStream("clinical_report"));

                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + result.getString("report_file_name") + "\"");
                response.setContentLength(documentBytes.length);
                response.getOutputStream().write(documentBytes);
            }
            finally
            {
                result.close();
            }
        }

        @Override
        public void validate(SpecimenForm form, BindException errors)
        {
            if (form.getSpecimenId() == null)
            {
                errors.reject(ERROR_MSG, "SpecimenId is required");
            }
        }
    }

    public static class SpecimenForm
    {
        private Integer _specimenId;

        public Integer getSpecimenId()
        {
            return _specimenId;
        }

        public void setSpecimenId(Integer specimenId)
        {
            _specimenId = specimenId;
        }
    }

    @RequiresPermission(ReadPermission.class)
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
                    PageFlowUtil.streamFile(response, Collections.emptyMap(), r.getName(), r.getInputStream(), false);
                }
                catch (IOException ioe)
                {
                    throw new RuntimeException(ioe);
                }
            }
        }
    }

    @RequiresPermission(ReadPermission.class)
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
            getPageConfig().setTitle("Shipping Manifest");
            getPageConfig().setTemplate(PageConfig.Template.Print);

            return view;
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

    @AdminConsoleAction
    @RequiresPermission(AdminPermission.class)
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

    @RequiresPermission(AdminPermission.class)
    @Marshal(Marshaller.Jackson)
    public class AddLabWareOutboundRequestAction extends ApiAction<LabWareOutboundRequestForm>
    {
        @Override
        public Object execute(LabWareOutboundRequestForm form, BindException errors) throws Exception
        {
            HDRLManager.get().insertLabWareOutboundRequest(form, getUser(), getContainer());

            return success();
        }

        @Override
        public void validateForm(LabWareOutboundRequestForm form, Errors errors)
        {
            if (form.getBatchId() == null)
                errors.rejectValue("batchId", ERROR_MSG, "batchId is required");
            if (form.getDateReceived() == null)
                errors.rejectValue("dateReceived", ERROR_MSG, "dateReceived is required");
            if (form.getDateModified() == null)
                errors.rejectValue("dateModified", ERROR_MSG, "dateModified is required");
            if (form.getHdrlStatus() == null)
                errors.rejectValue("hdrlStatus", ERROR_MSG, "hdrlStatus is required");
        }
    }

    public static class LabWareOutboundRequestForm
    {
        private Integer _batchId;
        private String _hdrlStatus;
        private String _customerNote;
        private Date _dateReceived;
        private Date _dateCompleted;
        private Date _dateModified;

        public Integer getBatchId()
        {
            return _batchId;
        }

        public void setBatchId(Integer batchId)
        {
            _batchId = batchId;
        }

        public String getCustomerNote()
        {
            return _customerNote;
        }

        public void setCustomerNote(String customerNote)
        {
            _customerNote = customerNote;
        }

        public Date getDateCompleted()
        {
            return _dateCompleted;
        }

        public void setDateCompleted(Date dateCompleted)
        {
            _dateCompleted = dateCompleted;
        }

        public Date getDateReceived()
        {
            return _dateReceived;
        }

        public void setDateReceived(Date dateReceived)
        {
            _dateReceived = dateReceived;
        }

        public String getHdrlStatus()
        {
            return _hdrlStatus;
        }

        public void setHdrlStatus(String hdrlStatus)
        {
            _hdrlStatus = hdrlStatus;
        }

        public Date getDateModified()
        {
            return _dateModified;
        }

        public void setDateModified(Date dateModified)
        {
            _dateModified = dateModified;
        }
    }

    @RequiresPermission(AdminPermission.class)
    @Marshal(Marshaller.Jackson)
    public class AddLabWareOutboundSpecimenAction extends ApiAction<LabWareOutboundSpecimenForm>
    {
        @Override
        public Object execute(LabWareOutboundSpecimenForm form, BindException errors) throws Exception
        {
            HDRLManager.get().insertLabWareOutboundSpecimen(form, getUser(), getContainer());

            return success();
        }

        @Override
        public void validateForm(LabWareOutboundSpecimenForm form, Errors errors)
        {
            if (form.getTestRequestId() == null)
                errors.rejectValue("testRequestId", ERROR_MSG, "testRequestId is required");
            if (form.getBatchId() == null)
                errors.rejectValue("batchId", ERROR_MSG, "batchId is required");
            if (form.getDateReceived() == null)
                errors.rejectValue("dateReceived", ERROR_MSG, "dateReceived is required");
            if (form.getDateModified() == null)
                errors.rejectValue("dateModified", ERROR_MSG, "dateModified is required");
            if (form.getHdrlStatus() == null)
                errors.rejectValue("hdrlStatus", ERROR_MSG, "hdrlStatus is required");
        }
    }

    public static class LabWareOutboundSpecimenForm
    {
        private Integer _testRequestId;
        private Integer _batchId;
        private Date _dateReceived;
        private Date _dateCompleted;
        private String _sampleIntegrity;
        private String _testResult;
        private String _customerCode;
        private String _clinicalReportFile;
        private String _hdrlStatus;
        private Date _dateModified;
        private String _modifiedResultFlag;
        private String _reportFileName;

        public Integer getBatchId()
        {
            return _batchId;
        }

        public void setBatchId(Integer batchId)
        {
            _batchId = batchId;
        }

        public String getClinicalReportFile()
        {
            return _clinicalReportFile;
        }

        public void setClinicalReportFile(String clinicalReportFile)
        {
            _clinicalReportFile = clinicalReportFile;
        }

        public String getHdrlStatus()
        {
            return _hdrlStatus;
        }

        public void setHdrlStatus(String hdrlStatus)
        {
            _hdrlStatus = hdrlStatus;
        }

        public String getCustomerCode()
        {
            return _customerCode;
        }

        public void setCustomerCode(String customerCode)
        {
            _customerCode = customerCode;
        }

        public Date getDateCompleted()
        {
            return _dateCompleted;
        }

        public void setDateCompleted(Date dateCompleted)
        {
            _dateCompleted = dateCompleted;
        }

        public Date getDateModified()
        {
            return _dateModified;
        }

        public void setDateModified(Date dateModified)
        {
            _dateModified = dateModified;
        }

        public Date getDateReceived()
        {
            return _dateReceived;
        }

        public void setDateReceived(Date dateReceived)
        {
            _dateReceived = dateReceived;
        }

        public String getModifiedResultFlag()
        {
            return _modifiedResultFlag;
        }

        public void setModifiedResultFlag(String modifiedResultFlag)
        {
            _modifiedResultFlag = modifiedResultFlag;
        }

        public String getSampleIntegrity()
        {
            return _sampleIntegrity;
        }

        public void setSampleIntegrity(String sampleIntegrity)
        {
            _sampleIntegrity = sampleIntegrity;
        }

        public Integer getTestRequestId()
        {
            return _testRequestId;
        }

        public void setTestRequestId(Integer testRequestId)
        {
            _testRequestId = testRequestId;
        }

        public String getTestResult()
        {
            return _testResult;
        }

        public void setTestResult(String testResult)
        {
            _testResult = testResult;
        }

        public String getReportFileName()
        {
            return _reportFileName;
        }

        public void setReportFileName(String reportFileName)
        {
            _reportFileName = reportFileName;
        }
    }

}