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
ALTER TABLE hdrl.specimenResult ADD COLUMN RequestId INT;
UPDATE hdrl.specimenResult res SET RequestId = req.InboundRequestId
  FROM hdrl.InboundSpecimen req
  WHERE res.SpecimenId = req.RowId;
ALTER TABLE hdrl.specimenResult ALTER COLUMN RequestId SET NOT NULL;

ALTER TABLE hdrl.specimenResult ADD COLUMN ReportFileName VARCHAR(20);
ALTER TABLE hdrl.specimenResult ADD CONSTRAINT FK_SpecimenResult_InboundRequest FOREIGN KEY (RequestId) REFERENCES hdrl.InboundRequest(RequestId) ON DELETE CASCADE;

ALTER TABLE hdrl.specimenResult ALTER COLUMN modifiedResultFlag SET DEFAULT 'F';