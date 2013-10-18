/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
