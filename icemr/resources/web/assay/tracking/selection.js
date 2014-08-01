/*
 * Copyright (c) 2013-2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * User: Dax
 * Date: 1/10/13
 * Time: 10:14 AM
 *
 * This implements the tracking assay "interface" for the adaptation specific
 * behavior.  This file must implement all methods in tracking.js
 */

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
        // TODO: Update this to use a LABKEY.ext4.data.Store
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
                LABKEY.icemr.tracking.fetchFlasks();
            }
        });
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
         * check whether this assay's sample set exists without changing state of LABKEY.icemr.
         */
        checkFlasks : function (success) {
            LABKEY.icemr.tracking.fetchFlasks(success);
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
            if (flask[LABKEY.icemr.flask.resistanceProtocol] == 'days')
            {
                // Days could be null if this is the first daily update since day 0.  Since the resistance
                // protocol is Days, set to 0 in this case
                var days = flask[LABKEY.icemr.flask.consecutiveDays] || 0;
                if (days < flask[LABKEY.icemr.flask.resistanceNumber])
                {
                    // the flask has not resisted yet so we should update our consecutive days (either incraese
                    // or reset to 0 depending on whether the daily parasitemia value exceeded the day0 minimum
                    // threshold value)
                    if (dailyResult[LABKEY.icemr.tracking.parasitemia] > flask[LABKEY.icemr.flask.minimumParasitemia])
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
