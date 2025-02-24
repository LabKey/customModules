package org.scharp.atlas.pepdb.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jun 7, 2007
 * Time: 12:23:53 PM
 * Scott changed getDescription()
 */
public class PeptidePool extends Entity
{
    private Integer peptide_pool_id;
    private String peptide_pool_name;
    private String comment;
    private Integer pool_type_id;
    private String pool_type_desc;
    private boolean archived;
    private Integer parent_pool_id;
    private String parent_pool_name;
    private String matrix_peptide_pool_id;
    private static Logger log = LogManager.getLogger(PeptidePool.class);

    public PeptidePool()
    {
    }

    public PeptidePool(Integer peptide_pool_id, String peptide_pool_name)
    {
        this.peptide_pool_id = peptide_pool_id;
        this.peptide_pool_name = peptide_pool_name;
    }

    public Integer getPeptide_pool_id()
    {
        return peptide_pool_id;
    }

    public void setPeptide_pool_id(Integer peptide_pool_id)
    {
        this.peptide_pool_id = peptide_pool_id;
    }

    public String getPeptide_pool_name()
    {
        return peptide_pool_name;
    }

    public void setPeptide_pool_name(String peptide_pool_name)
    {
        this.peptide_pool_name = peptide_pool_name;
    }

    public Integer getPool_type_id()
    {
        return pool_type_id;
    }

    public void setPool_type_id(Integer pool_type_id)
    {
        this.pool_type_id = pool_type_id;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getPool_type_desc()
    {
        return pool_type_desc;
    }

    public void setPool_type_desc(String pool_type_desc)
    {
        this.pool_type_desc = pool_type_desc;
    }

    public boolean getArchived()
    {
        return archived;
    }

    public void setArchived(boolean archived)
    {
        this.archived = archived;
    }

    public Integer getParent_pool_id()
    {
        return parent_pool_id;
    }

    public void setParent_pool_id(Integer parent_pool_id)
    {
        this.parent_pool_id = parent_pool_id;
    }

    public String getParent_pool_name()
    {
        return parent_pool_name;
    }

    public void setParent_pool_name(String parent_pool_name)
    {
        this.parent_pool_name = parent_pool_name;
    }

    public String getMatrix_peptide_pool_id()
    {
        return matrix_peptide_pool_id;
    }

    public void setMatrix_peptide_pool_id(String matrix_peptide_pool_id)
    {
        this.matrix_peptide_pool_id = matrix_peptide_pool_id;
    }
}
