package org.scharp.atlas.pepdb.model;

import org.labkey.api.data.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Nov 5, 2007
 * Time: 9:01:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class PeptidePoolAssignment extends Entity
{
   private Integer peptide_pool_id;
    private Integer peptide_id;
    private Integer peptide_group_assignment_id;

    public Integer getPeptide_pool_id()
    {
        return peptide_pool_id;
    }

    public void setPeptide_pool_id(Integer peptide_pool_id)
    {
        this.peptide_pool_id = peptide_pool_id;
    }

    public Integer getPeptide_id()
    {
        return peptide_id;
    }

    public void setPeptide_id(Integer peptide_id)
    {
        this.peptide_id = peptide_id;
    }

    public Integer getPeptide_group_assignment_id()
    {
        return peptide_group_assignment_id;
    }

    public void setPeptide_group_assignment_id(Integer peptide_group_assignment_id)
    {
        this.peptide_group_assignment_id = peptide_group_assignment_id;
    }
}
