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
f.PatientID, f.SampleID, f.Scientist, f.Stage, f.Parasitemia, f.Gametocytemia, f.PatientpRBCs, f.Hematocrit,
f.CultureMedia, f.SerumBatchID, f.AlbumaxBatchID, f.FoldIncrease1, f.FoldIncrease2, f.FoldIncrease3,
f.AdaptationCriteria,f.Comments, f.MaintenanceStopped, f.MaintenanceDate,
a.IncreaseTest1, a.IncreaseTest2, a.IncreaseTest3, a.PassTest1, a.PassTest2, a.PassTest3, a.SuccessfulAdaptation,
f.AdaptationDate, d.NumDaysInCulture, d.NumDaysToGrowthTestStart, d.NumDaysFromGrowthTestFinishToAdaptation, d.NumDaysToAdaptation,
FROM
Samples."Adaptation Flasks" as f,
adapt_parasitemia_pass as a,
stored_dates_numdays as d
WHERE f.SampleID = d.SampleID AND d.SampleID = a.SampleID


