package org.scharp.atlas.pepdb.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jul 12, 2007
 * Time: 1:19:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class Pathogen{
    private Integer pathogen_id;
    private String pathogen_desc;

    public Pathogen() {
    }

    public Integer getPathogen_id() {
        return pathogen_id;
    }

    public void setPathogen_id(Integer pathogen_id) {
        this.pathogen_id = pathogen_id;
    }

    public String getPathogen_desc() {
        return pathogen_desc;
    }

    public void setPathogen_desc(String pathogen_desc) {
        this.pathogen_desc = pathogen_desc;
    }
}
