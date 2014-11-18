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

    extend: 'Ext.tab.Panel',

    constructor: function (config)
    {
        this.callParent([config]);
    },

    initComponent: function ()
    {
        this.items = [
            {xtype : 'search-peptide-panel', title : 'Search Peptides'},
            {xtype : 'search-pool-panel', title : 'Search Pools'}
        ];

        this.callParent();
    }
});

/**
 * Base search panel that handles assingment of selected peptides
 */
Ext4.define('LABKEY.ext4.BaseSearchPanel', {

    extend : 'Ext.panel.Panel',

    initComponent : function() {

        this.callParent();
    },

    assignLocation : function(assignSlots) {

        // get the selected peptides passed back through the callback
        this.getSelectedPeptides(function(peptides){

            var formItems = [];
            this.peptideSlots = {};

            formItems.push({
                xtype       : 'displayfield',
                value       : 'Specify the freezer, rack, drawer, shelf and box for all selected Peptides or Peptide Pools.'
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

            if (assignSlots) {

                formItems.push({
                    xtype       : 'displayfield',
                    value       : 'Specify the slot number for each of the selected Peptides.'
                });

                Ext4.each(peptides, function(rec){

                    var slotLabel = 'Slot (' + rec.peptide_id + ')';
                    formItems.push({
                        xtype       : 'textfield',
                        fieldLabel  : slotLabel,
                        name        : rec.peptide_id,
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
            }

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

                                    Ext4.each(peptides, function(rec){
                                        var id = rec.peptide_id;
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
                                            scope       : this,
                                            success     : function() {
                                                if (dialog){
                                                    dialog.close();
                                                    dialog = null;
                                                    this.showFadeOutMessage('Success', 'Vials successfully assigned');
                                                }
                                            },
                                            failure     : function(){
                                                Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Vials to the freezer location.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                            }
                                        });
                                    }

                                    if (updatedRows.length > 0) {

                                        LABKEY.Query.updateRows({
                                            schemaName  : 'peptideInventory',
                                            queryName   : 'vial',
                                            rows        : updatedRows,
                                            scope       : this,
                                            success     : function() {
                                                if (dialog){
                                                    dialog.close();
                                                    dialog = null;
                                                    this.showFadeOutMessage('Success', 'Vials successfully assigned');
                                                }
                                            },
                                            failure     : function(){
                                                Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Vials to the freezer location.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                            }
                                        });
                                    }
                                },
                                scope: this
                            });
                        }
                        else
                        {
                            Ext4.Msg.show({title: "Error", msg: 'All required fields must be specified.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
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
        });
    },

    assignLotNumber : function() {

        // get the selected peptides passed back through the callback
        this.getSelectedPeptides(function(peptides){

            var formItems = [];
            var lotNumber = null;

            formItems.push({
                xtype       : 'displayfield',
                value       : 'Specify the lot number to assign to all selected Peptides.'
            });

            formItems.push({
                xtype       : 'textfield',
                fieldLabel  : 'Lot Number',
                width       : 100,
                allowBlank : false,
                listeners : {
                    scope: this,
                    change : function(cmp, value) {
                        lotNumber = value;
                    }
                }
            });

            var dialog = Ext4.create('Ext.window.Window', {
                width   : 400,
                height  : 175,
                layout  : 'fit',
                draggable   : false,
                modal       : true,
                title  : 'Assign Lot Number for Selections',
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
                            var updatedRows = [];
                            var insertedRows = [];

                            Ext4.each(peptides, function(rec){
                                var row = {
                                    peptideId   : rec.peptide_id,
                                    lotNumber   : lotNumber,
                                    container   : LABKEY.container.id
                                };

                                if (rec.lotNumber)
                                    updatedRows.push(row);
                                else
                                    insertedRows.push(row);
                            }, this);

                            // handle inserts & updates
                            if (insertedRows.length > 0) {

                                LABKEY.Query.insertRows({
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'lotAssignment',
                                    rows        : insertedRows,
                                    scope       : this,
                                    success     : function() {
                                        if (dialog){
                                            dialog.close();
                                            dialog = null;
                                            this.showFadeOutMessage('Success', 'Lot numbers successfully assigned');
                                            this.dataStore.reload();
                                        }
                                    },
                                    failure     : function(){
                                        Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Lot number to the Vials.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                    }
                                });
                            }

                            if (updatedRows.length > 0) {

                                LABKEY.Query.updateRows({
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'lotAssignment',
                                    rows        : updatedRows,
                                    scope       : this,
                                    success     : function() {
                                        if (dialog){
                                            dialog.close();
                                            dialog = null;
                                            this.showFadeOutMessage('Success', 'Lot numbers successfully assigned');
                                            this.dataStore.reload();
                                        }
                                    },
                                    failure     : function(){
                                        Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Lot number to the Vials.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                    }
                                });
                            }
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
        });
    },

    showFadeOutMessage : function(title, msg){
        var msgbox = Ext4.create('Ext.window.Window', {
            title    : title,
            modal    : false,
            closable : false,
            border   : false,
            html     : '<div style="padding: 15px;"><span class="labkey-message">' + msg + '</span></div>'
        });
        msgbox.show();
        msgbox.getEl().fadeOut({duration : 3000, callback : function(){ msgbox.close(); }});
    },

    /**
     * Returns the select SQL to return the list of peptides in a specified pool
     */
    getPeptidesInPoolSql : function(poolId) {
        return 'SELECT x.peptide_id, x.peptide_sequence, x.proteinCategory, pg.name AS peptideGroup, pg.pathogen, pg.clade, ' +
                'pg.groupType, pg.alignRef, ga.peptide_id_in_group, la.lotNumber ' +
                'FROM (' +
                'SELECT * FROM peptideInventory.peptide p ' +
                'WHERE p.peptide_id IN (SELECT peptide_id FROM peptideInventory.peptidePoolAssignment WHERE peptide_pool_id = ' + poolId + ')' +
                ') x LEFT JOIN peptideInventory.peptideGroupAssignment ga ON x.peptide_id = ga.peptide_id ' +
                'LEFT JOIN peptideInventory.peptideGroup pg ON ga.peptide_group_id = pg.peptide_group_id ' +
                'LEFT JOIN peptideInventory.lotAssignment la ON x.peptide_id = la.peptideId';
    },

    /**
     * Returns the array of selected peptide records
     */
    getSelectedPeptides : function() {

        console.error('This function must be overridden');
    }
});

/**
 * Assign locations by individual peptide
 */
Ext4.define('LABKEY.ext4.SearchPeptidePanel', {

    extend : 'LABKEY.ext4.BaseSearchPanel',

    alias: 'widget.search-peptide-panel',

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
                    handler : function() {this.assignLocation(true);},
                    scope   : this
                },{
                    text    : 'Assign Lot Number',
                    disabled: true,
                    handler : function() {this.assignLotNumber();},
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
                value       : 'This tab is used to assign individual peptides to a freezer location. Start by selecting a specific peptide pool from the dropdown ' +
                        'check the individual peptides before clicking on the "Assign Location" button to assign the set of peptides to a specific freezer location</p>'
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
                    {name: 'lotNumber'},
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
                    {header : 'Lot Number', dataIndex : 'lotNumber', width : 150},
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
                        var lotBtn = this.down('button[text="Assign Lot Number"]');
                        if (editBtn)
                            editBtn.setDisabled(selected.length == 0);
                        if (lotBtn)
                            lotBtn.setDisabled(selected.length == 0);
                    },
                    scope: this
                }
            });

            this.centerPanel.add(this.peptideResultsGrid);
            this.centerPanel.enable();
        }
        this.dataStore.proxy.extraParams.sql = this.getPeptidesInPoolSql(this.peptidePool);
        this.dataStore.load();
    },

    getSelectedPeptides : function(callback) {
        var peptides = [];
        var peptideMap = {};

        Ext4.each(this.peptideResultsGrid.getSelectionModel().getSelection(), function(rec){

            if (!peptideMap[rec.data.peptide_id]) {
                peptides.push(rec.data);
                peptideMap[rec.data.peptide_id] = true;
            }
        }, this);

        if (callback)
            callback.call(this, peptides);
    }
});

/**
 * Assign locations by peptide pool or subpool
 */
Ext4.define('LABKEY.ext4.SearchPoolPanel', {

    extend : 'LABKEY.ext4.BaseSearchPanel',

    alias: 'widget.search-pool-panel',

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
                    text    : 'Assign Location',
                    disabled: true,
                    handler : function() {this.assignLocation();},
                    scope   : this
                }]
            }]
        });

        this.items.push(this.centerPanel);

        this.northPanel = Ext4.create('Ext.panel.Panel', {
            bodyPadding : 20,
            border      : false,
            height      : 100,
            region      : 'north',
            items : [{
                xtype       : 'displayfield',
                value       : 'This tab is used to assign peptide pools or subpools to a freezer location. Start by checking the specific peptide pool(s) from the grid ' +
                        'before clicking on the "Assign Location" button to assign the set of peptide pools to a specific freezer location</p>'
            }]
        });
        this.items.push(this.northPanel);

        this.initGridPanel();
        this.callParent();
    },

    /**
     * Populate the grid with the set of peptides in the selected pool
     */
    initGridPanel : function() {

        if (!this.poolResultsGrid) {

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
                    {name: 'name'},
                    {name: 'comment'},
                    {name: 'poolType'},
                    {name: 'parent_pool_id'}
                ]
            });

            this.dataStore = Ext4.create('Ext.data.Store', {
                model : 'LABKEY.Peptide.Pools',
                autoLoad: true,
                pageSize: 200,
                listeners : {
                    beforeload : function(){
                        if (this.poolResultsGrid)
                            this.poolResultsGrid.setLoading(true);
                    },
                    load : function(){
                        if (this.poolResultsGrid)
                            this.poolResultsGrid.setLoading(false);
                    },
                    scope: this
                }
            });

            this.poolResultsGrid = Ext4.create('Ext.grid.Panel', {
                store   : this.dataStore,
                border  : false, frame : false,
                //selType : 'checkboxmodel',
                scope   : this,
                emptyText : '<h3>There were no rows returned that matched the specified peptide pool.</h3>',
                columns : [
                    {header : 'Pool Id', dataIndex : 'peptide_pool_id'},
                    {header : 'Pool Name', dataIndex : 'name', flex : 1.1},
                    {header : 'Comment', dataIndex : 'comment', width : 250},
                    {header : 'Pool Type', dataIndex : 'poolType', width : 150},
                    {header : 'Parent Pool Id', dataIndex : 'parent_pool_id', width : 150}
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

            this.centerPanel.add(this.poolResultsGrid);
            this.centerPanel.enable();
        }
    },

    getSelectedPeptides : function(callback) {

        var pools = this.poolResultsGrid.getSelectionModel().getSelection();
        if (pools) {

            LABKEY.Query.executeSql({
                schemaName  : 'peptideinventory',
                sql         : this.getPeptidesInPoolSql(pools[0].data.peptide_pool_id),
                success     : function(data) {
                    var me = this;
                    var peptides = [];
                    var peptideMap = {};

                    Ext4.each(data.rows, function(rec){

                        if (!peptideMap[rec.peptide_id]) {
                            peptides.push(rec);
                            peptideMap[rec.peptide_id] = true;
                        }
                    });
                    if (callback)
                        callback.call(me, peptides);
                },
                scope: this
            });
        }
    }
});
