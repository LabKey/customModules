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
Ext4.define('LABKEY.ext4.EditRequestPanel', {

    extend: 'Ext.panel.Panel',

    constructor : function(config) {

        Ext4.applyIf(config, {
            frame     : false,
            border    : false,
            bodyStyle   : {background  : 'transparent'}
        });

        // map for display values to keys
        this.lkMap = {};

        this.createDataModels();
        this.callParent([config]);
    },

    createDataModels : function() {

        if (!Ext4.ModelManager.isRegistered('LABKEY.HDRL.Specimen')) {
            Ext4.define('LABKEY.HDRL.Specimen', {
                extend: 'Ext.data.Model',
                fields : [
                    {name: 'RowId', mapping : 'RowId.value'},
                    {name: 'InboundRequestId', mapping : 'InboundRequestId.value'},
                    {name: 'CustomerBarcode', mapping : 'CustomerBarcode.value'},
                    {name: 'LastName', mapping : 'LastName.value'},
                    {name: 'FirstName', mapping : 'FirstName.value'},
                    {name: 'BirthDate', type : 'date', mapping : 'BirthDate.value'},
                    {name: 'SSN', mapping : 'SSN.value'},
                    {name: 'DODId', mapping : 'DODId.value'},
                    {name: 'FMPId', mapping : 'FMPId.displayValue'},
                    {name: 'DutyCodeId', mapping : 'DutyCodeId.displayValue'},
                    {name: 'TestingSourceId', mapping : 'TestingSourceId.displayValue'},
                    {name: 'DrawDate', type : 'date', mapping : 'DrawDate.value'},
                    {name: 'status'}
                ]
            });
        }

        this.specimenModelMap = {
            inboundrequestid : 'InboundRequestId',
            customerbarcode : 'CustomerBarcode',
            lastname : 'LastName',
            firstname : 'FirstName',
            birthdate : 'BirthDate',
            ssn : 'SSN',
            dodid : 'DODId',
            fmpid : 'FMPId',
            fmp : 'FMPId',
            dutycodeid : 'DutyCodeId',
            duc : 'DutyCodeId',
            testingsourceid : 'TestingSourceId',
            sot : 'TestingSourceId',
            drawdate : 'DrawDate',
            status : 'status'
        };

        if (!Ext4.ModelManager.isRegistered('LABKEY.HDRL.SpecimenLK')) {
            Ext4.define('LABKEY.HDRL.SpecimenLK', {
                extend: 'Ext.data.Model',
                fields: [
                    {name: 'RowId'},
                    {name: 'Code'},
                    {name: 'Description'},
                    {name: 'Service'}
                ]
            });
        }

        if (!Ext4.ModelManager.isRegistered('LABKEY.HDRL.Lookup')) {
            Ext4.define('LABKEY.HDRL.Lookup', {
                extend: 'Ext.data.Model',
                fields: [
                    {name: 'rowid'},
                    {name: 'name'}
                ]
            });
        }
    },

    initComponent : function() {

        this.items = [];

        this.northPanel = this.createNorthPanel();
        this.gridPanel = this.createGridPanel();
        this.southPanel = this.createSouthPanel();

        this.items.push(this.northPanel);
        this.items.push(this.gridPanel);
        this.items.push(this.southPanel);

        this.callParent();
        window.onbeforeunload = LABKEY.beforeunload(this.beforeUnload, this);
    },

    createGridPanel : function() {

        var items = [];

        // the row editing plugin
        this.roweditor = Ext4.create('Ext.grid.plugin.RowEditing', {
            clicksToMoveEditor  : 1,
            autoCancel          : false,
            triggerEvent        : 'cellclick',
            listeners           : {
                beforeedit : {fn : function(editor, context){

                    // don't activate the editor for checkbox selector or status field
                    if (context.colIdx <= 1){
                        return false;
                    }
                }, scope : this}
            }
        });

        // add the request id to the select rows filter
        var params = LABKEY.Query.buildQueryParams('hdrl', 'inboundSpecimen', [LABKEY.Filter.create('InboundRequestId', this.requestId)]);
        params.apiVersion = 9.1;

        this.grid = Ext4.create('Ext.grid.Panel', {
            title   : '',
            flex    : 1.2,
            maxHeight   : 400,
            autoScroll  : true,
            border  : false, frame : false,
            selType : 'checkboxmodel',
            store : {
                model   : 'LABKEY.HDRL.Specimen',
                autoLoad: true,
                pageSize: 200,
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : params,
                    reader: { type: 'json', root: 'rows', metaProperty : 'none' }
                },
                listeners : {
                    load : {fn : function(){this.resetDirty();}, scope : this},
                    update : {fn : function(cmp, rec, opt, modified){
                        this.markDirty(true);
                        this.verifyRows([rec], modified);
                    }, scope : this},
                    datachanged : {fn : function(){this.markDirty(true);}, scope : this}
                }
            },
            columns: [
                {text: 'Status', dataIndex: 'status', minWidth : 150, flex : 1},
                {text: 'Customer Barcode',  dataIndex: 'CustomerBarcode', width : 150, editor: {xtype: 'textfield'}},
                {text: 'Last Name', dataIndex: 'LastName', editor: {xtype: 'textfield'}},
                {text: 'First Name', dataIndex: 'FirstName', editor: {xtype: 'textfield'}},
                {text: 'Date of Birth', dataIndex: 'BirthDate', width : 150, editor : {xtype: 'datefield'}},
                {text: 'FMP', dataIndex: 'FMPId',
                    editor : {
                        xtype : 'combo',
                        store : {
                            model   : 'LABKEY.HDRL.SpecimenLK',
                            autoLoad: true,
                            proxy : {
                                type : 'ajax',
                                url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                                extraParams : {
                                    schemaName  : 'hdrl',
                                    queryName   : 'familyMemberPrefix'
                                },
                                reader : { type : 'json', root : 'rows' }
                            },
                            listeners : {
                                load : {fn: function(cmp, recs){this.createDisplayValueMap('FMPId', recs, 'RowId', 'Code');}, scope : this}
                            }
                        },
                        valueField      : 'Code',
                        displayField    : 'Code',
                        editable        : false
                    }
                },
                {text: 'SSN', dataIndex: 'SSN', width : 150, editor: {xtype: 'textfield'}, renderer : this.SSNRenderer, scope : this},
                {text: 'DoD ID', dataIndex: 'DODId', editor: {xtype: 'numberfield'}},
                {text: 'DUC', dataIndex: 'DutyCodeId',
                    editor : {
                        xtype : 'combo',
                        store : {
                            model   : 'LABKEY.HDRL.SpecimenLK',
                            autoLoad: true,
                            proxy : {
                                type : 'ajax',
                                url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                                extraParams : {
                                    schemaName  : 'hdrl',
                                    queryName   : 'dutyCode'
                                },
                                reader : { type : 'json', root : 'rows' }
                            },
                            listeners : {
                                load : {fn: function(cmp, recs){this.createDisplayValueMap('DutyCodeId', recs, 'RowId', 'Code');}, scope : this}
                            }
                        },
                        valueField      : 'Code',
                        displayField    : 'Code',
                        editable        : false
                    }
                },
                {text: 'SOT', dataIndex: 'TestingSourceId',
                    editor : {
                        xtype : 'combo',
                        store : {
                            model   : 'LABKEY.HDRL.SpecimenLK',
                            autoLoad: true,
                            proxy : {
                                type : 'ajax',
                                url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                                extraParams : {
                                    schemaName  : 'hdrl',
                                    queryName   : 'sourceOfTesting'
                                },
                                reader : { type : 'json', root : 'rows' }
                            },
                            listeners : {
                                load : {fn: function(cmp, recs){this.createDisplayValueMap('TestingSourceId', recs, 'RowId', 'Code');}, scope : this}
                            }
                        },
                        valueField      : 'Code',
                        displayField    : 'Code',
                        editable        : false
                    }
                },
                {text: 'Date of Draw', dataIndex: 'DrawDate', width : 150, editor : {xtype : 'datefield'}}
            ],
            dockedItems : [{
                xtype   : 'toolbar',
                doc     : 'top',
                items   : [
                    {xtype : 'button', text : 'delete', handler : function(){
                        var sm = this.grid.getSelectionModel();
                        this.roweditor.cancelEdit();

                        this.grid.getStore().remove(sm.getSelection());
                        if (this.grid.getStore().getCount() > 0) {
                            sm.select(0);
                        }
                    }, scope : this},
                    {xtype : 'button', text : 'add specimen', handler : function() {
                        this.roweditor.cancelEdit();

                        // Create a model instance
                        var r = Ext4.create('LABKEY.HDRL.Specimen', {InboundRequestId : this.requestId});

                        this.grid.getStore().insert(0, r);
                        this.roweditor.startEdit(0, 2);
                    }, scope : this},
                ]
            }],
            plugins : [this.roweditor]
        });

        items.push(this.grid);

        return Ext4.create('Ext.panel.Panel', {
            border      : true,
            minHeight   : 150,
            flex        : 1.2,
            layout      : 'fit',
            items       : items
        });
    },

    createNorthPanel : function() {

        var formItems = [];

        formItems.push({
            xtype       : 'displayfield',
            value       : '<strong>Test Request</strong><br/>' +
            'Edit existing specimens or add specimens to this test request. Click on the "ADD SPECIMEN" button to add a new specimen record. ' +
            'You can edit existing records by clicking directly on the row in the grid to activate the row editing tool.</p>'
        });

        formItems.push({
            xtype           : 'combo',
            fieldLabel      : 'Request Type',
            name            : 'requestType',
            itemId          : 'requestTypeCombo',
            labelSeparator  : '',
            store : {
                model   : 'LABKEY.HDRL.Lookup',
                autoLoad : true,
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : {
                        schemaName  : 'hdrl',
                        queryName   : 'testType',
                        sort        : 'name'
                    },
                    reader : { type : 'json', root : 'rows' }
                },
                listeners : {
                    load : {fn: function(cmp, recs){
                        if (this.testTypeId){
                            this.northPanel.getComponent('requestTypeCombo').setValue(this.testTypeId);
                        }
                    }, scope : this}
                }
            },
            listeners : {
                scope       : this,
                change      : function(cmp, value) {
                    if (this.testTypeId != value)
                        this.markDirty(true);
                    this.requestType = value;
                }
            },
            valueField      : 'RowId',
            displayField    : 'Name',
            allowBlank      : false,
            editable        : false
        });

        return Ext4.create('Ext.form.Panel', {
            bodyPadding : 20,
            border      : false,
            flex        : 1.2,
            items       : formItems,
            defaults    : {
                width       : 550,
                labelWidth  : 150
            },
            bodyStyle   : {background  : 'transparent'}
        });
    },

    createSouthPanel : function() {

        var formItems = [];

        formItems.push({xtype: 'hidden', name: 'X-LABKEY-CSRF', value: LABKEY.CSRF});

        formItems.push({
            xtype       : 'fieldcontainer',
            fieldLabel  : 'Upload Request Details',
            width       : 650,
            layout      : 'hbox',
            items       : [
                {
                    xtype       : 'fileuploadfield',
                    name        : 'file',
                    width       : 300,
                    listeners   : {
                        change : {fn: function(cmp, value){
                            this.southPanel.getComponent('specimenUploadButton').enable();

                        }, scope : this}
                    }
                },{
                    xtype       : 'button',
                    text        : 'download template',
                    width       : 175,
                    margin      : '0 0 0 10',
                    scope       : this,
                    handler     : function(){
                        var form = new Ext4.form.BasicForm(this.southPanel);
                        form.submit({
                            url     : LABKEY.ActionURL.buildURL('hdrl', 'downloadSpecimenTemplate')
                        });
                    }
                }
            ]
        })

        formItems.push({
            xtype       : 'button',
            itemId      : 'specimenUploadButton',
            text        : 'upload file',
            width       : 150,
            disabled    : true,
            scope       : this,
            handler     : function(){
                var form = new Ext4.form.BasicForm(this.southPanel);

                var processResponse = function(form, action) {

                    var fileContents = Ext4.decode(action.response.responseText);

                    // unable to parse the file
                    if (!fileContents.sheets && fileContents.exception){
                        Ext4.Msg.show({title: 'Error', msg: fileContents.exception, buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                        return;
                    }

                    var data = fileContents.sheets[0].data;

                    // create the model records and load them into the store
                    var newRecords = [];
                    var validatedRecords = [];
                    var cols = [];

                    // need to convert the parsed column names so they match our model names (case sensitive)
                    Ext4.each(data[0], function(field){

                        var key = field.replace(/\s+/g, '').toLowerCase();
                        if (this.specimenModelMap[key])
                            cols.push(this.specimenModelMap[key]);
                        else
                            cols.push(undefined);
                    }, this);

                    for (var i = 1; i < data.length; i++){

                        var row = data[i];
                        var rec = Ext4.create('LABKEY.HDRL.Specimen');
                        for (var j=0; j< cols.length; j++){

                            if (cols[j])
                                rec.set(cols[j], row[j] ? new String(row[j]) : row[j]);
                        }
                        newRecords.push(rec);
                    }

                    this.verifyRows(newRecords);
                    this.grid.getStore().loadData(newRecords, true);
                };

                form.submit({
                    url     : LABKEY.ActionURL.buildURL('experiment', 'parseFile'),
                    scope   : this,
                    success : processResponse,
                    failure : processResponse
                });

            }
        })

        formItems.push({
            xtype       : 'displayfield',
            value       : '<p/><strong>Shipping Information</strong><p/>'
        });

        formItems.push({
            xtype           : 'combo',
            fieldLabel      : 'Carrier',
            name            : 'shippingCarrier',
            itemId          : 'shippingCarrierCombo',
            labelSeparator  : '',
            store : {
                model   : 'LABKEY.HDRL.Lookup',
                autoLoad : true,
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : {
                        schemaName  : 'hdrl',
                        queryName   : 'shippingCarrier',
                        sort        : 'name'
                    },
                    reader : { type : 'json', root : 'rows' }
                },
                listeners : {
                    load : {fn: function(cmp, recs){
                        if (this.shippingCarrierId){
                            var c = this.southPanel.getComponent('shippingCarrierCombo');
                            c.setValue(this.shippingCarrierId);
                        }
                    }, scope : this}
                }
            },
            listeners : {
                scope       : this,
                change      : function(cmp, value) {
                    if (this.shippingCarrierId != value)
                        this.markDirty(true);
                    this.shippingCarrier = value;
                }
            },
            valueField      : 'RowId',
            displayField    : 'Name',
            editable        : false
        });

        formItems.push({
            xtype           : 'textfield',
            fieldLabel      : 'Tracking #',
            name            : 'trackingNumber',
            labelSeparator  : '',
            value           : this.shippingNumber,
            listeners : {
                scope: this,
                change : function(cmp, value) {
                    if (this.shippingNumber != value)
                        this.markDirty(true);
                    this.trackingNumber = value;
                }
            }
        });

        return Ext4.create('Ext.form.Panel', {
            bodyPadding : 20,
            border      : false,
            flex        : 1,
            items       : formItems,
            defaults    : {
                width       : 550,
                labelWidth  : 150
            },
            bodyStyle   : {background  : 'transparent'},
            buttonAlign : 'left',
            buttons     : [{
                text    : 'Submit Requests',
                formBind: true,
                scope   : this,
                handler : this.handleSubmit
            },{
                text    : 'Save',
                formBind: true,
                scope   : this,
                handler : function(){this.handleSave(1);}
            },{
                text    : 'Cancel',
                scope   : this,
                handler : function(){
                    this.resetDirty();
                    window.location = LABKEY.ActionURL.buildURL('hdrl', 'begin.view');
                }
            }]
        });
    },

    handleSubmit : function() {

        var form = this.down('form').getForm();
        if (form.isValid()) {

            // validate the store before we save
            var records = [];
            for (var i=0; i < this.grid.getStore().getCount(); i++){
                records.push(this.grid.getStore().getAt(i));
            }

            this.verifyRows(records, null, function(success){

                if (success) {
                    this.handleSave(2);
                }
                else {
                    Ext4.Msg.show({title: 'Error', msg: 'Unable to submit the request. There are invalid specimen records.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                }
            });
        }
        else {
            Ext4.Msg.show({title: "Error", msg: 'Please enter all required fields.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
        }
    },

    handleSave : function(requestStatusId) {

        var form = this.down('form').getForm();
        var fn = this.requestId != -1 ? LABKEY.Query.updateRows : LABKEY.Query.insertRows;
        if (form.isValid()) {

            this.roweditor.completeEdit();
            var row = {requestStatusId : requestStatusId, shippingCarrierId : this.shippingCarrier, testTypeId : this.requestType, shippingNumber : this.trackingNumber};
            if (this.requestId != -1){
                row.requestId = this.requestId;
            }

            var extraContext = null;
            if (requestStatusId == 1){
                extraContext = {validationMode : 'OFF'};
            } else if (requestStatusId == 2) {
                extraContext = {validationMode : 'WITH_UQ'};
            }

            // handle inserts & updates
            fn({
                schemaName  : 'hdrl',
                queryName   : 'inboundRequest',
                rows        : [row],
                scope       : this,
                success     : function(res) {
                    if (res.rows && res.rows.length === 1)
                    {
                        this.requestId = res.rows[0].requestid;

                        // save any specimen requests
                        var insertedRows = [];
                        var updatedRows = [];
                        var deletedRows = [];

                        Ext4.each(this.grid.getStore().getModifiedRecords(), function(rec){

                            var row = this.prepareRow(rec.data);
                            if (!row.RowId){
                                row.RowId = null;
                                insertedRows.push(row);
                            }
                            else {
                                updatedRows.push(row);
                            }
                        }, this);

                        Ext4.each(this.grid.getStore().getRemovedRecords(), function(rec){

                            var row = this.prepareRow(rec.data);
                            if (row.RowId){
                                deletedRows.push(row);
                            }
                        }, this);

                        var multi = new LABKEY.MultiRequest();
                        this.error = false;

                        if (insertedRows.length > 0){
                            multi.add(LABKEY.Query.insertRows, {
                                schemaName  : 'hdrl',
                                queryName   : 'inboundSpecimen',
                                rows        : insertedRows,
                                extraContext: extraContext,
                                scope       : this,
                                success     : function(res) {},
                                failure     : function(res){
                                    this.error = true;
                                    var message = 'An error saving the test request.';
                                    if (res.exception)
                                        message = res.exception;
                                    Ext4.Msg.show({title: "Error", msg: message, buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                }
                            }, this);
                        }

                        if (updatedRows.length > 0){
                            multi.add(LABKEY.Query.updateRows, {
                                schemaName  : 'hdrl',
                                queryName   : 'inboundSpecimen',
                                rows        : updatedRows,
                                extraContext: extraContext,
                                scope       : this,
                                success     : function(res) {},
                                failure     : function(res){
                                    this.error = true;
                                    var message = 'An error saving the test request.';
                                    if (res.exception)
                                        message = res.exception;
                                    Ext4.Msg.show({title: "Error", msg: message, buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                }
                            }, this);
                        }

                        if (deletedRows.length > 0){
                            multi.add(LABKEY.Query.deleteRows, {
                                schemaName  : 'hdrl',
                                queryName   : 'inboundSpecimen',
                                rows        : deletedRows,
                                scope       : this,
                                success     : function(res) {},
                                failure     : function(res){
                                    this.error = true;
                                    var message = 'An error saving the test request.';
                                    if (res.exception)
                                        message = res.exception;
                                    Ext4.Msg.show({title: "Error", msg: message, buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                                }
                            }, this);
                        }

                        // perform the updates
                        if (insertedRows.length > 0 || updatedRows.length > 0 || deletedRows.length > 0){
                            multi.send(function(){
                                if (!this.error) {
                                    this.resetDirty();
                                    window.location = LABKEY.ActionURL.buildURL('hdrl', 'begin.view');
                                }
                            }, this);
                        }
                        else{
                            this.resetDirty();
                            window.location = LABKEY.ActionURL.buildURL('hdrl', 'begin.view');
                        }
                    }
                },
                failure     : function(){
                    Ext4.Msg.show({title: "Error", msg: 'An error saving the test request.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                }
            });
        }
        else {
            Ext4.Msg.show({title: "Error", msg: 'Please enter all required fields.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
        }
    },

    /**
     * Update the status field based on the server validation of the record
     */
    verifyRows : function(rows, modifiedFields, callback){

        if (modifiedFields && (modifiedFields.length == 0 || (modifiedFields.length ==1 && modifiedFields[0] == 'status'))) {

            if (callback)
                callback.call(this, true);
            else
                return;
        }

        var ret = true;
        var data = [];
        Ext4.each(rows, function(rec){
            data.push(this.prepareRow(rec.copy().data));
        }, this);

        LABKEY.Ajax.request({
            url : LABKEY.ActionURL.buildURL("hdrl", "verifySpecimen.api"),
            method : 'POST',
            scope : this,
            jsonData: {
                rows : data
            },
            success : function(res) {
                var o = Ext4.decode(res.responseText);

                for (var i=0; i < o.rows.length; i++){
                    if (o.rows[i].ValidationStatus)
                        ret = false;
                    rows[i].set('status', o.rows[i].ValidationStatus);
                }

                if (callback)
                    callback.call(this, ret);
            }
        });
    },

    /**
     * Prepares the client side row for inserting or updating to the server
     * @private
     */
    prepareRow : function(row){

        // convert any lookup display values back to keys
        for (var key in row){
            if (row.hasOwnProperty(key)){

                if (this.lkMap[key]){
                    row[key] = this.lkMap[key][row[key]];
                }
/*
                else if ('SSN' === key){
                    // remove hyphens and whitespace from the SSN
                    row[key] = row[key].replace(/-|\s+/g, '');
                }
*/
            }
        }

        row.InboundRequestId = this.requestId;
        return row;
    },

    /**
     * Need to build the map from display value to key for the combo editors so that we can translate the store records
     * at insert or update time.
     * @private
     */
    createDisplayValueMap : function(colName, recs, keyName, valueName) {

        var map = this.lkMap[colName] || {};
        Ext4.each(recs, function(rec){

            var data = rec.data;
            if (data[keyName] && data[valueName]) {
                map[data[valueName]] = data[keyName];
            }
        });

        this.lkMap[colName] = map;
    },

    markDirty : function(dirty) {
        this.dirty = dirty;
    },

    resetDirty : function() {
        this.dirty = false;
    },

    isDirty : function() {
        return this.dirty;
    },

    beforeUnload : function() {
        if (this.isDirty()) {
            return 'please save your changes';
        }
    },

    /**
     * grid column renderer for SSN's
     * @private
     */
    SSNRenderer : function(value, meta, rec, idx){

        if (value.indexOf('-') == -1 && value.length == 9){
            return value.substr(0,3) + '-' + value.substr(3,2) + '-' + value.substr(5,4);
        }
        return value;
    }
});
