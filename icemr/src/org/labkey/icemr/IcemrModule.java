/*
 * Copyright (c) 2013-2017 LabKey Corporation
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

package org.labkey.icemr;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.module.DefaultModule;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.QueryService;
import org.labkey.api.study.PlateService;
import org.labkey.api.study.assay.AssayService;
import org.labkey.api.view.WebPartFactory;
import org.labkey.icemr.assay.DrugSensitivity.DrugSensitivityAssayProvider;
import org.labkey.icemr.assay.DrugSensitivity.DrugSensitivityDataHandler;
import org.labkey.icemr.assay.DrugSensitivity.DrugSensitivityPlateTypeHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class IcemrModule extends DefaultModule
{
    @Override
    public String getName()
    {
        return "icemr";
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

    @NotNull
    @Override
    protected Collection<WebPartFactory> createWebPartFactories()
    {
        return Collections.emptyList();
    }

    @Override
    protected void init()
    {
        addController("icemr", IcemrController.class);
    }

    @Override
    public void doStartup(ModuleContext moduleContext)
    {
        for (final String schemaName : getSchemaNames())
        {
            DefaultSchema.registerProvider(schemaName, new DefaultSchema.SchemaProvider(this)
            {
                public QuerySchema createSchema(final DefaultSchema schema, Module module)
                {
                    return QueryService.get().createSimpleUserSchema(schemaName, null, schema.getUser(), schema.getContainer(), IcemrSchema.getInstance().getSchema());
                }
            });
        }
        AssayService.get().registerAssayProvider(new DrugSensitivityAssayProvider());
        ExperimentService.get().registerExperimentDataHandler(new DrugSensitivityDataHandler());
        PlateService.get().registerPlateTypeHandler(new DrugSensitivityPlateTypeHandler());
    }

    @NotNull
    @Override
    public Collection<String> getSummary(Container c)
    {
        return Collections.emptyList();
    }

    @Override
    @NotNull
    public Set<String> getSchemaNames()
    {
        return Collections.singleton("icemr");
    }
}
