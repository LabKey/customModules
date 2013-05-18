package org.labkey.icemr.assay;

import org.labkey.api.assay.dilution.DilutionManager;

/**
 * Created by IntelliJ IDEA.
 * User: klum
 * Date: 5/13/13
 */
public class DrugSensitivityManager extends DilutionManager
{
    private static final DrugSensitivityManager _instance = new DrugSensitivityManager();

    private DrugSensitivityManager(){}

    public static DrugSensitivityManager get()
    {
        return _instance;
    }
}
