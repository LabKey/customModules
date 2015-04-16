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
            border    : false
        });

        this.createDataModels();
        this.callParent([config]);
    },

    createDataModels : function() {

        if (!Ext4.ModelManager.isRegistered('LABKEY.HDRL.Specimen')) {
            Ext4.define('LABKEY.HDRL.Specimen', {
                extend: 'Ext.data.Model',
                fields : [
                    {name: 'RowId'},
                    {name: 'InboundRequestId'},
                    {name: 'CustomerBarcode', mapping : 'CustomerBarcode.value'},
                    {name: 'LastName', mapping : 'LastName.value'},
                    {name: 'FirstName', mapping : 'FirstName.value'},
                    {name: 'BirthDate', type : 'date', mapping : 'BirthDate.value'},
                    {name: 'SSN', mapping : 'SSN.value'},
                    {name: 'FMPId', mapping : 'FMPId.displayValue'},
                    {name: 'DutyCodeId', mapping : 'DutyCodeId.displayValue'},
                    {name: 'TestingSourceId', mapping : 'TestingSourceId.displayValue'},
                    {name: 'DrawDate', type : 'date', mapping : 'DrawDate.value'},
                    {name: 'status'}
                ]
            });
        }

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

        this.items.push(this.createNorthPanel());
        this.items.push(this.createGridPanel());
        //this.items.push(this.createSouthPanel());

        this.callParent();
    },

    createGridPanel : function() {

        var items = [];

        // the row editing plugin
        this.roweditor = Ext4.create('Ext.grid.plugin.RowEditing', {
            clicksToMoveEditor  : 1,
            autoCancel          : false,
            triggerEvent        : 'cellclick'
        });

        // add the request id to the select rows filter
        var params = LABKEY.Query.buildQueryParams('hdrl', 'inboundSpecimen', [LABKEY.Filter.create('InboundRequestId', this.requestId)]);
        params.apiVersion = 9.1;

        this.grid = Ext4.create('Ext.grid.Panel', {
            title   : '',
            flex    : 1.2,
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
                    update : {fn : function(cmp, rec){console.log('store update : ', rec);}, scope : this}
                }
            },
            columns: [
                { text: 'Customer Barcode',  dataIndex: 'CustomerBarcode', width : 150, editor: {xtype: 'textfield'}},
                { text: 'Last Name', dataIndex: 'LastName', editor: {xtype: 'textfield'}},
                { text: 'First Name', dataIndex: 'FirstName', editor: {xtype: 'textfield'}},
                { text: 'Date of Birth', dataIndex: 'BirthDate', width : 150, editor : {xtype: 'datefield'}},
                { text: 'FMP', dataIndex: 'FMPId', width : 250,
                    editor : {
                        xtype : 'combo',
                        store : {
                            model   : 'LABKEY.HDRL.SpecimenLK',
                            proxy : {
                                type : 'ajax',
                                url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                                extraParams : {
                                    schemaName  : 'hdrl',
                                    queryName   : 'familyMemberPrefix'
                                },
                                reader : { type : 'json', root : 'rows' }
                            }
                        },
                        valueField      : 'Description',
                        displayField    : 'Description',
                        editable        : false
                    }
                },
                { text: 'SSN', dataIndex: 'SSN', width : 150, editor: {xtype: 'textfield'}},
                { text: 'DUC', dataIndex: 'DutyCodeId',
                    editor : {
                        xtype : 'combo',
                        store : {
                            model   : 'LABKEY.HDRL.SpecimenLK',
                            proxy : {
                                type : 'ajax',
                                url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                                extraParams : {
                                    schemaName  : 'hdrl',
                                    queryName   : 'dutyCode'
                                },
                                reader : { type : 'json', root : 'rows' }
                            }
                        },
                        valueField      : 'Code',
                        displayField    : 'Code',
                        editable        : false
                    }
                },
                { text: 'DOT', dataIndex: 'TestingSourceId',
                    editor : {
                        xtype : 'combo',
                        store : {
                            model   : 'LABKEY.HDRL.SpecimenLK',
                            proxy : {
                                type : 'ajax',
                                url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                                extraParams : {
                                    schemaName  : 'hdrl',
                                    queryName   : 'sourceOfTesting'
                                },
                                reader : { type : 'json', root : 'rows' }
                            }
                        },
                        valueField      : 'Code',
                        displayField    : 'Code',
                        editable        : false
                    }
                },
                { text: 'Date of Draw', dataIndex: 'DrawDate', width : 150, editor : {xtype : 'datefield'}},
                { text: 'Status', dataIndex: 'status', flex : 1}
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
                        this.roweditor.startEdit(0, 0);
                    }, scope : this},
                ]
            }],
            plugins : [this.roweditor]
        });

        items.push(this.grid);

        return Ext4.create('Ext.panel.Panel', {
            bodyPadding : 20,
            border      : false,
            height      : 400,
            layout      : 'fit',
            items       : items,
            buttonAlign : 'left',
            buttons     : [{
                text: 'Submit Requests',
                formBind: true,
                handler : function(){Ext4.Msg.show({title: "Error", msg: 'E_NOT_IMPL', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});}
            },{
                text    : 'Save',
                formBind: true,
                scope   : this,
                handler : function(){
                    var form = this.down('form').getForm();
                    if (form.isValid()) {
                        var multi = new LABKEY.MultiRequest();

                        // handle inserts & updates
                        multi.add(LABKEY.Query.insertRows, {
                            schemaName  : 'hdrl',
                            queryName   : 'inboundRequest',
                            rows        : [{requestStatusId : 1, shippingCarrierId : this.shippingCarrier, testTypeId : this.requestType, shippingNumber : this.trackingNumber}],
                            scope       : this,
                            success     : function() {},
                            failure     : function(){
                                Ext4.Msg.show({title: "Error", msg: 'An error saving the test request.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                            }
                        }, this);

                        // perform the updates
                        multi.send(function(){
                        }, this);
                    }
                    else {
                        Ext4.Msg.show({title: "Error", msg: 'Please enter all required fields.', buttons: Ext4.MessageBox.OK, icon: Ext4.MessageBox.ERROR});
                    }
                }
            },{
                text: 'Cancel',
                formBind: true
            }]
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
            labelSeparator  : '',
            store : {
                model   : 'LABKEY.HDRL.Lookup',
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : {
                        schemaName  : 'hdrl',
                        queryName   : 'testType',
                        sort        : 'name'
                    },
                    reader : { type : 'json', root : 'rows' }
                }
            },
            listeners : {
                scope: this,
                change : function(cmp, value) {
                    this.requestType = value;
                }
            },
            valueField      : 'RowId',
            displayField    : 'Name',
            allowBlank      : false,
            editable        : false
        });

        formItems.push({
            xtype           : 'combo',
            fieldLabel      : 'Carrier',
            name            : 'shippingCarrier',
            labelSeparator  : '',
            store : {
                model   : 'LABKEY.HDRL.Lookup',
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : {
                        schemaName  : 'hdrl',
                        queryName   : 'shippingCarrier',
                        sort        : 'name'
                    },
                    reader : { type : 'json', root : 'rows' }
                }
            },
            listeners : {
                scope: this,
                change : function(cmp, value) {
                    this.shippingCarrier = value;
                }
            },
            valueField      : 'RowId',
            displayField    : 'Name',
            allowBlank      : false,
            editable        : false
        });

        formItems.push({
            xtype           : 'textfield',
            fieldLabel      : 'Tracking #',
            name            : 'trackingNumber',
            labelSeparator  : '',
            allowBlank      : false,
            listeners : {
                scope: this,
                change : function(cmp, value) {
                    this.trackingNumber = value;
                }
            }
        });

        return Ext4.create('Ext.form.Panel', {
            bodyPadding : 20,
            border      : false,
            height      : 250,
            items       : formItems,
            defaults    : {
                width : 350
            }
        });
    },

    createSouthPanel : function() {

        var formItems = [];

        formItems.push({
            xtype       : 'displayfield',
            value       : '<strong>Shipping Information</strong></p>'
        });

        formItems.push({
            xtype           : 'combo',
            fieldLabel      : 'Carrier',
            name            : 'shippingCarrier',
            labelSeparator  : '',
            store : {
                model   : 'LABKEY.HDRL.Lookup',
                proxy : {
                    type : 'ajax',
                    url    : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                    extraParams : {
                        schemaName  : 'hdrl',
                        queryName   : 'shippingCarrier',
                        sort        : 'name'
                    },
                    reader : { type : 'json', root : 'rows' }
                }
            },
            valueField      : 'RowId',
            displayField    : 'Name',
            allowBlank      : false,
            editable        : false
        });

        formItems.push({
            xtype           : 'textfield',
            fieldLabel      : 'Tracking #',
            name            : 'trackingNumber',
            labelSeparator  : ''
        });

        return Ext4.create('Ext.form.Panel', {
            bodyPadding : 20,
            border      : false,
            height      : 250,
            items       : formItems,
            defaults    : {
                width : 350
            }
        });
    }
});
