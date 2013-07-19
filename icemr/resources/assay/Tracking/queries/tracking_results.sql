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
SELECT Run.RowId, Run.PatientID, Run.ExperimentID,
SampleID,
-- remove the time component from the date so that MeasurementDate - StartDate only counts full days
cast(cast(MeasurementDate As Date) as Timestamp) As MeasurementDate,
Scientist, Parasitemia, Gametocytemia, Stage, Removed, RBCBatchID, SerumBatchID, AlbumaxBatchID,
GrowthFoldTestInitiated, GrowthFoldTestFinished, Contamination, MycoTestResult, FreezerProIDs, FlaskMaintenanceStopped,
InterestingResult, Comments,
 -- remove the time component from the date so that MeasurementDate - StartDate only counts full days
cast(cast(Run.StartDate As Date) as Timestamp) as StartDate
FROM Data