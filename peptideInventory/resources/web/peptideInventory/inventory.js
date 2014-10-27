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
Ext4.define('LABKEY.ext4.InventoryPanel', {

    extend : 'Ext.panel.Panel',

    constructor : function(config) {

        Ext4.QuickTips.init();

        Ext4.applyIf(config, {
            layout    : 'border',
            frame     : false,
            border    : false
        });

        this.callParent([config]);
    },

    initComponent : function() {

        this.items = [];

        this.centerPanel = Ext4.create('Ext.panel.Panel', {
            border   : false, frame : false,
            layout   : 'fit',
            disabled : true,
            region   : 'center',
            dockedItems : [{
                xtype : 'toolbar',
                dock  : 'top',
                cls   : 'report-toolbar',
                items : [{
                    text: 'Hide Filters',
                    handler: function (btn)
                    {
                        if (this.northPanel.isHidden())
                        {
                            btn.setText('Hide Filters');
                            this.northPanel.show();
                        }
                        else
                        {
                            btn.setText('Show Filters');
                            this.northPanel.hide();
                        }
                    },
                    scope: this
                },{
                    text    : 'Assign Location',
                    disabled: true,
                    handler : this.assignLocation,
                    scope   : this
                }]
            }]
        });

        this.items.push(this.centerPanel);

        this.northPanel = Ext4.create('Ext.panel.Panel', {
            bodyPadding : 20,
            border      : false,
            height      : 150,
            region      : 'north',
            items : [{
                xtype       : 'displayfield',
                value       : 'This webpart is used to assign individual peptides to a freezer location. Start by selecting a specific peptide pool from the dropdown ' +
                        'check the individual peptides before clicking on the "Assign Location" button to assign the set of peptides to a specific freezer location</p>'
            }],
            buttons  : [{
                text    : 'Search',
                hidden  : true,
                handler : function() {
                    this.getPeptidesInPool();
                },
                scope   : this
            }]
        });
        this.items.push(this.northPanel);

        this.initNorthPanel();
        this.callParent();
    },

    initNorthPanel : function() {

        Ext4.define('LABKEY.Peptide.Pools', {
            extend: 'Ext.data.Model',
            proxy : {
                type : 'ajax',
                url    : LABKEY.ActionURL.buildURL('query', 'executeSql.api'),
                extraParams : {
                    schemaName  : 'peptideinventory',
                    queryName   : 'peptidepool',
                    sql         : 'SELECT * FROM peptidePool WHERE peptide_pool_id IN (SELECT DISTINCT(peptide_pool_id) FROM peptidePoolAssignment) ORDER BY name',
                    sort        : 'name'
                },
                reader : { type : 'json', root : 'rows' }
            },
            fields : [
                {name: 'peptide_pool_id'},
                {name: 'name'}
            ]
        });

        var poolStore = Ext4.create('Ext.data.Store', {
            model : 'LABKEY.Peptide.Pools',
            pageSize    : 10000,
            autoLoad: true
        });

        this.peptidePools = Ext4.create('Ext.form.field.ComboBox', {
            width : 800,
            fieldLabel : 'Peptide Pools',
            labelWidth : 150,
            labelSeparator : '',
            store : poolStore,
            valueField : 'peptide_pool_id',
            displayField : 'name',
            forceSelection : true,
            editable : false,
            listeners : {
                scope: this,
                change : function(combo, value) {
                    this.peptidePool = value;
                    this.getPeptidesInPool();
                }
            }
        });

        this.northPanel.add(this.peptidePools);
    },

    /**
     * Populate the grid with the set of peptides in the selected pool
     */
    getPeptidesInPool : function() {

        if (!this.peptideResultsGrid) {

            Ext4.define('LABKEY.Peptide.Peptides', {
                extend: 'Ext.data.Model',
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'executeSql.api'),
                    extraParams : {
                        schemaName  : 'peptideinventory',
                        queryName   : 'peptide',
                        sort        : 'name'
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
                    {name: 'peptide_id_in_group'}
                ]
            });

            this.dataStore = Ext4.create('Ext.data.Store', {
                model : 'LABKEY.Peptide.Peptides',
                autoLoad: false,
                pageSize: 200,
                listeners : {
                    beforeload : function(){
                        if (this.peptideResultsGrid)
                            this.peptideResultsGrid.setLoading(true);
                    },
                    load : function(){
                        if (this.peptideResultsGrid)
                            this.peptideResultsGrid.setLoading(false);
                    },
                    scope: this
                }
            });

            this.peptideResultsGrid = Ext4.create('Ext.grid.Panel', {
                store   : this.dataStore,
                border  : false, frame : false,
                selType : 'checkboxmodel',
                scope   : this,
                emptyText : '<h3>There were no rows returned that matched the specified peptide pool.</h3>',
                columns : [
                    {header : 'Peptide Id', dataIndex : 'peptide_id'},
                    {header : 'Peptide Sequence', dataIndex : 'peptide_sequence', flex : 1.1},
                    {header : 'Protein Category', dataIndex : 'proteinCategory', width : 150},
                    {header : 'Group', dataIndex : 'peptideGroup', width : 75},
                    {header : 'Pathogen', dataIndex : 'pathogen', width : 75},
                    {header : 'Clade', dataIndex : 'clade', width : 100},
                    {header : 'Group Type', dataIndex : 'groupType', width : 75},
                    {header : 'Align Ref', dataIndex : 'alignRef', width : 75},
                    {header : 'Id in Group', dataIndex : 'peptide_id_in_group', width : 125}
                ],
                dockedItems : [{
                    xtype   : 'pagingtoolbar',
                    store   : this.dataStore,
                    dock    : 'bottom',
                    displayInfo : true
                }],
                listeners : {
                    selectionchange : function(cmp, selected){
                        var editBtn = this.down('button[text="Assign Location"]');
                        if (editBtn)
                            editBtn.setDisabled(selected.length == 0);
                    },
                    scope: this
                }
            });

            this.centerPanel.add(this.peptideResultsGrid);
            this.centerPanel.enable();
        }
         var sql = 'SELECT x.peptide_id, x.peptide_sequence, x.proteinCategory, pg.name AS peptideGroup, pg.pathogen, pg.clade, ' +
                        'pg.groupType, pg.alignRef, ga.peptide_id_in_group ' +
                        'FROM (' +
                            'SELECT * FROM peptideInventory.peptide p ' +
                            'WHERE p.peptide_id IN (SELECT peptide_id FROM peptideInventory.peptidePoolAssignment WHERE peptide_pool_id = ' + this.peptidePool + ')' +
                        ') x LEFT JOIN peptideInventory.peptideGroupAssignment ga ON x.peptide_id = ga.peptide_id ' +
                        'LEFT JOIN peptideInventory.peptideGroup pg ON ga.peptide_group_id = pg.peptide_group_id';
        this.dataStore.proxy.extraParams.sql = sql;
        this.dataStore.load();
    },

    assignLocation : function() {

        var formItems = [];
        this.peptideSlots = {};

        formItems.push({
            xtype       : 'displayfield',
            value       : 'Specify the freezer, rack, drawer, shelf and box for all selected Peptides.'
        });

        Ext4.define('LABKEY.Peptide.Location', {
            extend: 'Ext.data.Model',
            fields : [
                {name: 'rowId'},
                {name: 'name'}
            ]
        });

        formItems.push(Ext4.create('Ext.form.field.ComboBox', {
            fieldLabel      : 'Freezer',
            name            : 'freezer',
            labelSeparator  : '',
            store : {
                model   : 'LABKEY.Peptide.Location',
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : {
                        schemaName  : 'peptideinventory',
                        queryName   : 'freezer',
                        sort        : 'name'
                    },
                    reader : { type : 'json', root : 'rows' }
                }
            },
            valueField      : 'rowId',
            displayField    : 'name',
            allowBlank      : false,
            editable        : false
        }));

        formItems.push(Ext4.create('Ext.form.field.ComboBox', {
            fieldLabel      : 'Rack',
            name            : 'rack',
            labelSeparator  : '',
            store : {
                model   : 'LABKEY.Peptide.Location',
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : {
                        schemaName  : 'peptideinventory',
                        queryName   : 'rack',
                        sort        : 'name'
                    },
                    reader : { type : 'json', root : 'rows' }
                }
            },
            valueField      : 'rowId',
            displayField    : 'name',
            allowBlank      : false,
            editable        : false
        }));

        formItems.push(Ext4.create('Ext.form.field.ComboBox', {
            fieldLabel      : 'Drawer',
            name            : 'drawer',
            labelSeparator  : '',
            store : {
                model   : 'LABKEY.Peptide.Location',
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : {
                        schemaName  : 'peptideinventory',
                        queryName   : 'drawer',
                        sort        : 'name'
                    },
                    reader : { type : 'json', root : 'rows' }
                }
            },
            valueField      : 'rowId',
            displayField    : 'name',
            allowBlank      : false,
            editable        : false
        }));

        formItems.push(Ext4.create('Ext.form.field.ComboBox', {
            fieldLabel      : 'Shelf',
            name            : 'shelf',
            labelSeparator  : '',
            store : {
                model   : 'LABKEY.Peptide.Location',
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : {
                        schemaName  : 'peptideinventory',
                        queryName   : 'shelf',
                        sort        : 'name'
                    },
                    reader : { type : 'json', root : 'rows' }
                }
            },
            valueField      : 'rowId',
            displayField    : 'name',
            allowBlank      : false,
            editable        : false
        }));

        formItems.push(Ext4.create('Ext.form.field.ComboBox', {
            fieldLabel      : 'Box',
            name            : 'box',
            labelSeparator  : '',
            store : {
                model   : 'LABKEY.Peptide.Location',
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : {
                        schemaName  : 'peptideinventory',
                        queryName   : 'box',
                        sort        : 'name'
                    },
                    reader : { type : 'json', root : 'rows' }
                }
            },
            valueField      : 'rowId',
            displayField    : 'name',
            allowBlank      : false,
            editable        : false
        }));

        formItems.push({
            xtype       : 'displayfield',
            value       : 'Specify the slot number for each of the selected Peptides.'
        });

        // get the selected peptides
        var peptides = this.peptideResultsGrid.getSelectionModel().getSelection();

        Ext4.each(peptides, function(rec){

            var slotLabel = 'Slot (' + rec.data.peptide_id + ')';
            formItems.push({
                xtype       : 'textfield',
                fieldLabel  : slotLabel,
                name        : rec.data.peptide_id,
                width       : 100,
                allowBlank : false,
                listeners : {
                    scope: this,
                    change : function(cmp, value) {
                        this.peptideSlots[cmp.name] = value;
                    }
                }
            });
        }, this);

        var dialog = Ext4.create('Ext.window.Window', {
            width   : 400,
            height  : 500,
            layout  : 'fit',
            draggable   : false,
            modal       : true,
            title  : 'Assign Freezer Location for Selections',
            defaults: {
                border: false, frame: false
            },
            bodyPadding : 20,
            items   : [{
                xtype       : 'form',
                items       : formItems,
                autoScroll  : true,
                layout      : {
                    type  : 'vbox',
                    align : 'stretch'
                }
            }],
            buttons     : [{
                text : 'Save',
                formBind: true,
                handler : function(btn)
                {
                    var form = dialog.down('form').getForm();
                    if (form.isValid())
                    {
                        LABKEY.Query.selectRows({
                            schemaName  : 'peptideinventory',
                            queryName   : 'vial',
                            columns     : 'peptideId',
                            success     : function(data) {
                                var me = this;
                                var existingVials = {};

                                Ext4.each(data.rows, function(row){

                                    existingVials[row['peptideId']] = row['peptideId'];
                                }, me);

                                var values = form.getValues();
                                var insertedRows = [];
                                var updatedRows = [];

                                var peptides = this.peptideResultsGrid.getSelectionModel().getSelection();
                                Ext4.each(peptides, function(rec){
                                    var id = rec.data.peptide_id;
                                    var row = {
                                        freezer : values.freezer,
                                        shelf   : values.shelf,
                                        rack    : values.rack,
                                        drawer  : values.drawer,
                                        box     : values.box,
                                        slot    : me.peptideSlots[id],
                                        peptideId   : id,
                                        container   : LABKEY.container.id
                                    };

                                    if (existingVials[id])
                                        updatedRows.push(row);
                                    else
                                        insertedRows.push(row);
                                }, this);

                                // handle inserts & updates
                                if (insertedRows.length > 0) {

                                    LABKEY.Query.insertRows({
                                        schemaName  : 'peptideInventory',
                                        queryName   : 'vial',
                                        rows        : insertedRows,
                                        success     : function() {
                                            dialog.close();
                                            var msgbox = Ext4.create('Ext.window.Window', {
                                                title    : 'Success',
                                                modal    : false,
                                                closable : false,
                                                border   : false,
                                                html     : '<div style="padding: 15px;"><span class="labkey-message">Vials successfully inserted</span></div>'
                                            });
                                            msgbox.show();
                                            msgbox.getEl().fadeOut({duration : 3000, callback : function(){ msgbox.close(); }});
                                        }
                                    });
                                }

                                if (updatedRows.length > 0) {

                                    LABKEY.Query.updateRows({
                                        schemaName  : 'peptideInventory',
                                        queryName   : 'vial',
                                        rows        : updatedRows,
                                        success     : function() {
                                            dialog.close();
                                            var msgbox = Ext4.create('Ext.window.Window', {
                                                title    : 'Success',
                                                modal    : false,
                                                closable : false,
                                                border   : false,
                                                html     : '<div style="padding: 15px;"><span class="labkey-message">Vials successfully updated</span></div>'
                                            });
                                            msgbox.show();
                                            msgbox.getEl().fadeOut({duration : 3000, callback : function(){ msgbox.close(); }});
                                        }
                                    });
                                }
                            },
                            scope: this
                        });
                    }
                    else
                    {
                        Ext4.Msg.show({
                            title: "Error",
                            msg: 'All required fields must be specified.',
                            buttons: Ext4.MessageBox.OK,
                            icon: Ext4.MessageBox.ERROR
                        });
                    }
                },
                scope   : this
            },{
                text : 'Cancel',
                handler : function(btn) {
                    dialog.close();
                }
            }],
            scope : this
        });

        dialog.show();
    }
});
