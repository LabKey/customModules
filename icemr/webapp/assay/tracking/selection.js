/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * Created with IntelliJ IDEA.
 * User: Dax
 * Date: 1/10/13
 * Time: 10:14 AM
 * To change this template use File | Settings | File Templates.
 *
 * This implements the tracking assay "interface" for the adaptation specific
 * behavior.  This file must implement all methods in tracking.js
 */


LABKEY.icemr.tracking.selection = new function() {
    /**
     * Private functions specific to the selection assay
     */
    var selectionFlasks = 'Selection Flasks';

    function fetchCompounds()
    {
        var storeCompounds = new LABKEY.ext.Store({
            schemaName : 'icemr',
            queryName  : 'lk_compound'
        });

        storeCompounds.load({
            callback : function( records, options, success)
            {
                for (var i = 0; i < records.length; i++)
                {
                    var rec = [];
                    rec.push(records[i].data.compound);
                    LABKEY.icemr.flask.compoundOptions.push(rec);
                }
                fetchFlasks();
            }
        });
    }

    function fetchFlasks()
    {
        var flasks = new LABKEY.Exp.SampleSet({name: selectionFlasks});
        flasks.getDomain({
            success : LABKEY.icemr.tracking.onFlasksDomainReady,
            failure : LABKEY.icemr.tracking.onFlasksFailure
        });
    }

    function saveDailyMaintenance(data, response, options)
    {
        if (!data || !(data.rows) || (0 == data.rows.length))
            return;

        var keyMap = LABKEY.icemr.tracking.makeSyncFieldsKeyMap(data.rows[0]);
        LABKEY.icemr.tracking.syncMaterialInputs(data.rows, keyMap);
        LABKEY.icemr.tracking.saveBatch();
    }

    /**
     * Public interface
     */
    return {
        /**
         * return the name of the flask sample set required for the Drug Selection assay
         */
        getFlasksSampleSetName : function () {
            return selectionFlasks;
        },

        /**
         * return the flask sample set specific to selection
         */
        getFlasks : function () {
            // fetch the compounds from our schema and then get the flasks
            fetchCompounds();
        },

        /**
         * return the fields that must be updated for the selection flask
         */
        getSyncFields : function() {
            // we don't have any fields to add yet so just return the base sync fields
            return LABKEY.icemr.flask.syncFields;
        },

        setDefaultValues : function(metaType, config) {
            // nothing to do here for now
        },

        /**
         * upload flask data to the selection sample set
         */
        uploadFlasks: function(flasks, success, failure){
            LABKEY.Query.insertRows( {
                schemaName : 'Samples',
                queryName : this.getFlasksSampleSetName(),
                rows : flasks,
                success : success,
                failure : failure
            });
        },

        /**
         * Save daily maintenance data and update flask sample set.
         */
        saveDaily: function(flasks, success, failure){
            // create a new update context
            LABKEY.icemr.tracking.updateContext = {
                success : success,
                failure : failure
            };

            LABKEY.Query.updateRows( {
                schemaName : 'Samples',
                queryName : this.getFlasksSampleSetName(),
                rows : LABKEY.icemr.tracking.makeUpdateRowset(flasks),
                success : saveDailyMaintenance,
                failure : failure
            });
        }
    };
};
