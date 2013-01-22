/**
 * Created with IntelliJ IDEA.
 * User: Dax
 * Date: 1/10/13
 * Time: 1:24 PM
 * To change this template use File | Settings | File Templates.
 */
Ext.namespace("LABKEY.icemr.test");

//
// now that you got rid of your id for field names you are completely broken
// add a bunch-o-special case code here (including ids, i guess)
//
LABKEY.icemr.test.flaskId = 1;

function generateExperimentData(experiment, patientId, experimentId)
{
    experiment['StartDate'] = new Date();
    experiment['PatientID'] = patientId;
    experiment['ExperimentID'] = experimentId;
}

function generateFlaskData(experiment, flask)
{
    flask['PatientID'] = experiment['PatientID'];
    flask['SampleID'] = experiment['PatientID'] + LABKEY.icemr.test.flaskId;
    flask['Scientist'] = "Hayflick";
    flask['Stage'] = LABKEY.icemr.adaptation.stageOptions[Math.floor(Math.random()*LABKEY.icemr.adaptation.stageOptions.length)][0];
    flask['Parasitemia'] = Math.floor(Math.random() * 100);
    flask['Gametocytemia'] = Math.floor(Math.random() * 100);
    flask['PatientpRBCs'] = LABKEY.icemr.adaptation.pRBCOptions[Math.floor(Math.random()*LABKEY.icemr.adaptation.pRBCOptions.length)][0];
    flask['Hematocrit'] = Math.floor(Math.random() * 100);
    flask['CultureMedia'] = LABKEY.icemr.adaptation.cultureMediaOptions[Math.floor(Math.random()*LABKEY.icemr.adaptation.cultureMediaOptions.length)][0];
    if (flask['CultureMedia'] == 'serum')
        flask['SerumBatchID'] = 1;
    else
        flask['AlbumaxBatchID'] = 1;
    flask['FoldIncrease'] = 4;
    flask['AdaptationCriteria'] = 2;
    flask['Comments'] = "this is a comment for flask " + LABKEY.icemr.test.flaskId;
    LABKEY.icemr.test.flaskId++;
}

function generateDailyData(experiment, flask, stopMaintenance, dailyResult)
{
    dailyResult['Scientist'] = "N. Tesla";
    dailyResult['SampleID'] = flask[LABKEY.icemr.adaptation.sample];  // flask id
    dailyResult['Stage'] = LABKEY.icemr.adaptation.stageOptions[Math.floor(Math.random()*LABKEY.icemr.adaptation.stageOptions.length)][0];
    dailyResult['Parasitemia'] = Math.floor(Math.random() * 100);
    dailyResult['Gametocytemia'] = Math.floor(Math.random() * 100);
    if (flask['CultureMedia'] == 'serum')
        dailyResult['SerumBatchID'] = 22;
    else
        dailyResult['AlbumaxBatchID'] = 33;
    dailyResult['MeasurementDate'] = new Date();
    dailyResult['DateIndex'] = getDateIndex(new Date(experiment['StartDate']), dailyResult['MeasurementDate']);
    dailyResult['Removed'] = Math.floor(Math.random() * 100);
    dailyResult['RBCBatchID'] = Math.floor(Math.random() * 10);
    dailyResult['GrowthFoldTestInitiated'] = Math.floor(Math.random() * 3);
    dailyResult['GrowthFoldTestFinished'] = Math.floor(Math.random() * 3);
    dailyResult['Contamination'] = false;
    dailyResult['MycoTestResult'] = true;
    dailyResult['FlaskMaintenanceStopped'] = stopMaintenance;
    dailyResult['InterestingResult'] = true;
    dailyResult['FreezerProIDs'] = "10 20 3a 4b";
    dailyResult['Comments'] = "this is my comment for the daily maintenace of: " + dailyResult['SampleID'];
}


