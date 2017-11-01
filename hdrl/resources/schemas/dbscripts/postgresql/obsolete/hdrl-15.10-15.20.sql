/*
 * Copyright (c) 2015-2017 LabKey Corporation
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
/* hdrl-15.10-15.11.sql */

ALTER TABLE hdrl.InboundRequest ADD COLUMN Title VARCHAR(128);

/* hdrl-15.11-15.12.sql */

ALTER TABLE hdrl.InboundRequest DROP COLUMN IF EXISTS Title;
ALTER TABLE hdrl.InboundRequest ADD COLUMN Title VARCHAR(64);

ALTER TABLE hdrl.InboundSpecimen ALTER COLUMN SSN TYPE VARCHAR(11);

/* hdrl-15.12-15.13.sql */

ALTER TABLE hdrl.InboundSpecimen ADD COLUMN ValidationStatus VARCHAR(128);

/* hdrl-15.13-15.14.sql */

UPDATE hdrl.RequestStatus SET Description='Request is more than 30 days old' WHERE Name='Archived';

/* hdrl-15.14-15.15.sql */

ALTER TABLE hdrl.InboundRequest DROP COLUMN IF EXISTS Title;
ALTER TABLE hdrl.InboundSpecimen ADD  DoDId BIGINT;

/* hdrl-15.15-15.16.sql */

ALTER TABLE hdrl.InboundSpecimen DROP CONSTRAINT IF EXISTS UQ_InboundSpecimen;
ALTER TABLE hdrl.InboundSpecimen DROP CONSTRAINT IF EXISTS FK_InboundSpecimen_Request, ADD CONSTRAINT FK_InboundSpecimen_Request FOREIGN KEY (InboundRequestId) REFERENCES hdrl.InboundRequest (RequestId) ON DELETE CASCADE;

/* hdrl-15.16-15.17.sql */

UPDATE hdrl.InboundSpecimen SET SSN=NULL WHERE CHAR_LENGTH(SSN) > 9;
ALTER TABLE hdrl.InboundSpecimen ALTER SSN TYPE VARCHAR(9);

ALTER TABLE hdrl.FamilyMemberPrefix ALTER CODE TYPE CHAR(2);
UPDATE hdrl.FamilyMemberPrefix SET CODE='00' where CODE='0';
UPDATE hdrl.FamilyMemberPrefix SET CODE='01' where CODE='1';
UPDATE hdrl.FamilyMemberPrefix SET CODE='02' where CODE='2';
UPDATE hdrl.FamilyMemberPrefix SET CODE='03' where CODE='3';
UPDATE hdrl.FamilyMemberPrefix SET CODE='04' where CODE='4';
UPDATE hdrl.FamilyMemberPrefix SET CODE='05' where CODE='5';
UPDATE hdrl.FamilyMemberPrefix SET CODE='06' where CODE='6';
UPDATE hdrl.FamilyMemberPrefix SET CODE='07' where CODE='7';
UPDATE hdrl.FamilyMemberPrefix SET CODE='08' where CODE='8';
UPDATE hdrl.FamilyMemberPrefix SET CODE='09' where CODE='9';

/* hdrl-15.17-15.171.sql */

ALTER TABLE hdrl.InboundSpecimen ADD COLUMN Container ENTITYID;

UPDATE hdrl.InboundSpecimen s SET Container=r.Container from hdrl.InboundRequest r where s.InboundRequestId = r.RequestId;

ALTER TABLE hdrl.InboundSpecimen ALTER Container SET NOT NULL;

/* hdrl-15.171-15.172.sql */

ALTER TABLE hdrl.InboundSpecimen ADD COLUMN MiddleName VARCHAR(64);

/* hdrl-15.172-15.173.sql */

ALTER TABLE hdrl.InboundRequest ADD COLUMN ArchivedRequestCount INT;