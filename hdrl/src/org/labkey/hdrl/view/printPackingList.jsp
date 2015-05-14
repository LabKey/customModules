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
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="org.labkey.hdrl.HDRLController.PackingListBean" %>
<%@ page import="org.labkey.hdrl.view.InboundSpecimenBean" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.List" %>
<%@ page import="org.labkey.hdrl.HDRLController" %>
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
    PackingListBean packingListBean = (PackingListBean) httpView.getModelBean();
    List<InboundSpecimenBean> inboundSpecimens = packingListBean.getInboundSpecimens();

    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    Date date = new Date();
    String dateToday = dateFormat.format(date);
%>
<style type="text/css" media="print">
    @page { size: landscape; }
</style>

<header>
    <h1 align="center">Shipping Manifest</h1>
</header>
<section>
    <h3 align="center"><i>HIPAA warning message here</i></h3>
</section>
<table style="width:100%">
    <tr>
        <td col width="50%">Customer Name: </td>
        <td col width="50%"align="right">Date: <%=h(dateToday)%></td>
    </tr>
    <tr><td col width="50%">Customer Address:</td></tr>
    <tr><td col width="50%"><br><b>Shipment Information:</b></td></tr>
    <tr>
        <td col width="50%">Courier: <%=h(packingListBean.getShippingCarrier())%></td>
        <td  col width="50%" align="left">Total Samples: <%=h(packingListBean.getTotalSamples())%> </td>
    </tr>
    <tr>
        <td col width="50%">Tracking Number: <%=h(packingListBean.getShippingNumber())%></td>
        <td col width="50%" align="left">Test Requested (if bulk request): </td>
    </tr>
</table>
<br>

<table style="width:100%">
    <tr bgcolor="#d3d3d3">
        <th col width="10%">Barcode</th>
        <th col width="10%">FMP/SSN</th>
        <th col width="10%">Last Name</th>
        <th col width="10%">First Name</th>
        <th col width="10%">Middle Name</th>
        <th col width="10%">Date of Birth</th>
        <th col width="10%">SOT</th>
        <th col width="10%">DUC</th>
        <th col width="10%">Specimen Type</th>
        <th col width="10%">Test Requested</th>
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

            <td col width="10%"><div class="barcode"><%=h(isb.getCustomerBarCode())%></div><div><%=h(isb.getCustomerBarCode())%></div></td>
            <td col width="10%"><div class="barcode"><%=h(Integer.parseInt(isb.getFmpId())+Integer.parseInt(isb.getSsn()))%></div><div><%=h(Integer.parseInt(isb.getFmpId())+Integer.parseInt(isb.getSsn()))%></div></td>
            <td col width="10%"><%=h(isb.getLastName())%></td>
            <td col width="10%"><%=h(isb.getFirstName())%></td>
            <td col width="10%"><%=h(isb.getMiddleName())%></td>
            <td col width="10%"><%=h(isb.getBirthDate())%></td>
            <td col width="10%"><%=h(isb.getTestingSourceId())%></td>
            <td col width="10%"><%=h(isb.getDutyCodeId())%></td>
            <td col width="10%"><%=h(isb.getSpecimenType())%></td>
            <td col width="10%"><%=h(packingListBean.getTestType())%></td>
            </tr>
    <%
        }
    %>

</table>
