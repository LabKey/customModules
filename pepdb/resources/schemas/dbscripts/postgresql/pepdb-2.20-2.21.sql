BEGIN;

DROP VIEW IF EXISTS pepdb.parent_child_details;
DROP VIEW IF EXISTS pepdb.pool_peptides;
DROP VIEW IF EXISTS pepdb.group_peptides;

-- RT #103413. Modify peptide_id_in_group's column type to varchar because the underlying data source
-- (an uploaded text file) changed format.
ALTER TABLE pepdb.peptide_group_assignment ALTER COLUMN peptide_id_in_group SET DATA TYPE varchar;

CREATE OR REPLACE VIEW pepdb.parent_child_details AS 
 SELECT par.peptide_id AS child_id, pchild.peptide_sequence AS child_sequence, pchild.protein_cat_id AS child_protein, pgchild.peptide_group_id AS child_group, pgchild.peptide_id_in_group AS child_lab_id, pchild.sequence_length AS child_seq_length, pchild.amino_acid_start_pos AS child_aastart, pchild.amino_acid_end_pos AS child_aaend, pchild.optimal_epitope_list_id AS child_optimal_epitope_list_id, pchild.hla_restriction AS child_hla_restriction, pchild.peptide_flag AS child_peptide_flag, pchild.peptide_notes AS child_peptide_notes, par.linked_parent AS parent_id, pparent.peptide_sequence AS parent_sequence, pparent.protein_cat_id AS parent_protein, pgparent.peptide_group_id AS parent_group, pgparent.peptide_id_in_group AS parent_lab_id, pparent.sequence_length AS parent_seq_length, pparent.amino_acid_start_pos AS parent_aastart, pparent.amino_acid_end_pos AS parent_aaend, pparent.peptide_flag AS parent_peptide_flag, pparent.peptide_notes AS parent_peptide_notes
   FROM pepdb.parent par
   LEFT JOIN pepdb.peptides pchild ON par.peptide_id = pchild.peptide_id
   LEFT JOIN pepdb.peptides pparent ON par.linked_parent = pparent.peptide_id
   LEFT JOIN pepdb.peptide_group_assignment pgchild ON par.peptide_id = pgchild.peptide_id
   LEFT JOIN pepdb.peptide_group_assignment pgparent ON par.linked_parent = pgparent.peptide_id;
   
CREATE OR REPLACE VIEW pepdb.pool_peptides AS 
 SELECT src.peptide_pool_assignment_id, src.peptide_id, src.peptide_pool_id, p.peptide_sequence, p.protein_cat_id, pg.peptide_group_id, pg.peptide_id_in_group, p.sequence_length, p.amino_acid_start_pos, p.amino_acid_end_pos, p.child, p.parent, p.peptide_flag, p.peptide_notes, pp.pool_type_id, pp.peptide_pool_name, pt.pool_type_desc, pp.archived
   FROM pepdb.peptide_pool_assignment src
   LEFT JOIN (pepdb.peptide_pool pp
   LEFT JOIN pepdb.pool_type pt ON pp.pool_type_id = pt.pool_type_id) ON src.peptide_pool_id = pp.peptide_pool_id
   LEFT JOIN pepdb.peptide_group_assignment pg ON src.peptide_group_assignment_id = pg.peptide_group_assignment_id, pepdb.peptides p
  WHERE src.peptide_id = p.peptide_id;

CREATE OR REPLACE VIEW pepdb.group_peptides AS 
 SELECT src.peptide_group_assignment_id, src.peptide_id, src.peptide_group_id, src.peptide_id_in_group, pgroup.peptide_group_name, pgroup.pathogen_id, p.peptide_sequence, p.protein_cat_id, p.sequence_length, p.amino_acid_start_pos, p.amino_acid_end_pos, p.child, p.parent, p.optimal_epitope_list_id, p.hla_restriction, p.peptide_flag, p.peptide_notes, src.frequency_number, src.frequency_number_date, src.in_current_file
   FROM pepdb.peptide_group_assignment src
   LEFT JOIN pepdb.peptide_group pgroup ON src.peptide_group_id = pgroup.peptide_group_id
   LEFT JOIN pepdb.peptides p ON src.peptide_id = p.peptide_id;

COMMIT;
