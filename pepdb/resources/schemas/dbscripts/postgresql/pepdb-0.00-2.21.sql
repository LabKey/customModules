CREATE SCHEMA pepdb;

CREATE TABLE pepdb.clade (
    clade_id SERIAL NOT NULL,
    clade_desc text
);

CREATE TABLE pepdb.peptide_group (
    _ts timestamp without time zone DEFAULT now(),
    createdby public.userid,
    created timestamp without time zone,
    modifiedby public.userid,
    modified timestamp without time zone,
    peptide_group_id SERIAL NOT NULL,
    peptide_group_name text NOT NULL,
    pathogen_id integer,
    seq_ref text,
    clade_id integer,
    pep_align_ref_id integer,
    group_type_id integer
);

CREATE TABLE pepdb.peptide_group_assignment (
    _ts timestamp without time zone DEFAULT now(),
    createdby public.userid,
    created timestamp without time zone,
    modifiedby public.userid,
    modified timestamp without time zone,
    peptide_group_assignment_id SERIAL NOT NULL,
    peptide_id integer NOT NULL,
    peptide_group_id integer NOT NULL,
    peptide_id_in_group character varying,
    frequency_number double precision,
    frequency_number_date date,
    in_current_file boolean DEFAULT false
);

CREATE TABLE pepdb.peptides (
    _ts timestamp without time zone DEFAULT now(),
    createdby public.userid,
    created timestamp without time zone,
    modifiedby public.userid,
    modified timestamp without time zone,
    peptide_id SERIAL NOT NULL,
    peptide_sequence text NOT NULL,
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
    peptide_flag boolean DEFAULT false,
    peptide_notes text
);

CREATE TABLE pepdb.group_type (
    group_type_id SERIAL NOT NULL,
    group_type_desc text
);

CREATE TABLE pepdb.optimal_epitope_list (
    optimal_epitope_list_id SERIAL NOT NULL,
    optimal_epitope_list_desc text
);

CREATE TABLE pepdb.parent (
    _ts timestamp without time zone DEFAULT now(),
    createdby public.userid,
    created timestamp without time zone,
    modifiedby public.userid,
    modified timestamp without time zone,
    peptide_id integer NOT NULL,
    linked_parent integer NOT NULL
);

CREATE TABLE pepdb.pathogen (
    pathogen_id SERIAL NOT NULL,
    pathogen_desc text
);

CREATE TABLE pepdb.pep_align_ref (
    pep_align_ref_id SERIAL NOT NULL,
    pep_align_ref_desc text
);

CREATE TABLE pepdb.peptide_pool (
    _ts timestamp without time zone DEFAULT now(),
    createdby public.userid,
    created timestamp without time zone,
    modifiedby public.userid,
    modified timestamp without time zone,
    peptide_pool_id SERIAL NOT NULL,
    peptide_pool_name text,
    pool_type_id integer,
    comment text,
    archived boolean DEFAULT false,
    parent_pool_id integer,
    matrix_peptide_pool_id text
);

CREATE TABLE pepdb.peptide_pool_assignment (
    _ts timestamp without time zone DEFAULT now(),
    createdby public.userid,
    created timestamp without time zone,
    modifiedby public.userid,
    modified timestamp without time zone,
    peptide_pool_assignment_id SERIAL NOT NULL,
    peptide_pool_id integer NOT NULL,
    peptide_id integer NOT NULL,
    peptide_group_assignment_id integer
);

CREATE TABLE pepdb.pool_type (
    pool_type_id SERIAL NOT NULL,
    pool_type_desc text
);

CREATE TABLE pepdb.protein_category (
    protein_cat_id SERIAL NOT NULL,
    protein_cat_desc text
);

CREATE TABLE pepdb.temp_peppoolgroupassign (
    peptide_pool_assignment_id integer,
    peptide_group_assignment_id integer
);

ALTER TABLE ONLY pepdb.peptide_group_assignment
    ADD CONSTRAINT peptide_group_assignment_peptide_group_assignment_id_key UNIQUE (peptide_group_assignment_id);

ALTER TABLE ONLY pepdb.peptide_group
    ADD CONSTRAINT peptide_group_peptide_group_name_key UNIQUE (peptide_group_name);

ALTER TABLE ONLY pepdb.peptide_pool_assignment
    ADD CONSTRAINT peptide_pool_assignment_peptide_pool_assignment_id_key UNIQUE (peptide_pool_assignment_id);

ALTER TABLE ONLY pepdb.peptides
    ADD CONSTRAINT peptides_peptide_sequence_key UNIQUE (peptide_sequence);

ALTER TABLE ONLY pepdb.clade
    ADD CONSTRAINT pk_clade PRIMARY KEY (clade_id);

ALTER TABLE ONLY pepdb.group_type
    ADD CONSTRAINT pk_group_type PRIMARY KEY (group_type_id);

ALTER TABLE ONLY pepdb.optimal_epitope_list
    ADD CONSTRAINT pk_optimal_epitope_list PRIMARY KEY (optimal_epitope_list_id);

ALTER TABLE ONLY pepdb.parent
    ADD CONSTRAINT pk_parent PRIMARY KEY (peptide_id, linked_parent);

ALTER TABLE ONLY pepdb.pathogen
    ADD CONSTRAINT pk_pathogen PRIMARY KEY (pathogen_id);

ALTER TABLE ONLY pepdb.pep_align_ref
    ADD CONSTRAINT pk_pep_align_ref PRIMARY KEY (pep_align_ref_id);

ALTER TABLE ONLY pepdb.peptide_group
    ADD CONSTRAINT pk_peptide_group PRIMARY KEY (peptide_group_id);

ALTER TABLE ONLY pepdb.peptide_group_assignment
    ADD CONSTRAINT pk_peptide_group_assignment PRIMARY KEY (peptide_id, peptide_group_id);

ALTER TABLE ONLY pepdb.peptide_pool
    ADD CONSTRAINT pk_peptide_pool PRIMARY KEY (peptide_pool_id);

ALTER TABLE ONLY pepdb.peptide_pool_assignment
    ADD CONSTRAINT pk_peptide_pool_assignment PRIMARY KEY (peptide_pool_id, peptide_id);

ALTER TABLE ONLY pepdb.peptides
    ADD CONSTRAINT pk_peptides PRIMARY KEY (peptide_id);

ALTER TABLE ONLY pepdb.pool_type
    ADD CONSTRAINT pk_pool_type PRIMARY KEY (pool_type_id);

ALTER TABLE ONLY pepdb.protein_category
    ADD CONSTRAINT pk_protein_category PRIMARY KEY (protein_cat_id);

ALTER TABLE ONLY pepdb.parent
    ADD CONSTRAINT fk_parent1 FOREIGN KEY (peptide_id) REFERENCES pepdb.peptides(peptide_id);

ALTER TABLE ONLY pepdb.parent
    ADD CONSTRAINT fk_parent2 FOREIGN KEY (linked_parent) REFERENCES pepdb.peptides(peptide_id);

ALTER TABLE ONLY pepdb.peptide_group
    ADD CONSTRAINT fk_peptide_group1 FOREIGN KEY (pathogen_id) REFERENCES pepdb.pathogen(pathogen_id);

ALTER TABLE ONLY pepdb.peptide_group
    ADD CONSTRAINT fk_peptide_group2 FOREIGN KEY (clade_id) REFERENCES pepdb.clade(clade_id);

ALTER TABLE ONLY pepdb.peptide_group
    ADD CONSTRAINT fk_peptide_group3 FOREIGN KEY (group_type_id) REFERENCES pepdb.group_type(group_type_id);

ALTER TABLE ONLY pepdb.peptide_group
    ADD CONSTRAINT fk_peptide_group4 FOREIGN KEY (pep_align_ref_id) REFERENCES pepdb.pep_align_ref(pep_align_ref_id);

ALTER TABLE ONLY pepdb.peptide_group_assignment
    ADD CONSTRAINT fk_peptide_group_assignment1 FOREIGN KEY (peptide_id) REFERENCES pepdb.peptides(peptide_id);

ALTER TABLE ONLY pepdb.peptide_group_assignment
    ADD CONSTRAINT fk_peptide_group_assignment2 FOREIGN KEY (peptide_group_id) REFERENCES pepdb.peptide_group(peptide_group_id);

ALTER TABLE ONLY pepdb.peptide_pool
    ADD CONSTRAINT fk_peptide_pool1 FOREIGN KEY (pool_type_id) REFERENCES pepdb.pool_type(pool_type_id);

ALTER TABLE ONLY pepdb.peptide_pool
    ADD CONSTRAINT fk_peptide_pool2 FOREIGN KEY (parent_pool_id) REFERENCES pepdb.peptide_pool(peptide_pool_id);

ALTER TABLE ONLY pepdb.peptide_pool_assignment
    ADD CONSTRAINT fk_peptide_pool_assignment1 FOREIGN KEY (peptide_pool_id) REFERENCES pepdb.peptide_pool(peptide_pool_id);

ALTER TABLE ONLY pepdb.peptide_pool_assignment
    ADD CONSTRAINT fk_peptide_pool_assignment2 FOREIGN KEY (peptide_id) REFERENCES pepdb.peptides(peptide_id);

ALTER TABLE ONLY pepdb.peptide_pool_assignment
    ADD CONSTRAINT fk_peptide_pool_assignment3 FOREIGN KEY (peptide_group_assignment_id) REFERENCES pepdb.peptide_group_assignment(peptide_group_assignment_id);

ALTER TABLE ONLY pepdb.peptides
    ADD CONSTRAINT fk_peptides1 FOREIGN KEY (protein_cat_id) REFERENCES pepdb.protein_category(protein_cat_id);

ALTER TABLE ONLY pepdb.peptides
    ADD CONSTRAINT fk_peptides2 FOREIGN KEY (optimal_epitope_list_id) REFERENCES pepdb.optimal_epitope_list(optimal_epitope_list_id);
