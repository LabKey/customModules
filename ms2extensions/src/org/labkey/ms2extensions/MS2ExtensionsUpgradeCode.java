package org.labkey.ms2extensions;

import org.apache.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.DeferredUpgrade;
import org.labkey.api.data.UpgradeCode;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.security.User;

/**
 * User: jeckels
 * Date: 10/15/13
 */
public class MS2ExtensionsUpgradeCode implements UpgradeCode
{
    private static final Logger LOG = Logger.getLogger(PeptideCountUpdater.class);

    /** called at 13.21->13.22 */
    @DeferredUpgrade
    public void updatePeptideCounts(final ModuleContext moduleContext)
    {
        // Don't bother upgrading if we're a new install and therefore don't have any data
        if (!moduleContext.isNewInstall())
        {
            updatePeptideCounts(ContainerManager.getRoot(), moduleContext.getUpgradeUser());
        }
    }

    private void updatePeptideCounts(Container container, User user)
    {
        String error = new PeptideCountUpdater().update(container, user);
        if (error != null)
        {
            LOG.warn(error);
        }
        for (Container child : container.getChildren())
        {
            updatePeptideCounts(child, user);
        }
    }
}
