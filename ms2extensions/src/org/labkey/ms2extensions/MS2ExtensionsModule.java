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

import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.module.DefaultModule;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.module.ModuleProperty;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;
import org.labkey.api.view.BaseWebPartFactory;
import org.labkey.api.view.Portal;
import org.labkey.api.view.ViewContext;
import org.labkey.api.view.WebPartFactory;
import org.labkey.api.view.WebPartView;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Collections;

/**
 * User: jeckels
 * Date: 1/16/13
 */
public class MS2ExtensionsModule extends DefaultModule
{
    public static final String NAME = "MS2Extensions";
    public static final String PEPTIDE_COUNT_SCHEMA_PROPERTY = "peptideCountSchema";
    public static final String PEPTIDE_COUNT_QUERY_PROPERTY = "peptideCountQuery";
    public static final String SCHEMA_NAME = "ms2extensions";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public double getVersion()
    {
        return 12.31;
    }

    @Override
    protected void init()
    {
        addController("ms2extensions", MS2ExtensionsController.class);
    }

    @Override
    protected Collection<WebPartFactory> createWebPartFactories()
    {
        return Collections.<WebPartFactory>singleton(new BaseWebPartFactory("MS2 Runs With Peptide Counts")
        {
            @Override
            public WebPartView getWebPartView(ViewContext portalCtx, Portal.WebPart webPart) throws Exception
            {
                return new RunGridWebPart(portalCtx);
            }
        });
    }

    @Override
    public boolean hasScripts()
    {
        return true;
    }

    @Override
    protected void doStartup(ModuleContext moduleContext)
    {

        ModuleProperty schemaProperty = new ModuleProperty(this, PEPTIDE_COUNT_SCHEMA_PROPERTY);
        schemaProperty.setDefaultValue("ms2extensions");
        schemaProperty.setCanSetPerContainer(false);
        addModuleProperty(schemaProperty);

        ModuleProperty queryProperty = new ModuleProperty(this, PEPTIDE_COUNT_QUERY_PROPERTY);
        queryProperty.setDefaultValue("GetSequestPeptideCounts");
        queryProperty.setCanSetPerContainer(false);
        addModuleProperty(queryProperty);

        ContainerManager.addContainerListener(new ContainerManager.ContainerListener()
        {
            @Override
            public void containerCreated(Container c, User user) {}

            @Override
            public void containerDeleted(Container c, User user)
            {
                new SqlExecutor(DbSchema.get("ms2extensions")).execute(new SQLFragment("DELETE FROM ms2extensions.Ms2RunAggregates WHERE container = ?", c.getEntityId()));
            }

            @Override
            public void containerMoved(Container c, Container oldParent, User user) {}

            @Override
            public void propertyChange(PropertyChangeEvent evt) {}
        });

        DefaultSchema.registerProvider(SCHEMA_NAME, new DefaultSchema.SchemaProvider()
        {
            public QuerySchema getSchema(DefaultSchema schema)
            {
                if (schema.getContainer().getActiveModules().contains(ModuleLoader.getInstance().getModule(NAME)))
                {
                    return new SimpleUserSchema(SCHEMA_NAME, null, schema.getUser(), schema.getContainer(), DbSchema.get(SCHEMA_NAME));
                }
                return null;
            }
        });
    }
}