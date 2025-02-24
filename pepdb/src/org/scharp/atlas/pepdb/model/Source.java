package org.scharp.atlas.pepdb.model;

import org.labkey.api.data.Entity;

import java.sql.Date;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jun 21, 2007
 * Time: 11:49:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class Source extends Entity
{
    private Integer peptide_group_assignment_id;
    private Integer peptide_id;
    private Integer peptide_group_id;
    private String peptide_group_name;
    private String peptide_id_in_group;
    private Float frequency_number;
    private Date frequency_number_date;
    private boolean in_current_file;

    public Source()
    {
    }

    public Integer getPeptide_group_assignment_id()
    {
        return peptide_group_assignment_id;
    }

    public void setPeptide_group_assignment_id(Integer peptide_group_assignment_id)
    {
        this.peptide_group_assignment_id = peptide_group_assignment_id;
    }

    public Integer getPeptide_id()
    {
        return peptide_id;
    }

    public void setPeptide_id(Integer peptide_id)
    {
        this.peptide_id = peptide_id;
    }

    public Integer getPeptide_group_id()
    {
        return peptide_group_id;
    }

    public void setPeptide_group_id(Integer peptide_group_id)
    {
        this.peptide_group_id = peptide_group_id;
    }

    public String getPeptide_id_in_group()
    {
        return peptide_id_in_group;
    }

    public void setPeptide_id_in_group(String peptide_id_in_group)
    {
        this.peptide_id_in_group = peptide_id_in_group;
    }

    public Float getFrequency_number()
    {
        return frequency_number;
    }

    public void setFrequency_number(Float frequency_number)
    {
        this.frequency_number = frequency_number;
    }

    public Date getFrequency_number_date()
    {
        return frequency_number_date;
    }

    public void setFrequency_number_date(Date frequency_number_date)
    {
        this.frequency_number_date = frequency_number_date;
    }

    public String getPeptide_group_name()
    {
        return peptide_group_name;
    }

    public void setPeptide_group_name(String peptide_group_name)
    {
        this.peptide_group_name = peptide_group_name;
    }

    public boolean isIn_current_file()
    {
        return in_current_file;
    }

    public void setIn_current_file(boolean in_current_file)
    {
        this.in_current_file = in_current_file;
    }
}
