<%@ page import="org.scharp.atlas.pepdb.PepDBController"%>
<%@ page import="org.scharp.atlas.pepdb.PepDBManager"%>
<%@ page import="org.scharp.atlas.pepdb.model.PeptideGroup"%>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    PeptideGroup[] peptides = PepDBManager.getPeptideGroups();
%>
This container contains <%= peptides.length %> peptide groups.<br>
<%= button("View Grid").href(urlFor(PepDBController.ShowAllPeptideGroupsAction.class)) %>
