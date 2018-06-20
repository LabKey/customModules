/*
 * Copyright (c) 2015-2017 LabKey Corporation
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

package org.labkey.hdrl;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.module.DefaultModule;
import org.labkey.api.module.FolderTypeManager;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.query.ValidationException;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.util.SystemMaintenance;
import org.labkey.api.view.WebPartFactory;
import org.labkey.hdrl.query.HDRLQuerySchema;
import org.labkey.hdrl.query.LabWareQuerySchema;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class HDRLModule extends DefaultModule
{
    private static final Logger _log = Logger.getLogger(HDRLModule.class);
    public static final String NAME = "HDRL";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public double getVersion()
    {
        return 18.10;
    }

    @Override
    public boolean hasScripts()
    {
        return true;
    }

    @Override
    @NotNull
    protected Collection<WebPartFactory> createWebPartFactories()
    {
        return Collections.emptyList();
    }

    @Override
    protected void init()
    {
        addController(HDRLController.NAME,  HDRLController.class);
    }

    @Override
    public void doStartup(ModuleContext moduleContext)
    {
        // add a container listener so we'll know when our container is deleted:
        ContainerManager.addContainerListener(new HDRLContainerListener());

        HDRLQuerySchema.register(this);
        try
        {
            LabWareQuerySchema.verifyLabWareDataSource();
            LabWareQuerySchema.register(this);
        }
        catch (ValidationException e)
        {
            _log.error(e.getMessage() + "  Check your data source configuration.");
        }

        FolderTypeManager.get().registerFolderType(this, new HDRLFolderType(this));

        SystemMaintenance.addTask(new HDRLMaintenanceTask());

        HDRLController.registerAdminConsoleLinks();
    }

    @Override
    @NotNull
    public Collection<String> getSummary(Container c)
    {
        return Collections.emptyList();
    }

    @Override
    @NotNull
    public Set<String> getSchemaNames()
    {
        return PageFlowUtil.set(HDRLQuerySchema.NAME, LabWareQuerySchema.getFullyQualifiedDataSource());
    }
}