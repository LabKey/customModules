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
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="org.labkey.api.view.template.PrintTemplate" %>
<%@ page import="org.labkey.hdrl.HDRLController.RequestForm" %>
<%@ page import="org.labkey.hdrl.view.InboundSpecimenBean" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.List" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%!
    public LinkedHashSet<ClientDependency> getClientDependencies()
    {
        LinkedHashSet<ClientDependency> resources = new LinkedHashSet<>();
        resources.add(ClientDependency.fromPath("hdrl/fonts/barcode.css"));
        return resources;
    }
%>
<%
    HttpView httpView =  HttpView.currentView();
    RequestForm requestForm = (RequestForm) httpView.getModelBean();
    List<InboundSpecimenBean> inboundSpecimens = requestForm.getInboundSpecimens();


    //TODO: find out how to show this view in a new window
%>

<header>
    <h1 align="center">Shipping Manifest</h1>
</header>
<section>
    <h3 align="center"><i>HIPAA warning message here</i></h3>
</section>
<table>
    <table>
        <td>
            <tr>Customer Name:</tr>
            <br>
            <tr>Customer Address: </tr>
            <br>
            <br>
            <tr><b>Shipping Information:</b></tr>
            <br>
            <tr>Courier:</tr>
            <br>
            <tr>Tracking Number:</tr>
        </td>
    </table>
    <table>
        <tr>Total Samples: </tr>
        <br>
        <tr>Test Requested (if bulk request)</tr>
        <br>
        <tr>Date: </tr>
    </table>

</table>

<table style="width:100%">
    <tr bgcolor="#d3d3d3">
        <th>Barcode</th>
        <th>FMP/SSN</th>
        <th>Last Name</th>
        <th>First Name</th>
        <th>Middle Name</th>
        <th>Date of Birth</th>
        <th>SOT</th>
        <th>DUC</th>
        <th>Specimen Type</th>
        <th>Test Requested</th>
    </tr>
    <%
        int count = 1;
        for(InboundSpecimenBean isb : inboundSpecimens)
        {
            if(count % 2 == 0)
            {
    %>
                <tr bgcolor="#d3d3d3" align="center">
    <%
            }
            else
            {
    %>
                <tr align="center">
    <%
            }
            count++;
    %>

            <td><div class="barcode"><%=h(isb.getCustomerBarCode())%></div></td>
            <td><div class="barcode"> <%=h(Integer.parseInt(isb.getFmpId())+Integer.parseInt(isb.getSsn()))%></div></td>
            <td><%=h(isb.getLastName())%></td>
            <td><%=h(isb.getFirstName())%></td>
            <td><%=h(isb.getMiddleName())%></td>
            <td><%=h(isb.getBirthDate())%></td>
            <td><%=h(isb.getTestingSourceId())%></td>
            <td><%=h(isb.getDutyCodeId())%></td>
            <td><%=h(isb.getSpecimenType())%></td>
            <td><%=h(requestForm.getTestTypeId())%></td>
            </tr>
    <%
        }
    %>

</table>
