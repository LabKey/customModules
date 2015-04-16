<%
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
%>
<%@ page import="org.labkey.api.data.Container" %>
<%@ page import="org.labkey.api.security.User" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.hdrl.view.InboundRequestBean" %>
<%@ page import="org.labkey.hdrl.HDRLController" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    HttpView me = HttpView.currentView();
    InboundRequestBean bean = (InboundRequestBean) me.getModelBean();
%>
<div>
    Request <%= bean.getRequestId() %> - <%= bean.getTestType() %> (Status: <%= bean.getRequestStatus() %>)
    <% if (bean.getTitle() != null)
    {
    %>
        <%= bean.getTitle() %>
    <%
    }
    %>
</div>

<div>
    <% me.include(me.getView("queryView"),out); %>
</div>
<%
    if (bean.getShippingCarrier() != null)
    {
%>
<table>
    <tr>
        <td colspan="2" align="left">Shipping Information</td>
    </tr>
    <tr>
        <td>Carrier</td>
        <td> <%= bean.getShippingCarrier() %></td>
    </tr>
    <tr>
        <td>Tracking #</td>
        <td><%= bean.getShippingNumber() %></td>
    </tr>
</table>
<%
    }
%>
<div>
<%=PageFlowUtil.textLink("Return to test status", new ActionURL(HDRLController.BeginAction.class, getViewContext().getContainer()))%>
</div>


