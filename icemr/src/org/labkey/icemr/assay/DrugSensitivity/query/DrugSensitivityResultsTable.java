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
package org.labkey.icemr.assay.DrugSensitivity.query;

import org.labkey.api.assay.dilution.DilutionDataHandler;
import org.labkey.api.assay.dilution.DilutionManager;
import org.labkey.api.assay.dilution.query.DilutionProviderSchema;
import org.labkey.api.assay.nab.query.CutoffValueTable;
import org.labkey.api.assay.nab.query.NAbSpecimenTable;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.TableInfo;
import org.labkey.api.exp.PropertyColumn;
import org.labkey.api.exp.PropertyDescriptor;
import org.labkey.api.exp.api.ExpProtocol;
import org.labkey.api.exp.api.ExpSampleSet;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.DomainProperty;
import org.labkey.api.exp.query.ExpMaterialTable;
import org.labkey.api.exp.query.ExpRunTable;
import org.labkey.api.exp.query.ExpSchema;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.LookupForeignKey;
import org.labkey.api.query.PropertyForeignKey;
import org.labkey.api.study.assay.AbstractAssayProvider;
import org.labkey.api.study.assay.AbstractPlateBasedAssayProvider;
import org.labkey.api.study.assay.AssayProtocolSchema;
import org.labkey.api.study.assay.AssayProvider;
import org.labkey.api.study.assay.AssayService;
import org.labkey.api.study.assay.SpecimenPropertyColumnDecorator;
import org.labkey.icemr.assay.DrugSensitivity.DrugSensitivityDataHandler;
import org.labkey.icemr.assay.DrugSensitivity.DrugSensitivityProtocolSchema;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * User: klum
 * Date: 5/14/13
 */
public class DrugSensitivityResultsTable extends NAbSpecimenTable
{
    protected final ExpProtocol _protocol;
    protected final AssayProvider _provider;

    public DrugSensitivityResultsTable(final AssayProtocolSchema schema)
    {
        super(schema);

        _protocol = _userSchema.getProtocol();
        _provider = _userSchema.getProvider();

        List<FieldKey> visibleColumns = new ArrayList<>();

        // add material lookup columns to the view first, so they appear at the left:
        String sampleDomainURI = AbstractAssayProvider.getDomainURIForPrefix(schema.getProtocol(), AbstractPlateBasedAssayProvider.ASSAY_DOMAIN_SAMPLE_WELLGROUP);
        final ExpSampleSet sampleSet = ExperimentService.get().getSampleSet(sampleDomainURI);
        if (sampleSet != null)
        {
            for (DomainProperty pd : sampleSet.getType().getProperties())
            {
                visibleColumns.add(FieldKey.fromParts(DrugSensitivityDataHandler.DILUTION_INPUT_MATERIAL_DATA_PROPERTY,
                        ExpMaterialTable.Column.Property.toString(), pd.getName()));
            }
        }

        // add the standard deviation property
        ColumnInfo objectURI = getColumn(FieldKey.fromParts("ObjectUri"));
        for (PropertyDescriptor prop : DrugSensitivityProtocolSchema.getExistingDataProperties(_protocol, DrugSensitivityDataHandler.DRUG_SENSITIVITY_PROPERTY_LSID_PREFIX))
        {
            if (objectURI != null && DilutionDataHandler.STD_DEV_PROPERTY_NAME.equals(prop.getName()))
            {
                ColumnInfo propColumn = new PropertyColumn(prop, objectURI, getContainer(), schema.getUser(), false);
                addColumn(propColumn);

                visibleColumns.add(FieldKey.fromParts(DilutionDataHandler.STD_DEV_PROPERTY_NAME));
            }
        }

        // add a lookup to the material table
        ColumnInfo specimenColumn = _columnMap.get(DrugSensitivityDataHandler.DILUTION_INPUT_MATERIAL_DATA_PROPERTY);
        specimenColumn.setFk(new LookupForeignKey("LSID")
        {
            public TableInfo getLookupTableInfo()
            {
                ExpMaterialTable materials = ExperimentService.get().createMaterialTable(ExpSchema.TableType.Materials.toString(), schema);
                // Make sure we are filtering to the same set of containers
                materials.setContainerFilter(getContainerFilter());
                if (sampleSet != null)
                {
                    materials.setSampleSet(sampleSet, true);
                }
                ColumnInfo propertyCol = materials.addColumn(ExpMaterialTable.Column.Property);
                if (propertyCol.getFk() instanceof PropertyForeignKey)
                {
                    ((PropertyForeignKey)propertyCol.getFk()).addDecorator(new SpecimenPropertyColumnDecorator(_provider, _protocol, schema));
                }
                propertyCol.setHidden(false);
                materials.addColumn(ExpMaterialTable.Column.LSID).setHidden(true);
                return materials;
            }
        });

        // join in the cutoff values from the cutoff table
        Set<Double> cutoffValuess = DilutionManager.getCutoffValues(_protocol);
        for (Double value : cutoffValuess)
        {
            final Integer intCutoff = (int)Math.floor(value);
            final CutoffValueTable cutoffValueTable = new CutoffValueTable(schema);
            cutoffValueTable.removeContainerAndProtocolFilters();
            cutoffValueTable.addCondition(new SimpleFilter(FieldKey.fromString("Cutoff"), intCutoff));
            ColumnInfo nabSpecimenColumn = cutoffValueTable.getColumn("NabSpecimenId");
            nabSpecimenColumn.setIsUnselectable(true);
            nabSpecimenColumn.setHidden(true);

            // Update column labels like IC_4pl to Curve ICxx 4pl
            for (ColumnInfo column : cutoffValueTable.getColumns())
                updateLabelWithCutoff(column, intCutoff);

            ColumnInfo cutoffColumn = wrapColumn("Cutoff" + intCutoff, DilutionManager.getTableInfoNAbSpecimen().getColumn("RowId"));
            cutoffColumn.setLabel("Cutoff " + intCutoff);
            cutoffColumn.setKeyField(false);
            cutoffColumn.setIsUnselectable(true);
            LookupForeignKey lfk = new LookupForeignKey("NabSpecimenId")
            {
                @Override
                public TableInfo getLookupTableInfo()
                {
                    return cutoffValueTable;  // _userSchema.getTable(NabProtocolSchema.CUTOFF_VALUE_TABLE_NAME);
                }
                @Override
                public ColumnInfo createLookupColumn(ColumnInfo parent, String displayField)
                {
                    ColumnInfo result = super.createLookupColumn(parent, displayField);
                    return result;
                }
            };
            cutoffColumn.setFk(lfk);
            addColumn(cutoffColumn);
            //visibleColumns.add(cutoffColumn.getFieldKey());
        }

/*
        for (ColumnInfo columnInfo : _rootTable.getColumns())
        {
            String columnName = columnInfo.getColumnName().toLowerCase();
            if (columnName.contains("auc_") || columnName.equals("fiterror"))
            {
                addWrapColumn(columnInfo);
            }
            else if (columnName.equals("wellgroupname"))
            {
                ColumnInfo wellgroupColumn = wrapColumn(columnInfo);
                wellgroupColumn.setLabel("Wellgroup Name");
                addColumn(wellgroupColumn);
            }
        }
*/
        for (PropertyDescriptor lookupCol : getExistingDataProperties(_protocol, cutoffValuess))
        {
            if (!isColumnHidden(lookupCol.getName()))
            {
                String legalName = ColumnInfo.legalNameFromName(lookupCol.getName());
                if (null != _rootTable.getColumn(legalName))
                {
                    // Column is in NabSpecimen
                    FieldKey key = FieldKey.fromString(legalName);
                    visibleColumns.add(key);
                    if (null == getColumn(key))
                        addWrapColumn(_rootTable.getColumn(key));
                }
                else
                {
                    // Cutoff table column or calculated column
                    DilutionManager.PropDescCategory pdCat = DilutionManager.getPropDescCategory(lookupCol.getName());
                    FieldKey key = DilutionManager.getCalculatedColumn(pdCat);
                    if (null != key)
                        visibleColumns.add(key);
                }
            }
        }

        // add run level properties
        Domain runDomain = _provider.getRunDomain(_protocol);
        for (DomainProperty prop : runDomain.getProperties())
        {
            if (!prop.isHidden())
                visibleColumns.add(FieldKey.fromParts("Run", prop.getName()));
        }

        // fk to the assay run
        SQLFragment runIdSQL = new SQLFragment("(SELECT RunId FROM ");
        runIdSQL.append(ExperimentService.get().getTinfoData(), "d");
        runIdSQL.append(" WHERE d.RowId = " + ExprColumn.STR_TABLE_ALIAS + ".DataId)");
        ExprColumn runColumn = new ExprColumn(this, "Run", runIdSQL, JdbcType.INTEGER);
        runColumn.setFk(new LookupForeignKey("RowID")
        {
            public TableInfo getLookupTableInfo()
            {
                ExpRunTable expRunTable = AssayService.get().createRunTable(_protocol, _provider, _userSchema.getUser(), _userSchema.getContainer());
                expRunTable.setContainerFilter(getContainerFilter());
                return expRunTable;
            }
        });
        runColumn.setUserEditable(false);
        runColumn.setShownInInsertView(false);
        runColumn.setShownInUpdateView(false);
        addColumn(runColumn);

        //visibleColumns.addAll(getDefaultVisibleColumns());
        setDefaultVisibleColumns(visibleColumns);
    }

    private boolean isColumnHidden(String propName)
    {
        if (propName.startsWith(DilutionDataHandler.CURVE_IC_PREFIX) ||
                propName.startsWith(DilutionDataHandler.POINT_IC_PREFIX) ||
                propName.startsWith(DilutionDataHandler.AUC_PREFIX) ||
                propName.startsWith(DilutionDataHandler.pAUC_PREFIX))
        {
            if (propName.indexOf('_') != -1)
                return true;
            else if (propName.indexOf("OORIndicator") != -1)
                return true;
        }
        else if (DilutionDataHandler.DILUTION_INPUT_MATERIAL_DATA_PROPERTY.equals(propName))
            return true;
        return false;
    }

    private static void updateLabelWithCutoff(ColumnInfo column, Integer intCutoff)
    {
        if (null != intCutoff)
        {
            String label = column.getLabel();
            if (label.startsWith("IC"))
            {
                column.setLabel("Curve IC" + intCutoff + label.substring(2));
            }
            else if (label.startsWith("Point"))
            {
                column.setLabel("Point IC" + intCutoff + label.substring(5));
            }
        }
    }

    public PropertyDescriptor[] getExistingDataProperties(ExpProtocol protocol, Set<Double> cutoffValues)
    {
        List<PropertyDescriptor>pds = DilutionProviderSchema.getExistingDataProperties(protocol, cutoffValues);

        pds.sort(Comparator.comparing(PropertyDescriptor::getName));
        return pds.toArray(new PropertyDescriptor[pds.size()]);
    }
}
