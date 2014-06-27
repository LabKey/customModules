/*
 * Copyright (c) 2013-2014 LabKey Corporation
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
package org.labkey.icemr.assay.DrugSensitivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.assay.dilution.AbstractDilutionAssayProvider;
import org.labkey.api.assay.dilution.DilutionDataHandler;
import org.labkey.api.assay.dilution.query.DilutionProviderSchema;
import org.labkey.api.assay.nab.NabSpecimen;
import org.labkey.api.data.Container;
import org.labkey.api.exp.PropertyType;
import org.labkey.api.exp.api.ExpData;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.api.ExpRun;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.DomainProperty;
import org.labkey.api.exp.property.Lookup;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.pipeline.PipelineProvider;
import org.labkey.api.query.FieldKey;
import org.labkey.api.security.User;
import org.labkey.api.study.actions.AssayRunUploadForm;
import org.labkey.api.study.assay.AssayPipelineProvider;
import org.labkey.api.study.assay.AssayProtocolSchema;
import org.labkey.api.study.assay.AssayProviderSchema;
import org.labkey.api.study.assay.AssaySchema;
import org.labkey.api.study.assay.AssayTableMetadata;
import org.labkey.api.study.assay.AssayUrls;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.HttpView;
import org.labkey.icemr.IcemrController;
import org.labkey.icemr.IcemrModule;
import org.labkey.icemr.assay.DrugSensitivity.actions.DrugSensitivityRunUploadForm;
import org.labkey.icemr.assay.DrugSensitivity.actions.DrugSensitivityUploadWizardAction;

/**
 * User: klum
 * Date: 5/12/13
 */
public class DrugSensitivityAssayProvider extends AbstractDilutionAssayProvider<DrugSensitivityRunUploadForm>
{
    public static final String RESOURCE_NAME = "Drug Sensitivity";
    public static final String NAME = "Drug Sensitivity";

    private static final String DRUG_RUN_LSID_PREFIX = "DrugSensitivityAssayRun";
    private static final String DRUG_ASSAY_PROTOCOL = "DrugSensitivityAssayProtocol";

    // run properties
    public static final String EXPERIMENT_ID_PROPERTY_NAME = "ExperimentID";
    public static final String FLASK_SAMPLEID_PROPERTY_NAME = "FlaskSampleID";
    public static final String FREEZER_PRO_ID_PROPERTY_NAME = "FreezerProID";
    public static final String MEDIA_TYPE_PROPERTY_NAME = "MediaType";
    public static final String MEDIA_FREEZER_PRO_ID_PROPERTY_NAME = "MediaFreezerProID";
    public static final String INITIAL_PARASITEMIA_PROPERTY_NAME = "InitialParasitemia";
    public static final String EXPERIMENT_PERFORMER_PROPERTY_NAME = "ExperimentPerformer";
    public static final String TOTAL_EVENTS_PER_WELL_PROPERTY_NAME = "TotalEventPerWell";
    public static final String DATA_ACQUISITION_PROPERTY_NAME = "DataAcquisitionFile";

    // sample properties
    public static final String TREATMENT_NAME_PROPERTY_NAME = "TreatmentName";
    public static final String TREATMENT_FREEZER_PRO_ID_PROPERTY_NAME = "TreatmentFreezerProID";
    public static final String INITIAL_CONCENTRATION_PROPERTY_NAME = "InitialConcentration";
    public static final String DILUTION_FACTOR_PROPERTY_NAME = "DilutionFactor";
    public static final String COMMENTS_PROPERTY_NAME = "Comments";

    public DrugSensitivityAssayProvider()
    {
        super(DRUG_ASSAY_PROTOCOL, DRUG_RUN_LSID_PREFIX, DrugSensitivityDataHandler.DRUG_SENSITIVITY_DATA_TYPE, ModuleLoader.getInstance().getModule(IcemrModule.class));
    }

    @Override
    protected void addPassThroughRunProperties(Domain runDomain)
    {
        addProperty(runDomain, EXPERIMENT_ID_PROPERTY_NAME, "Experiment ID", PropertyType.STRING);
        addProperty(runDomain, DATE_PROPERTY_NAME, DATE_PROPERTY_NAME, PropertyType.DATE_TIME);
        addProperty(runDomain, PARTICIPANTID_PROPERTY_NAME, PARTICIPANTID_PROPERTY_CAPTION, PropertyType.STRING);
        addProperty(runDomain, FLASK_SAMPLEID_PROPERTY_NAME, "Flask Sample ID", PropertyType.STRING);
        addProperty(runDomain, FREEZER_PRO_ID_PROPERTY_NAME, "Freezer Pro ID", PropertyType.STRING);
        addProperty(runDomain, MEDIA_TYPE_PROPERTY_NAME, "Media Type", PropertyType.STRING);
        addProperty(runDomain, MEDIA_FREEZER_PRO_ID_PROPERTY_NAME, "Media Type Freezer Pro ID", PropertyType.STRING);
        addProperty(runDomain, INITIAL_PARASITEMIA_PROPERTY_NAME, "Initial Parasitemia Percent", PropertyType.DOUBLE).setRequired(true);
        addProperty(runDomain, EXPERIMENT_PERFORMER_PROPERTY_NAME, "Experiment Performer", PropertyType.STRING);
        addProperty(runDomain, TOTAL_EVENTS_PER_WELL_PROPERTY_NAME, "Total Events Per Well", PropertyType.INTEGER);
        addProperty(runDomain, DATA_ACQUISITION_PROPERTY_NAME, "Data Acquisition File", PropertyType.FILE_LINK);
    }

    @Override
    protected void addPassThroughSampleWellGroupProperties(Container c, Domain domain)
    {
        addProperty(domain, TREATMENT_NAME_PROPERTY_NAME, "Treatment Name", PropertyType.STRING).setRequired(true);
        addProperty(domain, TREATMENT_FREEZER_PRO_ID_PROPERTY_NAME, "Treatment Freezer Pro ID", PropertyType.STRING);
        addProperty(domain, SAMPLE_INITIAL_DILUTION_PROPERTY_NAME, "Initial Concentration", PropertyType.DOUBLE).setRequired(true);
        addProperty(domain, SAMPLE_DILUTION_FACTOR_PROPERTY_NAME, "Dilution Factor", PropertyType.DOUBLE).setRequired(true);

        Container lookupContainer = c.getProject();
        DomainProperty method = addProperty(domain, SAMPLE_METHOD_PROPERTY_NAME, SAMPLE_METHOD_PROPERTY_CAPTION, PropertyType.STRING);
        method.setLookup(new Lookup(lookupContainer, AssaySchema.NAME + "." + getResourceName(), DilutionProviderSchema.SAMPLE_PREPARATION_METHOD_TABLE_NAME));
        method.setRequired(true);
    }

    @NotNull
    @Override
    public AssayTableMetadata getTableMetadata(@NotNull ExpProtocol protocol)
    {
        return new AssayTableMetadata(
                this,
                protocol,
                FieldKey.fromParts("Run"),
                FieldKey.fromParts("Run"),
                FieldKey.fromParts("RowId"));
    }

    @Override
    public DilutionDataHandler getDataHandler()
    {
        return new DrugSensitivityDataHandler();
    }

    @Override
    public ExpData getDataForDataRow(Object dataRowId, ExpProtocol protocol)
    {
        if (!(dataRowId instanceof Integer))
            return null;

        // dataRowId is NabSpecimen rowId
        NabSpecimen nabSpecimen = DrugSensitivityManager.get().getNabSpecimen((Integer)dataRowId);
        if (null != nabSpecimen)
            return ExperimentService.get().getExpData(nabSpecimen.getDataId());
        return null;
    }

    @Override
    public ActionURL getImportURL(Container container, ExpProtocol protocol)
    {
        return PageFlowUtil.urlProvider(AssayUrls.class).getProtocolURL(container, protocol, DrugSensitivityUploadWizardAction.class);
    }

    @Override
    public ActionURL getUploadWizardCompleteURL(DrugSensitivityRunUploadForm form, ExpRun run)
    {
        return new ActionURL(IcemrController.DetailsAction.class,
                    run.getContainer()).addParameter("rowId", run.getRowId()).addParameter("newRun", "true");
    }

    @Override
    public AssayProviderSchema createProviderSchema(User user, Container container, Container targetStudy)
    {
        return new DilutionProviderSchema(user, container, this, "DrugSensitivity", targetStudy, false);
    }

    @Override
    public AssayProtocolSchema createProtocolSchema(User user, Container container, @NotNull ExpProtocol protocol, @Nullable Container targetStudy)
    {
        return new DrugSensitivityProtocolSchema(user, container, this, protocol, targetStudy);
    }

    @Override
    public String getResourceName()
    {
        return RESOURCE_NAME;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public HttpView getDataDescriptionView(AssayRunUploadForm form)
    {
        return new HtmlView("The Drug Sensitivity data file is a tab delimited file with a .txt or .tsv extension.");
    }

    @Override
    public PipelineProvider getPipelineProvider()
    {
        return new AssayPipelineProvider(IcemrModule.class,
                new PipelineProvider.FileTypesEntryFilter(getDataType().getFileType()), this, "Import Drug Sensitivity");
    }

    @Override
    public String getDescription()
    {
        return "Measures parasitemia proliferation against anti-malarial drugs in different dilutions.";
    }
}
