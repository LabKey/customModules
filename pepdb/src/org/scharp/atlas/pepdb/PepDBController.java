package org.scharp.atlas.pepdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.labkey.api.action.ExportAction;
import org.labkey.api.action.FormViewAction;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.attachments.AttachmentFile;
import org.labkey.api.data.ActionButton;
import org.labkey.api.data.Aggregate;
import org.labkey.api.data.ButtonBar;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.CompareType;
import org.labkey.api.data.Container;
import org.labkey.api.data.DataRegion;
import org.labkey.api.data.DisplayColumn;
import org.labkey.api.data.ExcelWriter;
import org.labkey.api.data.RenderContext;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Sort;
import org.labkey.api.data.TSVGridWriter;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.security.permissions.UpdatePermission;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.DetailsView;
import org.labkey.api.view.GridView;
import org.labkey.api.view.HttpView;
import org.labkey.api.view.InsertView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.UpdateView;
import org.labkey.api.view.VBox;
import org.labkey.api.view.ViewContext;
import org.scharp.atlas.pepdb.model.PeptideGroup;
import org.scharp.atlas.pepdb.model.PeptidePool;
import org.scharp.atlas.pepdb.model.Peptides;
import org.scharp.atlas.pepdb.model.ProteinCategory;
import org.springframework.beans.PropertyValues;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * User: sravani
 * Date: Jul 6, 2009
 * Time: 12:19:21 PM
 */
public class PepDBController extends PepDBBaseController
{
    private static final Logger _log = LogManager.getLogger(PepDBController.class);
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(PepDBController.class);

    private static final String PAGE_INDEX = "/org/scharp/atlas/pepdb/view/index.jsp";
    private static final String PAGE_PEPTIDE_GROUP_SELECT = "/org/scharp/atlas/pepdb/view/peptideGroupSelect.jsp";
    private static final String PAGE_IMPORT_PEPTIDES = "/org/scharp/atlas/pepdb/view/importPeptides.jsp";

    // Maximum number of rows to display on a web page at once.  Specifying Table.ALL_ROWS was causing a JavaScript timeout.
    private static final int MAX_ROWS = 1000;

    public PepDBController()
    {
        setActionResolver(_actionResolver);
    }

    public ActionURL peptideURL(String action)
    {
        Container c = getViewContext().getContainer();
        return new ActionURL("PepDB", action, c);
    }

    protected HttpServletRequest getRequest()
    {
        return getViewContext().getRequest();
    }

    protected HttpServletResponse getResponse()
    {
        return getViewContext().getResponse();
    }

    @RequiresPermission(ReadPermission.class)
    public class BeginAction extends SimpleViewAction<DisplayPeptideForm>
    {
        @Override
        public ModelAndView getView(DisplayPeptideForm form, BindException errors) throws Exception
        {
            JspView v = new JspView(PAGE_INDEX, form, errors);
            return v;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Begin", peptideURL("begin"));
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class SearchForPeptidesAction extends FormViewAction<PeptideQueryForm>
    {
        @Override
        public ModelAndView getView(PeptideQueryForm form, boolean reshow, BindException errors) throws Exception
        {
            ViewContext ctx = getViewContext();
            HttpSession session = ctx.getRequest().getSession(true);
            PeptideQueryForm form1 = (PeptideQueryForm) session.getAttribute("QUERY_FORM");
            if (form1 != null && form.getQueryKey() == null)
                form = form1;
            JspView v = new JspView<PeptideQueryForm>(PAGE_PEPTIDE_GROUP_SELECT, form, errors);
            return v;
        }

        @Override
        public boolean handlePost(PeptideQueryForm form, BindException errors) throws Exception
        {
            String actionType = getRequest().getParameter("action_type");
            if (actionType != null && actionType.equals("Get Peptides"))
                return true;
            else
                return false;
        }

        @Override
        public void validateCommand(PeptideQueryForm form, Errors errors)
        {
            return;
        }

        @Override
        public ActionURL getSuccessURL(PeptideQueryForm form)
        {
            ActionURL urlTest = new ActionURL(GetPeptidesAction.class, getContainer());
            urlTest.addParameter("queryKey", form.getQueryKey());
            urlTest.addParameter("queryValue", form.getQueryValue());
            if (form.getQueryKey() != null && form.getQueryKey().equals(PepDBSchema.COLUMN_PROTEIN_CAT_ID))
            {
                if (form.getAAStart() != null)
                    urlTest.addParameter("AAStart", form.getAAStart());
                if (form.getAAEnd() != null)
                    urlTest.addParameter("AAEnd", form.getAAEnd());
            }
            if (form.getQueryKey() != null && form.getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID))
            {
                if (form.getLabId() != null)
                    urlTest.addParameter("labId", form.getLabId());
            }
            return urlTest;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Search For Peptides By Criteria", peptideURL("searchForPeptides"));
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class GetPeptidesAction extends SimpleViewAction<PeptideQueryForm>
    {
        @Override
        public ModelAndView getView(PeptideQueryForm form, BindException errors) throws Exception
        {
            if (!form.validate(errors))
            {
                return new JspView<>(PAGE_PEPTIDE_GROUP_SELECT, form, errors);
            }
            PropertyValues pv = this.getPropertyValues();
            ViewContext ctx = getViewContext();
            HttpSession session = ctx.getRequest().getSession(true);
            session.setAttribute("QUERY_FORM", form);
            GridView gridView = new GridView(new DataRegion(), (BindException) null);
            if (form.getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID))
            {
                gridView = getGridViewByGroup(form, pv);
            }
            if (form.getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_POOL_ID))
            {
                gridView = getGridViewByPool(form, pv);
            }
            if (form.getQueryKey().equals(PepDBSchema.COLUMN_PROTEIN_CAT_ID))
            {
                gridView = getGridViewByProtein(form, pv);
            }
            if (form.getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_SEQUENCE))
            {
                gridView = getGridViewBySequence(form, pv);
            }
            if (form.getQueryKey().equals(PepDBSchema.COLUMN_PARENT_SEQUENCE))
            {
                gridView = getGridViewByParent(form, pv);
            }
            if (form.getQueryKey().equals(PepDBSchema.COLUMN_CHILD_SEQUENCE))
            {
                gridView = getGridViewByChild(form, pv);
            }
            if (gridView == null)
            {
                HttpView.redirect(new ActionURL(SearchForPeptidesAction.class, getContainer()));
            }
            return gridView;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Get Peptides By Criteria", peptideURL("getPeptides"));
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class DisplayPeptideAction extends SimpleViewAction<DisplayPeptideForm>
    {
        @Override
        public ModelAndView getView(DisplayPeptideForm form, BindException errors) throws Exception
        {
            String pepId = form.getPeptide_id();
            if (pepId == null || pepId.length() == 0
                    || (!pepId.trim().toUpperCase().startsWith("P") && validateInteger(pepId.trim()) == null)
                    || (pepId.trim().toUpperCase().startsWith("P") && validateInteger(pepId.trim().substring(1)) == null))
            {
                errors.reject(null, "Peptide Id is required and It has to be an Integer with or without prefix 'P'.");
                JspView v = new JspView(PAGE_INDEX, form, errors);
                return v;
            }
            if (pepId.trim().toUpperCase().startsWith("P") && validateInteger(pepId.trim().substring(1)) != null)
                pepId = pepId.trim().substring(1);
            Peptides p = PepDBManager.getPeptideById(Integer.parseInt(pepId));
            if (p == null)
            {
                errors.reject(null, "Peptide Id not found in the database.");
                JspView v = new JspView(PAGE_INDEX, form, errors);
                return v;
            }
            _log.debug("DisplayPeptideForm: " + form.toString());
            VBox box = new VBox();
            PeptideQueryForm queryform = new PeptideQueryForm();
            queryform.setQueryValue(pepId);
            DetailsView dataView = getPeptideDetailsView(queryform, p);
            if (!p.isChild())
            {
                dataView.getDataRegion().getDisplayColumn("optimal_epitope_list_id").setVisible(false);
                dataView.getDataRegion().getDisplayColumn("hla_restriction").setVisible(false);
            }
            box.addView(dataView);
            JspView detailsView = new JspView<PeptideQueryForm>("/org/scharp/atlas/pepdb/view/peptideDetails.jsp", queryform, errors);
            box.addView(detailsView);
            PropertyValues pv = this.getPropertyValues();
            if (p.isParent())
            {
                PeptideQueryForm parentform = new PeptideQueryForm();
                parentform.setQueryValue((pepId));
                GridView gvChildren = getGridViewByParentId(parentform, pv);
                box.addView(gvChildren);
            }
            if (p.isChild())
            {
                PeptideQueryForm childform = new PeptideQueryForm();
                childform.setQueryValue((pepId));
                GridView gvParents = getGridViewByChildId(childform, pv);
                box.addView(gvParents);
            }
            return box;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Display Peptide Details", peptideURL("displayPeptide"));
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class EditPeptideAction extends FormViewAction<PeptideForm>
    {
        @Override
        public ModelAndView getView(PeptideForm form, boolean reshow, BindException errors)
        {
            Peptides p = PepDBManager.getPeptideById(form.getBean().getPeptide_id());
            UpdateView uView = new UpdateView(form, errors);
            ButtonBar bb = new ButtonBar();
            //bb.add(new ActionButton("editPeptide.post","Save Changes"));
            //ActionURL editPeptideUrl = new ActionURL(EditPeptideAction.class, getContainer());

            bb.add(new ActionButton(new ActionURL(EditPeptideAction.class, getContainer()), "Save Changes"));

            ActionURL backURL = new ActionURL(DisplayPeptideAction.class, getContainer());
            backURL.addParameter(PepDBSchema.COLUMN_PEPTIDE_ID, form.getBean().getPeptide_id());
            ActionButton cancelButton = new ActionButton(backURL, "Cancel");
            cancelButton.setActionType(ActionButton.Action.LINK);
            bb.add(cancelButton);
            uView.getDataRegion().setButtonBar(bb);
            if (!p.isChild())
            {
                uView.getDataRegion().getDisplayColumn("optimal_epitope_list_id").setVisible(false);
                uView.getDataRegion().getDisplayColumn("hla_restriction").setVisible(false);
            }
            uView.setTitle("Update Peptide data for : P" + form.getBean().getPeptide_id());
            return uView;
        }

        @Override
        public boolean handlePost(PeptideForm form, BindException errors) throws Exception
        {
            Peptides oldBean = PepDBManager.getPeptideById(form.getBean().getPeptide_id());
            form.setOldValues(oldBean);
            Peptides bean = form.getBean();
            PepDBManager.updatePeptide(getUser(), bean);
            return true;
        }

        @Override
        public void validateCommand(PeptideForm form, Errors errors)
        {
            Peptides bean = form.getBean();
            if (bean.isPeptide_flag() && (bean.getPeptide_notes() == null || bean.getPeptide_notes().length() == 0))
                errors.reject(null, "If a peptide is flagged then you must enter Peptide Flag Reason.");
            if (!bean.isPeptide_flag() && bean.getPeptide_notes() != null && bean.getPeptide_notes().trim().length() != 0)
                errors.reject(null, "If a peptide is not flagged then Peptide Flag Reason must be blank.");
        }

        @Override
        public ActionURL getSuccessURL(PeptideForm form)
        {
            ActionURL url = new ActionURL(DisplayPeptideAction.class, getContainer());
            url.addParameter(PepDBSchema.COLUMN_PEPTIDE_ID, form.getBean().getPeptide_id());
            return url;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Edit Peptide", peptideURL("editPeptide"));
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class EditPeptidePoolAction extends FormViewAction<PeptidePoolForm>
    {
        @Override
        public ModelAndView getView(PeptidePoolForm form, boolean reshow, BindException errors)
        {
            PeptidePool p = PepDBManager.getPeptidePoolByID(form.getBean().getPeptide_pool_id());
            UpdateView uView = new UpdateView(form, errors);
            ButtonBar bb = new ButtonBar();
            bb.add(new ActionButton(new ActionURL(EditPeptidePoolAction.class, getContainer()), "Save Changes"));
            //bb.add(new ActionButton("editPeptidePool.post","Save Changes"));
            ActionURL backURL = new ActionURL(DisplayPeptidePoolInformationAction.class, getContainer());
            backURL.addParameter(PepDBSchema.COLUMN_PEPTIDE_POOL_ID, form.getBean().getPeptide_pool_id());
            ActionButton cancelButton = new ActionButton(backURL, "Cancel");
            cancelButton.setActionType(ActionButton.Action.LINK);
            bb.add(cancelButton);
            uView.getDataRegion().setButtonBar(bb);
            uView.setTitle("Update Peptide Pool data for : PP" + form.getBean().getPeptide_pool_id());
            return uView;
        }

        @Override
        public boolean handlePost(PeptidePoolForm form, BindException errors) throws Exception
        {
            PeptidePool bean = form.getBean();
            PeptidePool dbBean = PepDBManager.getPeptidePoolByID(bean.getPeptide_pool_id());
            bean.setCreated(dbBean.getCreated());
            bean.setCreatedBy(dbBean.getCreatedBy());
            PepDBManager.updatePeptidePool(getUser(), bean);
            return true;
        }

        @Override
        public void validateCommand(PeptidePoolForm form, Errors errors)
        {

        }

        @Override
        public ActionURL getSuccessURL(PeptidePoolForm form)
        {
            ActionURL url = new ActionURL(DisplayPeptidePoolInformationAction.class, getContainer());
            url.addParameter(PepDBSchema.COLUMN_PEPTIDE_POOL_ID, form.getBean().getPeptide_pool_id());
            return url;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Edit Peptide Pool", peptideURL("editPeptidePool"));
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class ShowAllPeptideGroupsAction extends SimpleViewAction<PeptideQueryForm>
    {
        @Override
        public ModelAndView getView(PeptideQueryForm form, BindException errors) throws Exception
        {
            TableInfo tableInfo = PepDBSchema.getInstance().getTableInfoPeptideGroups();
            PropertyValues pv = this.getPropertyValues();
            form.setTInfo(tableInfo);
            List<ColumnInfo> columns = tableInfo.getColumns("peptide_group_name,pathogen_id,seq_ref,clade_id,pep_align_ref_id,group_type_id,createdby,created,modifiedby,modified");
            form.setCInfo(columns);
            form.setSort(new Sort(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID));
            form.setMessage("AllPeptideGroups");
            DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
            rgn.setButtonBar(getGridButtonbar(pv), DataRegion.MODE_GRID);
            ActionURL insertUrl = new ActionURL(InsertPeptideGroupAction.class, getContainer());
            ActionButton insert = new ActionButton(insertUrl, "Insert New Group");
            insert.setActionType(ActionButton.Action.LINK);
            rgn.getButtonBar(DataRegion.MODE_GRID).add(insert);
            DisplayColumn col = rgn.getDisplayColumn(PepDBSchema.COLUMN_PEPTIDE_GROUP_NAME);
            ActionURL displayAction = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            displayAction.addParameter(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID,"${" + PepDBSchema.COLUMN_PEPTIDE_GROUP_ID + "}");
            col.setURL(displayAction);
            GridView gridView = new GridView(rgn, errors);
            gridView.setTitle("All the Peptide Groups in the System are : ");
            return gridView;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Display All Peptide Groups", peptideURL("showAllPeptideGroups"));
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class DisplayPeptideGroupInformationAction extends SimpleViewAction<PeptideAndGroupForm>
    {
        @Override
        public ModelAndView getView(PeptideAndGroupForm form, BindException errors) throws Exception
        {
            _log.debug("PeptideAndGroupForm: " + form.toString());
            PeptideGroup pg = PepDBManager.getPeptideGroupByID(Integer.parseInt(form.getPeptide_group_id()));
            DataRegion rgn1 = new DataRegion();
            TableInfo tableInfo1 = PepDBSchema.getInstance().getTableInfoPeptideGroups();
            rgn1.setColumns(tableInfo1.getColumns("peptide_group_id,peptide_group_name,pathogen_id,seq_ref,clade_id,pep_align_ref_id,group_type_id,createdby,created,modifiedby,modified"));
            ButtonBar buttonBar1 = getButtonBar();
            ActionURL backUrl = new ActionURL(ShowAllPeptideGroupsAction.class, getContainer());
            ActionButton goBack = new ActionButton("List All Groups", backUrl);
            buttonBar1.add(goBack);
            if (getContainer().hasPermission(getUser(), UpdatePermission.class))
            {
                ActionURL updateGroupUrl = new ActionURL(UpdatePeptideGroupAction.class, getContainer());
                updateGroupUrl.addParameter(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID, form.getPeptide_group_id());
                ActionButton updateGroupButton = new ActionButton("Update Group Data", updateGroupUrl);
                buttonBar1.add(updateGroupButton);
            }
            rgn1.setButtonBar(buttonBar1, DataRegion.MODE_DETAILS);
            DetailsView dataView = new DetailsView(rgn1, form.getPeptide_group_id());
            dataView.setTitle("Group Information from peptide_group table for group : " + pg.getPeptide_group_name());
            VBox vBox = new VBox();
            vBox.addView(dataView);
            PropertyValues pv = this.getPropertyValues();
            PeptideQueryForm form2 = new PeptideQueryForm();
            form2.setQueryValue(form.getPeptide_group_id());
            GridView gv = getGridViewByGroup(form2, pv);
            vBox.addView(gv);
            return vBox;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Display Peptide Group Details", peptideURL("displayPeptideGroupInformation"));
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class ShowAllPeptidePoolsAction extends SimpleViewAction<PeptideQueryForm>
    {
        @Override
        public ModelAndView getView(PeptideQueryForm form, BindException errors) throws Exception
        {
            TableInfo tableInfo = PepDBSchema.getInstance().getTableInfoViewPoolDetails();
            PropertyValues pv = this.getPropertyValues();
            form.setTInfo(tableInfo);
            List<ColumnInfo> columns = tableInfo.getColumns("peptide_pool_id,peptide_pool_name,pool_type_desc,parent_pool_id,parent_pool_name,matrix_peptide_pool_id,comment,archived,createdby,created,modifiedby,modified");
            form.setCInfo(columns);
            form.setSort(new Sort(PepDBSchema.COLUMN_PEPTIDE_POOL_ID));
            form.setMessage("AllPeptidePools");
            DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
            rgn.setButtonBar(getGridButtonbar(pv), DataRegion.MODE_GRID);
            ActionURL importUrl = new ActionURL(ImportPeptidePoolsAction.class, getContainer());
            ActionButton importB = new ActionButton(importUrl, "Import New Pools");
            importB.setActionType(ActionButton.Action.LINK);
            rgn.getButtonBar(DataRegion.MODE_GRID).add(importB);
            GridView gridView = new GridView(rgn, errors);
            gridView.setTitle("All the Peptide Pools in the System are : ");
            return gridView;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Display All Peptide Pools", peptideURL("showAllPeptidePools"));
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class DisplayPeptidePoolInformationAction extends SimpleViewAction<PeptideAndPoolForm>
    {
        @Override
        public ModelAndView getView(PeptideAndPoolForm form, BindException errors) throws Exception
        {
            _log.debug("PeptideAndPoolForm: " + form.toString());
            PeptideQueryForm queryform = new PeptideQueryForm();
            TableInfo tableInfo = PepDBSchema.getInstance().getTableInfoPeptidePools();
            PropertyValues pv = this.getPropertyValues();
            queryform.setTInfo(tableInfo);
            queryform.setCInfo(tableInfo.getColumns("peptide_pool_id,peptide_pool_name,pool_type_id,parent_pool_id,matrix_peptide_pool_id,comment,archived,createdby,created,modifiedby,modified"));
            DataRegion rgn = getDataRegion(getContainer(), queryform, Table.ALL_ROWS);
            rgn.setShowBorders(true);
            rgn.setShadeAlternatingRows(true);
            ButtonBar buttonBar = getButtonBar();
            ActionURL editUrl = new ActionURL(EditPeptidePoolAction.class, getContainer());
            editUrl.addParameter(PepDBSchema.COLUMN_PEPTIDE_POOL_ID, form.getPeptide_pool_id());
            ActionButton editButton = new ActionButton("Edit Peptide Pool", editUrl);
            buttonBar.add(editButton);
            rgn.setButtonBar(buttonBar, DataRegion.MODE_DETAILS);
            DetailsView dataView = new DetailsView(rgn, form.getPeptide_pool_id());
            dataView.setTitle("Pool Information from peptide_pool table for pool : PP" + form.getPeptide_pool_id());
            queryform.setQueryValue(form.getPeptide_pool_id());
            GridView gv = getGridViewByPool(queryform, pv);

            // Because this page has potentially two data grids, we must give each grid a different
            // action url (in order to distinguish the two tables).
            gv.getDataRegion().setButtonBar(getGridButtonbarPeptidesInPool(pv));
            VBox box = new VBox();
            box.addView(dataView);
            box.addView(gv);
            if (PepDBManager.getChildrenPools(form.getPeptide_pool_id()) != null)
            {
                GridView childPools = getGridViewByParentPool(queryform, pv);
                childPools.getDataRegion().setButtonBar(getGridButtonbarPoolsInPool(pv));
                box.addView(childPools);
            }
            return box;

        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Display Peptide Pool Details", peptideURL("displayPeptidePoolInformation"));
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class UpdatePeptideGroupAction extends FormViewAction<PeptideGroupForm>
    {
        @Override
        public ModelAndView getView(PeptideGroupForm form, boolean reshow, BindException errors) throws Exception
        {
            PeptideGroup pg = PepDBManager.getPeptideGroupByID(form.getBean().getPeptide_group_id());
            UpdateView uView = new UpdateView(form, errors);
            ButtonBar bb = new ButtonBar();
            //bb.add(new ActionButton("updatePeptideGroup.post","Save Changes"));
            bb.add(new ActionButton(new ActionURL(UpdatePeptideGroupAction.class, getContainer()), "Save Changes"));
            ActionURL backURL = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            backURL.addParameter(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID, form.getBean().getPeptide_group_id());
            ActionButton cancelButton = new ActionButton(backURL, "Cancel");
            cancelButton.setActionType(ActionButton.Action.LINK);
            bb.add(cancelButton);
            uView.getDataRegion().setButtonBar(bb);
            if (pg.getPeptide_group_name().equalsIgnoreCase("Optimal Epitopes"))
                uView.getDataRegion().getDisplayColumn("peptide_group_name").setVisible(false);
            uView.setTitle("Update Peptide Group data for : " + pg.getPeptide_group_name());
            return uView;
        }

        @Override
        public boolean handlePost(PeptideGroupForm form, BindException errors) throws Exception
        {
            PeptideGroup bean = form.getBean();
            PeptideGroup dbBean = PepDBManager.getPeptideGroupByID(bean.getPeptide_group_id());
            bean.setCreated(dbBean.getCreated());
            bean.setCreatedBy(dbBean.getCreatedBy());
            PepDBManager.updatePeptideGroup(getUser(), bean);
            return true;
        }

        @Override
        public void validateCommand(PeptideGroupForm form, Errors errors)
        {
            try
            {
                form.validate(errors);
                form.validateName(errors);
            }
            catch (SQLException e)
            {
                errors.reject(null, "There's something wrong with database when trying to get all the existing groups.");
            }
        }

        @Override
        public ActionURL getSuccessURL(PeptideGroupForm form)
        {
            ActionURL url = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            url.addParameter(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID, form.getBean().getPeptide_group_id());
            return url;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Update Peptide Group", peptideURL("updatePeptideGroup"));
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class InsertPeptideGroupAction extends FormViewAction<PeptideGroupForm>
    {
        @Override
        public ModelAndView getView(PeptideGroupForm form, boolean reshow, BindException errors) throws Exception
        {
            ButtonBar bb = new ButtonBar();
            //bb.add(new ActionButton("insertPeptideGroup.post","Add New Peptide Group"));
            bb.add(new ActionButton(new ActionURL(InsertPeptideGroupAction.class, getContainer()), "Add New Peptide Group"));
            ActionURL backURL = new ActionURL(BeginAction.class, getContainer());
            ActionButton goBack = new ActionButton(backURL, "Cancel");
            goBack.setActionType(ActionButton.Action.LINK);
            bb.add(goBack);
            PeptideQueryForm qForm = new PeptideQueryForm();
            TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPeptideGroups();
            qForm.setTInfo(tInfo);
            qForm.setCInfo(tInfo.getColumns());
            DataRegion rgn = getDataRegion(getContainer(), qForm, Table.ALL_ROWS);
            rgn.setButtonBar(bb);
            InsertView iView = new InsertView(rgn, form, errors);
            return iView;
        }

        @Override
        public boolean handlePost(PeptideGroupForm form, BindException errors) throws Exception
        {
            PeptideGroup group = form.getBean();
            group = PepDBManager.insertGroup(getContainer(), getUser(), group);
            group.setContainerId(getContainer().getId());
            form.setBean(group);
            return true;
        }

        @Override
        public void validateCommand(PeptideGroupForm form, Errors errors)
        {
            try
            {
                form.validate(errors);
                form.validateName(errors);
            }
            catch (SQLException e)
            {
                errors.reject(null, "There's something wrong with database when trying to get all the existing groups.");
            }
        }

        @Override
        public ActionURL getSuccessURL(PeptideGroupForm form)
        {
            ActionURL url = new ActionURL(DisplayPeptideGroupInformationAction.class, getContainer());
            url.addParameter(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID, form.getBean().getPeptide_group_id().toString());
            return url;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Insert Peptide Group", peptideURL("insertPeptideGroup"));
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class ImportPeptidesAction extends FormViewAction<FileForm>
    {
        private List<Peptides> resultPeptides = new LinkedList<Peptides>();

        @Override
        public ModelAndView getView(FileForm form, boolean reshow, BindException errors) throws Exception
        {
            JspView v = new JspView<FileForm>(PAGE_IMPORT_PEPTIDES, form, errors);
            return v;
        }

        @Override
        public boolean handlePost(FileForm form, BindException errors) throws Exception
        {
            try
            {
                List<AttachmentFile> importFiles = getAttachmentFileList();
                AttachmentFile importFile = null;
                for (AttachmentFile a : importFiles)
                {
                    if (a != null && a.getSize() != 0)
                        importFile = a;
                }
                if (!form.validate(errors, importFile))
                    return false;
                if (isPost() && form.getActionType().equalsIgnoreCase(("PEPTIDES")))
                {
                    PeptideImporter importer = new PeptideImporter();
                    if (!importer.process(getViewContext().getUser(), importFile, errors, resultPeptides))
                        return false;
                    return true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                _log.error(e.getMessage(), e);
                errors.reject(null, "There was a problem uploading File: " + e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        public void validateCommand(FileForm form, Errors errors)
        {

        }

        @Override
        public ActionURL getSuccessURL(FileForm form)
        {
            ActionURL url = new ActionURL(DisplayResultAction.class, getContainer());
            url.addParameter("message", "The file has been successfully imported.");
            return url;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Import Peptides", peptideURL("importPeptides"));
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class DisplayResultAction extends SimpleViewAction<FileForm>
    {
        @Override
        public ModelAndView getView(FileForm form, BindException errors) throws Exception
        {
            PeptideQueryForm form1 = new PeptideQueryForm();
            PropertyValues pv = this.getPropertyValues();
            GridView v = getGridViewByLastImport(form1, pv);
            return v;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Display Results Page", peptideURL("displayResult"));
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class ImportPeptidePoolsAction extends FormViewAction<FileForm>
    {
        ActionURL url = null;

        @Override
        public ModelAndView getView(FileForm form, boolean reshow, BindException errors) throws Exception
        {
            JspView v = new JspView<FileForm>("/org/scharp/atlas/pepdb/view/importPools.jsp", form, errors);
            return v;
        }

        @Override
        public boolean handlePost(FileForm form, BindException errors) throws Exception
        {
            try
            {
                List<AttachmentFile> importFiles = getAttachmentFileList();
                AttachmentFile importFile = null;
                for (AttachmentFile a : importFiles)
                {
                    if (a != null && a.getSize() != 0)
                        importFile = a;
                }
                if (!form.validate(errors, importFile))
                    return false;

                PoolImporter importer = new PoolImporter();
                if (!importer.process(getViewContext().getUser(), form, importFile, errors))
                    return false;
                url = new ActionURL(BeginAction.class, getContainer());
                return true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                _log.error(e.getMessage(), e);
                errors.reject(null, "There was a problem uploading File: " + e.getMessage());
                return false;
            }
        }

        @Override
        public void validateCommand(FileForm form, Errors errors)
        {

        }

        @Override
        public ActionURL getSuccessURL(FileForm form)
        {
            ActionURL url = new ActionURL(ImportPeptidePoolsAction.class, getContainer());
            url.addParameter("message", "The file has been successfully imported.");
            return url;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Import Peptide Pools", peptideURL("importPeptidePools"));
        }
    }


    @RequiresPermission(ReadPermission.class)
    public abstract class PeptideExcelExportAction extends ExportAction
    {
        public void printExcel(Object bean, HttpServletResponse response, BindException errors, PeptideQueryForm form) throws Exception
        {
            try
            {
                RenderContext context = new RenderContext(getViewContext());
                DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
                context.setBaseFilter(form.getFilter());
                context.setBaseSort(form.getSort());
                ExcelWriter ew = new ExcelWriter(()->rgn.getResults(context), rgn.getDisplayColumns());
                ew.setAutoSize(true);
                ew.setFilenamePrefix(form.getMessage());
                ew.setSheetName(form.getMessage());
                ew.setFooter(form.getMessage());
                ew.renderWorkbook(getResponse());
            }
            catch (SQLException e)
            {
                _log.error("export: " + e);
            }
            catch (IOException e)
            {
                _log.error("export: " + e);
            }
            catch (Exception e)
            {
                _log.error("export: " + e);
            }
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class PeptidesInPoolExcelExportAction extends PeptideExcelExportAction
    {
        @Override
        public void export(Object bean, HttpServletResponse response, BindException errors) throws Exception
        {
            PeptideQueryForm form = new PeptideQueryForm();
            PropertyValues pv = this.getPropertyValues();
            form.setQueryValue((String)pv.getPropertyValue("peptide_pool_id").getValue());
            getGridViewByPool(form, pv);
            printExcel(bean, response, errors, form);
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class PoolsInPoolExcelExportAction extends PeptideExcelExportAction
    {
        @Override
        public void export(Object bean, HttpServletResponse response, BindException errors) throws Exception
        {
            PeptideQueryForm form = new PeptideQueryForm();
            PropertyValues pv = this.getPropertyValues();
            form.setQueryValue((String)pv.getPropertyValue("peptide_pool_id").getValue());
            getGridViewByParentPool(form, pv);
            printExcel(bean, response, errors, form);
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class PeptideDefaultExcelExportAction extends PeptideExcelExportAction
    {
        @Override
        public void export(Object bean, HttpServletResponse response, BindException errors) throws Exception
        {
            ViewContext ctx = getViewContext();
            HttpSession session = ctx.getRequest().getSession();
            PeptideQueryForm form = (PeptideQueryForm) session.getAttribute("PEPTIDE_QUERY_FORM");
            _log.error("Form " + form.getMessage() + " had filter : " + form.getFilter());
            printExcel(bean, response, errors, form);
        }
    }



    @RequiresPermission(ReadPermission.class)
    public abstract class PeptideTextExportAction extends ExportAction
    {
        public void printText(Object bean, HttpServletResponse response, BindException errors, PeptideQueryForm form) throws Exception
        {
            try
            {
                RenderContext context = new RenderContext(getViewContext());
                DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
                context.setBaseFilter(form.getFilter());
                context.setBaseSort(form.getSort());

                try (TSVGridWriter tsv = new TSVGridWriter(()->rgn.getResults(context), rgn.getDisplayColumns()))
                {
                    tsv.setFilenamePrefix(form.getMessage());
                    tsv.write(getResponse());
                }
            }
            catch (Exception e)
            {
                _log.error("export: " + e);
            }
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class PeptidesInPoolTextExportAction extends PeptideTextExportAction
    {
        @Override
        public void export(Object bean, HttpServletResponse response, BindException errors) throws Exception
        {
            PeptideQueryForm form = new PeptideQueryForm();
            PropertyValues pv = this.getPropertyValues();
            form.setQueryValue((String)pv.getPropertyValue("peptide_pool_id").getValue());
            getGridViewByPool(form, pv);
            printText(bean, response, errors, form);
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class PoolsInPoolTextExportAction extends PeptideTextExportAction
    {
        @Override
        public void export(Object bean, HttpServletResponse response, BindException errors) throws Exception
        {
            PeptideQueryForm form = new PeptideQueryForm();
            PropertyValues pv = this.getPropertyValues();
            form.setQueryValue((String)pv.getPropertyValue("peptide_pool_id").getValue());
            getGridViewByParentPool(form, pv);
            printText(bean, response, errors, form);
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class PeptideDefaultTextExportAction extends PeptideTextExportAction
    {
        @Override
        public void export(Object bean, HttpServletResponse response, BindException errors) throws Exception
        {
            ViewContext ctx = getViewContext();
            HttpSession session = ctx.getRequest().getSession();
            PeptideQueryForm form = (PeptideQueryForm) session.getAttribute("PEPTIDE_QUERY_FORM");
            printText(bean, response, errors, form);
        }
    }



    protected GridView getGridViewByLastImport(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        //PeptideGroup pg = PepDBManager.getPeptideGroupByID(Integer.parseInt(form.getQueryValue()));
        TableInfo tableInfo = PepDBSchema.getInstance()
                .getTableInfoViewGroupPeptides();
        form.setTInfo(tableInfo);
        //_log.debug("Creating a Filter for peptideGroup." + PepDBSchema.COLUMN_PEPTIDE_GROUP_ID + ": " + form);
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_IN_CURRENT_FILE, true);
        Sort sort = new Sort(PepDBSchema.COLUMN_PEPTIDE_GROUP_ASSIGNMENT_ID);
        form.setFilter(sFilter);
        form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,protein_cat_id,peptide_group_name,peptide_id_in_group,pathogen_id," +
                "sequence_length,amino_acid_start_pos,amino_acid_end_pos,child,parent,optimal_epitope_list_id,hla_restriction,frequency_number,frequency_number_date"));

        form.setSort(sort);
        form.setMessage("Peptides_IN_Last_File");
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonbar(pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        return gridView;
    }

    protected GridView getGridViewByGroup(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        PeptideGroup pg = PepDBManager.getPeptideGroupByID(Integer.parseInt(form.getQueryValue()));
        TableInfo tableInfo = PepDBSchema.getInstance()
                .getTableInfoViewGroupPeptides();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for peptideGroup." + PepDBSchema.COLUMN_PEPTIDE_GROUP_ID + ": " + form);
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID, Integer.parseInt(form.getQueryValue()));
        if (form.getLabId() != null)
            sFilter.addCondition(PepDBSchema.COLUMN_PEPTIDE_ID_IN_GROUP, form.getLabId());
        Sort sort = new Sort(PepDBSchema.COLUMN_PEPTIDE_ID_IN_GROUP);
        form.setFilter(sFilter);
        if (pg.getPeptide_group_name().equalsIgnoreCase("Optimal Epitopes"))
            form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,protein_cat_id,peptide_group_name,peptide_id_in_group,pathogen_id," +
                    "sequence_length,amino_acid_start_pos,amino_acid_end_pos,child,parent,peptide_flag,peptide_notes,optimal_epitope_list_id,hla_restriction,frequency_number,frequency_number_date"));
        else
            form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,protein_cat_id,peptide_group_name,peptide_id_in_group,pathogen_id," +
                    "sequence_length,amino_acid_start_pos,amino_acid_end_pos,child,parent,peptide_flag,peptide_notes,frequency_number,frequency_number_date"));
        form.setSort(sort);
        form.setMessage("Peptides_IN_Group_" + pg.getPeptide_group_name());
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonbar(pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(
                "There are (" +
                        PepDBManager.getCount(Integer.parseInt(form.getQueryValue())) +
                        ") peptides in the '" + pg.getPeptide_group_name() + "' peptide group.");
        return gridView;
    }

    protected GridView getGridViewByPool(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        PeptidePool pp = PepDBManager.getPeptidePoolByID(Integer.parseInt(form.getQueryValue()));
        TableInfo tableInfo = PepDBSchema.getInstance()
                .getTableInfoViewPoolPeptides();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for peptidePool." + PepDBSchema.COLUMN_PEPTIDE_POOL_ID + ": " + form);
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PEPTIDE_POOL_ID, Integer.parseInt(form.getQueryValue()));
        Sort sort = new Sort(PepDBSchema.COLUMN_PEPTIDE_ID);
        form.setFilter(sFilter);
        form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,protein_cat_id,peptide_pool_id,peptide_pool_name,pool_type_id," +
                "peptide_group_id,peptide_id_in_group,sequence_length,amino_acid_start_pos,amino_acid_end_pos,child,parent,peptide_flag,peptide_notes"));
        form.setSort(sort);
        form.setMessage("Peptides_IN_Pool_PP" + pp.getPeptide_pool_name());
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        ButtonBar bb = getGridButtonbar(pv);
        rgn.setButtonBar(bb, DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(
                "The Peptides in the pool " + pp.getPeptide_pool_name() + "(PP" + form.getQueryValue() + ") are : ");
        return gridView;
    }

    protected GridView getGridViewByParentPool(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        PeptidePool pp = PepDBManager.getPeptidePoolByID(Integer.parseInt(form.getQueryValue()));
        TableInfo tableInfo = PepDBSchema.getInstance()
                .getTableInfoViewPoolDetails();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for parent peptidePool." + PepDBSchema.COLUMN_PARENT_POOL_ID + ": " + form);
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PARENT_POOL_ID, Integer.parseInt(form.getQueryValue()));
        Sort sort = new Sort(PepDBSchema.COLUMN_PEPTIDE_POOL_ID);
        form.setFilter(sFilter);
        form.setCInfo(tableInfo.getColumns("peptide_pool_id,peptide_pool_name,pool_type_desc,parent_pool_id,parent_pool_name,matrix_peptide_pool_id,comment,archived,createdby,created,modifiedby,modified"));
        form.setSort(sort);
        form.setMessage("Peptides_WITH_Parent_Pool_PP" + form.getQueryValue());
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonbar(pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(
                "The Sub-Pools of Parent Pool " + pp.getPeptide_pool_name() + "(PP" + form.getQueryValue() + ") are : ");
        return gridView;
    }

    protected GridView getGridViewBySequence(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        TableInfo tableInfo = PepDBSchema.getInstance()
                .getTableInfoViewGroupPeptides();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for peptideSequence." + PepDBSchema.COLUMN_PEPTIDE_SEQUENCE + ": " + form);
        SimpleFilter sFilter = new SimpleFilter();

        boolean sequenceIsEmpty = true;
        String sequence = form.getQueryValue();
        if((sequence != null) && (!sequence.trim().isEmpty())){
            sequenceIsEmpty = false;
        }
        if(!sequenceIsEmpty)
        {
            sequence = sequence.trim().toUpperCase();
            sFilter.addWhereClause(PepDBSchema.COLUMN_PEPTIDE_SEQUENCE + " LIKE ?", new Object[]{"%" + sequence + "%"},
                    FieldKey.fromString(PepDBSchema.COLUMN_PEPTIDE_SEQUENCE));
            form.setFilter(sFilter);
        }
        Sort sort = new Sort(PepDBSchema.COLUMN_PEPTIDE_ID);

        form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,protein_cat_id,peptide_group_name,peptide_id_in_group,pathogen_id," +
                "sequence_length,amino_acid_start_pos,amino_acid_end_pos,child,parent,peptide_flag,peptide_notes,optimal_epitope_list_id,hla_restriction"));
        form.setSort(sort);
        if(!sequenceIsEmpty)
        {
            form.setMessage("Peptides_WITH_Sequence_" + sequence);
        }
        else
        {
            form.setMessage("All_Peptides_In_DB");
        }
        DataRegion rgn = getDataRegion(getContainer(), form, MAX_ROWS);
        rgn.setButtonBar(getGridButtonbar(pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setSort(sort);
        if(!sequenceIsEmpty) {
            gridView.setFilter(sFilter);
            gridView.setTitle(
                               "The Peptides Containing the Sequence string '" + sequence + "' are : ");
        }
        else
        {
            gridView.setTitle("All the Peptides in Peptide DB : ");
        }
        return gridView;
    }

    protected GridView getGridViewByProtein(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        ProteinCategory pc = PepDBManager.getProCatByID(Integer.parseInt(form.getQueryValue()));
        TableInfo tableInfo = PepDBSchema.getInstance()
                .getTableInfoViewGroupPeptides();
        form.setTInfo(tableInfo);
        String title = "Peptides in Protein Category " + pc.getProtein_cat_desc();
        _log.debug("Creating a Filter for proteinCategory." + PepDBSchema.COLUMN_PROTEIN_CAT_ID + ": " + form);
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PROTEIN_CAT_ID, Integer.parseInt(form.getQueryValue()));
        if (form.getAAEnd() == null && form.getAAStart() != null)
        {
            sFilter.addCondition(PepDBSchema.COLUMN_AMINO_ACID_START_POS, Integer.parseInt(form.getAAStart()), CompareType.GTE);
            title = "Peptides in Protein Category " + pc.getProtein_cat_desc() + " and after AAStart " + form.getAAStart();
        }
        if (form.getAAEnd() != null && form.getAAStart() == null)
        {
            sFilter.addCondition(PepDBSchema.COLUMN_AMINO_ACID_END_POS, Integer.parseInt(form.getAAEnd()), CompareType.LTE);
            title = "Peptides in Protein Category " + pc.getProtein_cat_desc() + " and before AAEnd " + form.getAAEnd();
        }
        if (form.getAAStart() != null && form.getAAEnd() != null)
        {
            sFilter.addBetween(tableInfo.getColumn(PepDBSchema.COLUMN_AMINO_ACID_START_POS).getFieldKey(), Integer.parseInt(form.getAAStart()), Integer.parseInt(form.getAAEnd()));
            sFilter.addBetween(tableInfo.getColumn(PepDBSchema.COLUMN_AMINO_ACID_END_POS).getFieldKey(), Integer.parseInt(form.getAAStart()), Integer.parseInt(form.getAAEnd()));
            title = "Peptides in Protein Category " + pc.getProtein_cat_desc() + " and between AAStart " + form.getAAStart() + " and AAEnd " + form.getAAEnd();
        }
        Sort sort = new Sort(PepDBSchema.COLUMN_PEPTIDE_ID);
        form.setFilter(sFilter);
        form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,protein_cat_id,peptide_group_name,peptide_id_in_group,pathogen_id," +
                "sequence_length,amino_acid_start_pos,amino_acid_end_pos,child,parent,peptide_flag,peptide_notes,optimal_epitope_list_id,hla_restriction"));
        form.setSort(sort);
        form.setMessage("Peptides_IN_Protein_" + form.getQueryValue());
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonbar(pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(title);
        return gridView;
    }

    protected GridView getGridViewByParentId(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        TableInfo tableInfo = PepDBSchema.getInstance()
                .getTableInfoViewParentChildDetails();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for parentID. : " + form);
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PARENT_ID, Integer.parseInt(form.getQueryValue()));
        Sort sort = new Sort(PepDBSchema.COLUMN_CHILD_ID);
        form.setFilter(sFilter);
        form.setCInfo(tableInfo.getColumns("child_id,child_sequence,child_protein,child_group,child_lab_id,child_seq_length," +
                "child_aastart,child_aaend,child_peptide_flag,child_peptide_notes,child_optimal_epitope_list_id,child_hla_restriction"));
        form.setSort(sort);
        form.setMessage("CHILD_Peptides_WITH_Parent_P" + form.getQueryValue());
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonbar(pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(
                "The Child Peptides of Parent peptide ID P" + form.getQueryValue() + " are : ");
        return gridView;
    }

    protected GridView getGridViewByParent(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        TableInfo tableInfo = PepDBSchema.getInstance()
                .getTableInfoViewParentChildDetails();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for parentSequence. : " + form);
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PARENT_SEQUENCE, form.getQueryValue().trim().toUpperCase());
        Sort sort = new Sort(PepDBSchema.COLUMN_CHILD_ID);
        form.setFilter(sFilter);
        form.setCInfo(tableInfo.getColumns("parent_id,child_id,child_sequence,child_protein,child_group,child_lab_id,child_seq_length," +
                "child_aastart,child_aaend,child_peptide_flag,child_peptide_notes,child_optimal_epitope_list_id,child_hla_restriction"));
        form.setSort(sort);
        form.setMessage("CHILD_Peptides_WITH_Parent_Sequence_" + form.getQueryValue());
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonbar(pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(
                "The Child Peptides of Parent Sequence " + form.getQueryValue() + " are : ");
        return gridView;
    }

    protected GridView getGridViewByChildId(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        TableInfo tableInfo = PepDBSchema.getInstance()
                .getTableInfoViewParentChildDetails();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for childID. : " + form);
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_CHILD_ID, Integer.parseInt(form.getQueryValue()));
        Sort sort = new Sort(PepDBSchema.COLUMN_PARENT_ID);
        form.setFilter(sFilter);
        form.setCInfo(tableInfo.getColumns("parent_id,parent_sequence,parent_protein,parent_group,parent_lab_id," +
                "parent_seq_length,parent_aastart,parent_aaend,parent_peptide_flag,parent_peptide_notes"));
        form.setSort(sort);
        form.setMessage("PARENT_Peptides_WITH_Child_P" + form.getQueryValue());
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonbar(pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(
                "The Parent Peptides of Child peptide ID P" + form.getQueryValue() + " are : ");
        return gridView;
    }

    protected GridView getGridViewByChild(PeptideQueryForm form, PropertyValues pv) throws Exception
    {
        TableInfo tableInfo = PepDBSchema.getInstance()
                .getTableInfoViewParentChildDetails();
        form.setTInfo(tableInfo);
        _log.debug("Creating a Filter for childID. : " + form);
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_CHILD_SEQUENCE, form.getQueryValue().trim().toUpperCase());
        Sort sort = new Sort(PepDBSchema.COLUMN_PARENT_ID);
        form.setFilter(sFilter);
        form.setCInfo(tableInfo.getColumns("child_id,parent_id,parent_sequence,parent_protein,parent_group,parent_lab_id," +
                "parent_seq_length,parent_aastart,parent_aaend,parent_peptide_flag,parent_peptide_notes"));
        form.setSort(sort);
        form.setMessage("PARENT_Peptides_WITH_Child_Sequence_" + form.getQueryValue());
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setButtonBar(getGridButtonbar(pv), DataRegion.MODE_GRID);
        GridView gridView = new GridView(rgn, (BindException) null);
        gridView.setFilter(sFilter);
        gridView.setSort(sort);
        gridView.setTitle(
                "The Parent Peptides of Child Sequence " + form.getQueryValue() + " are : ");
        return gridView;
    }

    protected DetailsView getPeptideDetailsView(PeptideQueryForm form, Peptides p) throws Exception
    {
        TableInfo tableInfo = PepDBSchema.getInstance().getTableInfoPeptides();
        form.setTInfo(tableInfo);
        form.setCInfo(tableInfo.getColumns("peptide_id,peptide_sequence,protein_cat_id,sequence_length,amino_acid_start_pos," +
                "amino_acid_end_pos,child,parent,optimal_epitope_list_id,hla_restriction,storage_location,src_file_name,peptide_flag,peptide_notes,createdby,created,modifiedby,modified"));
        DataRegion rgn = getDataRegion(getContainer(), form, Table.ALL_ROWS);
        rgn.setShowBorders(true);
        rgn.setShadeAlternatingRows(true);
        ButtonBar buttonBar = new ButtonBar();
        ActionURL homeUrl = new ActionURL(BeginAction.class, getContainer());
        ActionButton homeButton = new ActionButton("Peptide Home", homeUrl);
        buttonBar.add(homeButton);
        ActionURL editUrl = new ActionURL(EditPeptideAction.class, getContainer());
        editUrl.addParameter(PepDBSchema.COLUMN_PEPTIDE_ID, p.getPeptide_id());
        ActionButton editButton = new ActionButton("Edit Peptide", editUrl);
        buttonBar.add(editButton);
        rgn.setButtonBar(buttonBar, DataRegion.MODE_DETAILS);
        DetailsView dataView = new DetailsView(rgn, p.getPeptide_id());
        dataView.setTitle("Peptide Detail Information from peptides table for peptide : P" + p.getPeptide_id());
        return dataView;
    }

    private DataRegion getDataRegion(Container c, PeptideQueryForm form, int maxRows) throws Exception
    {
        DataRegion rgn = new DataRegion();
        List<String> columnList = new ArrayList<String>();
        List<DisplayColumn> displayColumnList = new ArrayList<DisplayColumn>();
        
        for (ColumnInfo col : form.getCInfo())
        {
            if (col != null)
            {
                columnList.add(col.getName());
                DisplayColumn dc;

                if (PepDBSchema.COLUMN_PEPTIDE_ID.equals(col.getName()) || PepDBSchema.COLUMN_PARENT_ID.equals(col.getName()) || PepDBSchema.COLUMN_CHILD_ID.equals(col.getName()))
                {
                    dc = new DCpeptideId(col);
                }
                else if (PepDBSchema.COLUMN_PEPTIDE_POOL_ID.equals(col.getName()))
                {
                    dc = new DCpeptidePoolId(col);
                }
                else if (PepDBSchema.COLUMN_PARENT_POOL_ID.equals(col.getName()))
                {
                    dc = new DCparentPoolId(col);
                }
                else {
                    dc = col.getRenderer();
                }
                displayColumnList.add(dc);
            }
        }
        rgn.setColumns(form.getCInfo());
        rgn.setDisplayColumns(displayColumnList);
        rgn.setShowBorders(true);
        rgn.setShadeAlternatingRows(true);
        rgn.setMaxRows(maxRows);
        ViewContext ctx = getViewContext();
        HttpSession session = ctx.getRequest().getSession(true);
        session.setAttribute("PEPTIDE_QUERY_FORM", form);


        if (columnList.contains(PepDBSchema.COLUMN_PEPTIDE_ID))
        {
            ColumnInfo ci = rgn.getTable().getColumn("peptide_id");
            QuerySettings qs = new QuerySettings(getViewContext(), rgn.getName());
            qs.addAggregates(new Aggregate(ci, Aggregate.BaseType.COUNT));
            qs.setMaxRows(Table.ALL_ROWS);
            rgn.setSettings(qs);
            // We want MOST of the query settings into our dataregion settings, but we still want to paginate the rows.
            rgn.setMaxRows(maxRows);

        }
        return rgn;
    }

    private ButtonBar getButtonBar()
    {
        ButtonBar buttonBar = new ButtonBar();
        ActionURL homeUrl = new ActionURL(BeginAction.class, getContainer());
        ActionButton homeButton = new ActionButton("Peptide Home", homeUrl);
        buttonBar.add(homeButton);
        ActionURL searchURL = new ActionURL(SearchForPeptidesAction.class, getContainer());
        ActionButton searchButton = new ActionButton(searchURL, "Peptide Search Page");
        buttonBar.add(searchButton);
        return buttonBar;
    }

    /*
     * Returns a ButtonBar whose excel and text buttons will
     * render whatever grid view is set in the user's Session.
     */
    private ButtonBar getGridButtonbar(PropertyValues pv)
    {
        return getGridButtonbarForClasses(pv, PeptideDefaultExcelExportAction.class,PeptideDefaultTextExportAction.class);
    }

    /*
     * Returns a ButtonBar whose excel and text buttons explicitly point to an
     * action that will generate the Peptide Pools in Pool report. Use when
     * multiple grids exist on the same view.
     */
    private ButtonBar getGridButtonbarPoolsInPool(PropertyValues pv)
    {
        return getGridButtonbarForClasses(pv,PoolsInPoolExcelExportAction.class,PoolsInPoolTextExportAction.class);
    }

    /*
     * Returns a ButtonBar whose excel and text buttons explicitly point to an
     * action that will generate the Peptides in Pool report. Use when
     * multiple grids exist on the same view.
     */
    private ButtonBar getGridButtonbarPeptidesInPool(PropertyValues pv)
    {
        return getGridButtonbarForClasses(pv,PeptidesInPoolExcelExportAction.class,PeptidesInPoolTextExportAction.class);
    }

    private ButtonBar getGridButtonbarForClasses(PropertyValues pv, Class excelActionClass, Class textActionClass)
    {
        ButtonBar gridButtonBar = getButtonBar();
        //ActionURL exportUrl = new ActionURL(PeptideDefaultExcelExportAction.class, getContainer());
        ActionURL exportUrl = new ActionURL(excelActionClass, getContainer());
        exportUrl.setPropertyValues(pv);
        ActionButton export = new ActionButton(exportUrl, "Export to Excel");
        export.setActionType(ActionButton.Action.LINK);
        gridButtonBar.add(export);

        ActionURL exportTextURL = new ActionURL(textActionClass, getContainer());
        exportTextURL.setPropertyValues(pv);
        ActionButton exportToText = new ActionButton(exportTextURL, "Export All To Text");
        exportToText.setActionType(ActionButton.Action.LINK);
        gridButtonBar.add(exportToText);

        return gridButtonBar;
        //rgn.setButtonBar(gridButtonBar, DataRegion.MODE_GRID);
    }
}
