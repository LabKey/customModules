<%
/*
 * Copyright (c) 2015-2016 LabKey Corporation
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
<%@ page import="org.labkey.api.admin.AdminUrls" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.labkey.hdrl.HDRLController" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>

<%
    JspView<HDRLController.SensitiveDataForm> sensitiveDataFormJspView = (JspView<HDRLController.SensitiveDataForm>)HttpView.currentView();
    HDRLController.SensitiveDataForm bean = sensitiveDataFormJspView.getModelBean();
    String timeWindow = String.valueOf(bean.getTimeWindowInDays());
%>
<labkey:errors></labkey:errors>
<br>
<labkey:form method="post">
    Number of Days<%= PageFlowUtil.helpPopup("Number of Days", "Enter days after which sensitive data will be deleted.")%>:
    <input type="number" name="timeWindowInDays" value=<%=h(timeWindow)%> min="0">
    <tr>
        <td colspan=2>
            <br>
            <%= button("Save").submit(true) %>
            <%= button("Cancel").href(PageFlowUtil.urlProvider(AdminUrls.class).getAdminConsoleURL())%>
        </td>
    </tr>
</labkey:form>