package org.scharp.atlas.pepdb.model;

import org.labkey.api.data.Entity;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 12, 2007
 * Time: 1:11:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Peptides extends Entity
{
    private Integer peptide_id;
    private String peptide_sequence;
    private Integer protein_cat_id;
    private Integer amino_acid_start_pos;
    private Integer amino_acid_end_pos;
    private Integer sequence_length;
    private boolean child;
    private boolean parent;
    private String src_file_name;
    private String storage_location;
    private Integer optimal_epitope_list_id;
    private String hla_restriction;
    private Source src;
    private List<PeptideGroup> peptideGroups;
    private ProteinCategory proteinCat;
    private Pathogen pathogen;
    private Parent parentPep;
    private List<Parent> parents;
    private boolean peptide_flag;
    private String peptide_notes;

    public List<Parent> getParents()
    {
        return parents;
    }

    public void setParents(List<Parent> parents)
    {
        this.parents = parents;
    }


    public Parent getParentPep()
    {
        return parentPep;
    }

    public void setParentPep(Parent parentPep)
    {
        this.parentPep = parentPep;
    }

    public ProteinCategory getProteinCat()
    {
        return proteinCat;
    }

    public void setProteinCat(ProteinCategory proteinCat)
    {
        this.proteinCat = proteinCat;
    }

    public Pathogen getPathogen()
    {
        return pathogen;
    }

    public void setPathogen(Pathogen pathogen)
    {
        this.pathogen = pathogen;
    }

    public Integer getPeptide_id()
    {
        return peptide_id;
    }

    public void setPeptide_id(Integer peptide_id)
    {
        this.peptide_id = peptide_id;
    }

    public String getPeptide_sequence()
    {
        return peptide_sequence;
    }

    public void setPeptide_sequence(String peptide_sequence)
    {
        this.peptide_sequence = peptide_sequence;
    }

    public Integer getProtein_cat_id()
    {
        return protein_cat_id;
    }

    public void setProtein_cat_id(Integer protein_cat_id)
    {
        this.protein_cat_id = protein_cat_id;
    }

    public boolean isChild()
    {
        return child;
    }

    public void setChild(boolean child)
    {
        this.child = child;
    }

    public List<PeptideGroup> getPeptideGroups()
    {
        return peptideGroups;
    }

    public void setPeptideGroups(List<PeptideGroup> peptideGroups)
    {
        this.peptideGroups = peptideGroups;
    }

    public boolean isParent()
    {
        return parent;
    }

    public void setParent(boolean parent)
    {
        this.parent = parent;
    }

    public String getSrc_file_name()
    {
        return src_file_name;
    }

    public void setSrc_file_name(String src_file_name)
    {
        this.src_file_name = src_file_name;
    }

    public Integer getAmino_acid_start_pos()
    {
        return amino_acid_start_pos;
    }

    public void setAmino_acid_start_pos(Integer amino_acid_start_pos)
    {
        this.amino_acid_start_pos = amino_acid_start_pos;
    }

    public Integer getAmino_acid_end_pos()
    {
        return amino_acid_end_pos;
    }

    public void setAmino_acid_end_pos(Integer amino_acid_end_pos)
    {
        this.amino_acid_end_pos = amino_acid_end_pos;
    }

    public Integer getSequence_length()
    {
        return sequence_length;
    }

    public void setSequence_length(Integer sequence_length)
    {
        this.sequence_length = sequence_length;
    }

    public String getStorage_location()
    {
        return storage_location;
    }

    public void setStorage_location(String storage_location)
    {
        this.storage_location = storage_location;
    }

    public Integer getOptimal_epitope_list_id()
    {
        return optimal_epitope_list_id;
    }

    public void setOptimal_epitope_list_id(Integer optimal_epitope_list_id)
    {
        this.optimal_epitope_list_id = optimal_epitope_list_id;
    }

    public String getHla_restriction()
    {
        return hla_restriction;
    }

    public void setHla_restriction(String hla_restriction)
    {
        this.hla_restriction = hla_restriction;
    }

    public Source getSrc()
    {
        return src;
    }

    public void setSrc(Source src)
    {
        this.src = src;
    }

    public boolean isPeptide_flag()
    {
        return peptide_flag;
    }

    public void setPeptide_flag(boolean peptide_flag)
    {
        this.peptide_flag = peptide_flag;
    }

    public String getPeptide_notes()
    {
        return peptide_notes;
    }

    public void setPeptide_notes(String peptide_notes)
    {
        this.peptide_notes = peptide_notes;
    }
}
