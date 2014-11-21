/*
 * Copyright (c) 2014 LabKey Corporation
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
Ext4.define('LABKEY.ext4.RemainingVialsPanel', {

    extend: 'Ext.panel.Panel',

    constructor : function(config) {

        Ext4.applyIf(config, {
            layout    : 'vbox',
            frame     : false,
            border    : false
        });

        this.callParent([config]);
    },

    initComponent : function() {

        this.items = [];

        Ext4.define('LABKEY.Peptide.Lots', {
            extend: 'Ext.data.Model',
            proxy : {
                type : 'ajax',
                url    : LABKEY.ActionURL.buildURL('query', 'executeSql.api'),
                extraParams : {
                    schemaName  : 'peptideinventory',
                    sql         : 'SELECT DISTINCT(lotNumber) FROM lotAssignment ORDER BY lotNumber'
                },
                reader : { type : 'json', root : 'rows' }
            },
            fields : [
                {name: 'lotNumber'}
            ]
        });

        var lotStore = Ext4.create('Ext.data.Store', {
            model : 'LABKEY.Peptide.Lots',
            pageSize    : 10000,
            autoLoad: true
        });

        var formPanel = Ext4.create('Ext.panel.Panel', {
            bodyPadding : 20,
            border      : false,
            height      : 150,
            region      : 'north',
            items : [{
                xtype       : 'displayfield',
                value       : 'Search for remaining vials by lot number</p>'
            },{
                xtype       : 'combobox',
                width : 400,
                fieldLabel : 'Peptide Lot Numbers',
                labelWidth : 150,
                labelSeparator : '',
                store : lotStore,
                valueField : 'lotNumber',
                displayField : 'lotNumber',
                forceSelection : true,
                editable : false,
                listeners : {
                    scope: this,
                    change : function(combo, value) {
                        this.lotNumber = value;
                        this.getRemainingVials();
                    }
                }
            }]
        });
        this.items.push(formPanel);

        var tpl = new Ext4.XTemplate(
            '<table style="border: none">',
                '<tr class="vial-row"">',
                    '<th class="lk-remaining-vial-th">Peptide Id</th>',
                    '<th class="lk-remaining-vial-th">Peptide Sequence</th>',
                    '<th class="lk-remaining-vial-th">Protein Category</th>',
                    '<th class="lk-remaining-vial-th">Group</th>',
                    '<th class="lk-remaining-vial-th">Pathogen</th>',
                    '<th class="lk-remaining-vial-th">Clade</th>',
                    '<th class="lk-remaining-vial-th">Group Type</th>',
                    '<th class="lk-remaining-vial-th">Align Ref</th>',
                    '<th class="lk-remaining-vial-th">Id in Group</th>',
                    '<th class="lk-remaining-vial-th">Status</th>',
                '</tr>',
                '<tpl for=".">',
                    '<tr class="vial-row {[xindex % 2 ===0 ? "labkey-alternate-row" : ""]}">',
                        '<td><div class="lk-remaining-vial-div">{peptide_id}</div></td>',
                        '<td><div class="lk-remaining-vial-div">{peptide_sequence}</div></td>',
                        '<td><div class="lk-remaining-vial-div">{proteinCategory}</div></td>',
                        '<td><div class="lk-remaining-vial-div">{peptideGroup}</div></td>',
                        '<td><div class="lk-remaining-vial-div">{pathogen}</div></td>',
                        '<td><div class="lk-remaining-vial-div">{clade}</div></td>',
                        '<td><div class="lk-remaining-vial-div">{groupType}</div></td>',
                        '<td><div class="lk-remaining-vial-div">{alignRef}</div></td>',
                        '<td><div class="lk-remaining-vial-div">{peptide_id_in_group}</div></td>',
                        '<td>{[this.getStatus(values)]}</td>',
                    '</tr>',
                '</tpl>',
            '</table>',
            {
                getStatus : function(rec) {
                    var checkedOut = rec['checkedOut'];
                    var used = rec['used'];

                    var color = 'black';
                    var label = 'Available';

                    if (used) {
                        color = 'red';
                        label = 'Used';
                    }
                    else if (checkedOut) {
                        color = 'orange';
                        label = 'Checked Out';
                    }
                    return '<div style="color:' + color + '">' + label + '</div>';
                },
                me : this
            }

        );

        var reportPanel = Ext4.create('Ext.view.View', {
            border  : false, frame : false,
            flex    : 1.2,
            tpl     : tpl,
            width   : '100%',
            autoScroll : true,
            store   : this.createVialStore(),
            itemSelector    : 'div.vial-div'
        });

        this.items.push(reportPanel);
        this.callParent();
    },

    /**
     * Populate the grid with the set of peptides in the selected pool
     */
    getRemainingVials : function() {

        this.vialStore.proxy.extraParams.sql = this.getRemainingVialsSql(this.lotNumber);
        this.vialStore.load();
    },

    createVialStore : function() {

        if (!this.vialStore) {

            if (!Ext4.ModelManager.isRegistered('LABKEY.Peptide.RemainingVials')) {

                Ext4.define('LABKEY.Peptide.RemainingVials', {
                    extend: 'Ext.data.Model',
                    proxy : {
                        type : 'ajax',
                        url    : LABKEY.ActionURL.buildURL('query', 'executeSql.api'),
                        extraParams : {
                            schemaName  : 'peptideinventory',
                            queryName   : 'peptide',
                            sort        : 'name',
                            apiVersion  : 8.3
                        },
                        reader : { type : 'json', root : 'rows' }
                    },
                    fields : [
                        {name: 'peptide_id'},
                        {name: 'peptide_sequence'},
                        {name: 'proteinCategory'},
                        {name: 'peptideGroup'},
                        {name: 'pathogen'},
                        {name: 'clade'},
                        {name: 'groupType'},
                        {name: 'alignRef'},
                        {name: 'peptide_id_in_group'},
                        {name: 'checkedOut'},
                        {name: 'used'}
                    ]
                });
            }
            this.vialStore = Ext4.create('Ext.data.Store', {
                model : 'LABKEY.Peptide.RemainingVials',
                autoLoad: false,
                pageSize: 10000
            });
        }
        return this.vialStore;
    },

    /**
     * Returns the select SQL to return the list of peptides remaining for a specified lot number
     */
    getRemainingVialsSql : function(lotNumber){

        return 'SELECT x.peptide_id, x.peptide_sequence, x.proteinCategory, pg.name AS peptideGroup, pg.pathogen, pg.clade, ' +
                'pg.groupType, pg.alignRef, ga.peptide_id_in_group, v.checkedOut, v.used, v.freezer, v.rack, v.shelf, v.drawer, v.box ' +
                'FROM (' +
                'SELECT * FROM peptideInventory.peptide p ' +
                'WHERE p.peptide_id IN (SELECT peptideId FROM peptideInventory.lotAssignment WHERE lotNumber = \'' + lotNumber + '\')' +
                ') x LEFT JOIN peptideInventory.peptideGroupAssignment ga ON x.peptide_id = ga.peptide_id ' +
                'LEFT JOIN peptideInventory.peptideGroup pg ON ga.peptide_group_id = pg.peptide_group_id ' +
                'LEFT JOIN peptideInventory.vial v ON x.peptide_id = v.peptideId';
    }
});
