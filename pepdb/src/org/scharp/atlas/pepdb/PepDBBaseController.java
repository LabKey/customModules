package org.scharp.atlas.pepdb;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.attachments.AttachmentFile;
import org.labkey.api.data.BeanViewForm;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.DataColumn;
import org.labkey.api.data.RenderContext;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Sort;
import org.labkey.api.data.TableInfo;
import org.labkey.api.util.DateUtil;
import org.labkey.api.util.HtmlString;
import org.labkey.api.util.HtmlStringBuilder;
import org.labkey.api.util.Link;
import org.labkey.api.view.ActionURL;
import org.scharp.atlas.pepdb.model.PeptideGroup;
import org.scharp.atlas.pepdb.model.PeptidePool;
import org.scharp.atlas.pepdb.model.Peptides;
import org.scharp.atlas.pepdb.model.ProteinCategory;
import org.springframework.validation.Errors;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 6, 2009
 * Time: 12:43:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class PepDBBaseController extends SpringActionController
{
    private final static Logger _log = LogManager.getLogger(PepDBBaseController.class);

    private static final String VIEW_DISPLAY_PEPTIDE = "displayPeptide.view";
    protected static final String QRY_STRING_PEPTIDE_ID = "peptideId";

    public static class PeptideQueryForm
    {
        private String queryKey;
        private String queryValue;
        private String message;
        private TableInfo tInfo;
        private SimpleFilter filter;
        private List<ColumnInfo> cInfo;
        private Sort sort;
        private String AAStart;
        private String AAEnd;
        private String labId;

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }

        public List<ColumnInfo> getCInfo()
        {
            return cInfo;
        }

        public void setCInfo(List<ColumnInfo> cInfo)
        {
            this.cInfo = cInfo;
        }

        public String getQueryValue()
        {
            return queryValue;
        }

        public void setQueryValue(String queryValue)
        {
            this.queryValue = queryValue;
        }

        public TableInfo getTInfo()
        {
            return tInfo;
        }

        public void setTInfo(TableInfo tInfo)
        {
            this.tInfo = tInfo;
        }

        public SimpleFilter getFilter()
        {
            return filter;
        }

        public void setFilter(SimpleFilter filter)
        {
            this.filter = filter;
        }

        public Sort getSort()
        {
            return sort;
        }

        public void setSort(Sort sort)
        {
            this.sort = sort;
        }

        public String getQueryKey()
        {
            return queryKey;
        }

        public void setQueryKey(String queryKey)
        {
            this.queryKey = queryKey;
        }

        public String getAAStart()
        {
            return AAStart;
        }

        public void setAAStart(String AAStart)
        {
            this.AAStart = AAStart;
        }

        public String getAAEnd()
        {
            return AAEnd;
        }

        public void setAAEnd(String AAEnd)
        {
            this.AAEnd = AAEnd;
        }

        public String getLabId()
        {
            return labId;
        }

        public void setLabId(String labId)
        {
            this.labId = labId;
        }

        public boolean validate(Errors errors) throws SQLException
        {
            if(getQueryKey() == null || StringUtils.trimToNull(getQueryKey()) == null)
                errors.reject(null, "The Search Criteria must be entered.");
            String qValue = getQueryValue();
            if (getQueryKey() != null && getQueryKey().equals(PepDBSchema.COLUMN_PARENT_SEQUENCE))
            {
                if (StringUtils.trimToNull(qValue) == null)
                    errors.reject(null, "The Parent Sequence must be entered.");
            }
            if (getQueryKey() != null && getQueryKey().equals(PepDBSchema.COLUMN_CHILD_SEQUENCE))
            {
                if (StringUtils.trimToNull(qValue) == null)
                    errors.reject(null, "The Child Sequence must be entered.");
            }
            if (getQueryKey() != null && getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID))
            {
                if (StringUtils.trimToNull(qValue) == null)
                    errors.reject(null, "Peptide Group must be selected to get peptides in a group.");
                /*
                if(StringUtils.trimToNull(getLabId()) == null)
                    errors.reject(null, "Peptide Number must be entered.");
                    */
            }
            if (getQueryKey() != null && getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_POOL_ID))
            {
                if (StringUtils.trimToNull(qValue) == null)
                    errors.reject(null, "Peptide Pool Name must be selected to get peptides in a pool.");
            }
            if (getQueryKey() != null && getQueryKey().equals(PepDBSchema.COLUMN_PROTEIN_CAT_ID))
            {
                if (StringUtils.trimToNull(qValue) == null)
                    errors.reject(null, "Protein Category must be selected to get peptides in a protein category.");
                else
                {
                    ProteinCategory pc = PepDBManager.getProCatByID(Integer.parseInt(getQueryValue()));
                    if(pc.getProtein_cat_desc().trim().contains("-"))
                    {
                        if(StringUtils.trimToNull(getAAStart()) != null || StringUtils.trimToNull(getAAEnd()) != null)
                            errors.reject(null,"When you select a hyphanated Protein Category : "+pc.getProtein_cat_desc()+" AAStart & AAEnd values are not allowed.");
                    }
                    else
                    {
                        if(StringUtils.trimToNull(getAAStart()) != null && validateInteger(getAAStart().trim()) == null)
                            errors.reject(null, "AAStart must be an Integer.");
                        if(StringUtils.trimToNull(getAAEnd()) != null && validateInteger(getAAEnd().trim()) == null)
                            errors.reject(null, "AAEnd must be an Integer.");
                        if(StringUtils.trimToNull(getAAStart()) != null && validateInteger(getAAStart().trim()) != null
                                && StringUtils.trimToNull(getAAEnd()) != null && validateInteger(getAAEnd().trim()) != null
                                && validateInteger(getAAStart().trim()) > validateInteger(getAAEnd().trim()))
                            errors.reject(null, "AAStart must be less than or equal to AAEnd.");
                    }
                }
            }
            if (getQueryKey() != null && getQueryKey().equals(PepDBSchema.COLUMN_PEPTIDE_ID))
            {
                if (StringUtils.trimToNull(qValue) == null)
                    errors.reject(null, "The Peptide Id range must be entered.");
                if (qValue != null && qValue.length() > 0)
                {
                    if (!(qValue.matches("\\d+-\\d+")))
                    {
                        errors.reject(null, "To get the peptides in the range you should specify the Range of numbers.\n" +
                                "The format for specify the range of peptide is <number>-<number> Example would be 30-100");
                    }
                    else
                    {
                        String[] range = qValue.split("-");
                        if (Integer.parseInt(range[0]) > Integer.parseInt(range[1]))
                        {
                            errors.reject(null, "The minimum value which is before '-' should be less than the max value which is after '-'.\n");

                        }
                    }
                }
            }
            if(errors != null && errors.getErrorCount() >0)
                return false;
            return true;
        }

    }

    public static class DisplayPeptideForm
    {
        private String peptide_id;
        private boolean modify = false;
        private String manufactureStatus;

        public String getPeptide_id()
        {
            return peptide_id;
        }

        public void setPeptide_id(String peptide_id)
        {
            this.peptide_id = peptide_id;
        }

        public void setModify(boolean modify)
        {
            this.modify = modify;
        }

        public boolean getModify()
        {
            return this.modify;
        }

        public String getManufactureStatus()
        {
            return manufactureStatus;
        }

        public void setManufactureStatus(String manufactureStatus)
        {
            this.manufactureStatus = manufactureStatus;
        }

        public String toString()
        {
            return "DisplayPeptideForm [peptideId:" + this.peptide_id +
                    ", modify:" + this.modify +
                    ", manufactureStatus:" + this.manufactureStatus + "]";
        }
    }

    public static class PeptideAndGroupForm extends DisplayPeptideForm
    {

        private String peptide_group_id;

        public String getPeptide_group_id()
        {
            return peptide_group_id;
        }

        public void setPeptide_group_id(String peptide_group_id)
        {
            this.peptide_group_id = peptide_group_id;
        }

        public String toString()
        {
            return "PeptideAndGroupForm [peptideGroup:" + this.peptide_group_id + "] - " + super.toString();
        }

    }

    public static class PeptideAndPoolForm extends DisplayPeptideForm
    {
        private String peptide_pool_id;

        public String getPeptide_pool_id()
        {
            return peptide_pool_id;
        }

        public void setPeptide_pool_id(String peptide_pool_id)
        {
            this.peptide_pool_id = peptide_pool_id;
        }

        public String toString()
        {
            return "PeptideAndPoolForm [peptidePool:" + this.peptide_pool_id + "] - " + super.toString();
        }
    }

    public static class PeptideForm extends BeanViewForm<Peptides> {
        public PeptideForm()
        {
            super(Peptides.class, PepDBSchema.getInstance().getTableInfoPeptides());

        }
        public PeptideForm(String peptideID)
        {
            this();
            set("peptide_id", String.valueOf(peptideID));
        }
    }

    public static class PeptidePoolForm extends BeanViewForm<PeptidePool> {
        public PeptidePoolForm()
        {
            super(PeptidePool.class, PepDBSchema.getInstance().getTableInfoPeptidePools());

        }
        public PeptidePoolForm(String peptidePoolID)
        {
            this();
            set("peptide_pool_id", String.valueOf(peptidePoolID));
        }

    }

    public static class PeptideGroupForm extends BeanViewForm<PeptideGroup> {
        public PeptideGroupForm()
        {
            super(PeptideGroup.class, PepDBSchema.getInstance().getTableInfoPeptideGroups());

        }
        public PeptideGroupForm(String peptideGroupID)
        {
            this();
            set("peptide_group_id", String.valueOf(peptideGroupID));
        }

        public void validate(Errors errors)
        {
            PeptideGroup bean = getBean();
            if(bean.getPathogen_id() == null || StringUtils.trimToNull(bean.getPathogen_id().toString())==null)
                errors.reject(null,"Pathogen is Required");
            if(bean.getClade_id() == null || StringUtils.trimToNull(bean.getClade_id().toString())==null)
                errors.reject(null,"Clade is Required");
            if(bean.getGroup_type_id() == null || StringUtils.trimToNull(bean.getGroup_type_id().toString())==null)
                errors.reject(null,"Group Type is Required");
            if (StringUtils.trimToNull(bean.getPeptide_group_name()) == null)
                errors.reject(null, "Peptide Group Name is required.");
        }

        public void validateName(Errors errors) throws SQLException
        {
            PeptideGroup bean = getBean();
            PeptideGroup pg = PepDBManager.getPeptideGroupByName(bean);
            if(pg != null)
                errors.reject(null, "Peptide Group with the name : "+bean.getPeptide_group_name()+" with a different ID already exists in the database.");
        }
    }

    public static class FileForm
    {
        private String message;
        private String actionType;

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }

        public String getActionType()
        {
            return actionType;
        }

        public void setActionType(String actionType)
        {
            this.actionType = actionType;
        }

        public boolean validate(Errors errors, AttachmentFile file) throws Exception
        {
            if (file == null || file.getSize() == 0)
                errors.reject(null, "File is required.File should be tab delimited text file and the number of field vary depending on file type.");
            else
            {
                if (!(file.getFilename().endsWith(".txt")))
                {
                    errors.reject(null, "File name must end with in .txt.\nFile should be tab delimited text file and the number of field vary depending on file type.");
                }
            }
            if (getActionType() == null || getActionType().length() == 0)
                errors.reject(null, "File Type is required");
            if(errors.getErrorCount() > 0)
                return false;
            return true;
        }
    }

    public class DCpeptideId extends DataColumn
    {
        public DCpeptideId(ColumnInfo col)
        {
            super(col);
        }

        @Override
        public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!PepDBSchema.COLUMN_PEPTIDE_ID.equals(c.getName()) && !PepDBSchema.COLUMN_PARENT_ID.equals(c.getName()) && !PepDBSchema.COLUMN_CHILD_ID.equals(c.getName()))
                super.renderGridCellContents(ctx, out);
            else
            {
                Integer peptideId = (Integer) rowMap.get(c.getName());
                try
                {
                    new Link.LinkBuilder("P" + peptideId).clearClasses()
                            .target("_self")
                            .href(new ActionURL(PepDBController.DisplayPeptideAction.class, getContainer())
                                    .addParameter(PepDBSchema.COLUMN_PEPTIDE_ID, peptideId))
                            .build()
                            .appendTo(out);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }

        @Override
        @NotNull
        public HtmlString getFormattedHtml(RenderContext ctx) {
            HtmlStringBuilder hsb = HtmlStringBuilder.of("P");
            hsb.append(super.getFormattedHtml(ctx));
            return hsb.getHtmlString();
        }

        @Override
        public Object getDisplayValue(RenderContext ctx)
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!PepDBSchema.COLUMN_PEPTIDE_ID.equals(c.getName()) && !PepDBSchema.COLUMN_PARENT_ID.equals(c.getName()) && !PepDBSchema.COLUMN_CHILD_ID.equals(c.getName()))
                return super.getValue(ctx);
            else
            {
                Integer peptideId = (Integer) rowMap.get(c.getName());
                try
                {
                    return ("P"+peptideId);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "EXPORT/OUTPUT ERROR";
                }
            }
        }

        @Override
        public Class getValueClass()
        {
            return Integer.class;
        }

        @Override
        public Class getDisplayValueClass()
        {
            return String.class;
        }
    }

    public class DCpeptidePoolId extends DataColumn
    {
        public DCpeptidePoolId(ColumnInfo col)
        {
            super(col);
        }

        @Override
        public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!PepDBSchema.COLUMN_PEPTIDE_POOL_ID.equals(c.getName()))
                super.renderGridCellContents(ctx, out);
            else
            {
                Integer peptidePoolId = (Integer) rowMap.get(c.getName());
                try
                {
                    new Link.LinkBuilder("PP" + peptidePoolId).clearClasses()
                            .target("_self")
                            .href(new ActionURL(PepDBController.DisplayPeptidePoolInformationAction.class, getContainer())
                                    .addParameter(PepDBSchema.COLUMN_PEPTIDE_POOL_ID, peptidePoolId))
                            .build()
                            .appendTo(out);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        @Override
        @NotNull
        public HtmlString getFormattedHtml(RenderContext ctx) {
            HtmlStringBuilder hsb = HtmlStringBuilder.of("PP");
            hsb.append(super.getFormattedHtml(ctx));
            return hsb.getHtmlString();
        }

        @Override
        public Object getDisplayValue(RenderContext ctx)
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!PepDBSchema.COLUMN_PEPTIDE_POOL_ID.equals(c.getName()))
                return super.getValue(ctx);
            else
            {
                Integer peptidePoolId = (Integer) rowMap.get(c.getName());
                try
                {
                    return ("PP" + peptidePoolId);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "EXPORT/OUTPUT ERROR";
                }
            }
        }

        @Override
        public Class getValueClass()
        {
            return Integer.class;
        }

        @Override
        public Class getDisplayValueClass()
        {
            return String.class;
        }
    }

    public class DCparentPoolId extends DataColumn
    {
        public DCparentPoolId(ColumnInfo col)
        {
            super(col);
        }

        @Override
        public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!PepDBSchema.COLUMN_PARENT_POOL_ID.equals(c.getName()))
                super.renderGridCellContents(ctx, out);
            else
            {
                Integer parentPoolId = (Integer) rowMap.get(c.getName());
                try
                {
                    if(parentPoolId != null)
                    {
                        new Link.LinkBuilder("PP" + parentPoolId).clearClasses()
                                .target("_self")
                                .href(new ActionURL(PepDBController.DisplayPeptidePoolInformationAction.class, getContainer())
                                        .addParameter(PepDBSchema.COLUMN_PEPTIDE_POOL_ID, parentPoolId))
                                .build()
                                .appendTo(out);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public Object getDisplayValue(RenderContext ctx)
        {
            ColumnInfo c = getColumnInfo();
            Map rowMap = ctx.getRow();
            if (!PepDBSchema.COLUMN_PARENT_POOL_ID.equals(c.getName()))
                return super.getValue(ctx);
            else
            {
                Integer parentPoolId = (Integer) rowMap.get(c.getName());
                try
                {
                    if (parentPoolId != null)
                    return ("PP" + parentPoolId);
                    else
                    return null;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "EXPORT/OUTPUT ERROR";
                }
            }
        }

        @Override
        public Class getValueClass()
        {
            return Integer.class;
        }

        @Override
        public Class getDisplayValueClass()
        {
            return String.class;
        }
    }

    public static Integer validateInteger(String value)
    {
        try
        {
            return Integer.valueOf(value);
        }
        catch(NumberFormatException e){return null;}
    }

    public static String toLZ(String s)
    {
        if (s.length() > 6) return s.substring(0, 6);
        else if (s.length() < 6) // pad on left with zeros
            return "000000000000000000000000000".substring(0, 6 - s.length()) + s;
        else return s;
    }

    public static java.util.Date isValidDate(String sDateIn) {

        try {
            java.util.Date dDate = new java.util.Date(DateUtil.parseDateTime(sDateIn));
            return dDate;
        } catch (ConversionException x) {
            return null;
        }
    }

    public static Float validateFloat(String value)
    {
        try
        {
            return Float.valueOf(value);
        }
        catch(NumberFormatException e){return null;}
    }
}
