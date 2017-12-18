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
/* hdrl-15.20-15.21.sql */

ALTER TABLE hdrl.InboundSpecimen ADD COLUMN Initials VARCHAR(5);
ALTER TABLE hdrl.InboundSpecimen ADD COLUMN NumContainers INT DEFAULT 1;
ALTER TABLE hdrl.InboundSpecimen ADD COLUMN GenderId INT;

CREATE TABLE hdrl.Gender
(
   RowId SERIAL NOT NULL,
   Code VARCHAR(1) NOT NULL,
   Description VARCHAR(20) NOT NULL,

   CONSTRAINT PK_Gender PRIMARY KEY (RowId),
   CONSTRAINT UQ_Gender UNIQUE (Code)
);

INSERT INTO hdrl.Gender VALUES(DEFAULT, 'A', 'Ambiguous');
INSERT INTO hdrl.Gender VALUES(DEFAULT, 'F', 'Female');
INSERT INTO hdrl.Gender VALUES(DEFAULT, 'M', 'Male');
INSERT INTO hdrl.Gender VALUES(DEFAULT, 'N', 'Not applicable');
INSERT INTO hdrl.Gender VALUES(DEFAULT, 'O', 'Other');
INSERT INTO hdrl.Gender VALUES(DEFAULT, 'U', 'Unknown');

ALTER TABLE hdrl.InboundSpecimen ADD CONSTRAINT FK_InboundSpecimen_Gender FOREIGN KEY (GenderId) REFERENCES hdrl.Gender (RowId);

/* hdrl-15.21-15.22.sql */

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

/* hdrl-15.22-15.23.sql */

ALTER TABLE hdrl.specimenResult ADD COLUMN RequestId INT;
UPDATE hdrl.specimenResult res SET RequestId = req.InboundRequestId
  FROM hdrl.InboundSpecimen req
  WHERE res.SpecimenId = req.RowId;
ALTER TABLE hdrl.specimenResult ALTER COLUMN RequestId SET NOT NULL;

ALTER TABLE hdrl.specimenResult ADD COLUMN ReportFileName VARCHAR(20);
ALTER TABLE hdrl.specimenResult ADD CONSTRAINT FK_SpecimenResult_InboundRequest FOREIGN KEY (RequestId) REFERENCES hdrl.InboundRequest(RequestId) ON DELETE CASCADE;

ALTER TABLE hdrl.specimenResult ALTER COLUMN modifiedResultFlag SET DEFAULT 'F';

/* hdrl-15.24-15.25.sql */

CREATE TABLE hdrl.labwareOutboundRequests
(
  batch_id INT NOT NULL,
  hdrl_status VARCHAR(20),
  customer_note VARCHAR(254),
  date_received DATE NOT NULL,
  date_completed DATE,
  date_modified DATE NOT NULL,
  CONSTRAINT PK_LabwareOutboundRequests PRIMARY KEY (batch_id),
  CONSTRAINT FK_LabwareOutboundRequests_InboundRequest FOREIGN KEY (batch_id) REFERENCES hdrl.InboundRequest (RequestId) ON DELETE CASCADE
);

CREATE TABLE hdrl.labwareOutboundSpecimens
(
  test_request_id INT NOT NULL,
  batch_id INT NOT NULL,
  date_received DATE NOT NULL,
  date_completed DATE,
  sample_integrity VARCHAR(80),
  test_result VARCHAR(254),
  customer_code VARCHAR(80),
  hdrl_status VARCHAR(20),
  date_modified DATE NOT NULL,
  modified_result_flag VARCHAR(1),
  report_file_name VARCHAR(20),

  CONSTRAINT PK_LabwareOutboundSpecimens PRIMARY KEY (test_request_id),
  CONSTRAINT FK_LabwareOutboundSpecimens_LabwareOutboundRequests FOREIGN KEY (batch_id) REFERENCES hdrl.labwareOutboundRequests (batch_id) ON DELETE CASCADE,
  CONSTRAINT FK_LabwareOutboundSpecimens_InboundSpecimen FOREIGN KEY (test_request_id) REFERENCES hdrl.InboundSpecimen (RowId) ON DELETE CASCADE
);