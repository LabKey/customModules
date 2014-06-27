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
import org.labkey.api.assay.dilution.query.DilutionResultsQueryView;
import org.labkey.api.data.CompareType;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilterable;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.TableSelector;
import org.labkey.api.exp.Lsid;
import org.labkey.api.exp.OntologyManager;
import org.labkey.api.exp.PropertyDescriptor;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.query.ExpRunTable;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.security.User;
import org.labkey.api.study.assay.AssayProtocolSchema;
import org.labkey.api.study.assay.RunListDetailsQueryView;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.ViewContext;
import org.labkey.icemr.IcemrController;
import org.labkey.icemr.assay.DrugSensitivity.query.DrugSensitivityResultsTable;
import org.springframework.validation.BindException;

/**
 * User: klum
 * Date: 5/13/13
 */
public class DrugSensitivityProtocolSchema extends AssayProtocolSchema
{
    public DrugSensitivityProtocolSchema(User user, Container container, @NotNull DrugSensitivityAssayProvider provider, @NotNull ExpProtocol protocol, @Nullable Container targetStudy)
    {
        super(user, container, provider, protocol, targetStudy);
    }

    @Override
    public ContainerFilterable createDataTable(boolean includeCopiedToStudyColumns)
    {
        DrugSensitivityResultsTable table = new DrugSensitivityResultsTable(this);
        if (includeCopiedToStudyColumns)
        {
            addCopiedToStudyColumns(table, true);
        }
        return table;
    }

    @Override
    protected RunListQueryView createRunsQueryView(ViewContext context, QuerySettings settings, BindException errors)
    {
        return new RunListQueryView(this, settings);
    }

    @Nullable
    @Override
    protected ResultsQueryView createDataQueryView(ViewContext context, QuerySettings settings, BindException errors)
    {
        return new ResultsQueryView(getProtocol(), context, settings);
    }

    public static PropertyDescriptor[] getExistingDataProperties(ExpProtocol protocol, String propertyPrefix)
    {
        String propPrefix = new Lsid(propertyPrefix, protocol.getName(), "").toString();
        SimpleFilter propertyFilter = new SimpleFilter();
        propertyFilter.addCondition(FieldKey.fromParts("PropertyURI"), propPrefix, CompareType.STARTS_WITH);

        PropertyDescriptor[] result = new TableSelector(OntologyManager.getTinfoPropertyDescriptor(),
                propertyFilter, null).getArray(PropertyDescriptor.class);

        return result;
    }

    public static class RunListQueryView extends RunListDetailsQueryView
    {
        public RunListQueryView(AssayProtocolSchema schema, QuerySettings settings)
        {
            super(schema, settings, IcemrController.DetailsAction.class, "rowId", ExpRunTable.Column.RowId.toString());
        }
    }

    public static class ResultsQueryView extends DilutionResultsQueryView
    {
        public ResultsQueryView(ExpProtocol protocol, ViewContext context, QuerySettings settings)
        {
            super(protocol, context, settings);
        }

        @Override
        public ActionURL getGraphSelectedURL()
        {
            return new ActionURL(IcemrController.DrugSensitivityGraphSelectedAction.class, getContainer());
        }

        @Override
        public ActionURL getRunDetailsURL(Object runId)
        {
            return new ActionURL(IcemrController.DetailsAction.class, getContainer()).addParameter("rowId", "" + runId);
        }

        @Override
        protected String getChartTitle(String propLabel)
        {
            return "Proliferation by " + propLabel;
        }
    }
}
