/**
 * Created with IntelliJ IDEA.
 * User: Dax
 * Date: 1/15/13
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */
LABKEY.requiresScript('clientapi/ext4/Util.js');
Ext.namespace("LABKEY.icemr");

//
// diagnostic assay constants
//
Ext.namespace("LABKEY.icemr.diagnostics");
LABKEY.icemr.diagnostics.participant='ParticipantID';
LABKEY.icemr.diagnostics.hemoglobin='patienthemoglobin';
LABKEY.icemr.diagnostics.thick = 'thickbloodsmear';
LABKEY.icemr.diagnostics.thin = 'thinbloodsmear';
LABKEY.icemr.diagnostics.RDT = 'rdt';
LABKEY.icemr.diagnostics.time = 'time';
LABKEY.icemr.diagnostics.date = 'date'
LABKEY.icemr.diagnostics.speciesOptions = [['Pf'], ['Pv'], ['mixed'], ['negative']];

//
// adaptation assay constants
//

Ext.namespace("LABKEY.icemr.adaptation");
LABKEY.icemr.adaptation.flaskSampleSet = 'Flasks';
LABKEY.icemr.adaptation.patient = 'patientid';
LABKEY.icemr.adaptation.stage = 'stage';
LABKEY.icemr.adaptation.pRBC = 'patientprbcs';
LABKEY.icemr.adaptation.cultureMedia = 'culturemedia';
LABKEY.icemr.adaptation.stageOptions =  [['rings'], ['trophozoites'], ['schizonts']];
LABKEY.icemr.adaptation.pRBCOptions = [['washed'], ['unwashed']];
LABKEY.icemr.adaptation.cultureMediaOptions = [['serum'], ['Albumax']];

//
// the ICEMR module must have these assay designs and
// sample sets
//
LABKEY.icemr.AdaptationAssayResults = 'Adaptation Assay';
LABKEY.icemr.DiagnosticsAssayResults = 'Diagnostics Assay';
LABKEY.icemr.metaType = {
    AssayDesign : 0,
    SampleSet : 1
}

//
// configuration errors
//
LABKEY.icemr.errConfigTitle = "Configuration Error";
LABKEY.icemr.errConfigMissingAssayDesign = "Could not find the specified assay design. Please see your LabKey administrator.";
LABKEY.icemr.errConfigMissingFlask = "Could not find the Flask Sample Set. Please see your LabKey administrator.";

function getAdaptationFieldConfigs(successCallback)
{
    getFieldConfigs(LABKEY.icemr.AdaptationAssayResults, successCallback);
}

function getDiagnosticsFieldConfigs(successCallback)
{
    getFieldConfigs(LABKEY.icemr.DiagnosticsAssayResults, successCallback)
}

function onAssayDesignReady(assays)
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
    LABKEY.icemr.successCallback(runConfigs, resultConfigs);
}

function getFieldConfigs(assayName, successCallback)
{
    LABKEY.icemr.successCallback = successCallback;

    if (assayName == LABKEY.icemr.AdaptationAssayResults ||
        assayName == LABKEY.icemr.DiagnosticsAssayResults)
    {
        LABKEY.Assay.getByName({
            name : assayName,
            success : onAssayDesignReady
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
}
