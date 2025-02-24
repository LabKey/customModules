/*
 * Copyright (c) 2016 LabKey Corporation
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
import org.labkey.api.module.MultiPortalFolderType;
import org.labkey.api.specimen.SpecimensPage;
import org.labkey.api.study.Study;
import org.labkey.api.study.StudyFolderTabs;
import org.labkey.api.study.StudyService;
import org.labkey.api.view.FolderTab;
import org.labkey.api.view.Portal;
import org.labkey.api.view.ViewContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * User: Nick
 * Date: 7/22/11
 */
public class StudyITNFolderType extends MultiPortalFolderType
{
    private static final String STUDY_ITN_FOLDER_TYPE_NAME = "Study (ITN)";

    private static final List<FolderTab> PAGES = List.of(
        new StudyFolderTabs.OverviewPage("Overview"),
        new StudyFolderTabs.DataAnalysisPage("Data & Reports"),
        new StudyFolderTabs.ParticipantsPage("Participants"),
        new SpecimensPage("Specimens"),
        new StudyFolderTabs.ManagePage("Manage")
    );

    StudyITNFolderType(TrialShareModule module)
    {
        super(STUDY_ITN_FOLDER_TYPE_NAME,
                "Standard Study folder type with tabs pre-configured for ITN TrialShare.",
                Collections.singletonList(Portal.getPortalPart("Study Overview").createWebPart()),
                null,
                getDefaultModuleSet(module, getModule("Experiment"), getModule("Study"), getModule("Pipeline")),
                getModule("Study"));
    }

    @NotNull
    @Override
    public Set<String> getLegacyNames()
    {
        return Collections.singleton("Study Redesign (ITN)");
    }

    @Override
    protected String getFolderTitle(ViewContext ctx)
    {
        Study study = StudyService.get().getStudy(ctx.getContainer());
        return study != null ? study.getLabel() : ctx.getContainer().getName();
    }

    @Override
    public String getStartPageLabel(ViewContext ctx)
    {
        Study study = StudyService.get().getStudy(ctx.getContainer());
        return study == null ? "New Study" : study.getLabel();
    }

    @Override
    public List<FolderTab> getDefaultTabs()
    {
        return PAGES;
    }
}