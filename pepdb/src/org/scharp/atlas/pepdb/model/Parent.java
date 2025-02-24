package org.scharp.atlas.pepdb.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Nov 1, 2007
 * Time: 10:28:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class Parent
{
    private Integer peptide_id;
    private Integer linked_parent;

    public Integer getPeptide_id()
    {
        return peptide_id;
    }

    public void setPeptide_id(Integer peptide_id)
    {
        this.peptide_id = peptide_id;
    }

    public Integer getLinked_parent()
    {
        return linked_parent;
    }

    public void setLinked_parent(Integer linked_parent)
    {
        this.linked_parent = linked_parent;
    }
}
