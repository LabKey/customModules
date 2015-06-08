/*
 * Copyright (c) 2014-2015 LabKey Corporation
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
            {xtype : 'search-pooled-peptide-panel', title : 'Search Pooled Peptides'}
        ];

        this.listeners = {
            poolCreated : {fn : function(){
                for (var i=0; i < this.items.items.length; i++) {

                    this.items.items[i].onPoolCreated();
                }
            }, scope : this}
        };
        this.callParent();
    }
});

/**
 * Base search panel that handles assingment of selected peptides
 */
Ext4.define('LABKEY.ext4.BaseSearchPanel', {

    extend : 'Ext.panel.Panel',

    bubbleEvents : ['poolCreated'],

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
                                    var slotNum = 1;

                                    Ext4.each(peptides, function(rec){
                                        var id = rec.peptide_id;
                                        var row = {
                                            freezer : values.freezer,
                                            shelf   : values.shelf,
                                            rack    : values.rack,
                                            drawer  : values.drawer,
                                            box     : values.box,
                                            slot    : assignSlots ? me.peptideSlots[id] : slotNum++,
                                            peptideId   : id,
                                            rcpoolId    : rec.rcPoolId,
                                            container   : LABKEY.container.id
                                        };

                                        if (existingVials[id])
                                            updatedRows.push(row);
                                        else
                                            insertedRows.push(row);
                                    }, this);

                                    var multi = new LABKEY.MultiRequest();

                                    // handle inserts & updates
                                    if (insertedRows.length > 0) {

                                        multi.add(LABKEY.Query.insertRows, {
                                            schemaName  : 'peptideInventory',
                                            queryName   : 'vial',
                                            rows        : insertedRows,
                                            scope       : this,
                                            success     : function() {},
                                            failure     : function(){
                                                Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Vials to the freezer location.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                            }
                                        }, this);
                                    }

                                    if (updatedRows.length > 0) {

                                        multi.add(LABKEY.Query.updateRows, {
                                            schemaName  : 'peptideInventory',
                                            queryName   : 'vial',
                                            rows        : updatedRows,
                                            scope       : this,
                                            success     : function() {},
                                            failure     : function(){
                                                Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Vials to the freezer location.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                            }
                                        }, this);
                                    }

                                    // perform the updates
                                    multi.send(function(){
                                        dialog.close();
                                        this.showFadeOutMessage('Success', 'Vials successfully assigned');
                                        this.dataStore.reload();
                                    }, this);
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

                            var multi = new LABKEY.MultiRequest();

                            // handle inserts & updates
                            if (insertedRows.length > 0) {

                                multi.add(LABKEY.Query.insertRows,{
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'lotAssignment',
                                    rows        : insertedRows,
                                    scope       : this,
                                    success     : function() {
                                        console.log('sucessfully added lot assignments')
                                    },
                                    failure     : function(){
                                        Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Lot number to the Vials.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                    }
                                }, this);
                            }

                            if (updatedRows.length > 0) {

                                multi.add(LABKEY.Query.updateRows,{
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'lotAssignment',
                                    rows        : updatedRows,
                                    scope       : this,
                                    success     : function() {
                                        console.log('sucessfully updated lot assignments')
                                    },
                                    failure     : function(){
                                        Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Lot number to the Vials.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                    }
                                }, this);
                            }

                            // perform the updates
                            multi.send(function(){
                                dialog.close();
                                this.showFadeOutMessage('Success', 'Lot numbers successfully assigned');
                                this.dataStore.reload();
                            }, this);
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

    createPool : function() {

        // get the selected peptides passed back through the callback
        this.getSelectedPeptides(function(peptides){

            var formItems = [];
            var lotNumber = null;
            var vialCount = null;

            formItems.push({
                xtype       : 'displayfield',
                value       : 'Specify the lot number and the number of vials to be created from the reconstituted peptides.'
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

            formItems.push({
                xtype       : 'numberfield',
                fieldLabel  : 'Vial Count',
                width       : 100,
                allowBlank : false,
                listeners : {
                    scope: this,
                    change : function(cmp, value) {
                        vialCount = value;
                    }
                }
            });

            var dialog = Ext4.create('Ext.window.Window', {
                width   : 400,
                height  : 250,
                layout  : 'fit',
                draggable   : false,
                modal       : true,
                title  : 'Create Peptide Pool for Selections',
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
                            var newVials = [];
                            var poolAssignments = [];

                            // create the new pool
                            var createNewPool = function(scope, callback) {
                                LABKEY.Query.insertRows({
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'RCPool',
                                    rows        : [{container : LABKEY.container.id, lotnumber : lotNumber, vialcount : vialCount}],
                                    scope       : scope,
                                    success     : function(res) {
                                        if (res.rows && res.rows.length === 1)
                                        {
                                            var rec = res.rows[0];
                                            var poolId = rec.rowid;
                                            var vialCount = rec.vialcount;
                                            var lotNumber = rec.lotnumber;

                                            // create the new pooled vials
                                            for (var i = 0; i < vialCount; i++)
                                            {
                                                newVials.push({container: LABKEY.container.id, rcpoolid: poolId, peptideid: i + 1});
                                            }

                                            Ext4.each(peptides, function (rec)
                                            {
                                                poolAssignments.push({container: LABKEY.container.id, peptideid: rec.peptide_id, rcpoolid: poolId})
                                            });

                                        }
                                        if (callback)
                                            callback.call(scope);
                                    },
                                    failure     : function(){
                                        Ext4.Msg.show({title: "Error", msg: 'An error occurred creating the Peptide Pool.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                    }
                                });
                            };

                            var createPooledVials = function(scope, callback){
                                LABKEY.Query.insertRows({
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'vial',
                                    rows        : newVials,
                                    scope       : scope,
                                    success     : function(){
                                        if (callback)
                                            callback.call(scope);
                                    },
                                    failure     : function(){
                                        Ext4.Msg.show({title: "Error", msg: 'An error occurred creating the Peptide Pool.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                    }
                                });
                            };

                            var markUsedPeptides = function(scope, callback) {
                                LABKEY.Query.selectRows({
                                    schemaName  : 'peptideinventory',
                                    queryName   : 'vial',
                                    columns     : 'peptideId',
                                    scope       : this,
                                    success     : function(data)
                                    {
                                        var me = this;
                                        var existingVials = {};

                                        Ext4.each(data.rows, function (row)
                                        {
                                            existingVials[row['peptideId']] = row['peptideId'];
                                        }, me);

                                        // mark the parent peptides as used & handle inserts and updates
                                        var usedPeptides = [];
                                        var newUsedPeptides = [];
                                        Ext4.each(peptides, function (rec)
                                        {
                                            if (existingVials[rec.peptide_id])
                                                usedPeptides.push({container: LABKEY.container.id, peptideid: rec.peptide_id, rcpoolid: -1, used: true})
                                            else
                                                newUsedPeptides.push({container: LABKEY.container.id, peptideid: rec.peptide_id, rcpoolid: -1, used: true})
                                        }, me);

                                        var multi = new LABKEY.MultiRequest();

                                        if (usedPeptides.length > 0){
                                            multi.add(LABKEY.Query.updateRows, {
                                                schemaName  : 'peptideInventory',
                                                queryName   : 'vial',
                                                rows        : usedPeptides,
                                                scope       : this,
                                                success     : function(){},
                                                failure     : function(){
                                                    Ext4.Msg.show({title: "Error", msg: 'An error occurred creating the Peptide Pool.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                                }
                                            }, me);
                                        }

                                        if (newUsedPeptides.length > 0){
                                            multi.add(LABKEY.Query.insertRows, {
                                                schemaName  : 'peptideInventory',
                                                queryName   : 'vial',
                                                rows        : newUsedPeptides,
                                                scope       : this,
                                                success     : function(){},
                                                failure     : function(){
                                                    Ext4.Msg.show({title: "Error", msg: 'An error occurred creating the Peptide Pool.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                                }
                                            }, me);
                                        }

                                        // perform the updates
                                        multi.send(function(){
                                            if (callback)
                                                callback.call(scope);
                                        }, me);
                                    },
                                    failure     : function(){
                                        Ext4.Msg.show({title: "Error", msg: 'An error occurred creating the Peptide Pool.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                    }
                                });

                                LABKEY.Query.updateRows({
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'vial',
                                    rows        : usedPeptides,
                                    scope       : this,
                                    success     : function(){
                                        if (callback)
                                            callback.call(scope);
                                    },
                                    failure     : function(){
                                        Ext4.Msg.show({title: "Error", msg: 'An error occurred creating the Peptide Pool.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                    }
                                });
                            };

                            var setRCPoolAssignment = function(scope, callback) {
                                LABKEY.Query.insertRows({
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'rcPoolAssignment',
                                    rows        : poolAssignments,
                                    scope       : this,
                                    success     : function(){
                                        dialog.close();
                                        scope.showFadeOutMessage('Success', 'Peptide Pool successfully created');
                                        scope.dataStore.reload();
                                        scope.fireEvent('poolCreated');

                                        if (callback)
                                            callback.call(scope);
                                    },
                                    failure     : function(){
                                        Ext4.Msg.show({title: "Error", msg: 'An error occurred creating the Peptide Pool.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                    }
                                });
                            };

                            // chain together the requests
                            createNewPool(this, function(){
                                createPooledVials(this, function(){
                                    markUsedPeptides(this, function(){
                                        setRCPoolAssignment(this);
                                    });
                                });
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
     *
     * Note: be aware of the left join on the vial table introducing nulls into the assign location query
     */
    getPeptidesInCategorySql : function(proteinCategory) {
        return 'SELECT x.peptide_id, x.peptide_sequence, x.proteinCategory, pg.name AS peptideGroup, pg.pathogen, pg.clade, ' +
                'pg.groupType, pg.alignRef, ga.peptide_id_in_group, la.lotNumber, v.checkedOut, v.used, ' +
                'CASE WHEN v.rcPoolId IS NULL THEN -1 ELSE v.rcPoolId END AS rcPoolId, ' +
                'v.freezer, v.rack, v.shelf, v.drawer, v.box ' +
                'FROM (' +
                'SELECT * FROM peptideInventory.peptide p ' +
                'WHERE p.proteinCategory = \'' + proteinCategory + '\'' +
                ') x LEFT JOIN peptideInventory.peptideGroupAssignment ga ON x.peptide_id = ga.peptide_id ' +
                'LEFT JOIN peptideInventory.peptideGroup pg ON ga.peptide_group_id = pg.peptide_group_id ' +
                'LEFT JOIN peptideInventory.lotAssignment la ON x.peptide_id = la.peptideId ' +
                'LEFT JOIN peptideInventory.vial v ON x.peptide_id = v.peptideId ' +
                'WHERE ((v.used = false OR v.used IS NULL) AND (v.checkedOut = false OR v.checkedOut IS NULL))';
    },

    locationRenderer : function(value, meta, rec){

        if (rec.raw.freezer.displayValue && rec.raw.rack.displayValue && rec.raw.shelf.displayValue && rec.raw.drawer.displayValue){
            var delim = '/';
            return rec.raw.freezer.displayValue + delim + rec.raw.rack.displayValue + delim + rec.raw.shelf.displayValue + delim +
                    rec.raw.drawer.displayValue + delim + rec.raw.box.displayValue;
        }
        return 'not assigned';
    },

    /**
     * Returns the array of selected peptide records
     */
    getSelectedPeptides : function() {

        console.error('This function must be overridden');
    },

    onPoolCreated : function() {
        console.log('callback : on pool created');
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
                    tooltip : 'Assign selected peptides to a freezer location',
                    handler : function() {this.assignLocation(true);},
                    scope   : this
                },{
                    text    : 'Create Pool',
                    disabled: true,
                    tooltip : 'Combined selected peptides into a new pool',
                    handler : function() {this.createPool();},
                    scope   : this
                },{
                    text    : 'Assign Lot Number',
                    disabled: true,
                    tooltip : 'Assign a lot number to the selected peptides',
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
                value       : 'This tab is used to assign individual peptides to a freezer location. Start by selecting a specific peptide protein category from the dropdown ' +
                        'check the individual peptides before clicking on the <strong>Assign Location</strong> button to assign the set of peptides to a specific freezer location</p>' +
                        'Individual peptides can also be pooled and then aliquoted into separate vials. To do this, select the individual peptides to be combined and click on the ' +
                        '<strong>Create Pool</strong> button</p>'
            }]
        });
        this.items.push(this.northPanel);

        this.initNorthPanel();
        this.callParent();
    },

    initNorthPanel : function() {

        if (!Ext4.ModelManager.isRegistered('LABKEY.Peptide.ProteinCategories')) {
            Ext4.define('LABKEY.Peptide.ProteinCategories', {
                extend: 'Ext.data.Model',
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'executeSql.api'),
                    extraParams : {
                        schemaName  : 'peptideinventory',
                        sql         : 'SELECT DISTINCT(proteinCategory) FROM peptide',
                        sort        : 'proteinCategory'
                    },
                    reader : { type : 'json', root : 'rows' }
                },
                fields : [
                    {name: 'proteinCategory'}
                ]
            });
        }

        var categoryStore = Ext4.create('Ext.data.Store', {
            model : 'LABKEY.Peptide.ProteinCategories',
            pageSize    : 10000,
            autoLoad: true
        });

        var proteinCategory = Ext4.create('Ext.form.field.ComboBox', {
            width : 800,
            fieldLabel : 'Protein Categories',
            labelWidth : 150,
            labelSeparator : '',
            store : categoryStore,
            valueField : 'proteinCategory',
            displayField : 'proteinCategory',
            forceSelection : true,
            editable : false,
            listeners : {
                scope: this,
                change : function(combo, value) {
                    this.proteinCategory = value;
                    this.getPeptidesInPool();
                }
            }
        });

        this.northPanel.add(proteinCategory);
    },

    /**
     * Populate the grid with the set of peptides in the selected pool
     */
    getPeptidesInPool : function() {

        if (!this.peptideResultsGrid) {

            if (!Ext4.ModelManager.isRegistered('LABKEY.Peptide.Peptides'))
            {
                Ext4.define('LABKEY.Peptide.Peptides', {
                    extend: 'Ext.data.Model',
                    proxy: {
                        type: 'ajax',
                        url: LABKEY.ActionURL.buildURL('query', 'executeSql.api'),
                        extraParams: {
                            schemaName  : 'peptideinventory',
                            queryName   : 'peptide',
                            sort        : 'name',
                            apiVersion  : 9.1
                        },
                        reader: { type: 'json', root: 'rows', metaProperty : 'none', totalProperty : 'rowCount' }
                    },
                    fields: [
                        {name: 'peptide_id', mapping : 'peptide_id.value'},
                        {name: 'peptide_sequence', mapping : 'peptide_sequence.value'},
                        {name: 'lotNumber', mapping : 'lotNumber.value'},
                        {name: 'proteinCategory', mapping : 'proteinCategory.value'},
                        {name: 'peptideGroup', mapping : 'peptideGroup.value'},
                        {name: 'pathogen', mapping : 'pathogen.value'},
                        {name: 'clade', mapping : 'clade.value'},
                        {name: 'groupType', mapping : 'groupType.value'},
                        {name: 'alignRef', mapping : 'alignRef.value'},
                        {name: 'peptide_id_in_group', mapping : 'peptide_id_in_group.value'},
                        {name: 'rcPoolId', mapping : 'rcPoolId.value'},
                        {name: 'freezer'},
                        {name: 'rack'},
                        {name: 'shelf'},
                        {name: 'drawer'},
                        {name: 'box'}
                    ]
                });
            }

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
                    {header : 'Peptide Sequence', dataIndex : 'peptide_sequence', flex : 1.2},
                    {header : 'Lot Number', dataIndex : 'lotNumber', width : 150},
                    {header : 'Protein Category', dataIndex : 'proteinCategory', width : 150},
                    {header : 'Group', dataIndex : 'peptideGroup', width : 75},
                    {header : 'Pathogen', dataIndex : 'pathogen', width : 75},
                    {header : 'Clade', dataIndex : 'clade', width : 100},
                    {header : 'Group Type', dataIndex : 'groupType', width : 75},
                    {header : 'Align Ref', dataIndex : 'alignRef', width : 75},
                    {header : 'Id in Group', dataIndex : 'peptide_id_in_group', width : 125},
                    {header : 'Freezer Location', dataIndex : 'freezer', width : 125, scope : this, renderer : this.locationRenderer, flex : 1.1}
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
                        var poolBtn = this.down('button[text="Create Pool"]');
                        if (editBtn)
                            editBtn.setDisabled(selected.length == 0);
                        if (lotBtn)
                            lotBtn.setDisabled(selected.length == 0);
                        if (poolBtn)
                            poolBtn.setDisabled(selected.length == 0);
                    },
                    scope: this
                }
            });

            this.centerPanel.add(this.peptideResultsGrid);
            this.centerPanel.enable();
        }
        this.dataStore.proxy.extraParams.sql = this.getPeptidesInCategorySql(this.proteinCategory);
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
 * Assign locations by pooled peptide
 */
Ext4.define('LABKEY.ext4.SearchPooledPeptidePanel', {

    extend : 'LABKEY.ext4.BaseSearchPanel',

    alias: 'widget.search-pooled-peptide-panel',

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

        Ext4.define('LABKEY.Peptide.PooledPeptides', {
            extend: 'Ext.data.Model',
            fields : [
                {name: 'peptide_id', mapping : 'peptide_id.value'},
                {name: 'lotNumber', mapping : 'lotNumber.value'},
                {name: 'vialCount', mapping : 'vialCount.value'},
                {name: 'rcPoolId', mapping : 'rcPoolId.value'},
                {name: 'freezer'},
                {name: 'rack'},
                {name: 'shelf'},
                {name: 'drawer'},
                {name: 'box'}
            ]
        });

        this.dataStore = Ext4.create('Ext.data.Store', {
            model : 'LABKEY.Peptide.PooledPeptides',
            autoLoad: true,
            proxy : {
                type : 'ajax',
                url    : LABKEY.ActionURL.buildURL('query', 'executeSql.api'),
                extraParams : {
                    schemaName  : 'peptideinventory',
                    queryName   : 'peptide',
                    sql         : this.getPooledPeptidesSql(),
                    apiVersion  : 9.1
                },
                reader : { type : 'json', root : 'rows', metaProperty : 'none'}
            },
            pageSize: 200
        });

        this.pooledPeptidesGrid = Ext4.create('Ext.grid.Panel', {
            store   : this.dataStore,
            border  : false, frame : false,
            selType : 'checkboxmodel',
            scope   : this,
            emptyText : '<h3>There are no Pooled Petides.</h3>',
            columns : [
                {header : 'Peptide Id', dataIndex : 'peptide_id'},
                {header : 'Lot Number', dataIndex : 'lotNumber', width : 200},
                {header : 'Vials in Lot', dataIndex : 'vialCount', width : 125},
                {header : 'Freezer Location', dataIndex : 'freezer', width : 125, scope : this, renderer : this.locationRenderer, flex : 1.1}
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

        var centerPanel = Ext4.create('Ext.panel.Panel', {
            border   : false, frame : false,
            layout   : 'fit',
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
                },{
                    text    : 'Delete Pool',
                    handler : function() {this.deletePool();},
                    scope   : this
                }]
            }],
            items : this.pooledPeptidesGrid
        });

        this.items.push(centerPanel);

        var northPanel = Ext4.create('Ext.panel.Panel', {
            bodyPadding : 20,
            border      : false,
            height      : 150,
            region      : 'north',
            items : [{
                xtype       : 'displayfield',
                value       : 'This tab is used to assign vials created from pooled peptides to a freezer location. Check the individual vials before clicking ' +
                        'on the "Assign Location" button to assign the set of vials to a specific freezer location</p>'
            }]
        });
        this.items.push(northPanel);

        this.callParent();
    },

    /**
     * Returns the select SQL to return the list of peptides in a specified pool
     */
    getPooledPeptidesSql : function() {
        return 'SELECT v.peptideId AS peptide_id, rc.lotNumber, rc.vialCount, v.rcPoolId, ' +
                    'v.freezer, v.rack, v.shelf, v.drawer, v.box ' +
                    'FROM peptideInventory.vial v JOIN peptideInventory.rcPool rc ON v.rcPoolId = rc.rowId ORDER BY rc.lotNumber, v.peptideId';
    },

    getSelectedPeptides : function(callback) {
        var peptides = [];
        var peptideMap = {};

        Ext4.each(this.pooledPeptidesGrid.getSelectionModel().getSelection(), function(rec){

            if (!peptideMap[rec.data.peptide_id]) {
                peptides.push(rec.data);
                peptideMap[rec.data.peptide_id] = true;
            }
        }, this);

        if (callback)
            callback.call(this, peptides);
    },

    deletePool : function() {

        var formItems = [];

        formItems.push({
            xtype       : 'displayfield',
            value       : 'Select the lot number of the combined Peptide Pool to delete, all vials associated with the pool will be deleted as well.</p>'
        });

        if (!Ext4.ModelManager.isRegistered('LABKEY.Peptide.RCPools'))
        {
            Ext4.define('LABKEY.Peptide.RCPools', {
                extend: 'Ext.data.Model',
                fields: [
                    {name: 'rowid'},
                    {name: 'vialCount'},
                    {name: 'lotNumber'}
                ]
            });
        }
        formItems.push({
            xtype           : 'combo',
            fieldLabel      : 'Combined Pools',
            name            : 'rcPoolId',
            labelSeparator  : '',
            store : {
                model   : 'LABKEY.Peptide.RCPools',
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : {
                        schemaName  : 'peptideinventory',
                        queryName   : 'rcPool',
                        sort        : 'name'
                    },
                    reader : { type : 'json', root : 'rows' }
                }
            },
            valueField      : 'rowid',
            displayField    : 'lotNumber',
            allowBlank      : false,
            editable        : false
        });

        var dialog = Ext4.create('Ext.window.Window', {
            width   : 450,
            height  : 275,
            layout  : 'fit',
            draggable   : false,
            modal       : true,
            title  : 'Delete Selected Pool',
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
                text : 'Delete',
                formBind: true,
                handler : function(btn)
                {
                    var form = dialog.down('form').getForm();
                    if (form.isValid())
                    {
                        var values = form.getValues();
                        var multi = new LABKEY.MultiRequest();
                        var rcPoolId = values.rcPoolId;
                        var oldAssignments = [];

                        // delete the created vials
                        multi.add(LABKEY.Query.executeSql,{
                            schemaName  : 'peptideInventory',
                            sql         : 'SELECT container, peptideId, rcPoolId FROM vial WHERE rcPoolId = ' + rcPoolId,
                            scope       : this,
                            success     : function(rec)
                            {
                                if (rec.rows.length > 0){
                                    LABKEY.Query.deleteRows({
                                        schemaName: 'peptideInventory',
                                        queryName: 'vial',
                                        rows: rec.rows,
                                        scope: this,
                                        failure: function()
                                        {
                                            Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Lot number to the Vials.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                        }
                                    });
                                }
                            },
                            failure     : function(){
                                Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Lot number to the Vials.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                            }
                        }, this);

                        // make the source peptides as available
                        multi.add(LABKEY.Query.executeSql,{
                            schemaName  : 'peptideInventory',
                            sql         : 'SELECT peptideId FROM rcPoolAssignment WHERE rcPoolId = ' + rcPoolId,
                            scope       : this,
                            success     : function(rec) {

                                var updated = [];
                                Ext4.each(rec.rows, function(rec){
                                    updated.push({container : LABKEY.container.id, peptideId : rec.peptideId, rcPoolId : -1, used : false, checkedOut : false});
                                    oldAssignments.push({container : LABKEY.container.id, peptideId : rec.peptideId});
                                });

                                if (updated.length > 0){
                                    LABKEY.Query.updateRows({
                                        schemaName: 'peptideInventory',
                                        queryName: 'vial',
                                        rows: updated,
                                        scope: this,
                                        failure: function()
                                        {
                                            Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Lot number to the Vials.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                        }
                                    });
                                }
                            },
                            failure     : function(){
                                Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Lot number to the Vials.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                            }
                        }, this);

                        // perform the updates
                        multi.send(function(){
                            // delete the peptide to pool assignments
                            if (oldAssignments.length > 0){
                                LABKEY.Query.deleteRows({
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'rcPoolAssignment',
                                    rows        : oldAssignments,
                                    scope: this,
                                    success: function ()
                                    {
                                        // delete the pool
                                        LABKEY.Query.deleteRows({
                                            schemaName  : 'peptideInventory',
                                            queryName   : 'rcPool',
                                            rows        : [{rowId : rcPoolId}],
                                            scope       : this,
                                            success     : function(){
                                                dialog.close();
                                                this.showFadeOutMessage('Success', 'Pool successfully deleted');
                                                this.dataStore.reload();
                                            },
                                            failure     : function(){
                                                Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Lot number to the Vials.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                            }
                                        });
                                    },
                                    failure: function ()
                                    {
                                        Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Lot number to the Vials.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                    }
                                });
                            }
                            else {
                                // delete the pool
                                LABKEY.Query.deleteRows({
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'rcPool',
                                    rows        : [{rowId : rcPoolId}],
                                    scope       : this,
                                    success     : function(){
                                        dialog.close();
                                        this.showFadeOutMessage('Success', 'Pool successfully deleted');
                                        this.dataStore.reload();
                                    },
                                    failure     : function(){
                                        Ext4.Msg.show({title: "Error", msg: 'An error occurred assigning the Lot number to the Vials.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                    }
                                });
                            }
                        }, this);
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
    },

    onPoolCreated : function() {
        this.dataStore.reload();
    }
});
