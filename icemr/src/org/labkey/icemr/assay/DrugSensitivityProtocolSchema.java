package org.labkey.icemr.assay;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.CompareType;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilterable;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.TableSelector;
import org.labkey.api.exp.Lsid;
import org.labkey.api.exp.OntologyManager;
import org.labkey.api.exp.PropertyDescriptor;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.DomainProperty;
import org.labkey.api.exp.property.PropertyService;
import org.labkey.api.exp.query.ExpRunTable;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.security.User;
import org.labkey.api.study.assay.AssayProtocolSchema;
import org.labkey.api.study.assay.RunListDetailsQueryView;
import org.labkey.api.study.query.RunListQueryView;
import org.labkey.api.view.ViewContext;
import org.labkey.icemr.IcemrController;
import org.labkey.icemr.assay.query.DrugSensitivityResultsTable;
import org.springframework.validation.BindException;

/**
 * Created by IntelliJ IDEA.
 * User: klum
 * Date: 5/13/13
 */
public class DrugSensitivityProtocolSchema extends AssayProtocolSchema
{
    public DrugSensitivityProtocolSchema(User user, Container container, ExpProtocol protocol, Container targetStudy)
    {
        super(user, container, protocol, targetStudy);
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

    public static class RunListQueryView extends RunListDetailsQueryView
    {
        public RunListQueryView(AssayProtocolSchema schema, QuerySettings settings)
        {
            super(schema, settings, IcemrController.DetailsAction.class, "rowId", ExpRunTable.Column.RowId.toString());
        }
    }

    @Override
    protected RunListQueryView createRunsQueryView(ViewContext context, QuerySettings settings, BindException errors)
    {
        return new RunListQueryView(this, settings);
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
}
