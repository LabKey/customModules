<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBBaseController.PeptideQueryForm" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBController" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBManager" %>
<%@ page import="org.scharp.atlas.pepdb.model.PeptidePool" %>
<%@ page import="org.scharp.atlas.pepdb.model.Source" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    JspView<PeptideQueryForm> me = (JspView<PeptideQueryForm>) HttpView.currentView();
    PeptideQueryForm bean = me.getModelBean();
%>
<table>
    <%
        Source[] sources = PepDBManager.getSourcesForPeptide(bean.getQueryValue());
        if (sources != null && sources.length > 0)
        {%>
        <h4>Peptide P<%=h(bean.getQueryValue())%> is a member of these Peptide Groups: </h4>
    <%
        for (Source source : sources)
        { %>

    <tr>
        <td>
            <%= link(source.getPeptide_group_name()).href(urlFor(PepDBController.DisplayPeptideGroupInformationAction.class)
                    .addParameter("peptide_group_id", source.getPeptide_group_id().toString())).clearClasses() %>
            (PEPTIDE NUMBER =<%=h(source.getPeptide_id_in_group())%>)
            <%if(source.getFrequency_number() != null){%>
            - Frequency Number =
            <%= source.getFrequency_number()%>
            <%}if(source.getFrequency_number_date() != null){%>
            - Frequency Update Date = <%=formatDateTime(source.getFrequency_number_date())%><%}%>
        </td>
    </tr>

    <%      }} // end for loop %>
</table>
<table>
    <%
        PeptidePool[] pools = PepDBManager.getPoolsForPeptide(bean.getQueryValue());
        if (pools != null && pools.length > 0)
        {
            %>
        <h4>Peptide P<%=h(bean.getQueryValue())%> is a member of these Peptide Pools: </h4>
    <%    for (PeptidePool pool : pools)
    {%>
    <tr>
        <td>
            <%= link("PP"+pool.getPeptide_pool_id()).href(urlFor(PepDBController.DisplayPeptidePoolInformationAction.class)
                    .addParameter("peptide_pool_id", pool.getPeptide_pool_id())).clearClasses() %> -
            <%=h(pool.getPeptide_pool_name())%>
            <%=h(pool.getPool_type_desc())%>
        </td>
    </tr>
    <% }} // end if statement %>
</table> 
