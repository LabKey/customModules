/*
 * Copyright (c) 2013 LabKey Corporation
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
LABKEY.icemr.tracking.adaptation = new function() {

    var syncFields = [LABKEY.icemr.flask.adaptationDate];
    var adaptationFlasks = "Adaptation Flasks";

    /**
     * Public interface
     */
    return {
        /**
         * return the name of flask sample set required for Culture Adaptation
         */
        getFlasksSampleSetName : function () {
            return adaptationFlasks;
        },

        /**
         * check whether this assay's sample set exists without changing state of LABKEY.icemr.
         */
        checkFlasks : function (success)
        {
            LABKEY.icemr.tracking.fetchFlasks(success);
        },

        /**
         * return the flask sample set specific to adaptation
         */
        getFlasks : function () {
            LABKEY.icemr.tracking.fetchFlasks();
        },

        /**
         * return the fields that must be updated for the adaptation flask.  These same fields are
         * hidden in the UI as well.
         */
        getSyncFields : function() {
            return syncFields;
        },

        /**
         * set default values on the passed in config depending on the metatype
         */
        setDefaultValues : function(metaType, config) {
            if (config.name == LABKEY.icemr.flask.adaptationCriteria)
            {
                config.value = LABKEY.icemr.flask.defaultAdaptationCriteria;
            }
        },

        /**
         * get any data we need to store in the flask before saving based on this daily maintenace
         * result
         */
        getFlaskUpdates : function(dailyResult, flask) {
            // nothing do do here for this assay
            return;
        },

        /**
         * upload flask data to the adaptation sample set
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
            return "adapt_flasks_viz";
        },

        /**
         * return the query used flaskSummary.html
         */
        getCalcQuery : function() {
            return "adapted_numdays";
        }
    };
};



