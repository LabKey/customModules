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
<%@ page import="org.labkey.api.util.UniqueID" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.labkey.api.view.ViewContext" %>
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="org.labkey.hdrl.HDRLController" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>

<%!
    public LinkedHashSet<ClientDependency> getClientDependencies()
    {
        LinkedHashSet<ClientDependency> resources = new LinkedHashSet<>();
        resources.add(ClientDependency.fromPath("Ext4"));
        resources.add(ClientDependency.fromPath("hdrl/requests.js"));
        resources.add(ClientDependency.fromPath("hdrl/fonts/barcode.css"));

        return resources;
    }
%>
<%
    JspView<HDRLController.RequestForm> me = (JspView<HDRLController.RequestForm>)HttpView.currentView();
    ViewContext ctx = getViewContext();
    Container c = getContainer();
    User user = getUser();
    HDRLController.RequestForm bean = me.getModelBean();

    String renderId = "requests-editor-" + UniqueID.getRequestScopedUID(HttpView.currentRequest());
%>
<style type="text/css">
    .labkey-warning  { color: red; }
</style>

<labkey:errors></labkey:errors>
<div id="<%= h(renderId)%>" class="requests-editor"></div>

<script type="text/javascript">
    Ext4.onReady(function(){

        Ext4.create('LABKEY.ext4.EditRequestPanel', {
            renderTo    : <%=q(renderId)%>,
            requestId   : <%=bean.getRequestId()%>,
            requestStatusId : <%=bean.getRequestStatusId()%>,
            shippingCarrierId : <%=bean.getShippingCarrierId()%>,
            testTypeId  : <%=bean.getTestTypeId()%>,
            shippingNumber : <%=q(bean.getShippingNumber())%>
        });
    });
</script>

