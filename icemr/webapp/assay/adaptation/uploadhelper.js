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
 */
LABKEY.requiresScript('clientapi/ext4/Util.js');
LABKEY.requiresScript('assay/schemahelper.js');
Ext.namespace("LABKEY.icemr");

LABKEY.icemr.errAssayTitle = "Assay Runtime Error";
LABKEY.icemr.errAssayMissingRun = "Could not find a matching Day 0 experiment to update";

LABKEY.icemr.errDailyTitle = "Daily Maintenance Error";
LABKEY.icemr.errDailyNoResults = "You must include at least one result to upload";
LABKEY.icemr.errDailyTooManyFlasks = "Invalid attempt to upload data for flasks that were not defined in Day 0";
LABKEY.icemr.errDailyInvalidFlaskDefined = "Invalid flask specified.  The following flask was not found in the Day 0 data or maintenance was already stopped: ";

LABKEY.icemr.errDailyUploadTitle = "Daily Upload Failed";
LABKEY.icemr.errDailyUploadFileNoContent = "The data file has no content";
LABKEY.icemr.errDailyUploadFileNoSheets = "The data file has no sheets of data";
LABKEY.icemr.errDailyUploadFileNoRows = "The data file has no rows of data";
LABKEY.icemr.errDailyUploadFileInvalidHeader = "The data file header row does not match the daily results schema";

LABKEY.icemr.errDay0Title = "Day 0 Upload Error";
LABKEY.icemr.errDay0NoFlasksDefined = "You must include at least one flask with your Day 0 data";

LABKEY.icemr.dailyUploadTemplateFilename = "dailyUpload.xls";

 /**
 * These functions manage the upload of Day 0 and daily data to LabKey Server
 */
function createExperiment()
{
    return createRecord(LABKEY.icemr.adaptation.runFieldConfigs);
}

function createFlask()
{
    return createRecord(LABKEY.icemr.flaskConfigs);
}

function createDaily()
{
    return createRecord(LABKEY.icemr.adaptation.resultFieldConfigs);
}

function createRecord(fieldConfigs)
{
    var record = {};
    for (var i = 0; i < fieldConfigs.length; i++)
    {
        record[fieldConfigs[i].name] = null;
    }

    return record;
}

/**
 * These functions are used for saving data
 */

function saveDaily(dailyResults, success, failure)
{
    //
    // add results to the existing run
    //
    //
    if (!dailyResults)
        throw "You must provide a non-empty array of results";

    if (!success)
        throw "You must provide a Success callback function";

    if (dailyResults.length == 0)
        return showError(LABKEY.icemr.errDailyTitle, LABKEY.icemr.errDailyNoResults, failure);

    var batch = LABKEY.icemr.adaptation.batch;
    var run = LABKEY.icemr.adaptation.run;
    var i;

    if (batch == undefined || run == undefined)
        return showError(LABKEY.icemr.errAssayTitle, LABKEY.icemr.errAssayMissingRun, failure);

    if (!verifyDailyData(dailyResults, failure))
    {
        return;
    }

    run.dataRows = run.dataRows || [];

    var flasks = [];
    for (i = 0; i < dailyResults.length; i++)
    {
        computeCalculatedValues(dailyResults[i]);
        //
        // see if we have any flask sample set data that we need to update
        // before saving the batch
        //
        var flask = getFlaskUpdates(dailyResults[i]);
        if (flask != null)
            flasks.push(flask);

        run.dataRows.push(dailyResults[i]);
    }

    var result = makeUpdateRowset(flasks);

    // create a new update context
    LABKEY.icemr.updateContext = {};
    LABKEY.icemr.updateContext.success = success;
    LABKEY.icemr.updateContext.failure = failure;
    LABKEY.icemr.updateContext.checkForAdaptation = result.checkForAdaptation;

    LABKEY.Query.updateRows( {
        schemaName : 'Samples',
        queryName : LABKEY.icemr.adaptation.flaskSampleSet,
        rows : result.rows,
        success : checkForAdaptation,
        failure : LABKEY.icemr.updateContext.failure
    });
}

function checkForAdaptation(data, response, options)
{
    if (!data || !(data.rows) || (0 == data.rows.length))
        return;

    var keyMap = makeSyncFieldsKeyMap(data.rows[0]);
    syncMaterialInputs(data.rows, keyMap, false);

    if (LABKEY.icemr.updateContext.checkForAdaptation)
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
            schemaName : 'assay.Adaptation.' + LABKEY.icemr.AdaptationAssayResults,
            queryName : 'adapted_numdays',
            columns : [LABKEY.icemr.flask.sample,
                LABKEY.icemr.flask.maintenanceDate,
                LABKEY.icemr.flask.adaptationDate,
                LABKEY.icemr.flask.successfulAdaptation],
            filterArray : [LABKEY.Filter.create(LABKEY.icemr.flask.sample, filterValues,
                    LABKEY.Filter.Types.IN)],
            success : saveAdaptationDate,
            failure : LABKEY.icemr.updateContext.failure
        });
    }
    else
    {
        saveBatch();
    }
}

function saveBatch()
{
    LABKEY.Experiment.saveBatch({
        assayId : LABKEY.icemr.adaptation.assayId,
        batch : LABKEY.icemr.adaptation.batch,
        successCallback : LABKEY.icemr.updateContext.success,
        failureCallback : LABKEY.icemr.updateContext.failure
    });
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
            queryName : LABKEY.icemr.adaptation.flaskSampleSet,
            rows : rows,
            success : saveDailyMaintenance,
            failure : LABKEY.icemr.updateContext.failure
        });
    }
    else
    {
        saveBatch();
    }
}

function saveDailyMaintenance(data, response, options)
{
    if (data && data.rows && data.rows.length > 0)
    {
        syncMaterialInputs(data.rows, makeSyncFieldsKeyMap(data.rows[0]), true);
        saveBatch();
    }
}

//
// Store off the date index for now.  Consider using this
// in your queries instead of calculating from the start date
//
function computeCalculatedValues(dailyResult)
{
    var startDate = new Date(LABKEY.icemr.adaptation.run.properties[LABKEY.icemr.flask.startDate]);
    var measurementDate = new Date(dailyResult[LABKEY.icemr.adaptation.measurementDate]);
    dailyResult[LABKEY.icemr.adaptation.dateIndex] = getDateIndex(startDate, measurementDate);
}

// convert milliseconds to days
function getDateIndex(start, end)
{
    return parseInt((end.getTime() - start.getTime())/(24*60*60*1000));
}

//
// make a rowset to copy from our local flasks -> rowset to update the database
//
function makeUpdateRowset(flasks)
{
    var result = {};
    result.checkForAdaptation = false;
    result.rows = [];

    for (var i = 0; i < flasks.length; i++)
    {
        var row = {};
        var newFlask = flasks[i];
        var oldFlask = findFlaskInMaterialInputs(newFlask[LABKEY.icemr.adaptation.sample]);

        // set the sample id for updating
        setRowProperty(LABKEY.icemr.flask.sample, row, newFlask, oldFlask);

        for (var j = 0; j < LABKEY.icemr.flask.syncFields.length; j++)
        {
            var key = LABKEY.icemr.flask.syncFields[j];
            setRowProperty(key, row, newFlask, oldFlask);

            // if we finished a growth test and the flask has not already adapted, then
            // we'll need to check for adaptation of this flask set before saving the batch so
            // that we can include the adaptation date
            if ( (key == LABKEY.icemr.flask.finishParasitemia + '1') ||
                 (key == LABKEY.icemr.flask.finishParasitemia + '2') ||
                 (key == LABKEY.icemr.flask.finishParasitemia + '3'))
            {
                if (row[key])
                    result.checkForAdaptation = true;
            }
        }
        result.rows.push(row);
    }

    return result;
}

//
// Map the normalized column name to the casing that the row object expects.  We do this because
// SQL Server and Postgress don't necessarily return the same column name casing in the
// metadata
//
//
function makeSyncFieldsKeyMap(row)
{
    var keyMap = {};
    for (var key in row)
    {
        var normalized = key.toLowerCase();

        // be sure to add in our primary key field
        if (normalized == LABKEY.icemr.flask.sample.toLowerCase())
        {
            keyMap[LABKEY.icemr.flask.sample] = key;
            continue;
        }

        for (var i = 0 ; i < LABKEY.icemr.flask.syncFields.length; i ++)
        {
            if (normalized == LABKEY.icemr.flask.syncFields[i].toLowerCase())
            {
                keyMap[LABKEY.icemr.flask.syncFields[i]] = key;
                break;
            }
        }
    }

    return keyMap;
}

//
// given a rowset, copy over the latest updated values to our material inputs
//
function syncMaterialInputs(rows, keyMap, syncAdaptationDateOnly)
{
    for (var i  = 0; i < rows.length; i++)
    {
        var row = rows[i];
        var flask = findFlaskInMaterialInputs(row[keyMap[LABKEY.icemr.flask.sample]]);

        if (syncAdaptationDateOnly)
        {
            flask[LABKEY.icemr.flask.adaptationDate] = row[keyMap[LABKEY.icemr.flask.adaptationDate]];
        }
        else
        {
            for (var j = 0; j < LABKEY.icemr.flask.syncFields.length; j++)
            {
                var key = LABKEY.icemr.flask.syncFields[j];
                flask[key] = row[keyMap[key]];
            }
        }
    }
}

function findFlaskInMaterialInputs(sampleId)
{
    var materialInputs = LABKEY.icemr.adaptation.materialInputs;
    for (var j = 0; j < materialInputs.length; j++)
    {
        var properties = materialInputs[j];
        if (properties[LABKEY.icemr.flask.sample] == sampleId)
            return properties;
    }
}

// if the new flask has the value then set it, otherwise
// use the old value
function setRowProperty(name, row, newFlask, oldFlask)
{
    if (newFlask[name] != null)
        row[name] = newFlask[name];
    else
        row[name] = oldFlask[name];
}

function storeGrowthTestParasitemia(growthTest, flask, dailyResult, finished)
{
    if (growthTest == '1' || growthTest == '2' || growthTest == '3')
    {
        var parasitemia = dailyResult[LABKEY.icemr.adaptation.parasitemia];

        if (finished)
            flask[LABKEY.icemr.flask.finishParasitemia + growthTest] = parasitemia;
        else
            flask[LABKEY.icemr.flask.startParasitemia + growthTest] = parasitemia;

        // we also record the start and finish dates for growth test 1
        if (growthTest == '1')
        {
            if (finished)
                flask[LABKEY.icemr.flask.finishDate1] = dailyResult[LABKEY.icemr.adaptation.measurementDate];
            else
                flask[LABKEY.icemr.flask.startDate1] = dailyResult[LABKEY.icemr.adaptation.measurementDate];

        }
    }
}

//
// store data in the flask based on events that happen
// for this daily maintenance.  At the very least we always
// record the date of the last daily maintenance
//
function getFlaskUpdates(dailyResult)
{
    // setup a flask with a sample id and the most recent maintenance date
    var flask = {};
    var measurementDate = dailyResult[LABKEY.icemr.adaptation.measurementDate];

    flask[LABKEY.icemr.flask.sample] = dailyResult[LABKEY.icemr.adaptation.sample];
    flask[LABKEY.icemr.flask.maintenanceDate] = measurementDate;

    // if maintenance was stopped, record the date
    // consider: this date may be redundant since we also save the last maintenance
    // consider: date above
    if (dailyResult[LABKEY.icemr.adaptation.flaskMaintenanceStopped])
        flask[LABKEY.icemr.flask.maintenanceStopped] = measurementDate;

    // growth test started?
    var growthTest = dailyResult[LABKEY.icemr.adaptation.growthFoldTestInitiated];
    storeGrowthTestParasitemia(growthTest, flask, dailyResult, false);

    // growth test finished?
    growthTest = dailyResult[LABKEY.icemr.adaptation.growthFoldTestFinished];
    storeGrowthTestParasitemia(growthTest, flask, dailyResult, true);

    return flask;
}

//
// generate an excel template file and autopopulate
// with the all the flasks specified in Day 0 for this run
// that have not had their maintenance stopped
//
function getDailyUploadTemplate(measurementDate)
{

    var flasks = getDay0Flasks();
    var rows = [];

    rows.push(buildHeaderRow());
    for (var i = 0; i < flasks.length; i++)
    {
        // skip any flasks whose daily maintennce has been stopped
        if (flasks[i][LABKEY.icemr.flask.maintenanceStopped] != null)
            continue;

        rows.push(buildDataRow(flasks[i], measurementDate));
    }

    //
    // build up our spreadsheet object
    //
    var spreadsheet = {};
    spreadsheet.fileName = LABKEY.icemr.dailyUploadTemplateFilename;
    spreadsheet.sheets = [];
    spreadsheet.sheets.push( {
        name : 'Sheet1',
        data : rows
    });
    LABKEY.Utils.convertToExcel(spreadsheet);
}

function buildHeaderRow()
{
    var columns = [];
    for (var i = 0; i < LABKEY.icemr.adaptation.resultFieldConfigs.length; i++)
    {
        var cfg = LABKEY.icemr.adaptation.resultFieldConfigs[i];

        // don't put calculated fields in the template
        if (cfg.name == LABKEY.icemr.adaptation.dateIndex)
            continue;

        columns.push(cfg.name);
    }

    return columns;
}

function buildDataRow(flask, measurementDate)
{
    var columns = [];

    for (var i = 0; i < LABKEY.icemr.adaptation.resultFieldConfigs.length; i++)
    {
        var cfg = LABKEY.icemr.adaptation.resultFieldConfigs[i];
        var data = null;

        // don't put calculated fields in the template
        if (cfg.name == LABKEY.icemr.adaptation.dateIndex)
            continue;

        //
        // just set measurement date and sample - client doesn't want
        // day0 data in the form
        //
        if (cfg.name == LABKEY.icemr.adaptation.measurementDate)
        {
            data = measurementDate;
        }
        else
        if (cfg.name == LABKEY.icemr.adaptation.sample)
        {
            data = flask[LABKEY.icemr.adaptation.sample];
        }

        if (data == null)
            data = '';

        columns.push(data);
    }

    return columns;
}

//
// Takes an array of daily results as input and verifies
// that we only have data for the flasks that were
// uploaded as day0 data.  It is okay for the dailyResults
// to not include data for all flasks established at day0.  However,
// we will error if we see flask data for a flask that was not
// uploaded at day0.
// Also if a flask is marked as maintenance stopped in day 0 then
// we will error.
//
function verifyDailyData(dailyResults, failure)
{
    var day0Flasks = getDay0Flasks();

    // cannot have more flasks to upload than we had on day 0
    if (dailyResults.length > day0Flasks.length)
        return showError(LABKEY.icemr.errDailyTitle, LABKEY.icemr.errDailyTooManyFlasks, failure);

    // all flasks that we want to upload must exist in the day0Flasks
    for (var i = 0; i < dailyResults.length; i++)
    {
        var sampleId = dailyResults[i][LABKEY.icemr.adaptation.sample];
        var found = false;
        for (var j = 0; j < day0Flasks.length; j++)
        {
            if (sampleId == day0Flasks[j][LABKEY.icemr.adaptation.sample])
            {
                if (day0Flasks[j][LABKEY.icemr.flask.maintenanceStopped] == null)
                {
                    found = true;
                }
                break;
            }
        }

        if (!found)
            return showError(LABKEY.icemr.errDailyTitle, LABKEY.icemr.errDailyInvalidFlaskDefined + sampleId, failure)
    }

    // verification succeeded
    return true;
}

//
// Daily Excel file upload handling
//
function getProcessDailyFileUploadCallbackWrapper(fn)
{
    return function(content, format)
    {
        if (!content)
        {
            showError(LABKEY.icemr.errDailyUploadTitle, LABKEY.icemr.errDailyUploadFileNoContent);
            return;
        }

        if (!content.sheets || content.sheets.length == 0)
        {
            showError(LABKEY.icemr.errDailyUploadTitle, LABKEY.icemr.errDailyUploadFileNoSheets);
            return;
        }

        var data = content.sheets[0].data;
        if (data.length == 0)
        {
            showError(LABKEY.icemr.errDailyUploadTitle, LABKEY.icemr.errDailyUploadFileNoRows);
            return;
        }

        // get field config to row index mappings from the header row
        var columnMappings = getColumnMappings(data[0]);

        // ensure that the file has the expected columns
        if (!verifyDailyFileUpload(columnMappings))
        {
            showError(LABKEY.icemr.errDailyUploadTitle, LABKEY.icemr.errDailyUploadFileInvalidHeader);
            return;
        }

        // iterate over the row data, start at 1 to skip the header row
        var dailyResults = [];
        for (var rowIdx = 1; rowIdx < data.length; rowIdx++)
        {
            var dailyResult = createDaily();
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
}

function isNameInSet(name, set)
{
    for (var i = 0; i < set.length; i++)
        if (set[i].name == name)
            return true;

    return false;
}

function isNameInArray(name, arr)
{
    for (var i = 0; i < arr.length; i++)
        if (arr[i] == name)
            return true;

    return false;
}

//
// verify that the header (row[0]) of data matches
// our result field schema for the adaptation assay
// we currently do a strict mapping except we don't
// require the file to include columns for data
// we calculate anyway
//
function verifyDailyFileUpload(columnMappings)
{
    var i;

    // verify that we have a column heading for each field config
    for (i = 0; i < LABKEY.icemr.adaptation.resultFieldConfigs.length; i++)
    {
        var config = LABKEY.icemr.adaptation.resultFieldConfigs[i];

        // skip internal or calculated fields
        if (config.name == LABKEY.icemr.adaptation.dateIndex)
            continue;

        if (!isNameInSet(config.name, columnMappings))
            return false;
    }

    // verify we have a field config for each column heading
    for (i = 0; i < columnMappings.length; i++)
    {
        if (!isNameInSet(columnMappings[i].name, LABKEY.icemr.adaptation.resultFieldConfigs))
            return false;
    }

    // verification passed
    return true;
}

function getColumnMappings(header)
{
    var columnMappings = [];
    for (var i = 0; i < header.length; i++)
    {
        var column = {};
        column["name"] = header[i];
        column["index"] = i;
        columnMappings.push(column);
    }

    return columnMappings;
}

function processDailyFileUpload(result, success)
{
    var data = new LABKEY.Exp.Data(result);

    if (!data.content)
    {
        data.getContent({
            format: 'jsonTSV',
            successCallback: getProcessDailyFileUploadCallbackWrapper(success),
            failureCallback: function (error, format) {
                Ext.Msg.alert("Upload Failed", "An error occurred while fetching the contents of the data file.");
            }
        })
    }
}

function getDay0Flasks()
{
    if (LABKEY.icemr.adaptation.run == undefined)
        return onLoadBatchFailure(); // we shouldn't get here

    var flasks = [];

    for (var i = 0; i < LABKEY.icemr.adaptation.materialInputs.length; i++)
    {
        flasks.push(LABKEY.icemr.adaptation.materialInputs[i]);
    }

    return flasks;
}

function saveDay0(experiment, flasks, success, failure)
{
    if (!experiment)
        throw "You must provide an experiment object";

    if (!success)
        throw "You must provide a Success callback function";

    if (!flasks)
        return showError(LABKEY.icemr.errDay0Title, LABKEY.icemr.errDay0NoFlasksDefined, failure);

    if (flasks.length == 0)
        return showError(LABKEY.icemr.errDay0Title, LABKEY.icemr.errDay0NoFlasksDefined, failure);


    // save off client callbacks
    LABKEY.icemr.saveDay0Success = success;
    LABKEY.icemr.saveDay0Failure = failure;

    //
    // start upload -> upload flask, then upload runs as part of a batch
    // with each flask as a material input
    //
    uploadFlasks(experiment, flasks);
}

function uploadFlasks(experiment, flasks)
{
    //
    // save experiment for context
    //
    LABKEY.icemr.experiment = experiment;

    LABKEY.Query.insertRows( {
        schemaName : 'Samples',
        queryName : LABKEY.icemr.adaptation.flaskSampleSet,
        rows : flasks,
        success : onInsertFlasksSuccess,
        failure : LABKEY.icemr.saveDay0Failure
    });
}

//
//  Once flasks are uploaded then establish them as material inputs
//  to the run
//
function onInsertFlasksSuccess(result)
{
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
    run.name = LABKEY.icemr.experiment[LABKEY.icemr.adaptation.patient];
    run.properties = LABKEY.icemr.experiment;

    LABKEY.page.batch.runs = [];
    LABKEY.page.batch.runs.push(run);
    LABKEY.setDirty(true);

    LABKEY.Experiment.saveBatch({
        assayId : LABKEY.page.assay.id,
        batch : LABKEY.page.batch,
        successCallback : LABKEY.icemr.saveDay0Success,
        failureCallback : LABKEY.icemr.saveDay0Failure
    });
}

/**
 * These functions are used for data
 */

function getAdaptationRunDataCallbackWrapper(fn)
{
    return function(batch) {
        var runs = batch.runs;
        var runData;

        for (var i = 0; i < runs.length; i++)
        {
            if (LABKEY.icemr.adaptation.runId == runs[i].id)
            {
                LABKEY.icemr.adaptation.batch = batch;
                LABKEY.icemr.adaptation.run = batch.runs[i];
                runData = LABKEY.icemr.adaptation.run.properties;
                decoupleMaterialInputs();
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

function decoupleMaterialInputs()
{
    // we save all of the material input data through direct updating of the flasks
    // sample set so don't include the custom properties in the batch
    var inputs = [];
    var materialInputs = LABKEY.icemr.adaptation.run.materialInputs;
    for (var j = 0; j < materialInputs.length; j++)
    {
        inputs.push(materialInputs[j].properties);
        materialInputs[j].properties = {};
    }

    LABKEY.icemr.adaptation.materialInputs = inputs;
}

function getAdaptationRunData(protocolId, batchId, rowId, success)
{
    LABKEY.icemr.adaptation.runId = rowId;
    LABKEY.icemr.adaptation.assayId = protocolId;

    LABKEY.Experiment.loadBatch({
        assayId : protocolId,
        batchId : batchId,
        success : getAdaptationRunDataCallbackWrapper(success),
        failure : onLoadBatchFailure
    });
}

function onLoadBatchFailure(response)
{
    showError(LABKEY.icemr.errAssayTitle, LABKEY.icemr.errAssayMissingRun);
}

/**
 * These functions are used for retrieving metadata
 */
function getDay0Configs(successCallback)
{
    LABKEY.icemr.getDay0Success = successCallback;
    //
    // Day 0 configs consist of the run properties for the adaptation
    // assay design as well as the flask metadata for the sample set
    // We also fetch the Flasks Sample Set ID and store it away for later use
    // before returning to the caller
    //
    getAdaptationFieldConfigs(onAdaptationAssayConfigsReady)
}

function onAdaptationAssayConfigsReady(runConfigs, resultConfigs)
{
    // retrieve flask sample set data
    var flasks = new LABKEY.Exp.SampleSet({name: LABKEY.icemr.adaptation.flaskSampleSet});
    flasks.getDomain({ success : onFlasksDomainReady, failure : onFlasksFailure });
}

function onFlasksDomainReady(domain)
{
    LABKEY.icemr.flaskConfigs = buildConfigs(domain.fields, LABKEY.icemr.metaType.SampleSet);

    // filter out our internal flask fields for the client
    var clientFlaskConfigs = [];
    for (var i=0; i < LABKEY.icemr.flaskConfigs.length; i++)
    {
        var config = LABKEY.icemr.flaskConfigs[i];
        if (!isNameInArray(config.name, LABKEY.icemr.flask.syncFields))
            clientFlaskConfigs.push(config);
    }

    LABKEY.icemr.getDay0Success(LABKEY.icemr.adaptation.runFieldConfigs, clientFlaskConfigs);
}

function onFlasksFailure(data)
{
    showError(LABKEY.icemr.errConfigTitle, LABKEY.icemr.errConfigMissingFlask);
}

function showError(title, message, failure)
{
    if (failure)
    {
        var json = new Object();
        json.exception = title + ": " + message;
        failure.call(this, json);
    }
    else
    {
        Ext.Msg.hide();
        Ext.Msg.alert(title, message);
    }
}
