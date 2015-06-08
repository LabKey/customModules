/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext4.QuickTips.init();

/**
 * Abstracts a shelf, rack intersction in a freezer. This represents a series of boxes per drawer with
 * boxes as columns and drawers as rows.
 */
Ext4.define('LABKEY.ext4.ShelfRackPanel', {

    extend: 'Ext.panel.Panel',

    border: false,

    alias: 'widget.labkey-shelf-rack-panel',

    itemSelector: 'table.tr',

    padding: 10,

    header : false,

    constructor: function (config)
    {
        this.callParent([config]);

        this.tooltipTpl = new Ext4.XTemplate(
                '<b>Freezer</b>&nbsp;{freezer}<br>',
                '<b>Rack</b>&nbsp;{rack}<br>',
                '<b>Shelf</b>&nbsp;{shelf}<br>',
                '<b>Drawer</b>&nbsp;{drawer}<br>',
                '<b>Box</b>&nbsp;{box}<br>'
        );

        this.summaryTpl = new Ext4.XTemplate(
                '<table>',
                    '<tr><td><b>Box Label</b></td><td>{boxLabel}</td></tr>',
                    '<tr><td><b>Freezer</b></td><td>{freezer}</td></tr>',
                    '<tr><td><b>Rack</b></td><td>{rack}</td></tr>',
                    '<tr><td><b>Shelf</b></td><td>{shelf}</td></tr>',
                    '<tr><td><b>Drawer</b></td><td>{drawer}</td></tr>',
                    '<tr><td><b>Box</b></td><td>{box}</td></tr>',
                    '<tr><td></td></tr>',
                    '<tr><td><b>Available Vials</b></td><td>{available}</td></tr>',
                    '<tr><td><b>Used</b></td><td>{used}</td></tr>',
                    '<tr><td><b>Checked Out</b></td><td>{checkedOut}</td></tr>',
                '</table>'
        );

        this.tpl = new Ext4.XTemplate(
            '<table style="border: none" class="detailstatus">',
                '<tpl for=".">',
                    '<tr class="status-row" >',
                    '<td style="background-color: #73a0e2"><div style="padding: 5px;">{name}</div></td>',
                    '<tpl for="this.getBoxes(this)">',
                    '<td style="background-color: #E0E0DD;border: solid 1px #DDDDDD;">',
                        '<div style="padding:5px;min-height: 60px;width: 85px;overflow: hidden" class="freezer-box-div" box-id="{rowId}" drawer-id="{parent.rowId}" data-qtip="{[this.getLocationTip(this, values, parent)]}">{[this.getBoxValue(this, values, parent)]}</div>',
                    '</td>',
                    '</tpl>',
                    '</tr>',
                '</tpl>',
            '</table>',
            {
                getBoxes : function(cmp) {
                    return cmp.initialConfig.me.getBoxes();
                },

                getLocationTip : function(cmp, box, drawer) {
                    var me = cmp.initialConfig.me;
                    return me.tooltipTpl.apply({freezer : me.freezer.name, rack : me.rack.name, shelf : me.shelf.name, drawer : drawer['name'], box : box['name']});
                },

                getRack : function(cmp) {
                    return cmp.initialConfig.me.rack['rowId'];
                },

                getBoxValue : function(cmp, box, drawer) {
                    return cmp.initialConfig.me.getBoxLabel(box, drawer);

                },
                me : this
            }
        );

        this.data = this.drawers;
    },

    initComponent: function ()
    {
        this.on('render', this.registerClickHandlers, this);

        this.callParent(arguments);
    },

    getBoxLabel : function(box, drawer) {

        var peptides = [];
        var boxIdentifier = this.getBoxIdentifier({
            freezer: this.freezer['rowId'],
            rack: this.rack['rowId'],
            shelf: this.shelf['rowId'],
            drawer: drawer['rowId'],
            box: box['rowId']
        });

        var rec = this.locationStore.findRecord('identifier', boxIdentifier);
        if (rec) {

            return '<a href="javascript:void(0)">' + rec.data.label + '</a>';
        }
        else {

            this.vialStore.each(function(rec) {

                if ((rec.get('rack') == this.rack['rowId']) &&
                        (rec.get('shelf') == this.shelf['rowId']) &&
                        (rec.get('box') == box['rowId']) &&
                        (rec.get('drawer') == drawer['rowId'])) {

                    // omit vials that are checked out or used
                    if (!rec.get('checkedOut') && !rec.get('used'))
                        peptides.push(rec);
                }
            }, this);

            if (peptides.length > 0) {

                var filterArray = [
                    LABKEY.Filter.create('freezer', this.freezer['rowId']),
                    LABKEY.Filter.create('rack', this.rack['rowId']),
                    LABKEY.Filter.create('shelf', this.shelf['rowId']),
                    LABKEY.Filter.create('drawer', drawer['rowId']),
                    LABKEY.Filter.create('box', box['rowId'])
                ];
                var params = LABKEY.Query.buildQueryParams('peptideinventory', 'vial', filterArray);
//            return '<a href="' + LABKEY.ActionURL.buildURL('query', 'executeQuery', null, params) + '">' + peptides.length + ' peptide(s)</a>';
                return '<a href="javascript:void(0)">' + peptides.length + ' peptide(s)</a>';
            }
            return '&nbsp;';
        }
    },

    redrawPanel : function() {

        // update the template with existing data
        this.update(this.drawers);
        this.registerClickHandlers();
    },

    registerClickHandlers : function() {

        var el = this.getEl();
        var boxes = el.query('div.freezer-box-div');

        for (i = 0; i < boxes.length; i++) {
            var box = Ext.get(boxes[i]);
            if (box.dom.innerHTML !== '&nbsp;')
                box.on('click', this.onBoxClick, this, {id: box.id});
        }
    },

    /**
     * Dialog to update vial used and checked out status
     */
    onBoxClick : function(event, target, opt)
    {
        var box = Ext.get(opt.id);
        if (box)
        {
            var freezerId = this.freezer['rowId'];
            var rackId = this.rack['rowId'];
            var shelfId = this.shelf['rowId'];
            var drawerId = box.getAttribute('drawer-id');
            var boxId = box.getAttribute('box-id');

            var id = this.getBoxIdentifier({
                freezer: freezerId,
                rack: rackId,
                shelf: shelfId,
                drawer: drawerId,
                box: boxId
            });
            this.showBoxDialog(boxId, drawerId, id);
        }
    },

    /**
     * Dialog to update vial used and checked out status
     */
    showBoxDialog : function(boxId, drawerId, locationId) {

        var boxRec;
        var drawerRec;
        var locationRec = this.locationStore.findRecord('identifier', locationId);

        for (var i=0; i < this.boxes.length; i++) {
            if (this.boxes[i].rowId == boxId) {
                boxRec = this.boxes[i];
                break;
            }
        }
        for (i=0; i < this.drawers.length; i++) {
            if (this.drawers[i].rowId == drawerId) {
                drawerRec = this.drawers[i];
                break;
            }
        }

        var peptides = [];
        this.vialStatus = {};

        // get the peptides that match the selected location
        this.vialStore.each(function(rec) {

            if ((rec.get('rack') == this.rack.rowId) &&
                (rec.get('shelf') == this.shelf.rowId) &&
                (rec.get('box') == boxId) &&
                (rec.get('drawer') == drawerId)) {

                peptides.push(rec.data);
            }
        }, this);

        this.boxLabel = (locationRec) ? locationRec.data.label : undefined;

        var formItems = [];
        formItems.push({
            xtype       : 'textfield',
            fieldLabel  : 'Box Label',
            name        : 'boxLabel',
            value       : this.boxLabel,
            width       : 100,
            listeners : {
                scope: this,
                change : function(cmp, value) {
                    this.boxLabel = value;
                }
            }
        });

        if (peptides.length > 0) {

            formItems.push({
                xtype       : 'displayfield',
                value       : 'Change the status for each peptide by selecting one of the options available.'
            });

            var statusStore = Ext4.create('Ext.data.Store', {
                fields: ['value', 'label'],
                data: [
                    {value: 'available', label: 'Available'},
                    {value: 'checkedOut', label: 'Checked Out'},
                    {value: 'used', label: 'Used'}
                ]
            });

            peptides = Ext4.Array.sort(peptides, function(a, b){
                return a.peptideId < b.peptideId ? -1 :
                                a.peptideId == b.peptideId ? 0 : 1;
            });

            var vialCount=0, checkedOut=0, used=0;

            Ext4.each(peptides, function(rec){

                vialCount++;
                if (rec.used)
                    used++;
                else if (rec.checkedOut)
                    checkedOut++;

                var label = "" + rec.peptideId;
                if (rec.rcPoolId != -1) {
                    label = rec.rcLotNumber + "-" + rec.slot;
                }
                formItems.push({
                    xtype       : 'combo',
                    fieldLabel  : label,
                    name        : "" + rec.peptideId,
                    store       : statusStore,
                    editable    : false,
                    forceSelection : true,
                    typeAhead   : false,
                    value       : rec.used ? 'used' : (rec.checkedOut ? 'checkedOut' : 'available'),
                    queryMode      : 'local',
                    displayField   : 'label',
                    valueField     : 'value',
                    emptyText      : 'Status',
                    listeners : {
                        scope: this,
                        change : function(cmp, value) {
                            this.vialStatus[cmp.name] = value;
                        }
                    }
                });
            }, this);
        }

        // first tab in the dialog
        var infoItems = [];
        infoItems.push({
                    xtype       : 'displayfield',
                    fieldLabel  : '',
                    value       : this.summaryTpl.apply({
                        boxLabel    : this.boxLabel ? this.boxLabel : 'default',
                        freezer     : this.freezer.name,
                        rack        : this.rack.name,
                        shelf       : this.shelf.name,
                        drawer      : drawerRec.name,
                        box         : boxRec.name,
                        available   : vialCount - used - checkedOut,
                        used        : used,
                        checkedOut  : checkedOut
                    })
                }
        );

        var infoTab = {
            title       : 'Summary',
            xtype       : 'form',
            items       : infoItems,
            border      : false
        };

        // create the second tab
        var editTab = {
            title       : 'Edit Vials',
            xtype       : 'form',
            items       : formItems,
            autoScroll  : true,
            border      : false,
            layout      : {
                type  : 'vbox',
                align : 'stretch'
            }
        };

        var dialog = Ext4.create('Ext.window.Window', {
            width   : 500,
            height  : 500,
            layout  : 'fit',
            draggable   : false,
            modal       : true,
            title  : 'Update Status for Vials',
            defaults: {
                border  : false,
                frame   : false,
                bodyPadding : 20
            },
            items   : [{
                xtype : 'tabpanel',
                items : [infoTab, editTab]
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
                        Ext4.each(peptides, function(rec){
                            var id = rec.peptideId;

                            if (this.vialStatus[id]) {

                                var row = {
                                    peptideId   : id,
                                    container   : LABKEY.container.id,
                                    checkedOut  : false,
                                    used        : false,
                                    rcPoolId    : rec.rcPoolId
                                };

                                var status = this.vialStatus[id];

                                if (status == 'checkedOut') {
                                    row.checkedOut = true;
                                }
                                else if (status == 'used'){
                                    row.checkedOut = true;
                                    row.used = true;
                                }

                                updatedRows.push(row);
                            }
                        }, this);

                        if (updatedRows.length > 0) {

                            LABKEY.Query.updateRows({
                                schemaName  : 'peptideInventory',
                                queryName   : 'vial',
                                rows        : updatedRows,
                                scope       : this,
                                success     : function() {
                                    var msgbox = Ext4.create('Ext.window.Window', {
                                        title    : 'Success',
                                        modal    : false,
                                        closable : false,
                                        border   : false,
                                        html     : '<div style="padding: 15px;"><span class="labkey-message">Vials successfully updated</span></div>'
                                    });
                                    msgbox.show();
                                    msgbox.getEl().fadeOut({duration : 3000, callback : function(){ msgbox.close(); }});
                                    this.vialStore.on('load', function(){this.redrawPanel();}, this, {single : true});
                                    this.vialStore.reload();
                                }
                            });
                        }

                        if (this.boxLabel) {

                            if (locationRec) {

                                LABKEY.Query.updateRows({
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'boxLocation',
                                    rows        : [{container : locationRec.data.container, identifier : locationId, label : this.boxLabel}],
                                    scope       : this,
                                    success     : function() {
                                        // need to redraw the panel to reflect the new location information
                                        this.locationStore.on('load', function(){this.redrawPanel();}, this, {single : true});
                                        this.locationStore.reload();
                                    }
                                });
                            }
                            else {

                                LABKEY.Query.insertRows({
                                    schemaName  : 'peptideInventory',
                                    queryName   : 'boxLocation',
                                    rows        : [{identifier : locationId, label : this.boxLabel}],
                                    scope       : this,
                                    success     : function() {
                                        this.locationStore.on('load', function(){this.redrawPanel();}, this, {single : true});
                                        this.locationStore.reload();
                                    }
                                });
                            }
                        }
                        dialog.close();
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

    getBoxIdentifier : function(cfg){
        return cfg.freezer + ':' + cfg.rack + ':' + cfg.shelf + ':' + cfg.drawer + ':' + cfg.box;
    },

    getBoxes : function() {
        return this.boxes;
    }
});

Ext4.define('LABKEY.ext4.FreezerPanel', {

    extend: 'Ext.panel.Panel',

    alias: 'widget.labkey-freezer-panel',

    border: false,

    layout  : {
        type : 'table',
        columns : 4
    },

    constructor: function (config)
    {
        this.callParent([config]);
        this.layout.columns = this.racks.length;
    },

    initComponent: function ()
    {
        this.callParent(arguments);
        var items = [];

        var selectSQL = 'SELECT v.peptideId, v.freezer, v.shelf, v.rack, v.drawer, v.box, ' +
                'v.slot, v.used, v.checkedOut, v.rcPoolId, rc.lotNumber AS rcLotNumber ' +
                'FROM vial v LEFT JOIN rcPool rc ON v.rcPoolId = rc.rowId ' +
                'WHERE v.freezer = ' + this.freezer.rowId;

        Ext4.define('Freezer.Vial', {
            extend: 'Ext.data.Model',
            proxy : {
                type : 'ajax',
                url : LABKEY.ActionURL.buildURL('query', 'executeSql.api'),
                extraParams : {
                    schemaName  : 'peptideinventory',
                    sql         : selectSQL,
                    sort        : 'proteinCategory'
                },
                reader : { type : 'json', root : 'rows' }
            },
            fields : [
                {name: 'peptideId'},
                {name: 'freezer'},
                {name: 'shelf'},
                {name: 'rack'},
                {name: 'drawer'},
                {name: 'box'},
                {name: 'slot'},
                {name: 'checkedOut',    type : 'boolean'},
                {name: 'used',          type : 'boolean'},
                {name: 'rcPoolId'},
                {name: 'rcLotNumber'}
            ]
        });
        var vialStore = Ext4.create('Ext.data.Store', {
            model   : 'Freezer.Vial',
            pageSize: 5000,
            autoLoad: true
        });

        vialStore.on('load', function(){
            // create the shelves and racks
            Ext4.each(this.shelves, function(shelf){

                Ext4.each(this.racks, function(rack){

                    items.push({
                        xtype   : 'labkey-shelf-rack-panel',
                        freezer : this.freezer,
                        rack    : rack,
                        shelf   : shelf,
                        vialStore   : vialStore,
                        drawers     : this.drawers,
                        boxes       : this.boxes,
                        locationStore : this.locationStore
                    });
                }, this);
            }, this);

            this.add(items);
        }, this, {single : true});
    }
});


Ext4.define('LABKEY.ext4.FreezerDiagramPanel', {

    extend: 'Ext.tab.Panel',

    alias: 'widget.labkey-freezer-diagram-panel',

    border: false,

    shrinkWrap: 3, // both width & height depend on content

    constructor: function (config)
    {
        this.callParent([config]);
    },

    initComponent: function ()
    {
        this.callParent(arguments);

        Ext4.define('Freezer.BoxLocation', {
            extend: 'Ext.data.Model',
            proxy : {
                type : 'ajax',
                url : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                extraParams : {schemaName : 'peptideinventory', queryName : 'boxLocation'},
                reader : { type : 'json', root : 'rows' }
            },
            fields : [
                {name: 'identifier'},
                {name: 'container'},
                {name: 'label'}
            ]
        });
        this.boxLocationStore = Ext4.create('Ext.data.Store', {
            model   : 'Freezer.BoxLocation',
            pageSize: 5000,
            autoLoad: true
        });
        this.boxLocationStore.on('load', this.createFreezers, this, {single : true});
    },

    createFreezers : function() {

        // get information for shelves, racks, drawers and boxes
        this.getShelves(this.getRacks(this.getDrawers(this.getBoxes(function(){

            // we should have all the drawer and box data, now create the freezer tabs
            LABKEY.Query.selectRows({
                schemaName  : 'peptideinventory',
                queryName   : 'freezer',
                columns     : 'RowId,Name',
                sort        : 'Name',
                success     : function(data) {
                    var me = this;
                    var freezers = [];
                    Ext4.each(data.rows, function(row){

                        freezers.push(new LABKEY.ext4.FreezerPanel({
                            title       : row['name'],
                            freezer     : row,
                            shelves     : this.shelves,
                            racks       : this.racks,
                            drawers     : this.drawers,
                            boxes       : this.boxes,
                            locationStore : this.boxLocationStore,
                            autoScroll  : true
                        }));

                    }, me);
                    this.add(freezers);
                    this.setActiveTab(0);
                },
                scope: this
            });
        }))));
    },

    getShelves : function(callback) {

        LABKEY.Query.selectRows({
            schemaName  : 'peptideinventory',
            queryName   : 'shelf',
            columns     : 'RowId,Name',
            sort        : 'Name',
            success     : function(data) {
                this.shelves = data.rows;
                if (callback)
                    callback.call(this);
            },
            scope: this
        });
    },

    getRacks : function(callback) {

        LABKEY.Query.selectRows({
            schemaName  : 'peptideinventory',
            queryName   : 'rack',
            columns     : 'RowId,Name',
            sort        : 'Name',
            success     : function(data) {
                this.racks = data.rows;
                if (callback)
                    callback.call(this);
            },
            scope: this
        });
    },

    /**
     * Get the set of drawers for this module
     */
    getDrawers : function(callback) {

        LABKEY.Query.selectRows({
            schemaName  : 'peptideinventory',
            queryName   : 'drawer',
            columns     : 'RowId,Name',
            sort        : 'Name',
            success     : function(data) {
                this.drawers = data.rows;
                if (callback)
                    callback.call(this);
            },
            scope: this
        });
    },

    getBoxes : function(callback) {

        LABKEY.Query.selectRows({
            schemaName  : 'peptideinventory',
            queryName   : 'box',
            columns     : 'RowId,Name',
            sort        : 'Name',
            success     : function(data) {
                this.boxes = data.rows;
                if (callback)
                    callback.call(this);
            },
            scope: this
        });
    }
});
