<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.security.User"%>
<%@ page import="org.labkey.api.security.permissions.UpdatePermission" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.ViewContext" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBBaseController.DisplayPeptideForm" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBController" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBController.DisplayPeptideAction" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>

<%  ViewContext ctx = getViewContext();
    User user = ctx.getUser();
    final var canUpdate = ctx.getContainer().hasPermission(user, UpdatePermission.class);
%>
<h4>If you see some of the links disabled then you don't have permission to enter data.
    If you need to enter any data contact Atlas Administrator.</h4>
<h3 style="color:blue;font:italic">Peptide Groups : </h3>
<ul>
	<li><%= simpleLink("List Peptide Groups", urlFor(PepDBController.ShowAllPeptideGroupsAction.class)) %></li>
    <%
        if(canUpdate){%>
    <li><%= simpleLink("Insert a New Group", urlFor(PepDBController.InsertPeptideGroupAction.class)) %></li>
    <%}else{%>
    <li>Insert a New Group</li>
    <%}%>
</ul>
<h3 style="color:blue;font:italic">Peptides : </h3>
<ul>
    <li><%= simpleLink("Search for Peptides by Criteria", urlFor(PepDBController.SearchForPeptidesAction.class)) %></li>
    <%if(canUpdate){%>
    <li><%= simpleLink("Import Peptides", urlFor(PepDBController.ImportPeptidesAction.class)) %></li>
    <%}else{%>
    <li>Import Peptides</li>
    <%}%>
    <li><%= simpleLink("Peptides From Last Import", urlFor(PepDBController.DisplayResultAction.class)) %></li>
</ul>
<labkey:errors/>
<%
    DisplayPeptideForm form = (DisplayPeptideForm) (HttpView.currentModel());
%>
<labkey:form action="<%=urlFor(DisplayPeptideAction.class)%>" method="get">
Lookup Peptide by Id: <input type="text" name="peptide_id" size="10" value="<%=h(form.getPeptide_id())%>"/> &nbsp; <%= button("Find").submit(true) %>
</labkey:form>
<p>
<h3 style="color:blue;font:italic">Peptide Pools :</h3>
<ul>
    <%if(canUpdate){%>
    <li><%= simpleLink("Import Peptide Pools", urlFor(PepDBController.ImportPeptidePoolsAction.class)) %></li>
    <%}else{%>
    <li>Import Peptide Pools</li>
    <%}%>
    <li><%= simpleLink("List All Peptide Pools", urlFor(PepDBController.ShowAllPeptidePoolsAction.class)) %></li>
</ul>
