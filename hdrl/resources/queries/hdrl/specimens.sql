/*
 * Copyright (c) 2015 LabKey Corporation
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
SELECT
  spec.RowId AS RowId,
  spec.inboundRequestId as InboundRequestId,
  res.RequestStatusId AS RequestStatusId,
  res.ModifiedResultFlag AS ModifiedResultFlag,
  spec.CustomerBarcode AS CustomerBarcode,
  spec.LastName AS LastName,
  spec.FirstName AS FirstName,
  spec.MiddleName AS MiddleName,
  spec.Initials AS Initials,
  spec.GenderId AS GenderId,
  spec.BirthDate AS BirthDate,
  spec.DODId AS DODId,
  spec.SSN AS SSN,
  spec.FMPId AS FMPId,
  spec.DutyCodeId AS DutyCodeid,
  spec.TestingSourceId AS TestingSourceId,
  spec.DrawDate as DrawDate,
  res.Received AS Received,
  res.Completed AS Completed,
  res.SampleIntegrity AS SampleIntegrity,
  res.TestResult AS TestResult,
  res.CustomerCode AS CustomerCode,
  res.ReportFileName as ReportFileName
FROM hdrl.InboundSpecimen spec LEFT JOIN hdrl.SpecimenResult res ON spec.RowId = res.specimenId;