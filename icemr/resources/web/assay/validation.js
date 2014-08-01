/*
 * Copyright (c) 2013-2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * User: Dax
 * Date: 1/10/13
 * Time: 11:03 AM
 */
Ext4.namespace("LABKEY.icemr");
//
// numeric field validation
//
LABKEY.icemr.percentNumber = {
    percentNumber : function (val, field)
    {
        var d = parseFloat(val);
        return (d >=0 && d <= 100);
    },
    percentNumberText : 'The value must be between 0 and 100'
};

LABKEY.icemr.intNumber = {
    intNumber : function (val, field)
    {
        var n = parseInt(val, 10);
        return ( n >=0 && (n == parseFloat(val, 10)) );
    },
    intNumberText : 'The value must be a positive integer'
};

LABKEY.icemr.doubleNumber = {
    doubleNumber: function (val, field)
    {
        var d = parseFloat(val);
        return (d >= 0);
    },
    doubleNumberText: 'The value must be positive'
};

LABKEY.icemr.initValidators = function() {
    //
    // numeric field validation
    //
    Ext4.apply(Ext4.form.field.VTypes, LABKEY.icemr.intNumber);
    Ext4.apply(Ext4.form.field.VTypes, LABKEY.icemr.percentNumber);
    Ext4.apply(Ext4.form.field.VTypes, LABKEY.icemr.doubleNumber);
};
