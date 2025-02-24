package org.scharp.atlas.pepdb.model;

/**
 * Created by IntelliJ IDEA.
 * User: slangley
 * Date: Dec 21, 2007
 * Time: 1:08:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class GroupType
{
    private Integer group_type_id;
    private String group_type_desc;

    public Integer getGroup_type_id()
    {
        return group_type_id;
    }

    public void setGroup_type_id(Integer group_type_id)
    {
        this.group_type_id = group_type_id;
    }

    public String getGroup_type_desc()
    {
        return group_type_desc;
    }

    public void setGroup_type_desc(String group_type_desc)
    {
        this.group_type_desc = group_type_desc;
    }
}
