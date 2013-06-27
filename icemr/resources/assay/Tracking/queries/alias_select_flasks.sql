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
-- these are the fields common between the adaptation and selection samplesets aliased
-- to be able to join with the assay result data
SELECT PatientID as FlaskPatientID, SampleID as FlaskSampleID, Scientist as InitialScientist,
AdaptationSampleID, FreezerProID, InitialPopulation, Compound, Concentration, Control, CultureMedia,
SuperstockBatchID, WorkingstockBatchID, RBCBatchID as InitialRBCBatchID, SerumBatchID as InitialSerumBatchID,
AlbumaxBatchID as InitialAlbumaxBatchID, ResistanceProtocol, ResistanceNumber, FoldIncrease1, FoldIncrease2,
FoldIncrease3, MinimumParasitemia, Comments as InitialComments, MaintenanceDate, MaintenanceStopped, StartParasitemia1,
FinishParasitemia1, StartParasitemia2, FinishParasitemia2, StartParasitemia3, FinishParasitemia3, StartDate1,
FinishDate1
FROM Samples."Selection Flasks"
