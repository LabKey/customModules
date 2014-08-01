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
 * This implements the flask tracking assay base code that is common to both
 * adaptation and selection instances.  Specific implementations of adaptation or selection
 * assay behavior can be found in adaptation.js and selection.js.  The UI layer should only need
 * to know about the LABKEY.icemr.tracking namespace
 *
 */
Ext4.namespace("LABKEY.icemr.speciesSpecific");
// -------------------------------------------------------------------
// dependencies
// -------------------------------------------------------------------

// -------------------------------------------------------------------
// configuration errors
// -------------------------------------------------------------------

// -------------------------------------------------------------------
// constants
// -------------------------------------------------------------------
LABKEY.icemr.speciesSpecific.participant='ParticipantID';
LABKEY.icemr.speciesSpecific.scientist='Scientist';
LABKEY.icemr.speciesSpecific.time = 'Time';
LABKEY.icemr.speciesSpecific.date = 'Date';
LABKEY.icemr.speciesSpecific.gelImage = 'GelImage';
// -------------------------------------------------------------------
// enums
// -------------------------------------------------------------------

// -------------------------------------------------------------------
// methods
// -------------------------------------------------------------------
LABKEY.icemr.speciesSpecific.getFieldConfigsCallbackWrapper = function(fn) {
    return function(runFieldConfigs, resultFieldConfigs) {
        LABKEY.icemr.speciesSpecific.runFieldConfigs = runFieldConfigs;
        LABKEY.icemr.speciesSpecific.resultFieldConfigs = resultFieldConfigs;
        if (fn)
            fn.call(this, runFieldConfigs, resultFieldConfigs);
    }
};

LABKEY.icemr.speciesSpecific.getFieldConfigs = function(successCallback) {
    LABKEY.icemr.getFieldConfigs(LABKEY.page.assay.name,
            LABKEY.icemr.speciesSpecific.getFieldConfigsCallbackWrapper(successCallback));
};

//
// if the user has specified a gel-image then we'll have an Exp data
// object that we wire up as a run input
//
LABKEY.icemr.speciesSpecific.saveBatch = function(row, data, success, failure)
{
    var run = new LABKEY.Exp.Run();

    if (data)
    {
        run.dataInputs = [data];
        row[LABKEY.icemr.speciesSpecific.gelImage] = data.name;
    }

    run.name = row[LABKEY.icemr.speciesSpecific.participant];
    run.dataRows = [];
    run.dataRows.push(row);

    LABKEY.page.batch.runs = [];
    LABKEY.page.batch.runs.push(run);
    LABKEY.setDirty(true);

    LABKEY.Experiment.saveBatch({
        assayId : LABKEY.page.assay.id,
        batch : LABKEY.page.batch,
        success : success,
        failure : failure
    });
}