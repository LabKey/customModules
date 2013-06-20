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
SELECT r.*, f.Scientist as InitialScientist, f.AdaptationSampleID, f.FreezerProID, f.InitialPopulation, f.Compound,
f.Concentration, f.Control, f.CultureMedia, f.SuperStockBatchID, f.WorkingStockBatchID,f.RBCBatchID as InitialRBCBatchID,
f.SerumBatchID as InitialSerumBatchID, f.AlbumaxBatchID as InitialAlbumaxBatchID, f.ResistanceProtocol,
f.ResistanceNumber, f.FoldIncrease1, f.FoldIncrease2, f.FoldIncrease3, f.MinimumParasitemia,
f.Comments as InitialComments
FROM tracking_results r INNER JOIN Samples."Selection Flasks" f ON r.SampleID = f.SampleID
