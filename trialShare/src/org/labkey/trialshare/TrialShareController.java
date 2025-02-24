/*
 * Copyright (c) 2015-2019 LabKey Corporation
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

import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.labkey.api.action.ApiResponse;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.action.MutatingApiAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.admin.AbstractFolderContext.ExportType;
import org.labkey.api.admin.FolderExportContext;
import org.labkey.api.admin.FolderSerializationRegistry;
import org.labkey.api.admin.FolderWriter;
import org.labkey.api.admin.FolderWriterImpl;
import org.labkey.api.admin.StaticLoggerGetter;
import org.labkey.api.data.Container;
import org.labkey.api.data.PHI;
import org.labkey.api.pipeline.PipeRoot;
import org.labkey.api.pipeline.PipelineService;
import org.labkey.api.pipeline.PipelineUrls;
import org.labkey.api.qc.SampleStatusService;
import org.labkey.api.security.IgnoresTermsOfUse;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.NotFoundException;
import org.labkey.api.writer.FileSystemFile;
import org.springframework.validation.BindException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Marshal(Marshaller.Jackson)
public class TrialShareController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(TrialShareController.class);
    static final String NAME = "trialshare";

    public TrialShareController()
    {
        setActionResolver(_actionResolver);
    }

    /*
        TrialShare: simple study export action:

        example usage (via LabKey Remote Java API):

        SimplePostCommand pc = new SimplePostCommand("pipeline","TrialShareExport");
        JSONObject jo = new JSONObject();
        pc.setJsonObject(jo);
        CommandResponse cr = pc.execute(cn, "/Studies/ITN027AIOPR/Study Data/");
        System.out.println(cr.getText());
     */

    public static class TrialShareExportForm
    {
        private boolean missingValueIndicators;
        private boolean study;
        private boolean assayDatasets;
        private boolean assaySchedule;
        private boolean categories;
        private boolean cohortSettings;
        private boolean crfDatasets;
        private boolean customParticipantView;
        private boolean datasetData;
        private boolean etlDefinitions;
        private boolean participantCommentSettings;
        private boolean participantGroups;
        private boolean protocolDocuments;
        private boolean qcStateSettings;
        private boolean specimenSettings;
        private boolean specimens;
        private boolean treatmentData;
        private boolean visitMap;
        private boolean folderTypeAndActiveModules;
        private boolean fullTextSearchSettings;
        private boolean webpartPropertiesAndLayout;
        private boolean containerSpecificModuleProperties;
        private boolean roleAssignmentsForUsersAndGroups;
        private boolean listDesigns;
        private boolean listData;
        private boolean queries;
        private boolean gridViews;
        private boolean reportsAndCharts;
        private boolean externalSchemaDefinitions;
        private boolean wikisAndTheirAttachments;
        private boolean notificationSettings;
        private boolean sampleTypeDesigns;
        private boolean sampleTypeData;
        private boolean dataClassDesigns;
        private boolean dataClassData;
        private boolean inventoryLocationsAndItems;
        private boolean experimentsAndRuns;

        public boolean getMissingValueIndicators()
        {
            return missingValueIndicators;
        }

        public void setMissingValueIndicators(boolean missingValueIndicators)
        {
            this.missingValueIndicators = missingValueIndicators;
        }

        public boolean getStudy()
        {
            return study;
        }

        public void setStudy(boolean study)
        {
            this.study = study;
        }

        public boolean getAssayDatasets()
        {
            return assayDatasets;
        }

        public void setAssayDatasets(boolean assayDatasets)
        {
            this.assayDatasets = assayDatasets;
        }

        public boolean getAssaySchedule()
        {
            return assaySchedule;
        }

        public void setAssaySchedule(boolean assaySchedule)
        {
            this.assaySchedule = assaySchedule;
        }

        public boolean getCategories()
        {
            return categories;
        }

        public void setCategories(boolean categories)
        {
            this.categories = categories;
        }

        public boolean getCohortSettings()
        {
            return cohortSettings;
        }

        public void setCohortSettings(boolean cohortSettings)
        {
            this.cohortSettings = cohortSettings;
        }

        public boolean getCrfDatasets()
        {
            return crfDatasets;
        }

        public void setCrfDatasets(boolean crfDatasets)
        {
            this.crfDatasets = crfDatasets;
        }

        public boolean getEtlDefinitions()
        {
            return etlDefinitions;
        }

        public void setEtlDefinitions(boolean etlDefinitions)
        {
            this.etlDefinitions = etlDefinitions;
        }

        public boolean getCustomParticipantView()
        {
            return customParticipantView;
        }

        public void setCustomParticipantView(boolean customParticipantView)
        {
            this.customParticipantView = customParticipantView;
        }

        public boolean getDatasetData()
        {
            return datasetData;
        }

        public void setDatasetData(boolean datasetData)
        {
            this.datasetData = datasetData;
        }

        public boolean getParticipantCommentSettings()
        {
            return participantCommentSettings;
        }

        public void setParticipantCommentSettings(boolean participantCommentSettings)
        {
            this.participantCommentSettings = participantCommentSettings;
        }

        public boolean getParticipantGroups()
        {
            return participantGroups;
        }

        public void setParticipantGroups(boolean participantGroups)
        {
            this.participantGroups = participantGroups;
        }

        public boolean getProtocolDocuments()
        {
            return protocolDocuments;
        }

        public void setProtocolDocuments(boolean protocolDocuments)
        {
            this.protocolDocuments = protocolDocuments;
        }

        public boolean getQcStateSettings()
        {
            return qcStateSettings;
        }

        public void setQcStateSettings(boolean qcStateSettings)
        {
            this.qcStateSettings = qcStateSettings;
        }

        public boolean getSpecimenSettings()
        {
            return specimenSettings;
        }

        public void setSpecimenSettings(boolean specimenSettings)
        {
            this.specimenSettings = specimenSettings;
        }

        public boolean getSpecimens()
        {
            return specimens;
        }

        public void setSpecimens(boolean specimens)
        {
            this.specimens = specimens;
        }

        public boolean getTreatmentData()
        {
            return treatmentData;
        }

        public void setTreatmentData(boolean treatmentData)
        {
            this.treatmentData = treatmentData;
        }

        public boolean getVisitMap()
        {
            return visitMap;
        }

        public void setVisitMap(boolean visitMap)
        {
            this.visitMap = visitMap;
        }

        public boolean getFolderTypeAndActiveModules()
        {
            return folderTypeAndActiveModules;
        }

        public void setFolderTypeAndActiveModules(boolean folderTypeAndActiveModules)
        {
            this.folderTypeAndActiveModules = folderTypeAndActiveModules;
        }

        public boolean getFullTextSearchSettings()
        {
            return fullTextSearchSettings;
        }

        public void setFullTextSearchSettings(boolean fullTextSearchSettings)
        {
            this.fullTextSearchSettings = fullTextSearchSettings;
        }

        public boolean getWebpartPropertiesAndLayout()
        {
            return webpartPropertiesAndLayout;
        }

        public void setWebpartPropertiesAndLayout(boolean webpartPropertiesAndLayout)
        {
            this.webpartPropertiesAndLayout = webpartPropertiesAndLayout;
        }

        public boolean getContainerSpecificModuleProperties()
        {
            return containerSpecificModuleProperties;
        }

        public void setContainerSpecificModuleProperties(boolean containerSpecificModuleProperties)
        {
            this.containerSpecificModuleProperties = containerSpecificModuleProperties;
        }

        public boolean getRoleAssignmentsForUsersAndGroups()
        {
            return roleAssignmentsForUsersAndGroups;
        }

        public void setRoleAssignmentsForUsersAndGroups(boolean roleAssignmentsForUsersAndGroups)
        {
            this.roleAssignmentsForUsersAndGroups = roleAssignmentsForUsersAndGroups;
        }

        public boolean isListDesigns()
        {
            return listDesigns;
        }

        public void setListDesigns(boolean listDesigns)
        {
            this.listDesigns = listDesigns;
        }

        public boolean isListData()
        {
            return listData;
        }

        public void setListData(boolean listData)
        {
            this.listData = listData;
        }

        public boolean getQueries()
        {
            return queries;
        }

        public void setQueries(boolean queries)
        {
            this.queries = queries;
        }

        public boolean getGridViews()
        {
            return gridViews;
        }

        public void setGridViews(boolean gridViews)
        {
            this.gridViews = gridViews;
        }

        public boolean getReportsAndCharts()
        {
            return reportsAndCharts;
        }

        public void setReportsAndCharts(boolean reportsAndCharts)
        {
            this.reportsAndCharts = reportsAndCharts;
        }

        public boolean getExternalSchemaDefinitions()
        {
            return externalSchemaDefinitions;
        }

        public void setExternalSchemaDefinitions(boolean externalSchemaDefinitions)
        {
            this.externalSchemaDefinitions = externalSchemaDefinitions;
        }

        public boolean getWikisAndTheirAttachments()
        {
            return wikisAndTheirAttachments;
        }

        public void setWikisAndTheirAttachments(boolean wikisAndTheirAttachments)
        {
            this.wikisAndTheirAttachments = wikisAndTheirAttachments;
        }

        public boolean getNotificationSettings()
        {
            return notificationSettings;
        }

        public void setNotificationSettings(boolean notificationSettings)
        {
            this.notificationSettings = notificationSettings;
        }

        public boolean getSampleTypeDesigns()
        {
            return sampleTypeDesigns;
        }

        public void setSampleTypeDesigns(boolean sampleTypeDesigns)
        {
            this.sampleTypeDesigns = sampleTypeDesigns;
        }

        public boolean getSampleTypeData()
        {
            return sampleTypeData;
        }

        public void setSampleTypeData(boolean sampleTypeData)
        {
            this.sampleTypeData = sampleTypeData;
        }

        public boolean getDataClassDesigns()
        {
            return dataClassDesigns;
        }

        public void setDataClassDesigns(boolean dataClassDesigns)
        {
            this.dataClassDesigns = dataClassDesigns;
        }

        public boolean getDataClassData()
        {
            return dataClassData;
        }

        public void setDataClassData(boolean dataClassData)
        {
            this.dataClassData = dataClassData;
        }

        public boolean getInventoryLocationsAndItems()
        {
            return inventoryLocationsAndItems;
        }

        public void setInventoryLocationsAndItems(boolean inventoryLocationsAndItems)
        {
            this.inventoryLocationsAndItems = inventoryLocationsAndItems;
        }

        public boolean getExperimentsAndRuns()
        {
            return experimentsAndRuns;
        }

        public void setExperimentsAndRuns(boolean experimentsAndRuns)
        {
            this.experimentsAndRuns = experimentsAndRuns;
        }
    }

    /*
        todo: explain who uses this and why
     */
    @RequiresPermission(AdminPermission.class)
    @IgnoresTermsOfUse
    public class TrialShareExportAction extends MutatingApiAction<TrialShareExportForm>
    {
        private ActionURL _successURL;

        @Override
        public ApiResponse execute(TrialShareExportForm form, BindException errors) throws Exception
        {
            // JSONObject json = form.getJsonObject();
            ApiSimpleResponse response = new ApiSimpleResponse();

            Container container = getContainer();
            if (container.isRoot())
            {
                throw new NotFoundException();
            }

            FolderWriterImpl writer = new FolderWriterImpl();

            Set<FolderDataTypes> types = new HashSet<>();

            for (FolderDataTypes type : FolderDataTypes.values())
            {
                if (type.shouldExport(form))
                    types.add(type);
            }

            if(types.isEmpty())
            {
                types = getDefaultExportDataTypes();
            }

            // todo: super important boolean false in 17.2 replaced by PHI enum.  what is new correct setting? PHI.Limited
            FolderExportContext ctx = new FolderExportContext(getUser(), container, types.stream().map(FolderDataTypes::getDescription).collect(Collectors.toSet()),
                    "new", false, PHI.NotPHI, false,
                    false, false, new StaticLoggerGetter(LogManager.getLogger(FolderWriterImpl.class)));

            PipeRoot root = PipelineService.get().findPipelineRoot(container);
            if (root == null || !root.isValid())
            {
                throw new NotFoundException("No valid pipeline root found");
            }
            File exportDir = root.resolvePath("export");
            try
            {
                writer.write(container, ctx, new FileSystemFile(exportDir));
            }
            catch (Container.ContainerException e)
            {
                errors.reject(SpringActionController.ERROR_MSG, e.getMessage());
                response.put("success", false);
                response.put("reason", e.getMessage());
                return response;
            }
            _successURL = PageFlowUtil.urlProvider(PipelineUrls.class).urlBrowse(container);

            response.put("success", true);
            return response;
        }
    }

    @NotNull
    private static Set<FolderDataTypes> getDefaultExportDataTypes()
    {
        return Set.of(
                FolderDataTypes.folderTypeAndActiveModules,
                FolderDataTypes.fullTextSearchSettings,
                FolderDataTypes.webpartProperties,
                FolderDataTypes.moduleProperties,
                FolderDataTypes.qcStateSettings,

                FolderDataTypes.mvIndicators,
                FolderDataTypes.study,
                    FolderDataTypes.assayDatasetData,
                    FolderDataTypes.assayDatasetDefinitions,
                    FolderDataTypes.assaySchedule,
                    FolderDataTypes.categories,
                    FolderDataTypes.cohortSettings,
                    FolderDataTypes.customParticipantView,
                    FolderDataTypes.sampleDatasetData,
                    FolderDataTypes.sampleDatasetDefinitions,
                    FolderDataTypes.studyDatasetData,
                    FolderDataTypes.studyDatasetDefinitions,
                    FolderDataTypes.participantCommentSettings,
                    FolderDataTypes.participantGroups,
                    FolderDataTypes.protocolDocuments,
                    FolderDataTypes.specimenSettings,
                    FolderDataTypes.specimens,
                    FolderDataTypes.treatmentData,
                    FolderDataTypes.visitMap,
                FolderDataTypes.queries,
                FolderDataTypes.gridViews,
                FolderDataTypes.reportsAndCharts,
                FolderDataTypes.externalSchemaDefinitions,
                FolderDataTypes.etlDefinitions,
                FolderDataTypes.listDesigns,
                FolderDataTypes.listData,
                FolderDataTypes.wikisAndAttachments,
                FolderDataTypes.notificationSettings,
                FolderDataTypes.sampleTypeDesigns,
                FolderDataTypes.sampleTypeData,
                FolderDataTypes.dataClassDesigns,
                FolderDataTypes.dataClassData,
                FolderDataTypes.inventoryLocationsAndItems,
                FolderDataTypes.experiments
        );
    }

    enum FolderDataTypes
    {
        mvIndicators("Missing value indicators", TrialShareExportForm::getMissingValueIndicators),
        study("Study", TrialShareExportForm::getStudy),
        assayDatasetDefinitions("Datasets: Assay Dataset Definitions", TrialShareExportForm::getAssayDatasets),
        assayDatasetData("Datasets: Assay Dataset Data", TrialShareExportForm::getAssayDatasets),
        assaySchedule("Assay Schedule", TrialShareExportForm::getAssaySchedule),
        categories("Categories", TrialShareExportForm::getCategories),
        cohortSettings("Cohort Settings", TrialShareExportForm::getCohortSettings),
        customParticipantView("Custom Participant View", TrialShareExportForm::getCustomParticipantView),
        sampleDatasetData("Datasets: Sample Dataset Data", TrialShareExportForm::getDatasetData),
        sampleDatasetDefinitions("Datasets: Sample Dataset Definitions", TrialShareExportForm::getDatasetData),
        studyDatasetData("Datasets: Study Dataset Data", TrialShareExportForm::getCrfDatasets),
        studyDatasetDefinitions("Datasets: Study Dataset Definitions", TrialShareExportForm::getCrfDatasets),
        etlDefinitions("ETL Definitions", TrialShareExportForm::getEtlDefinitions),
        participantCommentSettings("Participant Comment Settings", TrialShareExportForm::getParticipantCommentSettings),
        participantGroups("Participant Groups", TrialShareExportForm::getParticipantGroups),
        protocolDocuments("Protocol Documents", TrialShareExportForm::getProtocolDocuments),
        qcStateSettings(SampleStatusService.get().supportsSampleStatus() ? "Sample Status and QC State Settings" : "QC State Settings", TrialShareExportForm::getQcStateSettings),
        specimenSettings("Specimen Settings", TrialShareExportForm::getSpecimenSettings),
        specimens("Specimens", TrialShareExportForm::getSpecimens),
        treatmentData("Treatment Data", TrialShareExportForm::getTreatmentData),
        visitMap("Visit Map", TrialShareExportForm::getVisitMap),
        folderTypeAndActiveModules("Folder type and active modules", TrialShareExportForm::getFolderTypeAndActiveModules),
        fullTextSearchSettings("Full-text search settings", TrialShareExportForm::getFullTextSearchSettings),
        webpartProperties("Webpart properties and layout", TrialShareExportForm::getWebpartPropertiesAndLayout),
        moduleProperties("Container specific module properties", TrialShareExportForm::getContainerSpecificModuleProperties),
        roleAssignments("Role assignments for users and groups", TrialShareExportForm::getRoleAssignmentsForUsersAndGroups),
        listDesigns("List Designs", TrialShareExportForm::isListDesigns),
        listData("List Data", TrialShareExportForm::isListData),
        queries("Queries", TrialShareExportForm::getQueries),
        gridViews("Grid Views", TrialShareExportForm::getGridViews),
        reportsAndCharts("Reports and Charts", TrialShareExportForm::getReportsAndCharts),
        externalSchemaDefinitions("External schema definitions", TrialShareExportForm::getExternalSchemaDefinitions),
        wikisAndAttachments("Wikis and their attachments", TrialShareExportForm::getWikisAndTheirAttachments),
        notificationSettings("Notification settings", TrialShareExportForm::getNotificationSettings),
        sampleTypeDesigns("Sample Type Designs", TrialShareExportForm::getSampleTypeDesigns),
        sampleTypeData("Sample Type Data", TrialShareExportForm::getSampleTypeData),
        dataClassDesigns("Data Class Designs", TrialShareExportForm::getDataClassDesigns),
        dataClassData("Data Class Data", TrialShareExportForm::getDataClassData),
        inventoryLocationsAndItems("Inventory locations and items", TrialShareExportForm::getInventoryLocationsAndItems),
        experiments("Experiments, Protocols, and Runs", TrialShareExportForm::getExperimentsAndRuns);

        private final String _description;
        private final Function<TrialShareExportForm, Boolean> _formChecker;

        FolderDataTypes(String description, Function<TrialShareExportForm, Boolean> formChecker)
        {
            _description = description;
            _formChecker = formChecker;
        }

        public String getDescription()
        {
            return _description;
        }

        public boolean shouldExport(TrialShareExportForm form)
        {
            return _formChecker.apply(form);
        }
    }

    public static class TrialShareExportTest
    {
        // ignore these export types because they won't appear in the folder export UI
        private static final Set<String> _ignoredFolderWriters = Set.of("Notebooks", "LabBooks");

        private final Collection<FolderWriter> folderWriters = FolderSerializationRegistry.get().getRegisteredFolderWriters().stream()
                .filter(fw -> !_ignoredFolderWriters.contains(fw.getDataType()))
                .collect(Collectors.toList());

        @Test
        public void testDataTypes()
        {
            final Set<String> expectedDataTypes = Arrays.stream(FolderDataTypes.values()).map(FolderDataTypes::getDescription).collect(Collectors.toSet());
            final Set<String> actualDataTypes = getRegisteredDataTypes(false);

            Iterator<String> iterator = expectedDataTypes.iterator();
            while (iterator.hasNext())
            {
                String expectedDataType = iterator.next();
                if (actualDataTypes.contains(expectedDataType))
                {
                    iterator.remove();
                    actualDataTypes.remove(expectedDataType);
                }
            }

            if (!expectedDataTypes.isEmpty())
            {
                Assert.fail("Did not find expected data type(s): " + expectedDataTypes + ". Unaccounted for data types: " + actualDataTypes);
            }
        }

        @Test
        public void testDefaultDataTypes()
        {
            final List<String> expectedDataTypes = getDefaultExportDataTypes().stream().map(FolderDataTypes::getDescription).collect(Collectors.toList());
            final List<String> actualDefaultDataTypes = new ArrayList<>(getRegisteredDataTypes(true));

            // Sort to make error message more readable
            Collections.sort(expectedDataTypes);
            Collections.sort(actualDefaultDataTypes);

            expectedDataTypes.removeAll(actualDefaultDataTypes);

            Assert.assertTrue("TrialShareExport default data types missing expected values: " + expectedDataTypes, expectedDataTypes.isEmpty());
        }

        public Set<String> getRegisteredDataTypes(boolean onlyDefault)
        {
            Set<String> dataTypes = new HashSet<>();
            Set<FolderWriter> filteredFolderWriters;
            if (onlyDefault)
                filteredFolderWriters = folderWriters.stream().filter(fw -> fw.selectedByDefault(ExportType.ALL, false)).collect(Collectors.toSet());
            else
                filteredFolderWriters = new HashSet<>(folderWriters);

            filteredFolderWriters.forEach(fw -> {
                dataTypes.add(fw.getDataType());
                fw.getChildren(false, false)
                    .forEach(w -> dataTypes.add(w.getDataType()));
            });

            dataTypes.remove(null);
            return dataTypes;
        }
    }
}
