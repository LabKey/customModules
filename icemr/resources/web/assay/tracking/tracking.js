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
// -------------------------------------------------------------------
// namespaces
// -------------------------------------------------------------------
Ext4.namespace("LABKEY.icemr.tracking");
Ext4.namespace("LABKEY.icemr.flask");

// -------------------------------------------------------------------
// dependencies
// -------------------------------------------------------------------
LABKEY.requiresScript('assay/tracking/adaptation.js');
LABKEY.requiresScript('assay/tracking/selection.js');

// -------------------------------------------------------------------
// interface definition
// -------------------------------------------------------------------
LABKEY.icemr.tracking.interface = new function () {
    return {
        getFlasksSampleSetName : function () { throw "not implemented!"},
        checkFlasks: function (success) { throw "not implemented"},
        getFlasks : function () { throw "not implemented!" },
        getSyncFields : function() { throw "not implemented!" },
        setDefaultValues : function(metaTypa, config) {throw "not implemented!"},
        getFlaskUpdates : function(dailyResult, flask) { throw "not implemented!"},
        uploadFlasks : function(flasks, success, failure) {throw "not implemented!"},
        getVisQuery : function () { throw "not implemented!"},
        getCalcQuery : function () { throw "not implemented!"}
    };
};

// -------------------------------------------------------------------
// configuration errors
// -------------------------------------------------------------------
LABKEY.icemr.tracking.errAssayTitle = "Assay Runtime Error";
LABKEY.icemr.tracking.errAssayMissingRun = "Could not find a matching Day 0 experiment to update";
LABKEY.icemr.tracking.errDailyTitle = "Daily Maintenance Error";
LABKEY.icemr.tracking.errDailyNoResults = "You must include at least one result to upload";
LABKEY.icemr.tracking.errDailyTooManyFlasks = "Invalid attempt to upload data for flasks that were not defined in Day 0";
LABKEY.icemr.tracking.errDailyInvalidFlaskDefined = "Invalid flask specified.  The following flask was not found in the Day 0 data or maintenance was already stopped: ";
LABKEY.icemr.tracking.errDailyInvalidZeroParasitemia = "You must specify a non-zero Parasitemia value when a growth fold test is initiated for flask: ";
LABKEY.icemr.tracking.errDailyInvalidMeasurementDate = "Data for the specified measurement date already exists for flask: ";
LABKEY.icemr.tracking.errDailyInvalidExperiment = "Invalid experiment specified.  The following experiment was not found:  ";
LABKEY.icemr.tracking.errDailyUploadTitle = "Daily Upload Failed";
LABKEY.icemr.tracking.errDailyUploadFileNoContent = "The data file has no content";
LABKEY.icemr.tracking.errDailyUploadFileNoSheets = "The data file has no sheets of data";
LABKEY.icemr.tracking.errDailyUploadFileNoRows = "The data file has no rows of data";
LABKEY.icemr.tracking.errDailyUploadFileInvalidHeader = "The data file header row does not match the daily results schema";
LABKEY.icemr.tracking.errDay0Title = "Day 0 Upload Error";
LABKEY.icemr.tracking.errDay0NoFlasksDefined = "You must include at least one flask with your Day 0 data";


// -------------------------------------------------------------------
// constants
// -------------------------------------------------------------------

// -------------------------------------------------------------------
// These field names and options are used across the assay results as well
// as the sample sets.  The convention used is that assay field names
// are under the LABKEY.icemr.tracking namespace.  Sample set field names
// are under the LABKEY.icemr.flask namespace.  The adaptation and selection
// assays share the exact same field names.  The adaptation and selection flasks
// have a set of shared and different fields.
// -------------------------------------------------------------------
LABKEY.icemr.tracking.adaptationAssay = 'Culture Adaptation';
LABKEY.icemr.tracking.selectionAssay = 'Drug Selection';

// -------------------------------------------------------------------
// tracking assay (selection and adaptation
// -------------------------------------------------------------------
// run fields
LABKEY.icemr.tracking.patient = 'PatientID';
LABKEY.icemr.tracking.startDate = 'StartDate';
LABKEY.icemr.tracking.experiment = 'ExperimentID';
// result fields
LABKEY.icemr.tracking.sample = 'SampleID';
LABKEY.icemr.tracking.measurementDate = 'MeasurementDate';
LABKEY.icemr.tracking.scientist = 'Scientist';
LABKEY.icemr.tracking.parasitemia = 'Parasitemia';
LABKEY.icemr.tracking.gametocytemia = 'Gametocytemia';
LABKEY.icemr.tracking.stage = 'Stage';
LABKEY.icemr.tracking.removed = 'Removed';
LABKEY.icemr.tracking.rbcBatch = 'RBCBatchID';
LABKEY.icemr.tracking.serumBatch = 'SerumBatchID';
LABKEY.icemr.tracking.albumaxBatch = 'AlbumaxBatchID';
LABKEY.icemr.tracking.growthFoldTestInitiated = 'GrowthFoldTestInitiated';
LABKEY.icemr.tracking.growthFoldTestFinished = 'GrowthFoldTestFinished';
LABKEY.icemr.tracking.contamination = 'Contamination';
LABKEY.icemr.tracking.mycotest = 'MycoTestResult';
LABKEY.icemr.tracking.freezerProIds = "FreezerProIDs";
LABKEY.icemr.tracking.flaskMaintenanceStopped = 'FlaskMaintenanceStopped';
LABKEY.icemr.tracking.interestingResult = 'InterestingResult';
LABKEY.icemr.tracking.comments = 'Comments';
LABKEY.icemr.tracking.dateIndex = 'DateIndex';
// result option values
LABKEY.icemr.tracking.pRBC = 'PatientpRBCs';
LABKEY.icemr.tracking.cultureMedia = 'CultureMedia';
LABKEY.icemr.tracking.stageOptions =  [['none'], ['gametocyte'], ['rings'], ['trophozoites'], ['schizonts'], ['mixed']];
LABKEY.icemr.tracking.pRBCOptions = [['washed'], ['unwashed']];
LABKEY.icemr.tracking.cultureMediaOptions = [['serum'], ['Albumax']];
LABKEY.icemr.tracking.yesNoOptions = [['Yes'], ['No']];
LABKEY.icemr.tracking.positiveNegativeTestOptions = [['Positive'], ['Negative'], ['No Test']];
LABKEY.icemr.tracking.positiveNegativeOptions = [['Positive'], ['Negative'], ['No']];
LABKEY.icemr.tracking.oneTwoThreeOptions = [['1'], ['2'], ['3'], ['No']];
// daily maintenance column names
LABKEY.icemr.tracking.rowId = "RowId";
LABKEY.icemr.tracking.protocolId = "Batch/BatchProtocolId/RowId";
LABKEY.icemr.tracking.batchId = "Batch/RowId";
LABKEY.icemr.tracking.protocolName = "Protocol/Name";
// flasks collection used in saveDaily result object
LABKEY.icemr.tracking.flasks = "flasks";

// -------------------------------------------------------------------
// sample set fields (flasks)
// -------------------------------------------------------------------
// common flask fields
LABKEY.icemr.flask.patient = LABKEY.icemr.tracking.patient;
LABKEY.icemr.flask.sample = LABKEY.icemr.tracking.sample;
LABKEY.icemr.flask.scientist = LABKEY.icemr.tracking.scientist;
LABKEY.icemr.flask.cultureMedia = LABKEY.icemr.tracking.cultureMedia;
LABKEY.icemr.flask.serumBatch = LABKEY.icemr.tracking.serumBatch;
LABKEY.icemr.flask.albumaxBatch = LABKEY.icemr.tracking.albumaxBatch;
LABKEY.icemr.flask.foldIncrease = 'FoldIncrease'; // FoldIncrease[n] where n = 1,2, or 3
LABKEY.icemr.flask.startParasitemia = 'StartParasitemia';// StartParasitemia[n] where n = 1,2, or 3
LABKEY.icemr.flask.finishParasitemia = 'FinishParasitemia'; // save as above
LABKEY.icemr.flask.comments = LABKEY.icemr.tracking.comments;
LABKEY.icemr.flask.maintenanceStopped = 'MaintenanceStopped'; // date that maintenance on the flask is done
LABKEY.icemr.flask.maintenanceDate = 'MaintenanceDate'; // date of most recent daily upload
LABKEY.icemr.flask.startDate1 = 'StartDate1';
LABKEY.icemr.flask.finishDate1 = 'FinishDate1';
// adaptation specific flask fields
LABKEY.icemr.flask.parasitemia = LABKEY.icemr.tracking.parasitemia;
LABKEY.icemr.flask.gametocytemia = LABKEY.icemr.tracking.gametocytemia;
LABKEY.icemr.flask.patientpRBCs = 'PatientpRBCs';
LABKEY.icemr.flask.hematocrit = 'Hematocrit';
LABKEY.icemr.flask.stage = LABKEY.icemr.tracking.stage;
LABKEY.icemr.flask.adaptationCriteria = 'AdaptationCriteria';
LABKEY.icemr.flask.adaptationDate = 'AdaptationDate';
LABKEY.icemr.flask.successfulAdaptation = 'SuccessfulAdaptation'; // calculated field
// selection specific flask fields
LABKEY.icemr.flask.adaptationSample = 'AdaptationSampleID';
LABKEY.icemr.flask.freezerProId = 'FreezerProID';
LABKEY.icemr.flask.initialPopulation = 'InitialPopulation';
LABKEY.icemr.flask.compound = 'Compound';
LABKEY.icemr.flask.concentration = 'Concentration';
LABKEY.icemr.flask.control = 'Control';
LABKEY.icemr.flask.superstockBatch = 'SuperstockBatchID';
LABKEY.icemr.flask.workingstockBatch = 'WorkingstockBatchID';
LABKEY.icemr.flask.rbcBatch = LABKEY.icemr.tracking.rbcBatch;
LABKEY.icemr.flask.resistanceProtocol = 'ResistanceProtocol';
LABKEY.icemr.flask.resistanceNumber = 'ResistanceNumber';
LABKEY.icemr.flask.minimumParasitemia = 'MinimumParasitemia';
LABKEY.icemr.flask.consecutiveDays = 'ConsecutiveDays'; // calculated field
// sample set options
LABKEY.icemr.flask.compoundOptions = []; // filled in if the Drug Selection Assay is chosen
LABKEY.icemr.flask.resistanceProtocolOptions = [['growth-fold'], ['days']];

LABKEY.icemr.flask.syncFields = [
    LABKEY.icemr.flask.maintenanceStopped,
    LABKEY.icemr.flask.maintenanceDate,
    LABKEY.icemr.flask.startParasitemia + '1',
    LABKEY.icemr.flask.startParasitemia + '2',
    LABKEY.icemr.flask.startParasitemia + '3',
    LABKEY.icemr.flask.finishParasitemia + '1',
    LABKEY.icemr.flask.finishParasitemia + '2',
    LABKEY.icemr.flask.finishParasitemia + '3',
    LABKEY.icemr.flask.startDate1,
    LABKEY.icemr.flask.finishDate1
];
// used for calculations
LABKEY.icemr.flask.defaultFoldIncrease = 4;
LABKEY.icemr.flask.defaultAdaptationCriteria = 2;

// used for excel template upload
LABKEY.icemr.tracking.dailyUploadTemplateFilename = "dailyUpload.xls";
LABKEY.icemr.tracking.rowTypeExperiment = 0;
LABKEY.icemr.tracking.rowTypeHeader = 1;
LABKEY.icemr.tracking.rowTypeFlask = 2;
LABKEY.icemr.tracking.rowTypeBlank = 3;

// -------------------------------------------------------------------
// methods
// -------------------------------------------------------------------
LABKEY.icemr.tracking.onFlasksDomainReady = function(domain)
{
    LABKEY.icemr.tracking.flaskConfigs = LABKEY.icemr.buildConfigs(domain.fields, LABKEY.icemr.metaType.SampleSet);

    // filter out our internal flask fields for the client
    var clientFlaskConfigs = [];
    var syncFields = LABKEY.icemr.tracking.getSyncFields();
    for (var i=0; i < LABKEY.icemr.tracking.flaskConfigs.length; i++)
    {
        var config = LABKEY.icemr.tracking.flaskConfigs[i];
        if (!LABKEY.icemr.isNameInArray(config.name, syncFields))
            clientFlaskConfigs.push(config);
    }

    LABKEY.icemr.getDay0Success(LABKEY.icemr.tracking.runFieldConfigs, clientFlaskConfigs);
};

LABKEY.icemr.tracking.onFlasksFailure = function(data)
{
    LABKEY.icemr.showParamError(LABKEY.icemr.errConfigTitle, LABKEY.icemr.errConfigMissingFlask, LABKEY.icemr.tracking.interface.getFlasksSampleSetName());
};

LABKEY.icemr.tracking.onDay0ConfigsSuccess = function(runFieldConfigs, resultFieldConfigs) {
    LABKEY.icemr.tracking.interface.getFlasks();
};

LABKEY.icemr.tracking.getDay0Configs = function(successCallback) {
    LABKEY.icemr.getDay0Success = successCallback;
    //
    // Day 0 configs consist of the run properties for the adaptation
    // assay design as well as the flask metadata for the sample set
    // We also fetch the Flasks Sample Set ID and store it away for later use
    // before returning to the caller
    //
    LABKEY.icemr.tracking.getFieldConfigs(LABKEY.page.assay.name, LABKEY.icemr.tracking.onDay0ConfigsSuccess);
};

LABKEY.icemr.tracking.getFieldConfigsCallbackWrapper = function(fn) {
    return function(runFieldConfigs, resultFieldConfigs) {
        LABKEY.icemr.tracking.runFieldConfigs = runFieldConfigs;
        LABKEY.icemr.tracking.resultFieldConfigs = resultFieldConfigs;
        if (fn)
            fn.call(this, runFieldConfigs, resultFieldConfigs);
    }
};

LABKEY.icemr.tracking.getFieldConfigs = function(assayName, success) {
    //
    // This is so cheesy but key off the assay name to determine whether this is the adaptation or selection "flavor"
    // of the tracking assay. We could add a dummy field (batch, for example) but probably the better idea is to
    // add a custom property to a protocol.  This is not currently exposed, however.  Long term it would be nice if
    // assay designs could inherit from one another.  Since we don't have that mechanism, we provided our own "interface"
    // mechanism here.
    //
    LABKEY.icemr.tracking.isSelection = (assayName == LABKEY.icemr.tracking.selectionAssay);
    LABKEY.icemr.tracking.interface = (LABKEY.icemr.tracking.isSelection) ? LABKEY.icemr.tracking.selection : LABKEY.icemr.tracking.adaptation;

    // get the field configs for the name of the assay selected
    LABKEY.icemr.getFieldConfigs(assayName, LABKEY.icemr.tracking.getFieldConfigsCallbackWrapper(success));
};

LABKEY.icemr.tracking.setInterface = function(schemaName) {
    //
    // some pages have a schema name so set the appropriate interface here
    //
    if (schemaName == "assay.Tracking." + LABKEY.icemr.tracking.selectionAssay) {
        LABKEY.icemr.tracking.isSelection = true;
    }

    LABKEY.icemr.tracking.interface = (LABKEY.icemr.tracking.isSelection) ? LABKEY.icemr.tracking.selection : LABKEY.icemr.tracking.adaptation;
};

LABKEY.icemr.tracking.createExperiment = function() {
    return LABKEY.icemr.tracking.createRecord(LABKEY.icemr.tracking.runFieldConfigs);
};

LABKEY.icemr.tracking.createFlask = function(){
    return LABKEY.icemr.tracking.createRecord(LABKEY.icemr.tracking.flaskConfigs);
};

LABKEY.icemr.tracking.createDaily = function() {
    return LABKEY.icemr.tracking.createRecord(LABKEY.icemr.tracking.resultFieldConfigs);
};

LABKEY.icemr.tracking.createRecord = function(fieldConfigs){
    var record = {};
    for (var i = 0; i < fieldConfigs.length; i++)
    {
        record[fieldConfigs[i].name] = null;
    }

    return record;
};

LABKEY.icemr.tracking.getDay0Flasks = function(run) {
    if (run == undefined)
        return LABKEY.icemr.tracking.onLoadBatchFailure(); // we shouldn't get here

    var flasks = [];
    var materialInputs = run.materialInputs;

    for (var i = 0; i < materialInputs.length; i++)
    {
        flasks.push(materialInputs[i].properties);
    }

    return flasks;
};

LABKEY.icemr.tracking.onLoadBatchFailure = function(response){
    LABKEY.icemr.showError(LABKEY.icemr.errAssayTitle, LABKEY.icemr.errAssayMissingRun);
};

LABKEY.icemr.tracking.saveDay0 = function(experiment, flasks, success, failure){
    if (!experiment)
        throw "You must provide an experiment object";

    if (!success)
        throw "You must provide a Success callback function";

    if (!flasks)
        return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDay0Title, LABKEY.icemr.tracking.errDay0NoFlasksDefined, failure);

    if (flasks.length == 0)
        return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDay0Title, LABKEY.icemr.tracking.errDay0NoFlasksDefined, failure);

    // strip of the timezone for the start date
    var startDate = experiment[LABKEY.icemr.tracking.startDate];
    experiment[LABKEY.icemr.tracking.startDate] = LABKEY.icemr.stripTimeZoneDate(startDate);

    // save off client callbacks
    LABKEY.icemr.saveDay0Success = success;
    LABKEY.icemr.saveDay0Failure = failure;

    //
    // start upload -> upload flask, then upload runs as part of a batch
    // with each flask as a material input
    //
    LABKEY.icemr.tracking.uploadFlasks(experiment, flasks);
};

LABKEY.icemr.tracking.onInsertFlasksSuccess = function(result){
    //
    // establish materials as inputs to the run and upload
    //
    var materialInputs = [];
    for (var i = 0; i < result.rows.length; i++)
    {
        //
        // create a material from the flask we just inserted
        //
        materialInputs.push({
            id : result.rows[i]['rowId'] || result.rows[i]['rowid'],
            role : "Flask " + (i+1)
        });
    }

    var run = new LABKEY.Exp.Run();
    var exp = LABKEY.icemr.tracking.experiments[0];

    run.materialInputs = materialInputs;
    run.properties = exp;
    run.name = exp[LABKEY.icemr.tracking.patient];

    LABKEY.page.batch.runs = [];
    LABKEY.page.batch.runs.push(run);
    LABKEY.setDirty(true);

    LABKEY.Experiment.saveBatch({
        assayId : LABKEY.page.assay.id,
        batch : LABKEY.page.batch,
        success : LABKEY.icemr.saveDay0Success,
        failure : LABKEY.icemr.saveDay0Failure
    });
};

LABKEY.icemr.tracking.fetchFlasks = function (success) {
    var flasks = new LABKEY.Exp.SampleSet( {name: LABKEY.icemr.tracking.interface.getFlasksSampleSetName()});
    flasks.getDomain({
        success : success || LABKEY.icemr.tracking.onFlasksDomainReady,
        failure : LABKEY.icemr.tracking.onFlasksFailure
    });
};


LABKEY.icemr.tracking.uploadFlasks = function(experiment, flasks) {
    //
    // save experiment for context
    //
    LABKEY.icemr.tracking.experiments = [];
    LABKEY.icemr.tracking.experiments.push(experiment);

    LABKEY.icemr.tracking.interface.uploadFlasks(
            flasks,
            LABKEY.icemr.tracking.onInsertFlasksSuccess,
            LABKEY.icemr.saveDay0Failure);
};

LABKEY.icemr.tracking.getSelectedExperiments = function(schemaName, queryName, selectionKey, clientSuccess){
    LABKEY.Query.selectRows({
        schemaName: schemaName,
        queryName: queryName,
        selectionKey: selectionKey,
        showRows: "selected",
        columns: [LABKEY.icemr.tracking.rowId, LABKEY.icemr.tracking.protocolId, LABKEY.icemr.tracking.batchId,
            LABKEY.icemr.tracking.protocolName],
        success: function (data) {
            if (data.rows && data.rows.length > 0){
                LABKEY.icemr.tracking.experiments = data.rows;
                // protocol name is the same across all experiments for a given assay
                LABKEY.icemr.tracking.getFieldConfigs(
                        LABKEY.icemr.tracking.experiments[0][LABKEY.icemr.tracking.protocolName], clientSuccess);
            }
            else {
                LABKEY.icemr.tracking.onLoadBatchFailure();
            }
        },
        failure: LABKEY.icemr.tracking.onLoadBatchFailure
    });
};

//
// Load experiment information for all the selected batches
// When the process is done, LABKEY.icemr.tracking.batches
// will be loaded
//
LABKEY.icemr.tracking.getAllRunData = function(success)
{
    if (LABKEY.icemr.tracking.experiments.length <= 0)
        LABKEY.icemr.tracking.onLoadBatchFailure();

    LABKEY.icemr.tracking.onBatchesReady = success;
    LABKEY.icemr.tracking.batches = [];
    LABKEY.icemr.tracking.getRunData();
};

LABKEY.icemr.tracking.onLoadBatchSuccess = function(batch){
    if (batch == undefined) {
        LABKEY.icemr.tracking.onLoadBatchFailure();
    }
    else {
        LABKEY.icemr.tracking.batches.push(batch);
        if (LABKEY.icemr.tracking.experiments.length == LABKEY.icemr.tracking.batches.length) {
            // we are done loading batches, so call the client callback
            LABKEY.icemr.tracking.onBatchesReady(LABKEY.icemr.tracking.batches);
        }
        else {
            LABKEY.icemr.tracking.getRunData();
        }
    }
};

LABKEY.icemr.tracking.getRunData = function(){
    var exp = LABKEY.icemr.tracking.experiments[LABKEY.icemr.tracking.batches.length];
    LABKEY.icemr.tracking.assayId = exp[LABKEY.icemr.tracking.protocolId];
    LABKEY.Experiment.loadBatch({
        assayId : exp[LABKEY.icemr.tracking.protocolId],
        batchId : exp[LABKEY.icemr.tracking.batchId],
        success : LABKEY.icemr.tracking.onLoadBatchSuccess,
        failure : LABKEY.icemr.tracking.onLoadBatchFailure
    });
};

//
// generate an excel template file and autopopulate
// with all the flasks for all experiments specified in Day 0
// that have not had their maintenance stopped
//
LABKEY.icemr.tracking.getDailyUploadTemplate = function(measurementDate)
{
    var rows = [];
    var headerRow = LABKEY.icemr.tracking.buildHeaderRow();

    for (var i = 0; i < LABKEY.icemr.tracking.batches.length; i++)
    {
        var batch = LABKEY.icemr.tracking.batches[i];

        // this should never happen
        if (batch.runs.length == 0 || batch.runs[0] == undefined)
            return LABKEY.icemr.showError(LABKEY.icemr.tracking.errAssayTitle, LABKEY.icemr.tracking.errAssayMissingRun, failure);

        var run = batch.runs[0];

        // add a single value for the experiment ID
        rows.push(LABKEY.icemr.tracking.buildExperimentRow(run));
        // add a header row (same header across all experiments)
        rows.push(headerRow);
        // add a row per flask for this experiment
        var flasks = LABKEY.icemr.tracking.getDay0Flasks(run);
        for (var j = 0; j < flasks.length; j++)
        {
            // skip any flasks whose daily maintennce has been stopped
            if (flasks[j][LABKEY.icemr.flask.maintenanceStopped] != null)
                continue;

            rows.push(LABKEY.icemr.tracking.buildDataRow(flasks[j], measurementDate));
        }

        // add a blank row to separate experiments
        rows.push([]);
    }

    //
    // build up our spreadsheet object
    //
    var spreadsheet = {};
    spreadsheet.fileName = LABKEY.icemr.tracking.dailyUploadTemplateFilename;
    spreadsheet.sheets = [];
    spreadsheet.sheets.push( {
        name : 'Sheet1',
        data : rows
    });
    LABKEY.Utils.convertToExcel(spreadsheet);
};

LABKEY.icemr.tracking.buildHeaderRow = function(){
    var columns = [];
    for (var i = 0; i < LABKEY.icemr.tracking.resultFieldConfigs.length; i++)
    {
        var cfg = LABKEY.icemr.tracking.resultFieldConfigs[i];

        // don't put calculated fields in the template
        // undone: if nobody uses dataIndex then remove
        if (cfg.name == LABKEY.icemr.tracking.dateIndex)
            continue;

        columns.push(cfg.name);
    }

    return columns;
};

LABKEY.icemr.tracking.buildExperimentRow = function(run) {
    var columns = [];
    columns.push(LABKEY.icemr.tracking.experiment + ": " + run.properties[LABKEY.icemr.tracking.experiment]);
    return columns;
};

LABKEY.icemr.tracking.buildDataRow = function(flask, measurementDate){
    var columns = [];

    for (var i = 0; i < LABKEY.icemr.tracking.resultFieldConfigs.length; i++)
    {
        var cfg = LABKEY.icemr.tracking.resultFieldConfigs[i];
        var data = null;

        // don't put calculated fields in the template
        if (cfg.name == LABKEY.icemr.tracking.dateIndex)
            continue;

        //
        // just set measurement date and sample - client doesn't want
        // day0 data in the form
        //
        if (cfg.name == LABKEY.icemr.tracking.measurementDate)
        {
            data = measurementDate;
        }
        else
        if (cfg.name == LABKEY.icemr.tracking.sample)
        {
            data = flask[LABKEY.icemr.tracking.sample];
        }

        if (data == null)
            data = '';

        columns.push(data);
    }

    return columns;
};

LABKEY.icemr.tracking.makeDailyResult = function(experimentId, flasks) {
    var dailyResult = {};
    dailyResult[LABKEY.icemr.tracking.experiment] = experimentId;
    dailyResult[LABKEY.icemr.tracking.flasks] = flasks;
    return dailyResult;
};


//
// Daily Excel file upload handling
//
LABKEY.icemr.tracking.getProcessDailyFileUploadCallbackWrapper = function(fn){
    return function(content, format)
    {
        if (!content)
        {
            LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyUploadTitle, LABKEY.icemr.tracking.errDailyUploadFileNoContent);
            return;
        }

        if (!content.sheets || content.sheets.length == 0)
        {
            LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyUploadTitle, LABKEY.icemr.tracking.errDailyUploadFileNoSheets);
            return;
        }

        var data = content.sheets[0].data;
        if (data.length == 0)
        {
            LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyUploadTitle, LABKEY.icemr.tracking.errDailyUploadFileNoRows);
            return;
        }

        //
        // for each experiment we have:
        // experiment id row
        // header row
        // n flask rows
        // blank row
        //
        var rowType = LABKEY.icemr.tracking.rowTypeExperiment;
        var dailyResults = [];
        var flasks = null;
        var experimentId = null;
        var columnMappings = null;
        var experimentIdPrefix = LABKEY.icemr.tracking.experiment + ": ";

        for (var rowIdx = 0; rowIdx < data.length; rowIdx++)
        {
            var row = data[rowIdx];
            if (LABKEY.icemr.tracking.isBlankRow(row))
            {
                if (experimentId && flasks)
                   dailyResults.push(LABKEY.icemr.tracking.makeDailyResult(experimentId, flasks));

                experimentId = null;
                flasks = null;
                rowType = LABKEY.icemr.tracking.rowTypeExperiment;
            }
            else
            if (rowType == LABKEY.icemr.tracking.rowTypeExperiment)
            {
                experimentId = row[0].substring(experimentIdPrefix.length);
                flasks = [];
                rowType = LABKEY.icemr.tracking.rowTypeHeader;
            }
            else
            if (rowType == LABKEY.icemr.tracking.rowTypeHeader)
            {
                if (null == columnMappings)
                {
                    // get field config to row index mappings from the header row
                    columnMappings = LABKEY.icemr.tracking.getColumnMappings(row);

                    // ensure that the file has the expected columns
                    if (!LABKEY.icemr.tracking.verifyDailyFileUpload(columnMappings))
                    {
                        LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyUploadTitle, LABKEY.icemr.tracking.errDailyUploadFileInvalidHeader);
                        return;
                    }
                }
                rowType = LABKEY.icemr.tracking.rowTypeFlask;
            }
            else
            if (rowType == LABKEY.icemr.tracking.rowTypeFlask)
            {
                var flask = {};
                for (var colIdx = 0; colIdx < columnMappings.length; colIdx++)
                {
                    var map = columnMappings[colIdx];
                    flask[map.name] = row[map.index];
                }
                flasks.push(flask);
            }
        }

        // take care of the case when there is no blank row after the last experiment data
        if (experimentId && flasks)
            dailyResults.push(LABKEY.icemr.tracking.makeDailyResult(experimentId, flasks));

        if (fn)
            fn.call(this, dailyResults)
    }
};

LABKEY.icemr.tracking.isBlankRow = function(row){
    var value = row[0];
    if (!value || value == "")
        return true;
    return false;
};

//
// verify that the header (row[0]) of data matches
// our result field schema for the adaptation assay
// we currently do a strict mapping except we don't
// require the file to include columns for data
// we calculate anyway
//
LABKEY.icemr.tracking.verifyDailyFileUpload = function(columnMappings){
    var i;

    // verify that we have a column heading for each field config
    for (i = 0; i < LABKEY.icemr.tracking.resultFieldConfigs.length; i++)
    {
        var config = LABKEY.icemr.tracking.resultFieldConfigs[i];

        // skip internal or calculated fields
        if (config.name == LABKEY.icemr.tracking.dateIndex)
            continue;

        if (!LABKEY.icemr.isNameInSet(config.name, columnMappings))
            return false;
    }

    // verify we have a field config for each column heading
    for (i = 0; i < columnMappings.length; i++)
    {
        if (!LABKEY.icemr.isNameInSet(columnMappings[i].name, LABKEY.icemr.tracking.resultFieldConfigs))
            return false;
    }

    // verification passed
    return true;
};

LABKEY.icemr.tracking.getColumnMappings = function(header){
    var columnMappings = [];
    for (var i = 0; i < header.length; i++)
    {
        var column = {};
        column["name"] = header[i];
        column["index"] = i;
        columnMappings.push(column);
    }

    return columnMappings;
};

LABKEY.icemr.tracking.processDailyFileUpload = function(result, success) {
    var data = new LABKEY.Exp.Data(result);

    if (!data.content)
    {
        data.getContent({
            format: 'jsonTSV',
            success: LABKEY.icemr.tracking.getProcessDailyFileUploadCallbackWrapper(success),
            failure: function (error, format) {
                Ext4.Msg.alert("Upload Failed", "An error occurred while fetching the contents of the data file.");
            }
        })
    }
};

LABKEY.icemr.tracking.findBatch = function(experiment) {
    var batch = null;
    if (LABKEY.icemr.tracking.batches){
        for (var i = 0; i < LABKEY.icemr.tracking.batches.length; i++) {
                batch = LABKEY.icemr.tracking.batches[i];
            if (batch.runs.length > 0)
            {
                var run = batch.runs[0];
                if (run.properties[LABKEY.icemr.tracking.experiment] == experiment)
                   return batch;
            }
        }
    }
    return batch;
};

/**
 * These functions are used for saving data.  The dailyResults arg is an array of {experimentId, flasks[]} objects
 * filled in from the dailyUpload form.
 */
LABKEY.icemr.tracking.saveDaily = function(dailyResults, success, failure) {
    //
    // add results to the existing run for all batches
    //
    if (!dailyResults)
        throw "You must provide a non-empty array of results";

    if (!success)
        throw "You must provide a Success callback function";

    if (dailyResults.length == 0)
        return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyTitle, LABKEY.icemr.tracking.errDailyNoResults, failure);

    for (var i = 0; i < dailyResults.length; i++)
    {
        var dailyResult = dailyResults[i];
        var experiment = dailyResult[LABKEY.icemr.tracking.experiment];
        var batch = LABKEY.icemr.tracking.findBatch(experiment);
        if (null == batch)
        {
            return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyTitle, LABKEY.icemr.tracking.errDailyInvalidExperiment + experiment, failure);
        }

        if (batch.runs.length == 0 || batch.runs[0] == undefined)
            return LABKEY.icemr.showError(LABKEY.icemr.tracking.errAssayTitle, LABKEY.icemr.tracking.errAssayMissingRun, failure);

        var run = batch.runs[0];
        var flasks = dailyResult[LABKEY.icemr.tracking.flasks];

        if (!LABKEY.icemr.tracking.verifyDailyData(run, flasks, failure))
        {
            // error shown in verifyDailyData function
            return;
        }

        run.dataRows = run.dataRows || [];
        var materialInputs = run.materialInputs;
        for (var j = 0; j < flasks.length; j++)
        {
            LABKEY.icemr.tracking.computeCalculatedValues(run, flasks[j]);
            LABKEY.icemr.tracking.updateMaterialInputs(materialInputs, flasks[j]);
            run.dataRows.push(flasks[j]);
        }
    }

    LABKEY.Experiment.saveBatches({
        assayId : LABKEY.icemr.tracking.assayId,
        batches : LABKEY.icemr.tracking.batches,
        success : success,
        failure : failure
    });
};

/**
 * 
 * Takes an array of daily results as input and verifies
 * that we only have data for the flasks that were
 * uploaded as day0 data for the specified batch.  It is okay for the dailyResults
 * to not include data for all flasks established at day0.  However,
 * we will error if we see flask data for a flask that was not
 * uploaded at day0.
 *
 * other rules:
 * don't allow upload of a data if the date already exists
 * don't allow 0 parasitemia value if a growth test is started
 *
 */
LABKEY.icemr.tracking.verifyDailyData = function(run, dailyResults, failure){
    var day0Flasks = LABKEY.icemr.tracking.getDay0Flasks(run);

    // cannot have more flasks to upload than we had on day 0
    if (dailyResults.length > day0Flasks.length)
        return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyTitle, LABKEY.icemr.tracking.errDailyTooManyFlasks, failure);

    // all flasks that we want to upload must exist in the day0Flasks
    for (var i = 0; i < dailyResults.length; i++)
    {
        var result = dailyResults[i];
        var sampleId = result[LABKEY.icemr.tracking.sample];
        var found = false;
        for (var j = 0; j < day0Flasks.length; j++)
        {
            if (sampleId == day0Flasks[j][LABKEY.icemr.tracking.sample])
            {
                if (day0Flasks[j][LABKEY.icemr.flask.maintenanceStopped] == null)
                {
                    found = true;
                }
                break;
            }
        }

        if (!found)
            return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyTitle, LABKEY.icemr.tracking.errDailyInvalidFlaskDefined + sampleId, failure);

        // verify that parasitemia value is not zero if we are starting a growtth test
        if (result[LABKEY.icemr.tracking.growthFoldTestInitiated] &&
                (result[LABKEY.icemr.tracking.parasitemia] == 0))
            return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyTitle, LABKEY.icemr.tracking.errDailyInvalidZeroParasitemia + sampleId, failure);

        // ensure we don't have data for this data already
        if (LABKEY.icemr.tracking.maintenanceDateAlreadyExists(run, result[LABKEY.icemr.tracking.measurementDate]))
            return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyTitle, LABKEY.icemr.tracking.errDailyInvalidMeasurementDate + sampleId, failure);
    }

    // verification succeeded
    return true;
};

// consider: if iterating over all the rows is too slow then we can always just sort the dates
LABKEY.icemr.tracking.maintenanceDateAlreadyExists = function(run, measurementDate){
    var dataRows = run.dataRows;

    for (var i = 0; i < dataRows.length; i++)
    {
        if (LABKEY.icemr.compareDate(measurementDate, dataRows[i][LABKEY.icemr.tracking.measurementDate]))
            return true;
    }

    return false;
};

//
// Store off the date index for now.  Note that this field no longer needs
// to be stored in the database as we calculate the value now in our visualization
// queries
//
LABKEY.icemr.tracking.computeCalculatedValues = function(run, dailyResult){
    // strip timezone of date string
    var date = dailyResult[LABKEY.icemr.tracking.measurementDate];
    dailyResult[LABKEY.icemr.tracking.measurementDate] = LABKEY.icemr.stripTimeZoneDate(date);

    // note that dateIndex is no longer used; we calculate this on the fly now
    var startDate = new Date(run.properties[LABKEY.icemr.tracking.startDate]);
    var measurementDate = new Date(dailyResult[LABKEY.icemr.tracking.measurementDate]);
    dailyResult[LABKEY.icemr.tracking.dateIndex] = LABKEY.icemr.getDateIndex(startDate, measurementDate);
};

//
// Update the flask (material input) with the latest data from the submitted
// daily result
//
LABKEY.icemr.tracking.updateMaterialInputs = function(materialInputs, dailyResult) {

    var sampleId = dailyResult[LABKEY.icemr.tracking.sample];
    var measurementDate = dailyResult[LABKEY.icemr.tracking.measurementDate];
    var materialInput = LABKEY.icemr.tracking.findMaterialInput(materialInputs, sampleId);

    if (materialInput && materialInput.properties) {
        var flask = materialInput.properties;
        flask[LABKEY.icemr.flask.maintenanceDate] = measurementDate;

        // if maintenance was stopped, record the date
        if (dailyResult[LABKEY.icemr.tracking.flaskMaintenanceStopped])
            flask[LABKEY.icemr.flask.maintenanceStopped] = measurementDate;

        // growth test started?
        var growthTest = dailyResult[LABKEY.icemr.tracking.growthFoldTestInitiated];
        LABKEY.icemr.tracking.storeGrowthTestParasitemia(growthTest, flask, dailyResult, false);

        // growth test finished?
        growthTest = dailyResult[LABKEY.icemr.tracking.growthFoldTestFinished];
        LABKEY.icemr.tracking.storeGrowthTestParasitemia(growthTest, flask, dailyResult, true);

        // give the specific assay a chance to add flask properties
        LABKEY.icemr.tracking.interface.getFlaskUpdates(dailyResult, flask);
    }
};

LABKEY.icemr.tracking.findMaterialInput = function(materialInputs, sampleId){
    for (var i = 0; i < materialInputs.length; i++)
    {
        var properties = materialInputs[i].properties;
        if (properties[LABKEY.icemr.flask.sample] == sampleId)
            return materialInputs[i];
    }

    return null;
};

LABKEY.icemr.tracking.storeGrowthTestParasitemia = function(growthTest, flask, dailyResult, finished){
    if (growthTest == '1' || growthTest == '2' || growthTest == '3')
    {
        var parasitemia = dailyResult[LABKEY.icemr.tracking.parasitemia];

        if (finished)
            flask[LABKEY.icemr.flask.finishParasitemia + growthTest] = parasitemia;
        else
            flask[LABKEY.icemr.flask.startParasitemia + growthTest] = parasitemia;

        // we also record the start and finish dates for growth test 1
        if (growthTest == '1')
        {
            if (finished)
                flask[LABKEY.icemr.flask.finishDate1] = dailyResult[LABKEY.icemr.tracking.measurementDate];
            else
                flask[LABKEY.icemr.flask.startDate1] = dailyResult[LABKEY.icemr.tracking.measurementDate];

        }
    }
};

// if the new flask has the value then set it, otherwise
// use the old value
LABKEY.icemr.tracking.setRowProperty = function(name, row, newFlask, oldFlask){
    if (newFlask[name] != null)
        row[name] = newFlask[name];
    else
        row[name] = oldFlask[name];
};

//
// Map the normalized column name to the casing that the row object expects.  We do this because
// SQL Server and Postgress don't necessarily return the same column name casing in the
// metadata
//
//
LABKEY.icemr.tracking.makeSyncFieldsKeyMap = function(row){
    var keyMap = {};
    var syncFields = LABKEY.icemr.tracking.getSyncFields();

    for (var key in row)
    {
        var normalized = key.toLowerCase();

        // be sure to add in our primary key field
        if (normalized == LABKEY.icemr.flask.sample.toLowerCase())
        {
            keyMap[LABKEY.icemr.flask.sample] = key;
            continue;
        }

        for (var i = 0 ; i < syncFields.length; i ++)
        {
            if (normalized == syncFields[i].toLowerCase())
            {
                keyMap[syncFields[i]] = key;
                break;
            }
        }
    }

    return keyMap;
};

LABKEY.icemr.tracking.setDefaultValues = function(metaType, config){
    // take care of common defaults between selection and adaptation here
    if (config.name == LABKEY.icemr.flask.foldIncrease + '1' ||
            config.name == LABKEY.icemr.flask.foldIncrease + '2' ||
            config.name == LABKEY.icemr.flask.foldIncrease + '3')
    {
        config.value = LABKEY.icemr.flask.defaultFoldIncrease;
    }

    // now take care of assay-specific defaults
    LABKEY.icemr.tracking.interface.setDefaultValues(metaType, config);
};

LABKEY.icemr.tracking.getSyncFields = function()
{
    // return the union of the common sync fields and the assay-specific
    // sync fields
    var newSyncFields = [];
    var i;
    for (i = 0; i < LABKEY.icemr.flask.syncFields.length; i++)
        newSyncFields.push(LABKEY.icemr.flask.syncFields[i]);

    var specificSyncFields = LABKEY.icemr.tracking.interface.getSyncFields();

    for (i = 0; i < specificSyncFields.length; i++)
        newSyncFields.push(specificSyncFields[i]);

    return newSyncFields;
};

LABKEY.icemr.tracking.dailyMaintenance = function(dataRegion, dataRegionName)
{
    window.location = LABKEY.ActionURL.buildURL('icemr', 'DailyUpload.view', null, {
        selectionKey : dataRegion.selectionKey,
        queryName : dataRegion.queryName,
        schemaName : dataRegion.schemaName
     });
};





