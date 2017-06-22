/*
 * Copyright (c) 2014-2017 LabKey Corporation
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
CREATE SCHEMA peptideInventory;

CREATE TABLE peptideInventory.PeptideGroup
(
    Container ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    peptide_group_id INTEGER NOT NULL,
    name VARCHAR(500),
    seq_ref VARCHAR(500),
    Pathogen VARCHAR(500),
    Clade VARCHAR(500),
    GroupType VARCHAR(500),
    AlignRef VARCHAR(500),

    CONSTRAINT PK_PeptideGroup PRIMARY KEY (peptide_group_id)
);

CREATE TABLE peptideInventory.PeptidePool
(
    Container ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    peptide_pool_id INTEGER NOT NULL,
    name VARCHAR(500),
    comment TEXT,
    PoolType VARCHAR(500),
    archived BOOLEAN,
    parent_pool_id INTEGER,
    matrix_peptide_pool_id VARCHAR(500),

    CONSTRAINT PK_PeptidePool PRIMARY KEY (peptide_pool_id)
);

CREATE TABLE peptideInventory.Peptide
(
    Container ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    peptide_id INTEGER NOT NULL,
    peptide_sequence TEXT,
    proteinCategory VARCHAR(500),
    amino_acid_start_pos INTEGER,
    amino_acid_end_pos INTEGER,
    sequence_length INTEGER,
    child BOOLEAN,
    parent BOOLEAN,
    src_file_name VARCHAR(500),
    storage_location VARCHAR(500),
    optimalEpitopeList VARCHAR(500),
    hla_restriction TEXT,
    peptide_flag BOOLEAN,
    peptide_notes TEXT,

    CONSTRAINT PK_Peptide PRIMARY KEY (peptide_id)
);

CREATE TABLE peptideInventory.PeptidePoolAssignment
(
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    peptide_pool_assignment_id INTEGER NOT NULL,
    peptide_pool_id INTEGER NOT NULL,
    peptide_id INTEGER NOT NULL,

    CONSTRAINT PK_PeptidePoolAssignment PRIMARY KEY (peptide_pool_assignment_id),
    CONSTRAINT FK_PeptidePoolAssignment_PoolId FOREIGN KEY (peptide_pool_id) REFERENCES peptideInventory.PeptidePool(peptide_pool_id),
    CONSTRAINT FK_PeptidePoolAssignment_PeptideId FOREIGN KEY (peptide_id) REFERENCES peptideInventory.Peptide(peptide_id)
);

CREATE TABLE peptideInventory.PeptideGroupAssignment
(
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    peptide_group_assignment_id INTEGER NOT NULL,
    peptide_group_id INTEGER NOT NULL,
    peptide_id INTEGER NOT NULL,
    peptide_id_in_group VARCHAR(100),

    CONSTRAINT PK_PeptideGroupAssignment PRIMARY KEY (peptide_group_assignment_id),
    CONSTRAINT FK_PeptideGroupAssignment_GroupId FOREIGN KEY (peptide_group_id) REFERENCES peptideInventory.PeptideGroup(peptide_group_id),
    CONSTRAINT FK_PeptideGroupAssignment_PeptideId FOREIGN KEY (peptide_id) REFERENCES peptideInventory.Peptide(peptide_id)
);

CREATE TABLE peptideInventory.Freezer
(
    RowId SERIAL NOT NULL,
    Container ENTITYID NOT NULL,
    EntityId ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    Name VARCHAR(100),
    Description VARCHAR(500),

    CONSTRAINT PK_Freezer PRIMARY KEY (RowId)
);

CREATE TABLE peptideInventory.Shelf
(
    RowId SERIAL NOT NULL,
    Container ENTITYID NOT NULL,
    EntityId ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    Name VARCHAR(100),
    Description VARCHAR(500),

    CONSTRAINT PK_Shelf PRIMARY KEY (RowId)
);

CREATE TABLE peptideInventory.Rack
(
    RowId SERIAL NOT NULL,
    Container ENTITYID NOT NULL,
    EntityId ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    Name VARCHAR(100),
    Description VARCHAR(500),

    CONSTRAINT PK_Rack PRIMARY KEY (RowId)
);

CREATE TABLE peptideInventory.Drawer
(
    RowId SERIAL NOT NULL,
    Container ENTITYID NOT NULL,
    EntityId ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    Name VARCHAR(100),
    Description VARCHAR(500),

    CONSTRAINT PK_Drawer PRIMARY KEY (RowId)
);

CREATE TABLE peptideInventory.Box
(
    RowId SERIAL NOT NULL,
    Container ENTITYID NOT NULL,
    EntityId ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    Name VARCHAR(100),
    Description VARCHAR(500),

    CONSTRAINT PK_Box PRIMARY KEY (RowId)
);

CREATE TABLE peptideInventory.Vial
(
    Container ENTITYID NOT NULL,
    Created TIMESTAMP,
    CreatedBy USERID,
    Modified TIMESTAMP,
    ModifiedBy USERID,

    PeptideId INTEGER NOT NULL,
    StorageCondition VARCHAR(500),
    ThawCycles INTEGER,

    Freezer INTEGER,
    Shelf INTEGER,
    Rack INTEGER,
    Drawer INTEGER,
    Box INTEGER,
    Slot INTEGER,

    CheckedOut BOOLEAN NOT NULL DEFAULT false,
    Owner USERID,
    Concentration VARCHAR(500),

    CONSTRAINT PK_Vial PRIMARY KEY (PeptideId, Container),

    CONSTRAINT FK_Vial_Freezer FOREIGN KEY (Freezer) REFERENCES peptideInventory.Freezer(rowId),
    CONSTRAINT FK_Vial_Shelf FOREIGN KEY (Shelf) REFERENCES peptideInventory.Shelf(rowId),
    CONSTRAINT FK_Vial_Rack FOREIGN KEY (Rack) REFERENCES peptideInventory.Rack(rowId),
    CONSTRAINT FK_Vial_Drawer FOREIGN KEY (Drawer) REFERENCES peptideInventory.Drawer(rowId),
    CONSTRAINT FK_Vial_Box FOREIGN KEY (Box) REFERENCES peptideInventory.Box(rowId)
);


