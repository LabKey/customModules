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

// -------------------------------------------------------------------
// constants
// -------------------------------------------------------------------
// selection specific flask fields
LABKEY.icemr.flask.control = 'Control';
LABKEY.icemr.flask.resistanceNumber = 'ResistanceNumber';
LABKEY.icemr.flask.resistanceProtocol = 'ResistanceProtocol';
LABKEY.icemr.flask.compound = 'Compound';
LABKEY.icemr.flask.compoundOptions = []; // filled in if the Drug Selection Assay is chosen
LABKEY.icemr.flask.resistanceProtocolOptions = [['growth-fold'], ['days']];
LABKEY.icemr.flask.minimumParasitemia = 'MinimumParasitemia';

// sample set fields specific to drug selection flasks
LABKEY.icemr.flask.consecutiveDays = 'ConsecutiveDays';

// -------------------------------------------------------------------
// interface definition
// -------------------------------------------------------------------
LABKEY.icemr.tracking.selection = new function() {
    /**
     * Private functions specific to the selection assay
     */
    var selectionFlasks = 'Selection Flasks';
    var syncFields = [LABKEY.icemr.flask.consecutiveDays];

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
            return syncFields;
        },

        setDefaultValues : function(metaType, config) {
            // nothing to do here for now
        },

        /**
         * get any data we need to store in the flask before saving based on this daily maintenace
         * result
         */
        getFlaskUpdates : function(dailyResult, flask) {
            //
            // if the resistance protocol is 'days' then we need to track the number of consecutive days
            // that the parasitemia value is > than the minimum parasitemia value specified in day0.  Once we exceed
            // the number of days (also specified in Day0) than the flask has resisted the drug and we no longer track
            // consecutive days
            //

            // note that the 'flask' parameter is the flask we are building to send to the db.  'oldFlask' contains
            // the flask values we already have fetched from the db.
            var oldFlask = LABKEY.icemr.tracking.findFlaskInMaterialInputs(flask[LABKEY.icemr.tracking.sample]);
            var days = oldFlask[LABKEY.icemr.flask.consecutiveDays];
            // set our initial value to send over
            flask[LABKEY.icemr.flask.consecutiveDays] = days;

            if (oldFlask[LABKEY.icemr.flask.resistanceProtocol] == 'days')
            {
                // Days could be null if this is the first daily update since day 0.  Since the resistance
                // protocol is Days, set to 0 in this case
                days = oldFlask[LABKEY.icemr.flask.consecutiveDays] || 0;
                if (days < oldFlask[LABKEY.icemr.flask.resistanceNumber])
                {
                    // the flask has not resisted yet so we should update our consecutive days (either incraese
                    // or reset to 0 depending on whether the daily parasitemia value exceeded the day0 minimum
                    // threshold value)
                    if (dailyResult[LABKEY.icemr.tracking.parasitemia] > oldFlask[LABKEY.icemr.flask.minimumParasitemia])
                        days++;
                    else
                        days = 0;

                    // now be sure to copy the value over to the flask to be updated
                    flask[LABKEY.icemr.flask.consecutiveDays] = days;
                }
            }
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
        },

        /**
         * return the query used by vis.html
         */
        getVisQuery : function() {
            return "select_flasks_viz";
        },

        /**
         * return the query used flaskSummary.html
         */
        getCalcQuery : function() {
            return "select_all_calcs";
        }
    };
};
