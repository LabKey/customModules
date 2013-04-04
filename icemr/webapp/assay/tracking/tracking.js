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
 * This implements the flask tracking assay base code that is common to both
 * adaptation and selection instances.  Specific implementations of adaptation or selection
 * assay behavior can be found in adaptation.js and selection.js.  The UI layer should only need
 * to know about the LABKEY.icemr.tracking namespace
 *
 */
// -------------------------------------------------------------------
// namespaces
// -------------------------------------------------------------------
Ext.namespace("LABKEY.icemr.tracking");
Ext.namespace("LABKEY.icemr.flask");

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
        getFlasks : function () { throw "not implemented!" },
        getSyncFields : function() { throw "not implemented!" },
        setDefaultValues : function(metaTypa, config) {throw "not implemented!"},
        getFlaskUpdates : function(dailyResult, flask) { throw "not implemented!"},
        uploadFlasks : function(flasks, success, failure) {throw "not implemented!"},
        saveDaily : function(flasks, success, failure) { throw "not implemented!"},
        getVisQuery : function () { throw "not implemented"},
        getCalcQuery : function () { throw "not implemented"}
    };
};

// -------------------------------------------------------------------
// configuration errors
// -------------------------------------------------------------------
// undone: add the sample set name as a parameter here since we have more than one sample set now (drug or culture flasks)
// undone: having the err title constants and the err message constants in different places could be confusing.
LABKEY.icemr.tracking.errConfigMissingFlask = "Could not find the appropriate Sample Set. Please see your LabKey administrator.";
LABKEY.icemr.tracking.errAssayTitle = "Assay Runtime Error";
LABKEY.icemr.tracking.errAssayMissingRun = "Could not find a matching Day 0 experiment to update";

LABKEY.icemr.tracking.errDailyTitle = "Daily Maintenance Error";
LABKEY.icemr.tracking.errDailyNoResults = "You must include at least one result to upload";
LABKEY.icemr.tracking.errDailyTooManyFlasks = "Invalid attempt to upload data for flasks that were not defined in Day 0";
LABKEY.icemr.tracking.errDailyInvalidFlaskDefined = "Invalid flask specified.  The following flask was not found in the Day 0 data or maintenance was already stopped: ";
LABKEY.icemr.tracking.errDailyInvalidZeroParasitemia = "You must specify a non-zero Parasitemia value when a growth fold test is initiated for flask: ";
LABKEY.icemr.tracking.errDailyInvalidMeasurementDate = "Data for the specified measurement date already exists for flask: ";

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
LABKEY.icemr.tracking.adaptationAssay = 'Culture Adaptation';
LABKEY.icemr.tracking.selectionAssay = 'Drug Selection';
LABKEY.icemr.tracking.parasitemia = 'Parasitemia';
LABKEY.icemr.tracking.patient = 'PatientID';
LABKEY.icemr.tracking.sample = 'SampleID';
LABKEY.icemr.tracking.experiment = 'ExperimentID';
LABKEY.icemr.tracking.contamination = 'Contamination';
LABKEY.icemr.tracking.mycotest = 'MycoTestResult';
LABKEY.icemr.tracking.interestingResult = 'InterestingResult';
LABKEY.icemr.tracking.stage = 'Stage';
LABKEY.icemr.tracking.pRBC = 'PatientpRBCs';
LABKEY.icemr.tracking.cultureMedia = 'CultureMedia';
LABKEY.icemr.tracking.stageOptions =  [['none'], ['gametocyte'], ['rings'], ['trophozoites'], ['schizonts'], ['mixed']];
LABKEY.icemr.tracking.pRBCOptions = [['washed'], ['unwashed']];
LABKEY.icemr.tracking.cultureMediaOptions = [['serum'], ['Albumax']];
LABKEY.icemr.tracking.yesNoOptions = [['Yes'], ['No']];
LABKEY.icemr.tracking.positiveNegativeTestOptions = [['Positive'], ['Negative'], ['No Test']];
LABKEY.icemr.tracking.positiveNegativeOptions = [['Positive'], ['Negative'], ['No']];
LABKEY.icemr.tracking.oneTwoThreeOptions = [['1'], ['2'], ['3'], ['No']];
LABKEY.icemr.tracking.dateIndex = 'DateIndex';
LABKEY.icemr.tracking.measurementDate = 'MeasurementDate';
LABKEY.icemr.tracking.growthFoldTestInitiated = 'GrowthFoldTestInitiated';
LABKEY.icemr.tracking.growthFoldTestFinished = 'GrowthFoldTestFinished';
LABKEY.icemr.tracking.flaskMaintenanceStopped = 'FlaskMaintenanceStopped';
LABKEY.icemr.tracking.scientist = 'Scientist';
LABKEY.icemr.tracking.serumBatch = 'SerumBatchID';
LABKEY.icemr.tracking.albumaxBatch = 'AlbumaxBatchID';

// sample set fields common to both adaptation and selection flasks
LABKEY.icemr.flask.sample = LABKEY.icemr.tracking.sample;
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
LABKEY.icemr.flask.foldIncrease = 'FoldIncrease';
LABKEY.icemr.flask.defaultFoldIncrease = 4;

// used for excel template upload
LABKEY.icemr.tracking.dailyUploadTemplateFilename = "dailyUpload.xls";

// -------------------------------------------------------------------
// enums
// -------------------------------------------------------------------
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
    LABKEY.icemr.showError(LABKEY.icemr.errConfigTitle, LABKEY.icemr.errConfigMissingFlask);
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
}

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



LABKEY.icemr.tracking.getDay0Flasks = function() {
    if (LABKEY.icemr.tracking.run == undefined)
        return LABKEY.icemr.tracking.onLoadBatchFailure(); // we shouldn't get here

    var flasks = [];

    for (var i = 0; i < LABKEY.icemr.tracking.materialInputs.length; i++)
    {
        flasks.push(LABKEY.icemr.tracking.materialInputs[i]);
    }

    return flasks;
};

LABKEY.icemr.tracking.onLoadBatchFailure = function(response){
    LABKEY.icemr.showError(LABKEY.icemr.errAssayTitle, LABKEY.icemr.errAssayMissingRun);
};

LABKEY.icemr.tracking.decoupleMaterialInputs = function() {
    // we save all of the material input data through direct updating of the flasks
    // sample set so don't include the custom properties in the batch
    var inputs = [];
    var materialInputs = LABKEY.icemr.tracking.run.materialInputs;
    for (var j = 0; j < materialInputs.length; j++)
    {
        inputs.push(materialInputs[j].properties);
        materialInputs[j].properties = {};
    }

    LABKEY.icemr.tracking.materialInputs = inputs;
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
    run.materialInputs = materialInputs;
    run.name = LABKEY.icemr.tracking.experiment[LABKEY.icemr.tracking.patient];
    run.properties = LABKEY.icemr.tracking.experiment;

    LABKEY.page.batch.runs = [];
    LABKEY.page.batch.runs.push(run);
    LABKEY.setDirty(true);

    LABKEY.Experiment.saveBatch({
        assayId : LABKEY.page.assay.id,
        batch : LABKEY.page.batch,
        successCallback : LABKEY.icemr.saveDay0Success,
        failureCallback : LABKEY.icemr.saveDay0Failure
    });
};

LABKEY.icemr.tracking.uploadFlasks = function(experiment, flasks) {
    //
    // save experiment for context
    //
    LABKEY.icemr.tracking.experiment = experiment;
    LABKEY.icemr.tracking.interface.uploadFlasks(
            flasks,
            LABKEY.icemr.tracking.onInsertFlasksSuccess,
            LABKEY.icemr.saveDay0Failure);
};

LABKEY.icemr.tracking.getRunDataCallbackWrapper = function(fn){
    return function(batch) {
        var runs = batch.runs;
        var runData;

        for (var i = 0; i < runs.length; i++)
        {
            if (LABKEY.icemr.tracking.runId == runs[i].id)
            {
                LABKEY.icemr.tracking.batch = batch;
                LABKEY.icemr.tracking.run = batch.runs[i];
                runData = LABKEY.icemr.tracking.run.properties;
                LABKEY.icemr.tracking.decoupleMaterialInputs();
                break;
            }
        }

        if (runData == undefined)
        {
            onLoadBatchFailure();
        }
        else
        {
            if (fn)
                fn.call(this, runData)
        }
    }
}

LABKEY.icemr.tracking.getRunData = function(protocolId, batchId, rowId, success){
    LABKEY.icemr.tracking.runId = rowId;
    LABKEY.icemr.tracking.assayId = protocolId;

    LABKEY.Experiment.loadBatch({
        assayId : protocolId,
        batchId : batchId,
        success : LABKEY.icemr.tracking.getRunDataCallbackWrapper(success),
        failure : LABKEY.icemr.tracking.onLoadBatchFailure
    });
};

//
// generate an excel template file and autopopulate
// with the all the flasks specified in Day 0 for this run
// that have not had their maintenance stopped
//
LABKEY.icemr.tracking.getDailyUploadTemplate = function(measurementDate){
    var flasks = LABKEY.icemr.tracking.getDay0Flasks();
    var rows = [];

    rows.push(LABKEY.icemr.tracking.buildHeaderRow());
    for (var i = 0; i < flasks.length; i++)
    {
        // skip any flasks whose daily maintennce has been stopped
        if (flasks[i][LABKEY.icemr.flask.maintenanceStopped] != null)
            continue;

        rows.push(LABKEY.icemr.tracking.buildDataRow(flasks[i], measurementDate));
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

        // get field config to row index mappings from the header row
        var columnMappings = LABKEY.icemr.tracking.getColumnMappings(data[0]);

        // ensure that the file has the expected columns
        if (!LABKEY.icemr.tracking.verifyDailyFileUpload(columnMappings))
        {
            LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyUploadTitle, LABKEY.icemr.tracking.errDailyUploadFileInvalidHeader);
            return;
        }

        // iterate over the row data, start at 1 to skip the header row
        var dailyResults = [];
        for (var rowIdx = 1; rowIdx < data.length; rowIdx++)
        {
            var dailyResult = LABKEY.icemr.tracking.createDaily();
            var row = data[rowIdx];
            for (var colIdx = 0; colIdx < columnMappings.length; colIdx++)
            {
                var map = columnMappings[colIdx];
                dailyResult[map.name] = row[map.index];
            }
            dailyResults.push(dailyResult);
        }

        if (fn)
            fn.call(this, dailyResults)
    }
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
            successCallback: LABKEY.icemr.tracking.getProcessDailyFileUploadCallbackWrapper(success),
            failureCallback: function (error, format) {
                Ext.Msg.alert("Upload Failed", "An error occurred while fetching the contents of the data file.");
            }
        })
    }
}

/**
 * These functions are used for saving data
 */
LABKEY.icemr.tracking.saveDaily = function(dailyResults, success, failure) {
    //
    // add results to the existing run
    //
    //
    if (!dailyResults)
        throw "You must provide a non-empty array of results";

    if (!success)
        throw "You must provide a Success callback function";

    if (dailyResults.length == 0)
        return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyTitle, LABKEY.icemr.tracking.errDailyNoResults, failure);

    var batch = LABKEY.icemr.tracking.batch;
    var run = LABKEY.icemr.tracking.run;
    var i;

    if (batch == undefined || run == undefined)
        return LABKEY.icemr.showError(LABKEY.icemr.tracking.errAssayTitle, LABKEY.icemr.tracking.errAssayMissingRun, failure);

    if (!LABKEY.icemr.tracking.verifyDailyData(dailyResults, failure))
    {
        return;
    }

    run.dataRows = run.dataRows || [];

    var flasks = [];
    for (i = 0; i < dailyResults.length; i++)
    {
        LABKEY.icemr.tracking.computeCalculatedValues(dailyResults[i]);
        //
        // see if we have any flask sample set data that we need to update
        // before saving the batch
        //
        var flask = LABKEY.icemr.tracking.getFlaskUpdates(dailyResults[i]);
        if (flask != null)
            flasks.push(flask);

        run.dataRows.push(dailyResults[i]);
    }

    // behavior now is dependent on whether the flask is adaptation or selection
    LABKEY.icemr.tracking.interface.saveDaily(flasks, success, failure);
};

/**
 * 
 * Takes an array of daily results as input and verifies
 * that we only have data for the flasks that were
 * uploaded as day0 data.  It is okay for the dailyResults
 * to not include data for all flasks established at day0.  However,
 * we will error if we see flask data for a flask that was not
 * uploaded at day0.
 *
 * other rules:
 * don't allow upload of a data if the date already exists
 * don't allow 0 parasitemia value if a growth test is started
 *
 */
LABKEY.icemr.tracking.verifyDailyData = function(dailyResults, failure){
    var day0Flasks = LABKEY.icemr.tracking.getDay0Flasks();

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
            return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyTitle, LABKEY.icemr.tracking.errDailyInvalidFlaskDefined + sampleId, failure)

        // verify that parasitemia value is not zero if we are starting a growtth test
        if (result[LABKEY.icemr.tracking.growthFoldTestInitiated] &&
                (result[LABKEY.icemr.tracking.parasitemia] == 0))
            return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyTitle, LABKEY.icemr.tracking.errDailyInvalidZeroParasitemia + sampleId, failure);

        // ensure we don't have data for this data already
        if (LABKEY.icemr.tracking.maintenanceDateAlreadyExists(result[LABKEY.icemr.tracking.measurementDate]))
            return LABKEY.icemr.showError(LABKEY.icemr.tracking.errDailyTitle, LABKEY.icemr.tracking.errDailyInvalidMeasurementDate + sampleId, failure);
    }

    // verification succeeded
    return true;
};

// consider: if iterating over all the rows is too slow then we can always just sort the dates
LABKEY.icemr.tracking.maintenanceDateAlreadyExists = function(measurementDate){
    var dataRows = LABKEY.icemr.tracking.run.dataRows;

    for (var i = 0; i < dataRows.length; i++)
    {
        if (LABKEY.icemr.compareDate(measurementDate, dataRows[i][LABKEY.icemr.tracking.measurementDate]))
            return true;
    }

    return false;
};

//
// Store off the date index for now.  Consider using this
// in your queries instead of calculating from the start date
//
LABKEY.icemr.tracking.computeCalculatedValues = function(dailyResult){
    var startDate = new Date(LABKEY.icemr.tracking.run.properties[LABKEY.icemr.flask.startDate]);
    var measurementDate = new Date(dailyResult[LABKEY.icemr.tracking.measurementDate]);
    dailyResult[LABKEY.icemr.tracking.dateIndex] = LABKEY.icemr.getDateIndex(startDate, measurementDate);
};

//
// store data in the flask based on events that happen
// for this daily maintenance.  At the very least we always
// record the date of the last daily maintenance
//
LABKEY.icemr.tracking.getFlaskUpdates = function(dailyResult){
    // setup a flask with a sample id and the most recent maintenance date
    var flask = {};
    var measurementDate = dailyResult[LABKEY.icemr.tracking.measurementDate];

    flask[LABKEY.icemr.flask.sample] = dailyResult[LABKEY.icemr.tracking.sample];
    flask[LABKEY.icemr.flask.maintenanceDate] = measurementDate;

    // if maintenance was stopped, record the date
    // consider: this date may be redundant since we also save the last maintenance
    // consider: date above
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

    return flask;
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

LABKEY.icemr.tracking.findFlaskInMaterialInputs = function(sampleId){
    var materialInputs = LABKEY.icemr.tracking.materialInputs;
    for (var j = 0; j < materialInputs.length; j++)
    {
        var properties = materialInputs[j];
        if (properties[LABKEY.icemr.flask.sample] == sampleId)
            return properties;
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

LABKEY.icemr.tracking.saveBatch = function() {
    LABKEY.Experiment.saveBatch({
        assayId : LABKEY.icemr.tracking.assayId,
        batch : LABKEY.icemr.tracking.batch,
        successCallback : LABKEY.icemr.tracking.updateContext.success,
        failureCallback : LABKEY.icemr.tracking.updateContext.failure
    });
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

//
// given a rowset, copy over the latest updated values to our material inputs
//
LABKEY.icemr.tracking.syncMaterialInputs = function(rows, keyMap)
{
    for (var i  = 0; i < rows.length; i++)
    {
        var row = rows[i];
        var flask = LABKEY.icemr.tracking.findFlaskInMaterialInputs(row[keyMap[LABKEY.icemr.flask.sample]]);
        var syncFields = LABKEY.icemr.tracking.getSyncFields();

        // just sync common flask fields here
        for (var j = 0; j < syncFields.length; j++)
        {
            var key = syncFields[j];
            flask[key] = row[keyMap[key]];
        }
    }
}

LABKEY.icemr.tracking.makeUpdateRowset = function(flasks)
{
    var rows = [];

    for (var i = 0; i < flasks.length; i++)
    {
        var row = {};
        var newFlask = flasks[i];
        var oldFlask = LABKEY.icemr.tracking.findFlaskInMaterialInputs(newFlask[LABKEY.icemr.tracking.sample]);

        // set the sample id for updating
        LABKEY.icemr.tracking.setRowProperty(LABKEY.icemr.flask.sample, row, newFlask, oldFlask);
        var syncFields = LABKEY.icemr.tracking.getSyncFields();

        for (var j = 0; j < syncFields.length; j++)
        {
            var key = syncFields[j];
            LABKEY.icemr.tracking.setRowProperty(key, row, newFlask, oldFlask);
        }
        rows.push(row);
    }

    return rows;
}

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
}





