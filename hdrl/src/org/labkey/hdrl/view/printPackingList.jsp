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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.labkey.api.util.DateUtil" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.template.ClientDependencies" %>
<%@ page import="org.labkey.hdrl.HDRLController.PackingListBean" %>
<%@ page import="org.labkey.hdrl.view.InboundSpecimenBean" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%!
    @Override
    public void addClientDependencies(ClientDependencies dependencies)
    {
        dependencies.add("hdrl/fonts/barcode.css");
    }
%>
<%
    HttpView httpView =  HttpView.currentView();
    PackingListBean packingListBean = (PackingListBean) httpView.getModelBean();
    List<InboundSpecimenBean> inboundSpecimens = packingListBean.getInboundSpecimens();

    Date date = new Date();
    String dateToday = DateUtil.formatDate(getContainer(), date);
%>
<style type="text/css" media="print">
    @page { size: landscape; }
</style>

<style type="text/css">
    .wordbreak {
            word-break: break-all;
    }
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
        <th col width="10%">Draw Date</th>
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
                <tr class="labkey-alternate-row" align="center">
    <%
            }
            else
            {
    %>
                <tr class="labkey-row" align="center">
    <%
            }
            count++;
    %>

            <td col width="10%"><div class="barcode"><%=h(isb.getCustomerBarCode())%></div><div><%=h(isb.getCustomerBarCode())%></div></td>
            <td col width="10%"><div class="barcode"><%=h(getConcatenatedVal(isb.getFmpCode(), isb.getSsn()))%></div><div><%=h(getConcatenatedVal(isb.getFmpCode(), isb.getSsn()))%></div></td>
            <td col width="10%"><div class="wordbreak"><%=h(isb.getLastName())%></div></td>
            <td col width="10%"><div class="wordbreak"><%=h(isb.getFirstName())%></div></td>
            <td col width="10%"><div class="wordbreak"><%=h(isb.getMiddleName())%></div></td>
            <td col width="10%"><%=h(formatDate(isb.getBirthDate()))%></td>
            <td col width="5%"><%=h(isb.getSotCode())%></td>
            <td col width="5%"><%=h(isb.getDucCode())%></td>
            <td col width="10%"><%=h(formatDate(isb.getDrawDate()))%></td>
            <td col width="10%"><%=h(isb.getSpecimenType())%></td>
            <td col width="10%"><%=h(packingListBean.getTestType())%></td>
            </tr>
    <%
        }
    %>

</table>

<%!
    public String getConcatenatedVal(String fmpCode, String SSN)
    {
        if(StringUtils.isBlank(fmpCode) || StringUtils.isBlank(SSN))
            return null;

        return fmpCode + SSN;
    }
%>