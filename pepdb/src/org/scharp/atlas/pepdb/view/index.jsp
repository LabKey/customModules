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
	<li><%= link("List Peptide Groups").href(urlFor(PepDBController.ShowAllPeptideGroupsAction.class)).clearClasses() %></li>
    <%
        if(canUpdate){%>
    <li><%= link("Insert a New Group").href(urlFor(PepDBController.InsertPeptideGroupAction.class)).clearClasses() %></li>
    <%}else{%>
    <li>Insert a New Group</li>
    <%}%>
</ul>
<h3 style="color:blue;font:italic">Peptides : </h3>
<ul>
    <li><%= link("Search for Peptides by Criteria").href(urlFor(PepDBController.SearchForPeptidesAction.class)).clearClasses() %></li>
    <%if(canUpdate){%>
    <li><%= link("Import Peptides").href(urlFor(PepDBController.ImportPeptidesAction.class)).clearClasses() %></li>
    <%}else{%>
    <li>Import Peptides</li>
    <%}%>
    <li><%= link("Peptides From Last Import").href(urlFor(PepDBController.DisplayResultAction.class)).clearClasses() %></li>
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
    <li><%= link("Import Peptide Pools").href(urlFor(PepDBController.ImportPeptidePoolsAction.class)).clearClasses() %></li>
    <%}else{%>
    <li>Import Peptide Pools</li>
    <%}%>
    <li><%= link("List All Peptide Pools").href(urlFor(PepDBController.ShowAllPeptidePoolsAction.class)).clearClasses() %></li>
</ul>
