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
-- combines the results from the two calculated queries and combines them with the
-- over flask sample set data
SELECT
f.PatientID, f.SampleID, f.Scientist, f.AdaptationSampleID, f.FreezerProID, f.InitialPopulation, f.Compound,
f.Concentration, f.Control, f.CultureMedia, f.SerumBatchID, f.AlbumaxBatchID, f.SuperstockBatchID,
f.WorkingstockBatchID, f.RBCBatchID, f.ResistanceProtocol, f.ResistanceNumber, f.FoldIncrease1, f.FoldIncrease2,
f.FoldIncrease3, f.MinimumParasitemia, f.Comments, f.MaintenanceStopped, f.MaintenanceDate, f.ConsecutiveDays,
rf.IncreaseTest1, rf.IncreaseTest2, rf.IncreaseTest3, rf.PassTest1, rf.PassTest2, rf.PassTest3,
rf.SuccessfulPreliminaryResistanceGrowthFold,
rd.SuccessfulPreliminaryResistanceDays,
fg.FirstDayPositiveGrowth,
fr.FirstDayPositiveResistantPopulation
FROM
Samples."Selection Flasks" as f,
select_all_resistance_fold as rf,
select_all_resistance_days as  rd,
select_all_firstdaypositive_growth as fg,
select_all_firstdaypositive_resistant as fr
WHERE f.SampleID = rf.SampleID AND rf.SampleID = rd.SampleID AND rd.SampleID = fg.SampleID AND fg.SampleID = fr.SampleID
