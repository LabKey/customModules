/*
 * Copyright (c) 2012 LabKey Corporation
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
PARAMETERS (IntervalBegin TIMESTAMP, IntervalEnd TIMESTAMP)
SELECT
Folder.RowId AS FolderId,
Folder.Path,
Folder.EntityId,
Protocol.Name AS AssayDesignName,
Protocol.RowId AS AssayDesignId,
RowId AS RunId,
CASE WHEN (Runs.Created >= IntervalBegin AND Runs.Created < IntervalEnd) THEN 1 ELSE 0 END AS Uploaded,
CASE WHEN (approved.Created >= IntervalBegin AND approved.Created < IntervalEnd) THEN 1 ELSE 0 END AS Approved,
CASE WHEN (processing.Created >= IntervalBegin AND processing.Created < IntervalEnd) THEN 1 ELSE 0 END AS Processing
FROM Runs
LEFT JOIN RunGroupMap AS approved ON Runs.RowId = approved.Run AND approved.RunGroup.Name = 'Approved'
LEFT JOIN RunGroupMap AS processing ON Runs.RowId = processing.Run AND processing.RunGroup.Name = 'Processing'
WHERE Protocol.RowId IN (SELECT RowId FROM Project.assay.AssayList)