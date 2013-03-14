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
// namespaces
// -------------------------------------------------------------------
// -------------------------------------------------------------------
// dependencies
// -------------------------------------------------------------------
// -------------------------------------------------------------------
// configuration errors
// -------------------------------------------------------------------
// -------------------------------------------------------------------
// constants
// -------------------------------------------------------------------
// sample set fields specific to culture adaptation flasks
LABKEY.icemr.flask.adaptationCriteria = 'AdaptationCriteria';
LABKEY.icemr.flask.adaptationDate = 'AdaptationDate';
LABKEY.icemr.flask.successfulAdaptation = 'SuccessfulAdaptation';
// used for calculations
LABKEY.icemr.flask.defaultAdaptationCriteria = 2;

// -------------------------------------------------------------------
// enums
// -------------------------------------------------------------------
// -------------------------------------------------------------------
// interface definition
// -------------------------------------------------------------------
LABKEY.icemr.tracking.adaptation = new function() {

    var syncFields = [LABKEY.icemr.flask.adaptationDate];
    var adaptationFlasks = "Adaptation Flasks";

    /**
     * Private functions specific to adaptation assay
     */
    function isCheckForAdaptationRequired(rows)
    {
        for (var i = 0; i < rows.length; i++)
        {
            var row = rows[i];
            for (var j = 0; j < LABKEY.icemr.flask.syncFields.length; j++)
            {
                var key = LABKEY.icemr.flask.syncFields[j];

                // if we finished a growth test and the flask has not already adapted, then
                // we'll need to check for adaptation of this flask set before saving the batch so
                // that we can include the adaptation date
                if ( (key == LABKEY.icemr.flask.finishParasitemia + '1') ||
                        (key == LABKEY.icemr.flask.finishParasitemia + '2') ||
                        (key == LABKEY.icemr.flask.finishParasitemia + '3'))
                {
                    if (row[key])
                        return true;
                }
            }
        }

        return false;
    }

    function checkForAdaptation(data, response, options)
    {
        if (!data || !(data.rows) || (0 == data.rows.length))
            return;

        var keyMap = LABKEY.icemr.tracking.makeSyncFieldsKeyMap(data.rows[0]);
        LABKEY.icemr.tracking.syncMaterialInputs(data.rows, keyMap);

        if (LABKEY.icemr.tracking.updateContext.checkForAdaptation)
        {
            // at least one flask that we updated finished a growth test so we need to check whether
            // any of the flasks we just uploaded adapted to see if we have to update the flask's adaptation date
            // consider: if the adaptation calculation takes too long over all the samples then make a parameterized
            // consider: version that only checks the flask ids of flasks that could have possibly adapted for this
            // consider: update
            var filterValues = '';
            for (var i = 0 ; i < data.rows.length; i++)
            {
                filterValues += data.rows[i][keyMap[LABKEY.icemr.flask.sample]] + ';';
            }

            LABKEY.Query.selectRows({
                schemaName : 'assay.Tracking.' + LABKEY.page.assay.name,
                queryName : 'adapted_numdays',
                columns : [LABKEY.icemr.flask.sample,
                    LABKEY.icemr.flask.maintenanceDate,
                    LABKEY.icemr.flask.adaptationDate,
                    LABKEY.icemr.flask.successfulAdaptation],
                filterArray : [LABKEY.Filter.create(LABKEY.icemr.flask.sample, filterValues,
                        LABKEY.Filter.Types.IN)],
                success : saveAdaptationDate,
                failure : LABKEY.icemr.tracking.updateContext.failure
            });
        }
        else
        {
            LABKEY.icemr.tracking.saveBatch();
        }
    }

    function saveAdaptationDate(data, response, options)
    {
        var rows = [];

        if (data && data.rows)
        {
            for (var i = 0; i < data.rows.length; i++)
            {
                var dataRow = data.rows[i];
                // if the flask has just adapted then successfulAdaptation will be true
                // but it won't have an adaptation date.  In this case we need
                // to provide a list of rows
                if ((dataRow[LABKEY.icemr.flask.adaptationDate] == null) &&
                        (dataRow[LABKEY.icemr.flask.successfulAdaptation] == 'Yes'))
                {
                    var row = {};
                    row[LABKEY.icemr.flask.sample] = dataRow[LABKEY.icemr.flask.sample];
                    row[LABKEY.icemr.flask.adaptationDate] = dataRow[LABKEY.icemr.flask.maintenanceDate];
                    rows.push(row);
                }
            }
        }

        if (rows.length > 0)
        {
            LABKEY.Query.updateRows( {
                schemaName : 'Samples',
                queryName : adaptationFlasks,
                rows : rows,
                success : saveDailyMaintenance,
                failure : LABKEY.icemr.tracking.updateContext.failure
            });
        }
        else
        {
            LABKEY.icemr.tracking.saveBatch();
        }
    }

    function saveDailyMaintenance(data, response, options)
    {
        if (data && data.rows && data.rows.length > 0)
        {
            syncAdaptationDate(data.rows, LABKEY.icemr.tracking.makeSyncFieldsKeyMap(data.rows[0]));
            LABKEY.icemr.tracking.saveBatch();
        }
    }

    //
    // given a rowset, copy over the latest updated values to our material inputs
    //
    function syncAdaptationDate(rows, keyMap)
    {
        for (var i  = 0; i < rows.length; i++)
        {
            var row = rows[i];
            var flask = LABKEY.icemr.tracking.findFlaskInMaterialInputs(row[keyMap[LABKEY.icemr.flask.sample]]);
            flask[LABKEY.icemr.flask.adaptationDate] = row[keyMap[LABKEY.icemr.flask.adaptationDate]];
        }
    }

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
         * return the flask sample set specific to adaptation
         */
        getFlasks : function () {
            var flasks = new LABKEY.Exp.SampleSet( {name: this.getFlasksSampleSetName()});
            flasks.getDomain({
                success : LABKEY.icemr.tracking.onFlasksDomainReady,
                failure : LABKEY.icemr.tracking.onFlasksFailure
            });
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
         * Save daily maintenance data and update flask sample set.  Also check for flask adaptation
         * if needed.
         */
        saveDaily: function(flasks, success, failure){
            var rows = LABKEY.icemr.tracking.makeUpdateRowset(flasks);

            // create a new update context
            LABKEY.icemr.tracking.updateContext = {
                success : success,
                failure : failure,
                checkForAdaptation : isCheckForAdaptationRequired(rows)
            };

            LABKEY.Query.updateRows( {
                schemaName : 'Samples',
                queryName : this.getFlasksSampleSetName(),
                rows : rows,
                success : checkForAdaptation,
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
        },

        /**
         * return the query used for the generic fold increase calculator
         */
        getGenericFoldIncreaseQuery : function() {
            return "adapt_generic_foldincrease";
        }
    };
};



