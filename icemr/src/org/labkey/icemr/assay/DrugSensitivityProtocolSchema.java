package org.labkey.icemr.assay;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilterable;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.query.ExpRunTable;
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
}
