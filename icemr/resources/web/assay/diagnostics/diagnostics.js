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
Ext4.namespace("LABKEY.icemr.diagnostics");
// -------------------------------------------------------------------
// dependencies
// -------------------------------------------------------------------
// -------------------------------------------------------------------
// configuration errors
// -------------------------------------------------------------------
// -------------------------------------------------------------------
// constants
// -------------------------------------------------------------------
LABKEY.icemr.diagnostics.participant='ParticipantID';
LABKEY.icemr.diagnostics.hemoglobin='PatientHemoglobin';
LABKEY.icemr.diagnostics.thick = 'ThickBloodSmear';
LABKEY.icemr.diagnostics.thin = 'ThinBloodSmear';
LABKEY.icemr.diagnostics.RDT = 'RDT';
LABKEY.icemr.diagnostics.bacteria = 'BacteriaSeen';
LABKEY.icemr.diagnostics.time = 'Time';
LABKEY.icemr.diagnostics.date = 'Date';
LABKEY.icemr.diagnostics.scientist = 'Scientist';
LABKEY.icemr.diagnostics.speciesOptions = [['Pf'], ['Pv'], ['Pm'], ['mixed Pf/Pv'], ['mixed Pf/Pm'], ['negative']];
LABKEY.icemr.diagnostics.bacteriaOptions = [['Yes'], ['No'], ['Missing'], ['Uncertain']];
// -------------------------------------------------------------------
// enums
// -------------------------------------------------------------------

// -------------------------------------------------------------------
// methods
// -------------------------------------------------------------------
LABKEY.icemr.diagnostics.getFieldConfigsCallbackWrapper = function(fn) {
    return function(runFieldConfigs, resultFieldConfigs) {
        LABKEY.icemr.diagnostics.runFieldConfigs = runFieldConfigs;
        LABKEY.icemr.diagnostics.resultFieldConfigs = resultFieldConfigs;
        if (fn)
            fn.call(this, runFieldConfigs, resultFieldConfigs);
    }
};

LABKEY.icemr.diagnostics.getFieldConfigs = function(successCallback) {
    LABKEY.icemr.getFieldConfigs(LABKEY.page.assay.name,
            LABKEY.icemr.diagnostics.getFieldConfigsCallbackWrapper(successCallback));
};

