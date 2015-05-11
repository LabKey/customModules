package org.labkey.hdrl;

import org.labkey.api.module.Module;
import org.labkey.api.module.MultiPortalFolderType;
import org.labkey.api.view.Portal;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by: jeckels
 * Date: 5/10/15
 */
public class HDRLFolderType extends MultiPortalFolderType
{
    public HDRLFolderType(HDRLModule module)
    {
        super("HDRL Request Portal",
                "HDRL portal for submitting assay requests and receiving results.",
                Collections.<Portal.WebPart>emptyList(),
                Arrays.asList(Portal.getPortalPart("HDRL Summary").createWebPart()),
                Collections.<Module>singleton(module),
                module);
    }
}
