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

ALTER TABLE peptideInventory.Vial ADD RCPoolId INTEGER NOT NULL DEFAULT -1;

ALTER TABLE peptideInventory.Vial DROP CONSTRAINT PK_Vial;
ALTER TABLE peptideInventory.Vial ADD CONSTRAINT PK_Vial PRIMARY KEY (PeptideId, RCPoolId, Container);

CREATE TABLE peptideInventory.RCPool
(
    RowId SERIAL NOT NULL,
    Container ENTITYID NOT NULL,
    EntityId ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    VialCount INTEGER NOT NULL,
    LotNumber VARCHAR(100),

    CONSTRAINT UQ_RCPool UNIQUE(Container, LotNumber),
    CONSTRAINT PK_RCPool PRIMARY KEY (RowId)
);

CREATE TABLE peptideInventory.RCPoolAssignment
(
    Container ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    PeptideId INTEGER NOT NULL,
    RCPoolId INTEGER NOT NULL,

    CONSTRAINT PK_RCPoolAssignment PRIMARY KEY (PeptideId, Container),
    CONSTRAINT UQ_RCPoolAssignment UNIQUE(PeptideId, Container, RCPoolId),
    CONSTRAINT FK_RCPoolId_RCPool FOREIGN KEY (RcPoolId) REFERENCES peptideInventory.RCPool(RowId)
);
