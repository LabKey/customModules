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
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.util.UniqueID" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.template.ClientDependencies" %>
<%@ page import="org.labkey.hdrl.HDRLController" %>
<%@ page import="org.labkey.hdrl.view.InboundRequestBean" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%!
    @Override
    public void addClientDependencies(ClientDependencies dependencies)
    {
        dependencies.add("Ext4");
        dependencies.add("hdrl/fonts/barcode.css");
    }
%>
<%
    HttpView me = HttpView.currentView();
    InboundRequestBean bean = (InboundRequestBean) me.getModelBean();
%>
<div>
    <strong>Request <%= bean.getRequestId() %> - <%=h(bean.getTestType()) %></strong> (Status: <%= h(bean.getRequestStatus()) %>)
</div>
<br>

<div>
    <% me.include(me.getView("queryView"),out); %>
</div>
<%
    String renderId = "labkey-wp-"+ UniqueID.getRequestScopedUID(HttpView.currentRequest());

    if (bean.getShippingCarrier() != null)
    {
%>
<br>
<table>
    <tr>
        <td colspan="2" align="left"><strong>Shipping Information</strong></td>
    </tr>
    <tr>
        <td>Carrier</td>
        <td> <%= h(bean.getShippingCarrier()) %></td>
    </tr>
    <tr>
        <td>Tracking #</td>
        <td><%= h(bean.getShippingNumber() == null ? "unknown" : bean.getShippingNumber()) %></td>
    </tr>
</table>
<%
    }
%>

<br>
<div id="<%= h(renderId)%>" class="labkey-wp-body"></div>
<br>

<script type="text/javascript">
    Ext4.onReady(function()
    {
        Ext4.create('Ext.button.Button', {
            text: 'Print Packing List',
            renderTo: <%=q(renderId)%>,
            width: 150,
            handler: function(){
                window.open(LABKEY.ActionURL.buildURL('hdrl', 'printPackingList', null, {
                    requestId : <%=h(bean.getRequestId())%>
                }));
            }
        });
    });
</script>

<div>
    <%=PageFlowUtil.textLink("Return to test status", new ActionURL(HDRLController.BeginAction.class, getViewContext().getContainer()))%>
</div>
