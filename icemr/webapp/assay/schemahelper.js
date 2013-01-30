/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * Created with IntelliJ IDEA.
 * User: Dax
 * Date: 1/15/13
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */
LABKEY.requiresScript('clientapi/ext4/Util.js');
Ext.namespace("LABKEY.icemr");

// -------------------------------------------------------------------
// diagnostic assay constants
// -------------------------------------------------------------------
Ext.namespace("LABKEY.icemr.diagnostics");
LABKEY.icemr.diagnostics.participant='ParticipantID';
LABKEY.icemr.diagnostics.hemoglobin='PatientHemoglobin';
LABKEY.icemr.diagnostics.thick = 'ThickBloodSmear';
LABKEY.icemr.diagnostics.thin = 'ThinBloodSmear';
LABKEY.icemr.diagnostics.RDT = 'RDT';
LABKEY.icemr.diagnostics.time = 'Time';
LABKEY.icemr.diagnostics.date = 'Date'
LABKEY.icemr.diagnostics.speciesOptions = [['Pf'], ['Pv'], ['mixed'], ['negative']];

// -------------------------------------------------------------------
// adaptation assay constants for the result fields
// -------------------------------------------------------------------
Ext.namespace("LABKEY.icemr.adaptation");
LABKEY.icemr.adaptation.flaskSampleSet = 'Flasks';
LABKEY.icemr.adaptation.parasitemia = 'Parasitemia';
LABKEY.icemr.adaptation.patient = 'PatientID';
LABKEY.icemr.adaptation.sample = 'SampleID';
LABKEY.icemr.adaptation.experiment = 'ExperimentID';
LABKEY.icemr.adaptation.contamination = 'Contamination';
LABKEY.icemr.adaptation.mycotest = 'MycoTestResult';
LABKEY.icemr.adaptation.interestingResult = 'InterestingResult';
LABKEY.icemr.adaptation.stage = 'Stage';
LABKEY.icemr.adaptation.pRBC = 'PatientpRBCs';
LABKEY.icemr.adaptation.cultureMedia = 'CultureMedia';
LABKEY.icemr.adaptation.stageOptions =  [['rings'], ['trophozoites'], ['schizonts']];
LABKEY.icemr.adaptation.pRBCOptions = [['washed'], ['unwashed']];
LABKEY.icemr.adaptation.cultureMediaOptions = [['serum'], ['Albumax']];
LABKEY.icemr.adaptation.yesNoOptions = [['Yes'], ['No']];
LABKEY.icemr.adaptation.positiveNegativeOptions = [['Positive'], ['Negative'], ['No Test']];
LABKEY.icemr.adaptation.oneTwoThreeOptions = [['1'], ['2'], ['3'], ['No']];
LABKEY.icemr.adaptation.dateIndex = 'DateIndex';
LABKEY.icemr.adaptation.measurementDate = 'MeasurementDate';
LABKEY.icemr.adaptation.growthFoldTestInitiated = 'GrowthFoldTestInitiated';
LABKEY.icemr.adaptation.growthFoldTestFinished = 'GrowthFoldTestFinished';
LABKEY.icemr.adaptation.flaskMaintenanceStopped = 'FlaskMaintenanceStopped';
LABKEY.icemr.adaptation.scientist = 'Scientist';
LABKEY.icemr.adaptation.serumBatch = 'SerumBatchID';
LABKEY.icemr.adaptation.albumaxBatch = 'AlbumaxBatchID';

// -------------------------------------------------------------------
// these fields are added to the flask sample set automatically
// when daily uploads occur
// -------------------------------------------------------------------
Ext.namespace("LABKEY.icemr.flask");
LABKEY.icemr.flask.sample = LABKEY.icemr.adaptation.sample;
LABKEY.icemr.flask.startDate = 'StartDate';
// date that maintenance on the flask is done
LABKEY.icemr.flask.maintenanceStopped = 'MaintenanceStopped';
// date of most recent daily upload
LABKEY.icemr.flask.maintenanceDate = 'MaintenanceDate';
// StartParasitemia[n] where n = 1,2, or 3 corresponding to the parasitemia when the growth-fold test
// is started
LABKEY.icemr.flask.startParasitemia = 'StartParasitemia';
// same as above but when the test is finished
LABKEY.icemr.flask.finishParasitemia = 'FinishParasitemia';
// start date of growth test, ICEMR only cares about the Growth-Fold test1
LABKEY.icemr.flask.startDate1 = 'StartDate1';
LABKEY.icemr.flask.finishDate1 = 'FinishDate1';
// the date when the flask is considered to have a successful adaptation based on the passing
// of a growth test that puts it over the adaptation criteria
LABKEY.icemr.flask.adaptationCriteria = 'AdaptationCriteria';
LABKEY.icemr.flask.adaptationDate = 'AdaptationDate';

LABKEY.icemr.flask.internalFields = [
    LABKEY.icemr.flask.maintenanceStopped,
    LABKEY.icemr.flask.maintenanceDate,
    LABKEY.icemr.flask.startParasitemia + '1',
    LABKEY.icemr.flask.startParasitemia + '2',
    LABKEY.icemr.flask.startParasitemia + '3',
    LABKEY.icemr.flask.finishParasitemia + '1',
    LABKEY.icemr.flask.finishParasitemia + '2',
    LABKEY.icemr.flask.finishParasitemia + '3',
    LABKEY.icemr.flask.startDate1,
    LABKEY.icemr.flask.finishDate1,
    LABKEY.icemr.flask.adaptationDate
];

// used for calculations
LABKEY.icemr.flask.foldIncrease = 'FoldIncrease';

// -------------------------------------------------------------------
// the ICEMR module must have these assay designs and
// sample sets
// -------------------------------------------------------------------
LABKEY.icemr.AdaptationAssayResults = 'Adaptation Assay';
LABKEY.icemr.DiagnosticsAssayResults = 'Diagnostics Assay';
LABKEY.icemr.metaType = {
    AssayDesign : 0,
    SampleSet : 1
}

// -------------------------------------------------------------------
// configuration errors
// -------------------------------------------------------------------
LABKEY.icemr.errConfigTitle = "Configuration Error";
LABKEY.icemr.errConfigMissingAssayDesign = "Could not find the specified assay design. Please see your LabKey administrator.";
LABKEY.icemr.errConfigMissingFlask = "Could not find the Flask Sample Set. Please see your LabKey administrator.";

// -------------------------------------------------------------------
// functions
// -------------------------------------------------------------------
function getAdaptationFieldConfigsCallbackWrapper(fn) {
    return function(runFieldConfigs, resultFieldConfigs) {
        LABKEY.icemr.adaptation.runFieldConfigs = runFieldConfigs;
        LABKEY.icemr.adaptation.resultFieldConfigs = resultFieldConfigs;
        if (fn)
            fn.call(this, runFieldConfigs, resultFieldConfigs);
    }
}

function getAdaptationFieldConfigs(successCallback)
{
    getFieldConfigs(LABKEY.icemr.AdaptationAssayResults,
            getAdaptationFieldConfigsCallbackWrapper(successCallback));
}

function getDiagnosticFieldConfigsCallbackWrapper(fn) {
    return function(runFieldConfigs, resultFieldConfigs) {
        LABKEY.icemr.diagnostics.runFieldConfigs = runFieldConfigs;
        LABKEY.icemr.diagnostics.resultFieldConfigs = resultFieldConfigs;
        if (fn)
            fn.call(this, runFieldConfigs, resultFieldConfigs);
    }
}

function getDiagnosticsFieldConfigs(successCallback)
{
    getFieldConfigs(LABKEY.icemr.DiagnosticsAssayResults,
            getDiagnosticFieldConfigsCallbackWrapper(successCallback));
}

function getFieldConfigsSuccessCallback(fn) {
    return function(assays)
    {
        if (assays.length != 1)
        {
            Ext.Msg.hide();
            Ext.Msg.alert(LABKEY.icemr.errConfigTitle,  LABKEY.icemr.errConfigMissingAssayDesign);
        }

        var assay = assays[0];
        var runFields = assay.domains[assay.name + ' Run Fields'];
        var resultFields = assay.domains[assay.name + ' Result Fields'];
        var runConfigs = buildConfigs(runFields, LABKEY.icemr.metaType.AssayDesign);
        var resultConfigs = buildConfigs(resultFields, LABKEY.icemr.metaType.AssayDesign);

        if (fn)
            fn.call(this, runConfigs, resultConfigs);
    }
}

function getFieldConfigs(assayName, successCallback)
{
    if (assayName == LABKEY.icemr.AdaptationAssayResults ||
        assayName == LABKEY.icemr.DiagnosticsAssayResults)
    {
        LABKEY.Assay.getByName({
            name : assayName,
            success : getFieldConfigsSuccessCallback(successCallback)
        });
    }
    else
    {
        //
        // This denotes a bug in ICEMR module code.
        //
        throw 'invalid assay design or sample set request'
    }
}

function buildConfigs(fields, metaType)
{
    var configs = [];
    for (var i = 0; i < fields.length; i++)
    {
        configs.push(buildConfig(fields[i], metaType));
    }
    return configs;
}

function buildConfig(meta, metaType)
{
    config = LABKEY.ext4.Util.getFormEditorConfig(meta);
    //
    // set an id so that we use this is as the key name for the field
    // in any form we bind to
    //
    config.id = config.name;
    //
    // add in our custom validation code to deal with numeric fields
    // and sample-set meta data which is slighty different than assay design
    // metadata
    //
    fixupValidation(meta, metaType, config);
    return config;
}

// this function only should be called for meta data returned from
// a sample set domain
function fixupSampleSetTypeInformation(meta, config)
{
    var typename = meta.rangeURI.toLowerCase();
    if (typename == 'http://www.w3.org/2001/xmlschema#double')
    {
        config.xtype = 'numberfield';
        config.allowDecimals = true;
    }
    else
    if (typename == 'http://www.w3.org/2001/xmlschema#integer')
    {
        config.xtype = 'numberfield';
        config.allowDecimals = false;
    }
}

// fields that require validation
function fixupValidation(meta, metaType, config)
{
    config.allowBlank = (meta.required === false);

    if (metaType == LABKEY.icemr.metaType.SampleSet)
    {
        fixupSampleSetTypeInformation(meta, config);
    }

    if (config.xtype == 'numberfield')
    {
        if (config.allowDecimals)
        {
            config.vtype = 'percentNumber';
        }
        else
        {
            config.vtype = 'intNumber';
        }
    }
}

function setComboConfig(config, options)
{
    var name = config.name.toLowerCase();
    config.xtype = 'combo';
    config.store = new Ext4.data.SimpleStore({
        fields : [name],
        data : options
    });
    config.displayField = name;
    config.valueField = name;
    config.forceSelection = true;
    config.mode = 'local';
    config.value = options[0][0];
    config.vtype = undefined; // let combo provide validation
}

// map various fields from the db to ui values

function getFormGrowthTestValue(src)
{
    if (src == '1' || src == 1)
        return '1';
    else if (src == '2' || src == 2)
        return '2';
    else if (src == '3' || src == 3)
        return '3';

    // any other value we just set to 'No'
    return 'No';
}

function getFormYesNoValue(value)
{
    if (value)
    {
        if (typeof(value) == 'string')
        {
            var v = value.toLowerCase();
            if (v =='yes') return 'Yes';
            if (v =='no') return 'No';
        }
        // if defined but invalid, return null
        return null;
    }

    // if not defined, return no
    return 'No';
}
function getFormPosNegValue(value)
{

    if (value)
    {
        if (typeof(value) == 'string')
        {
            var v = value.toLowerCase();
            if (v == 'positive') return 'Positive';
            if (v == 'negative') return 'Negative';
        }
        // if defined but invalid, return null
        return null;
    }
    // if not defined, return 'No Test'
    return 'No Test';
}
