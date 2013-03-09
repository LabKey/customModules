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
SELECT DISTINCT f.SampleID,
cast(timestampdiff('SQL_TSI_DAY', d.Run.StartDate, f.MaintenanceDate) as INTEGER) As NumDaysInCulture,
cast(timestampdiff('SQL_TSI_DAY', d.Run.StartDate, f.StartDate1) as INTEGER) As NumDaysToGrowthTestStart,
cast(timestampdiff('SQL_TSI_DAY', f.FinishDate1, f.AdaptationDate) as INTEGER) As NumDaysFromGrowthTestFinishToAdaptation,
cast(timestampdiff('SQL_TSI_DAY', d.Run.StartDate, f.AdaptationDate) as INTEGER) As NumDaysToAdaptation,
FROM
Samples."Adaptation Flasks" as f
LEFT JOIN Data as d
On f.SampleID = d.SampleID
