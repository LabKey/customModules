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
PARAMETERS(start TIMESTAMP, finish TIMESTAMP)
-- unclear whether they want this query or not - right now we include flasks between these two dates
-- is that what they want?
-- postgress wants to have these explicit casts here
SELECT f.*, d.MeasurementDate, timestampdiff('SQL_TSI_DAY', cast(finish as TIMESTAMP), cast(start as TIMESTAMP)) As NumDays
FROM Samples."Adaptation Flasks" as f, DATA as d
WHERE (f.SampleID = d.SampleID) AND d.MeasurementDate >= start AND d.MeasurementDate <= finish