package org.scharp.atlas.pepdb.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jan 25, 2010
 * Time: 2:13:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class PoolType
{
    private Integer pool_type_id;
    private String pool_type_desc;

    public Integer getPool_type_id()
    {
        return pool_type_id;
    }

    public void setPool_type_id(Integer pool_type_id)
    {
        this.pool_type_id = pool_type_id;
    }

    public String getPool_type_desc()
    {
        return pool_type_desc;
    }

    public void setPool_type_desc(String pool_type_desc)
    {
        this.pool_type_desc = pool_type_desc;
    }
}
