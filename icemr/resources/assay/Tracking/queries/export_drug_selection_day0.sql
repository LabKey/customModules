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

-- Note that a patient in drug selection may be used in multiple experiments so it is not enough to simply join
-- the run data with the flask data by patient id.  Instead, we have to look into the flask and see for which
-- runs it is used as a material input and then choose the start date for that run as the start date for the flask
SELECT f.PatientID as ParticipantID, SampleID, Scientist, AdaptationSampleID, FreezerProID, InitialPopulation, Compound,
Concentration, Control, CultureMedia, SuperstockBatchID, WorkingstockBatchID, RBCBatchID, SerumBatchID,
AlbumaxBatchID, ResistanceProtocol, ResistanceNumber, FoldIncrease1, FoldIncrease2, FoldIncrease3, MinimumParasitemia,
f.Comments, MaintenanceDate, MaintenanceStopped, StartParasitemia1, FinishParasitemia1, StartParasitemia2,
FinishParasitemia2, StartParasitemia3, FinishParasitemia3, StartDate1, FinishDate1,
r.StartDate as Date
FROM Samples."Selection Flasks" f, Runs r,
(SELECT DISTINCT mi.Material, mi.TargetProtocolApplication.Run.RowId, m.name
 FROM exp.MaterialInputs as mi, exp.Materials as m where m.rowId = mi.material) m
WHERE r.RowId = m.RowID AND m.name = f.SampleID