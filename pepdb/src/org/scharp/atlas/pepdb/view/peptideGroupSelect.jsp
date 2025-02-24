<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%@ page import="org.labkey.api.view.HttpView"%>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBBaseController.PeptideQueryForm" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBController.SearchForPeptidesAction" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBManager" %>
<%@ page import="org.scharp.atlas.pepdb.PepDBSchema" %>
<%@ page import="org.scharp.atlas.pepdb.model.PeptideGroup" %>
<%@ page import="org.scharp.atlas.pepdb.model.PeptidePool" %>
<%@ page import="org.scharp.atlas.pepdb.model.ProteinCategory" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    JspView<PeptideQueryForm> me = (JspView<PeptideQueryForm>) HttpView.currentView();
    PeptideQueryForm bean = me.getModelBean();
    if(bean.getMessage() != null){
%>
<%=h(bean.getMessage())%><%}%>
<script type="text/javascript" nonce="<%=getScriptNonce()%>">
    function selectOptions()
    {
        document.PeptideQueryForm.submit();
    }
</script>
<labkey:errors/>
<labkey:form name="PeptideQueryForm" action="<%=urlFor(SearchForPeptidesAction.class)%>" method="post">
    <h3 align="left" style="color:blue">Search for Peptides using different criteria : </h3><br><br>
    <table>
        <tr>
            <td><th>Search Criteria : </th></td>
            <td>
                <%=select()
                    .id("queryKey")
                    .name("queryKey")
                    .onChange("selectOptions()")
                    .selected(bean.getQueryKey())
                    .className(null)
                    .addOption("")
                    .addOption("Peptides in a Peptide Group", PepDBSchema.COLUMN_PEPTIDE_GROUP_ID)
                    .addOption("Peptides in a Peptide Pool", PepDBSchema.COLUMN_PEPTIDE_POOL_ID)
                    .addOption("Peptides in a Protein Category", PepDBSchema.COLUMN_PROTEIN_CAT_ID)
                    .addOption("Peptides contain Sequence", PepDBSchema.COLUMN_PEPTIDE_SEQUENCE)
                    .addOption("Child Peptides By Parent Sequence", PepDBSchema.COLUMN_PARENT_SEQUENCE)
                    .addOption("Parent Peptides By Child Sequence", PepDBSchema.COLUMN_CHILD_SEQUENCE)
                %>
            </td>
        </tr>
        <tr>
            <td>
                <th><%=h((bean.getQueryKey() != null && bean.getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_SEQUENCE)) ? " Equals / Contains : " : " Equals : ")%>
                </th>
            </td>
            <td>

                <%if(bean.getQueryKey() == null || bean.getQueryKey().isEmpty()){  %>
                <input type="text" size="30" name="queryValue"/>
                <%}
                else if(bean.getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID)){
                    PeptideGroup[] peptideGroups = PepDBManager.getPeptideGroups();
                %>
                <select id="queryValue" name="queryValue">
                    <option value=""></option>
                    <% for(PeptideGroup pepGroup : peptideGroups) { %>
                    <option value="<%=pepGroup.getPeptide_group_id()%>"<%=selected(bean.getQueryValue() != null && bean.getQueryValue().equals(pepGroup.getPeptide_group_id().toString()))%>><%=h(pepGroup.getPeptide_group_name())%></option>
                    <% } %> </select>
                <% }
                else if (bean.getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_POOL_ID)) {
                    PeptidePool[] peptidePools = PepDBManager.getPeptidePools();
                %>
                <select id="queryValue" name="queryValue">
                    <option value=""></option>
                    <%for (PeptidePool pepPool : peptidePools) { %>
                    <option value="<%=pepPool.getPeptide_pool_id()%>"<%=selected(bean.getQueryValue() != null && bean.getQueryValue().equals(pepPool.getPeptide_pool_id().toString()))%>><%=h(pepPool.getPeptide_pool_name())%>(PP<%=pepPool.getPeptide_pool_id()%>)</option>
                    <% }%>
                </select>
                <% }
                else if (bean.getQueryKey().equals(PepDBSchema.COLUMN_PROTEIN_CAT_ID)) {
                    ProteinCategory[] proteinCategories = PepDBManager.getProteinCategory();
                %>
                <select id="queryValue" name="queryValue">
                    <option value=""></option>
                    <%for (ProteinCategory proCat : proteinCategories) { %>
                    <option value="<%=proCat.getProtein_cat_id()%>"<%=selected(bean.getQueryValue() != null && bean.getQueryValue().equals(proCat.getProtein_cat_id().toString()))%>><%=h(proCat.getProtein_cat_desc())%></option>
                    <% }%>
                </select>
                <%}
                else if(bean.getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_SEQUENCE) || bean.getQueryKey().equals(PepDBSchema.COLUMN_PARENT_SEQUENCE) || bean.getQueryKey().equals(PepDBSchema.COLUMN_CHILD_SEQUENCE)){%>
                <input type="text" name="queryValue" size="30" value="<%=h(bean.getQueryValue())%>"/> &nbsp;
                <%}%>
            </td>
        </tr>
        <%if(bean.getQueryKey() != null && bean.getQueryKey().equals(PepDBSchema.COLUMN_PROTEIN_CAT_ID)){%>
        <tr>
            <td>
                <th>Amino Acid Start Position: </th>
            </td>
            <td>
                <input type="text" name="AAStart" size="15" value="<%=h(bean.getAAStart())%>"/>
            </td>
        </tr>
        <tr>
            <td>
                <th>Amino Acid End Position: </th>
            </td>
            <td>
                <input type="text" name="AAEnd" size="15" value="<%=h(bean.getAAEnd())%>"/>
            </td>
        </tr>
        <%}%>
        <%if(bean.getQueryKey() != null && bean.getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID)){%>
        <tr>
            <td>
                <th>PEPTIDE NUMBER: </th>
            </td>
            <td>
                <input type="text" name="labId" size="15" value="<%=h(bean.getLabId())%>"/>
            </td>
        </tr>
        <%}%>
        <tr>
            <td></td>
            <td>
                <input type="submit" name="action_type" value="Get Peptides"/>
            </td>
        </tr>
    </table>
</labkey:form>
