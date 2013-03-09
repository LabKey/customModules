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

    /**
     * Public interface
     */
    return {
        /**
         * return the flask sample set specific to selection
         */
        getFlasks : function () {
            var flasks = new LABKEY.Exp.SampleSet({name: LABKEY.icemr.selectionFlasks});
            flasks.getDomain({
                success : LABKEY.icemr.tracking.onFlasksDomainReady,
                failure : LABKEY.icemr.tracking.onFlasksFailure
            });
        },

        /**
         * return the fields that must be updated for the selection flask
         */
        getSyncFields : function() {
            // we don't have any fields to add yet so just return the base sync fields
            return LABKEY.icemr.flask.syncFields;
        },

        setDefaultValues : function(metaType, config) {
            throw "not implemented!";
        },

        /**
         * upload flask data to the selection sample set
         */
        uploadFlasks: function(flasks, success, failure){
            LABKEY.Query.insertRows( {
                schemaName : 'Samples',
                queryName : 'Selection Flasks',
                rows : flasks,
                success : success,
                failure : failure
            });
        }
    };
};
