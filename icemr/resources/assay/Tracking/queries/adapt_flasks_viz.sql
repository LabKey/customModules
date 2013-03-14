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

 -- combines results from flasks, runs, and daily maintenance into
 -- a table for visualization

SELECT Runs.PatientID, f.SampleID, StartDate as MeasurementDate, f.Stage, f.Parasitemia, f.Gametocytemia, f.PatientpRBCs, f.Hematocrit,
    f.CultureMedia, NULL as Removed, NULL as GrowthFoldTestInitiated, NULL as GrowthFoldTestFinished,
    NULL As Contaminiation, NULL as MycoTestResults, NULL as FeezerProIDS, NULL as FlaskMaintenanceStopped
FROM Runs, Samples."Adaptation Flasks" as f
WHERE Runs.PatientID = f.PatientID

UNION ALL

SELECT Run.PatientID, SampleID, MeasurementDate, Stage, Parasitemia, Gametocytemia, NULL As PatientpRBCs, NULL as Hematocrit,
    NULL As CultureMedia, Removed, GrowthFoldTestInitiated, GrowthFoldTestFinished, Contamination, MycoTestResult,
    FreezerProIDs, FlaskMaintenanceStopped
FROM Data



