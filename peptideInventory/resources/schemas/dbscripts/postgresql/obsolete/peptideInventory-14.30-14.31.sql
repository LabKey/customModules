/*
 * Copyright (c) 2014-2015 LabKey Corporation
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

ALTER TABLE peptideInventory.Vial ADD Used BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE peptideInventory.BoxLocation
(
    Container ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    Label VARCHAR(500),
    Identifier VARCHAR(100),

    CONSTRAINT PK_BoxLocation PRIMARY KEY (Identifier, Container)
);

CREATE TABLE peptideInventory.LotAssignment
(
    Container ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    PeptideId INTEGER NOT NULL,
    LotNumber VARCHAR(100),

    CONSTRAINT PK_LotAssignment PRIMARY KEY (PeptideId, Container),
    CONSTRAINT UQ_LotAssignment UNIQUE(PeptideId, Container, LotNumber)
);
