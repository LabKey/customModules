/*
 * Copyright (c) 2015-2016 LabKey Corporation
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
CREATE TABLE hdrl.RequestResult
(
  RequestId INT NOT NULL,
  RequestStatusId INT NOT NULL,
  CustomerNote VARCHAR(254),
  Received TIMESTAMP NOT NULL,
  Completed TIMESTAMP,

  Container ENTITYID NOT NULL,
  CreatedBy USERID,
  Created TIMESTAMP,
  ModifiedBy USERID,
  Modified TIMESTAMP,

  CONSTRAINT PK_RequestResult PRIMARY KEY (RequestId),
  CONSTRAINT FK_RequestResult_InboundRequest FOREIGN KEY (RequestId) REFERENCES hdrl.InboundRequest(RequestId) ON DELETE CASCADE,
  CONSTRAINT FK_RequestResult_Status FOREIGN KEY (RequestStatusId) REFERENCES hdrl.RequestStatus(RowId)
);

CREATE TABLE hdrl.SpecimenResult
(
  SpecimenId INT NOT NULL,
  RequestStatusId INT NOT NULL,
  Received TIMESTAMP NOT NULL,
  Completed TIMESTAMP,
  SampleIntegrity VARCHAR(80),
  TestResult VARCHAR(254),
  CustomerCode VARCHAR(80),
  ModifiedResultFlag CHAR,

  Container ENTITYID NOT NULL,
  CreatedBy USERID,
  Created TIMESTAMP,
  ModifiedBy USERID,
  Modified TIMESTAMP,

  CONSTRAINT PK_SpecimenResult PRIMARY KEY (SpecimenId),
  CONSTRAINT FK_SpecimenResult_InboundSpecimen FOREIGN KEY (SpecimenId) REFERENCES hdrl.InboundSpecimen(RowId) ON DELETE CASCADE,
  CONSTRAINT FK_SpecimenResult_Status FOREIGN KEY (RequestStatusId) REFERENCES hdrl.RequestStatus(RowId)

);

ALTER TABLE hdrl.InboundRequest DROP COLUMN Completed;
ALTER TABLE hdrl.InboundSpecimen DROP COLUMN ValidationStatus;