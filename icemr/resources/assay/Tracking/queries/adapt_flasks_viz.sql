/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

-- get all the daily maintenance data with their day 0 data for filtering
SELECT r.*, f.*,
abs(timestampdiff('SQL_TSI_DAY', MeasurementDate, StartDate)) As DateIndex
FROM tracking_results r INNER JOIN alias_adapt_flasks f ON r.SampleID = f.FlaskSampleID

UNION ALL

-- combine with all the day 0 data
SELECT NULL As RowId, Runs.PatientID, Runs.ExperimentID,
f.FlaskSampleID as SampleID, Runs.StartDate As MeasurementDate, f.InitialScientist As Scientist,
f.InitialParasitemia as Parasitemia, f.InitialGametocytemia as Gametocytemia, f.InitialStage as Stage,
NULL As Removed, NULL As RBCBatchID, f.InitialSerumBatchID as SerumBatchID, f.InitialAlbumaxBatchID as AlbumaxBatchID,
NULL As GrowthFoldTestInitiated, NULL As GrowthFoldTestFinished, NULL As Contamination, NULL As MycoTestResult,
NULL As FreezerProIDs, NULL As FlaskMaintenanceStopped, NULL As InterestingResult, f.InitialComments as Comments,
NULL As StartDate, f.FlaskPatientID, f.FlaskSampleID, f.InitialScientist, f.InitialParasitemia,
f.InitialGametocytemia, f.PatientpRBCs, f.Hematocrit, f.InitialStage, f.AdaptationCriteria, f.CultureMedia,
f.InitialSerumBatchID, f.InitialAlbumaxBatchID, f.FoldIncrease1, f.FoldIncrease2, f.FoldIncrease3, f.InitialComments,
f.AdaptationDate, f.MaintenanceDate, f.MaintenanceStopped, f.StartParasitemia1, f.FinishParasitemia1,
f.StartParasitemia2, f.FinishParasitemia2, f.StartParasitemia3, f.FinishParasitemia3, f.StartDate1, f.FinishDate1,
0 as DateIndex,
FROM Runs, alias_adapt_flasks as f
WHERE Runs.PatientID = f.FlaskPatientID

ORDER BY DateIndex LIMIT 50000