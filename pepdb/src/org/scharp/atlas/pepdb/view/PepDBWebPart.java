package org.scharp.atlas.pepdb.view;

import org.labkey.api.view.JspView;

public class PepDBWebPart extends JspView<Object>
{
    public PepDBWebPart()
    {
        super("/org/scharp/atlas/pepdb/view/pepDBWebPart.jsp", null);
        setTitle("PepDB Web Part");
    }
}
