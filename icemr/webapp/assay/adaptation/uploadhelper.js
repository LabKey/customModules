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
LABKEY.icemr.errAssayMissingRun = "Could not find a matching experiment to update";

LABKEY.icemr.errDailyTitle = "Daily Maintenance Error";
LABKEY.icemr.errDailyTooManyFlasks = "Invalid attempt to upload data for flasks that were not defined in Day 0";
LABKEY.icemr.errDailyInvalidFlaskDefined = "Invalid flask specified.  The following flask was not found in the Day 0 data or maintenance was already stopped: ";

LABKEY.icemr.errDailyUploadTitle = "Dailiy Upload Failed"
LABKEY.icemr.errDailyUploadFileNoContent = "The data file has no content";
LABKEY.icemr.errDailyUploadFileNoSheets = "The data file has no sheets of data";
LABKEY.icemr.errDailyUploadFileNoRows = "The data file has no rows of data";
LABKEY.icemr.errDailyUploadFileInvalidHeader = "The data file header row does not match the daily results schema";

LABKEY.icemr.errDay0Title = "Day 0 Upload Error";
LABKEY.icemr.errDay0NoFlasksDefined = "You must include at least one flask with your Day 0 data"

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
    var batch = LABKEY.icemr.adaptation.batch;
    var run = LABKEY.icemr.adaptation.run;
    var i;

    if (batch == undefined || run == undefined)
        return onLoadBatchFailure(); // we shouldn't get here

    if (!verifyDailyData(dailyResults))
    {
        failure();
        return;
    }

    run.dataRows = run.dataRows || [];

    var flasks = [];
    for (i = 0; i < dailyResults.length; i++)
    {
        //
        // see if we have any flask sample set data that we need to save
        // before saving the batch
        //
        var flask = getFlaskUpdateData(dailyResults[i]);
        if (flask != null)
            flasks.push(flask);

        run.dataRows.push(dailyResults[i]);
    }

    // undone: issue 16960 we need to get the "role" property back out of the database
    // undone: so that when we persist we roundtrip it correctly.  Alternatively
    // undone: we need to not save data that we already have!

    // update the flask sample set if appropriate
    if (flasks.length > 0)
    {
        LABKEY.Query.updateRows( {
            schemaName : 'Samples',
            queryName : LABKEY.icemr.adaptation.flaskSampleSet,
            rows : flasks,
            success : getUpdateFlasksSuccessCallbackWrapper(success, failure),
        });
    }
    else
    {
        // just call save batch directly
        var fn = getUpdateFlasksSuccessCallbackWrapper(success, failure);
        fn.call(this);
    }
}

function getUpdateFlasksSuccessCallbackWrapper(success, failure)
{
    return function(result)
    {
        if (result != undefined)
        {
            //
            // we just changed our material inputs (using selectRow)
            // therefore we should update to the values we chanaged to
            // consider:  may want to change SaveBatch not to update inputmaterials
            //
            mergeFlaskChanges(result.rows);
        }
        // now that we've saved the flask data, go ahead and
        // save our batch
        LABKEY.Experiment.saveBatch({
            assayId : LABKEY.icemr.adaptation.assayId,
            batch : LABKEY.icemr.adaptation.batch,
            successCallback : success,
            failureCallback : failure
        });
    }
}

function mergeFlaskChanges(rows)
{
    var materialInputs = LABKEY.icemr.adaptation.batch.runs[0].materialInputs;

    for (var i = 0; i < rows.length; i++)
    {
        var row = rows[i];
        //
        // find the material input that matches this flask in the batch and update the
        // FlaskMaintenanceStopped property
        //
        for (var j = 0; j < materialInputs.length; j++)
        {
            var properties = materialInputs[j].properties;
            if (properties[LABKEY.icemr.adaptation.sample] == row[LABKEY.icemr.adaptation.sample.toLowerCase()])
            {
                properties[LABKEY.icemr.adaptation.flaskMaintenanceStopped] = row[LABKEY.icemr.adaptation.flaskMaintenanceStopped.toLowerCase()];
                break;
            }
        }
    }
}

function getFlaskUpdateData(dailyResult)
{
    // right now we only update the flask sample if daily maintenance has been stopped
    var maintenanceStopped = dailyResult[LABKEY.icemr.adaptation.flaskMaintenanceStopped];

    if (maintenanceStopped)
    {
        var flask = {};
        // the sample id is the primary key
        flask[LABKEY.icemr.adaptation.sample] = dailyResult[LABKEY.icemr.adaptation.sample];
        flask[LABKEY.icemr.adaptation.flaskMaintenanceStopped] = true;
        return flask;
    }

    return null;
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
function verifyDailyData(dailyResults)
{
    var day0Flasks = getDay0Flasks();

    // cannot have more flasks to upload than we had on day 0
    if (dailyResults.length > day0Flasks.length)
        return showError(LABKEY.icemr.errDailyTitle, LABKEY.icemr.errDailyTooManyFlasks);

    // all flasks that we want to upload must exist in the day0Flasks
    for (var i = 0; i < dailyResults.length; i++)
    {
        var sampleId = dailyResults[i][LABKEY.icemr.adaptation.sample];
        var found = false;
        for (var j = 0; j < day0Flasks.length; j++)
        {
            if (sampleId == day0Flasks[j][LABKEY.icemr.adaptation.sample])
            {
                var stopped = day0Flasks[j][LABKEY.icemr.adaptation.flaskMaintenanceStopped]
                if (stopped != true)
                {
                    found = true;
                }
                break;
            }
        }

        if (!found)
            return showError(LABKEY.icemr.errDailyTitle, LABKEY.icemr.errDailyInvalidFlaskDefined + sampleId)
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
            addCalculatedFields(dailyResult);
            dailyResults.push(dailyResult);
        }

        if (fn)
            fn.call(this, dailyResults)
    }
}

function addCalculatedFields(dailyResult)
{
    var startDate = new Date(LABKEY.icemr.adaptation.run.properties[LABKEY.icemr.adaptation.startDate]);
    var measurementDate = new Date(dailyResult[LABKEY.icemr.adaptation.measurementDate]);
    dailyResult[LABKEY.icemr.adaptation.dateIndex] = getDateIndex(startDate, measurementDate);
}

// convert milliseconds to days
function getDateIndex(start, end)
{
    return parseInt((end.getTime() - start.getTime())/(24*60*60*1000));
}

function isNameInSet(name, set)
{
    for (var i = 0; i < set.length; i++)
    {
        if (set[i].name == name)
            return true;
    }

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
    var run = LABKEY.icemr.adaptation.run;

    if (run == undefined)
        return onLoadBatchFailure(); // we shouldn't get here

    var flasks = [];

    for (var i = 0; i < run.materialInputs.length; i++)
    {
        flasks.push(run.materialInputs[i].properties);
    }

    return flasks;
}

function saveDay0(experiment, flasks, success, failure)
{
    if (!experiment)
        throw "You must provide an experiment object";

    if (!flasks)
        return showError(LABKEY.icemr.errDay0Title, LABKEY.icemr.icemr.errDay0NoFlasksDefined);

    if (flasks.length == 0)
        return showError(LABKEY.icemr.errDay0Title, LABKEY.icemr.icemr.errDay0NoFlasksDefined);

    if (!success)
        throw "You must provide a Success callback function";

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
        success : onInsertFlasksSuccess
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
            id : result.rows[i]['rowid'],
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
    //
    // now get flask data
    //
    var flasks = new LABKEY.Exp.SampleSet({name: LABKEY.icemr.adaptation.flaskSampleSet});
    flasks.getDomain({ success : onFlasksDomainReady, failure : onFlasksFailure });
}

function onFlasksDomainReady(domain)
{
    LABKEY.icemr.flaskConfigs = buildConfigs(domain.fields, LABKEY.icemr.metaType.SampleSet);
    LABKEY.icemr.getDay0Success(LABKEY.icemr.adaptation.runFieldConfigs, LABKEY.icemr.flaskConfigs);
}

function onFlasksFailure(data)
{
    showError(LABKEY.icemr.errConfigTitle, LABKEY.icemr.errConfigMissingFlask);
}

function showError(title, message)
{
    Ext.Msg.hide();
    Ext.Msg.alert(title, message);
}
