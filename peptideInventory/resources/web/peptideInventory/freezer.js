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

        this.tpl = new Ext4.XTemplate(
            '<table style="border: none" class="detailstatus">',
                '<tpl for=".">',
                    '<tr class="status-row" >',
                    '<td style="background-color: #73a0e2">{name}</td>',
                    '<tpl for="this.getBoxes(this)">',
                    '<td style="background-color: #E0E0DD;border: solid 1px #DDDDDD;min-height: 60px;min-width: 75px" data-qtip="{[this.getLocation(this, values, parent)]}">{[this.getBoxValue(this, values, parent)]}</td>',
                    '</tpl>',
                    '</tr>',
                '</tpl>',
            '</table>',
            {
                getBoxes : function(cmp) {
                    return cmp.initialConfig.me.getBoxes();
                },

                getLocation : function(cmp, box, drawer) {
                    var me = cmp.initialConfig.me;

                    var tip = 'freezer: ' + me.freezer['name'] +
                            ' rack: ' + me.rack['name'] +
                            ' shelf: ' + me.shelf['name'] +
                            ' drawer: ' + drawer['name'] +
                            ' box: ' + box['name'];

                    return tip;
                },

                getRack : function(cmp) {
                    return cmp.initialConfig.me.rack['rowId'];
                },

                getBoxValue : function(cmp, box, drawer) {
                    return cmp.initialConfig.me.getPeptides(box, drawer);

                },
                me : this
            }
        );

        this.data = this.drawers;
    },

    initComponent: function ()
    {
        this.callParent(arguments);
    },

    getBoxes : function() {
        return this.boxes;
    },

    getPeptides : function(box, drawer) {

        var peptides = [];
        this.vialStore.each(function(rec) {

            if ((rec.get('rack') == this.rack['rowId']) &&
                (rec.get('shelf') == this.shelf['rowId']) &&
                (rec.get('box') == box['rowId']) &&
                (rec.get('drawer') == drawer['rowId'])) {

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
            return '<a href="' + LABKEY.ActionURL.buildURL('query', 'executeQuery', null, params) + '">' + peptides.length + ' peptide(s)</a>';
        }
        return '&nbsp;';
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

        var filterArray = [
            LABKEY.Filter.create('freezer', this.freezer['rowId'])
        ];
        var params = LABKEY.Query.buildQueryParams('peptideinventory', 'vial', filterArray);

        Ext4.define('Freezer.Vial', {
            extend: 'Ext.data.Model',
            proxy : {
                type : 'ajax',
                url : LABKEY.ActionURL.buildURL('query', 'selectRows.api'),
                extraParams : params,
                reader : { type : 'json', root : 'rows' }
            },
            fields : [
                {name: 'peptideId'},
                {name: 'freezer'},
                {name: 'shelf'},
                {name: 'rack'},
                {name: 'drawer'},
                {name: 'box'},
                {name: 'slot'}
            ]
        });
        var vialStore = Ext4.create('Ext.data.Store', {
            model   : 'Freezer.Vial',
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
                        boxes       : this.boxes
                    });
                }, this);
            }, this);

            this.add(items);
        }, this);
    }
});


Ext4.define('LABKEY.ext4.InventoryPanel', {

    extend: 'Ext.tab.Panel',

    alias: 'widget.labkey-inventory-panel',

    border: false,

    constructor: function (config)
    {
        this.callParent([config]);
    },

    initComponent: function ()
    {
        this.callParent(arguments);

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
                            boxes       : this.boxes
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
