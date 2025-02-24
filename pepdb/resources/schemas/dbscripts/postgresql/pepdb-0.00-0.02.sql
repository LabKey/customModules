/*
 * Copyright (c) 2003-2005 Fred Hutchinson Cancer Research Center
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

BEGIN;

CREATE SCHEMA pepdb;

CREATE TABLE pepdb.clade (
    clade_id serial NOT NULL,
    clade_desc text,

    CONSTRAINT PK_clade PRIMARY KEY (clade_id)
);

CREATE TABLE pepdb.pathogen (
    pathogen_id serial NOT NULL,
    pathogen_desc text,

    CONSTRAINT PK_pathogen PRIMARY KEY (pathogen_id)
);

CREATE TABLE pepdb.group_type (
    group_type_id serial NOT NULL,
    group_type_desc text,

    CONSTRAINT PK_group_type PRIMARY KEY (group_type_id)
);

CREATE TABLE pepdb.pep_align_ref (
    pep_align_ref_id serial NOT NULL,
    pep_align_ref_desc text,

    CONSTRAINT PK_pep_align_ref PRIMARY KEY (pep_align_ref_id)
);

CREATE TABLE pepdb.peptide_group (
    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    peptide_group_id serial NOT NULL,
    peptide_group_name text NOT NULL UNIQUE,
    pathogen_id integer,
    seq_ref text,
    clade_id integer,
    pep_align_ref_id integer,
    group_type_id integer,

    CONSTRAINT PK_peptide_group PRIMARY KEY (peptide_group_id),
    CONSTRAINT FK_peptide_group1 FOREIGN KEY(pathogen_id) REFERENCES pepdb.pathogen(pathogen_id),
    CONSTRAINT FK_peptide_group2 FOREIGN KEY(clade_id) REFERENCES pepdb.clade(clade_id),
    CONSTRAINT FK_peptide_group3 FOREIGN KEY(group_type_id) REFERENCES pepdb.group_type(group_type_id),
    CONSTRAINT FK_peptide_group4 FOREIGN KEY(pep_align_ref_id) REFERENCES pepdb.pep_align_ref(pep_align_ref_id)
);

CREATE TABLE pepdb.protein_category (
    protein_cat_id serial NOT NULL,
    protein_cat_desc text,

    CONSTRAINT PK_protein_category PRIMARY KEY (protein_cat_id)
);

CREATE TABLE pepdb.optimal_epitope_list(
    optimal_epitope_list_id serial NOT NULL,
    optimal_epitope_list_desc text,

    CONSTRAINT PK_optimal_epitope_list PRIMARY KEY (optimal_epitope_list_id)
);


CREATE TABLE pepdb.peptides (
    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    peptide_id serial NOT NULL,    
    peptide_sequence text NOT NULL UNIQUE,    
    protein_cat_id integer,
    amino_acid_start_pos integer,
    amino_acid_end_pos integer,
    sequence_length integer,
    child boolean,
    parent boolean DEFAULT false NOT NULL,
    src_file_name text,
    storage_location text,
    optimal_epitope_list_id integer,
    hla_restriction text,
    
    CONSTRAINT PK_peptides PRIMARY KEY (peptide_id),
    CONSTRAINT FK_peptides1 FOREIGN KEY(protein_cat_id) REFERENCES pepdb.protein_category(protein_cat_id),
    CONSTRAINT FK_peptides2 FOREIGN KEY(optimal_epitope_list_id) REFERENCES pepdb.optimal_epitope_list(optimal_epitope_list_id)
);


CREATE TABLE pepdb.peptide_group_assignment (
    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    peptide_group_assignment_id serial NOT NULL UNIQUE,
    peptide_id integer NOT NULL,
    peptide_group_id integer NOT NULL,
    peptide_id_in_group integer,
    frequency_number float,
    frequency_number_date date,
    in_current_file boolean DEFAULT false,

    CONSTRAINT PK_peptide_group_assignment PRIMARY KEY (peptide_id,peptide_group_id),
    CONSTRAINT FK_peptide_group_assignment1 FOREIGN KEY(peptide_id) REFERENCES pepdb.peptides(peptide_id),
    CONSTRAINT FK_peptide_group_assignment2 FOREIGN KEY(peptide_group_id) REFERENCES pepdb.peptide_group(peptide_group_id)
);

CREATE TABLE pepdb.parent (
    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    peptide_id integer NOT NULL,
    linked_parent integer NOT NULL,

    CONSTRAINT PK_parent PRIMARY KEY (peptide_id,linked_parent),
    CONSTRAINT FK_parent1 FOREIGN KEY(peptide_id) REFERENCES pepdb.peptides(peptide_id),
    CONSTRAINT FK_parent2 FOREIGN KEY(linked_parent) REFERENCES pepdb.peptides(peptide_id)    
);

CREATE TABLE pepdb.pool_type (
    pool_type_id serial NOT NULL,
    pool_type_desc text,

    CONSTRAINT PK_pool_type PRIMARY KEY (pool_type_id)
);

CREATE TABLE pepdb.peptide_pool (
    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    peptide_pool_id serial NOT NULL,
    peptide_pool_name text,
    pool_type_id integer,
    comment text,

    CONSTRAINT PK_peptide_pool PRIMARY KEY (peptide_pool_id),
    CONSTRAINT FK_peptide_pool1 FOREIGN KEY(pool_type_id) REFERENCES pepdb.pool_type(pool_type_id)
);


CREATE TABLE pepdb.peptide_pool_assignment (
    _ts TIMESTAMP DEFAULT now(),
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,

    peptide_pool_assignment_id serial NOT NULL UNIQUE,
    peptide_pool_id integer NOT NULL,
    peptide_id integer NOT NULL,

    CONSTRAINT PK_peptide_pool_assignment PRIMARY KEY (peptide_pool_id,peptide_id),
    CONSTRAINT FK_peptide_pool_assignment1 FOREIGN KEY(peptide_pool_id) REFERENCES pepdb.peptide_pool(peptide_pool_id),
    CONSTRAINT FK_peptide_pool_assignment2 FOREIGN KEY(peptide_id) REFERENCES pepdb.peptides(peptide_id)
);

CREATE VIEW pepdb.parent_child_details AS
select par.peptide_id AS child_id,pchild.peptide_sequence AS child_sequence,
pchild.protein_cat_id AS child_protein,pchild.sequence_length AS child_seq_length,
pchild.amino_acid_start_pos AS child_AAStart,pchild.amino_acid_end_pos AS child_AAEnd,
pchild.optimal_epitope_list_id AS child_optimal_epitope_list_id,pchild.hla_restriction AS child_hla_restriction,
par.linked_parent AS parent_id,pparent.peptide_sequence AS parent_sequence,
pparent.protein_cat_id AS parent_protein,pparent.sequence_length AS parent_seq_length,
pparent.amino_acid_start_pos AS parent_AAStart,pparent.amino_acid_end_pos AS parent_AAEnd 
from pepdb.parent par LEFT JOIN pepdb.peptides pchild ON (par.peptide_id = pchild.peptide_id)
LEFT JOIN pepdb.peptides pparent ON(par.linked_parent = pparent.peptide_id);

CREATE VIEW pepdb.group_peptides AS
SELECT src.peptide_id, src.peptide_group_id, src.peptide_id_in_group,
    pgroup.peptide_group_name,pgroup.pathogen_id,
    p.peptide_sequence,p.protein_cat_id,
    p.sequence_length,p.amino_acid_start_pos,p.amino_acid_end_pos,
     p.child, p.parent,p.optimal_epitope_list_id,p.hla_restriction,
     src.frequency_number,src.frequency_number_date,src.in_current_file
FROM ((pepdb.peptide_group_assignment src LEFT JOIN pepdb.peptide_group pgroup ON ((src.peptide_group_id
    = pgroup.peptide_group_id))) LEFT JOIN pepdb.peptides p ON ((src.peptide_id =
    p.peptide_id)));

CREATE VIEW pepdb.pool_peptides AS
SELECT src.peptide_id, p.peptide_sequence, src.peptide_pool_id, pp.pool_type_id, pp.peptide_pool_name, pt.pool_type_desc
FROM
pepdb.peptides p,
pepdb.peptide_pool_assignment src
LEFT JOIN pepdb.peptide_pool pp
LEFT JOIN pepdb.pool_type pt ON(pp.pool_type_id = pt.pool_type_id) ON (src.peptide_pool_id = pp.peptide_pool_id)
WHERE (src.peptide_id = p.peptide_id);

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('pepdb.peptides', 'peptide_id'), 500000, true);
SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('pepdb.peptide_pool', 'peptide_pool_id'), 500000, true);

COMMIT;







