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

