package org.scharp.atlas.pepdb.model;

import org.labkey.api.data.Entity;


public class PeptideGroup extends Entity
{
    private  Integer peptide_group_id;
    private  String peptide_group_name;
    private Integer pathogen_id;
    private String seq_ref;
    private Integer clade_id;
    private Integer pep_align_ref_id;
    private Integer group_type_id;

    public Integer getPeptide_group_id()
    {
        return peptide_group_id;
    }

    public void setPeptide_group_id(Integer peptide_group_id)
    {
        this.peptide_group_id = peptide_group_id;
    }

    public PeptideGroup() {
    }

    public Integer getPathogen_id() {
        return pathogen_id;
    }

    public void setPathogen_id(Integer pathogen_id) {
        this.pathogen_id = pathogen_id;
    }

    public String getPeptide_group_name() {
        return peptide_group_name;
    }

    public void setPeptide_group_name(String peptide_group_name) {
        this.peptide_group_name = peptide_group_name;
    }

    public String getSeq_ref()
    {
        return seq_ref;
    }

    public void setSeq_ref(String seq_ref)
    {
        this.seq_ref = seq_ref;
    }

    public Integer getClade_id()
    {
        return clade_id;
    }

    public void setClade_id(Integer clade_id)
    {
        this.clade_id = clade_id;
    }

    public Integer getPep_align_ref_id()
    {
        return pep_align_ref_id;
    }

    public void setPep_align_ref_id(Integer pep_align_ref_id)
    {
        this.pep_align_ref_id = pep_align_ref_id;
    }

    public Integer getGroup_type_id()
    {
        return group_type_id;
    }

    public void setGroup_type_id(Integer group_type_id)
    {
        this.group_type_id = group_type_id;
    }
}
