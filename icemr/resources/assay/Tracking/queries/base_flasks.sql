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
PARAMETERS(isSelectionFlask INTEGER)
-- these are the fields common between the adaptation and selection samplesets
SELECT t.PatientID, t.SampleID, t.Scientist, t.CultureMedia, t.SerumBatchID, t.AlbumaxBatchID, t.FoldIncrease1,
t.FoldIncrease2, t.FoldIncrease3, t.Comments, t.MaintenanceDate, t.MaintenanceStopped, t.StartParasitemia1,
t.FinishParasitemia1, t.StartParasitemia2, t.FinishParasitemia2, t.StartParasitemia3, t.FinishParasitemia3,
t.StartDate1, t.FinishDate1
FROM(
SELECT 0 as SelectionFlask,
PatientID, SampleID, Scientist, CultureMedia, SerumBatchID, AlbumaxBatchID, FoldIncrease1,
FoldIncrease2, FoldIncrease3, Comments, MaintenanceDate, MaintenanceStopped, StartParasitemia1,
FinishParasitemia1, StartParasitemia2, FinishParasitemia2, StartParasitemia3, FinishParasitemia3,
StartDate1, FinishDate1
FROM
Samples."Adaptation Flasks"
UNION
SELECT 1 as SelectionFlask,
PatientID, SampleID, Scientist, CultureMedia, SerumBatchID, AlbumaxBatchID, FoldIncrease1,
FoldIncrease2, FoldIncrease3, Comments, MaintenanceDate, MaintenanceStopped, StartParasitemia1,
FinishParasitemia1, StartParasitemia2, FinishParasitemia2, StartParasitemia3, FinishParasitemia3,
StartDate1, FinishDate1
FROM
Samples."Selection Flasks"
) as t
WHERE t.SelectionFlask = isSelectionFlask;



