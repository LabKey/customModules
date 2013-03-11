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
 *
 *  Code and utility functions common to all ICEMR assays goes here
 */
LABKEY.requiresScript('clientapi/ext4/Util.js');
Ext.namespace("LABKEY.icemr");
// -------------------------------------------------------------------
// configuration errors
// -------------------------------------------------------------------
LABKEY.icemr.errConfigTitle = "Configuration Error";
//consider: adding the context of the missing sample set name here (do your own token replacement function here)
LABKEY.icemr.errConfigMissingAssayDesign = "Could not find the specified assay design. Please see your LabKey administrator.";
LABKEY.icemr.errConfigMissingFlask = "Could not find the Flask Sample Set. Please see your LabKey administrator.";

// -------------------------------------------------------------------
// constants
// -------------------------------------------------------------------

// -------------------------------------------------------------------
// enums
// -------------------------------------------------------------------
LABKEY.icemr.metaType = {
    AssayDesign : 0,
    SampleSet : 1
};

// -------------------------------------------------------------------
// methods
// -------------------------------------------------------------------
LABKEY.icemr.getFieldConfigsSuccessCallback = function(fn) {
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
        //var batchFields = assay.domains[assay.name + ' Batch Fields'];
        //var batchConfigs = LABKEY.icemr.buildConfigs(batchFields, LABKEY.icemr.metaType.AssayDesign);
        var runConfigs = LABKEY.icemr.buildConfigs(runFields, LABKEY.icemr.metaType.AssayDesign);
        var resultConfigs = LABKEY.icemr.buildConfigs(resultFields, LABKEY.icemr.metaType.AssayDesign);

        if (fn)
            fn.call(this, runConfigs, resultConfigs);
    }
};

LABKEY.icemr.getFieldConfigs = function(assayName, successCallback) {
    LABKEY.Assay.getByName({
        name : assayName,
        success : LABKEY.icemr.getFieldConfigsSuccessCallback(successCallback)
    });
};

LABKEY.icemr.buildConfigs = function(fields, metaType) {
    var configs = [];
    for (var i = 0; i < fields.length; i++)
    {
        configs.push(LABKEY.icemr.buildConfig(fields[i], metaType));
    }
    return configs;
};

LABKEY.icemr.buildConfig = function(meta, metaType){
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
    LABKEY.icemr.fixupValidation(meta, metaType, config);
    LABKEY.icemr.setDefaultValues(metaType, config);
    return config;
};

LABKEY.icemr.setDefaultValues = function(metaType, config) {
    // this is cheesy but we currently only set default values for samplesets which are only
    // used in the tracking assay provider type.  If we set default values for non-tracking assay types (like
    // diagnostics or speciesSelection, then we'll need to provide a more global interface)
    if (metaType == LABKEY.icemr.metaType.SampleSet)
    {
        LABKEY.icemr.tracking.setDefaultValues(metaType, config);
    }
};

LABKEY.icemr.isNameInSet = function(name, set){
    for (var i = 0; i < set.length; i++)
        if (set[i].name == name)
            return true;

    return false;
};

LABKEY.icemr.isNameInArray = function(name, arr) {
    for (var i = 0; i < arr.length; i++)
        if (arr[i] == name)
            return true;

    return false;
};

LABKEY.icemr.showError = function(title, message, failure){
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
};

LABKEY.icemr.compareDate = function(date1, date2){
    var t1 = new Date(date1).getTime();
    return (t1 == new Date(date2).getTime())
};


// this function only should be called for meta data returned from
// a sample set domain
LABKEY.icemr.fixupSampleSetTypeInformation = function(meta, config){
    var typename = meta.rangeURI.toLowerCase();
    if (typename == 'http://www.w3.org/2001/xmlschema#double')
    {
        config.xtype = 'numberfield';
        config.allowDecimals = true;
    }
    else
    if (typename == 'http://www.w3.org/2001/xmlschema#int')
    {
        config.xtype = 'numberfield';
        config.allowDecimals = false;
    }
};

// fields that require validation
LABKEY.icemr.fixupValidation = function (meta, metaType, config) {
    config.allowBlank = (meta.required === false);

    if (metaType == LABKEY.icemr.metaType.SampleSet)
    {
        LABKEY.icemr.fixupSampleSetTypeInformation(meta, config);
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
};

LABKEY.icemr.setComboConfig = function(config, options) {
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
};

// map various fields from the db to ui values

LABKEY.icemr.getFormGrowthTestValue = function(src) {
    if (src == '1' || src == 1)
        return '1';
    else if (src == '2' || src == 2)
        return '2';
    else if (src == '3' || src == 3)
        return '3';

    // any other value we just set to 'No'
    return 'No';
};

LABKEY.icemr.getFormYesNoValue = function(value) {
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
};

LABKEY.icemr.getFormPosNegValue = function(value) {
    if (value)
    {
        if (typeof(value) == 'string')
        {
            var v = value.toLowerCase();
            if (v.toUpperCase() == 'positive'.toUpperCase()) return 'Positive';
            if (v.toUpperCase() == 'negative'.toUpperCase()) return 'Negative';
            if (v.toUpperCase() == 'no test'.toUpperCase()) return 'No Test';
        }
        // if defined but invalid, return null
        return null;
    }
    // if not defined, return 'No Test'
    return 'No Test';
};

// convert milliseconds to days
LABKEY.icemr.getDateIndex = function(start, end){
    return parseInt((end.getTime() - start.getTime())/(24*60*60*1000));
};

