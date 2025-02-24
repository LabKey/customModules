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

package org.labkey.trialshare;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.module.CodeOnlyModule;
import org.labkey.api.module.FolderTypeManager;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.study.SpecimenService;
import org.labkey.api.view.WebPartFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class TrialShareModule extends CodeOnlyModule
{
    public static final String NAME = "TrialShare";

    @Override
    public String getName()
    {
        return NAME;
    }

    public TrialShareModule()
    {
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
        addController(TrialShareController.NAME, TrialShareController.class);
    }

    @Override
    public void doStartup(ModuleContext moduleContext)
    {
        FolderTypeManager.get().registerFolderType(this, new StudyITNFolderType(this));
        SpecimenService.get().registerRequestCustomizer(new DelegatingSpecimenRequestCustomizer(SpecimenService.get().getRequestCustomizer()));
    }

    @Override
    @NotNull
    public Collection<String> getSummary(Container c)
    {
        return Collections.emptyList();
    }

    @Override
    @NotNull
    public Set<Class> getIntegrationTests()
    {
        return Set.of(
            TrialShareController.TrialShareExportTest.class
        );
    }
}