CREATE VIEW pepdb.group_peptides AS
    SELECT src.peptide_group_assignment_id, src.peptide_id, src.peptide_group_id, src.peptide_id_in_group, pgroup.peptide_group_name, pgroup.pathogen_id, p.peptide_sequence, p.protein_cat_id, p.sequence_length, p.amino_acid_start_pos, p.amino_acid_end_pos, p.child, p.parent, p.optimal_epitope_list_id, p.hla_restriction, p.peptide_flag, p.peptide_notes, src.frequency_number, src.frequency_number_date, src.in_current_file FROM ((pepdb.peptide_group_assignment src LEFT JOIN pepdb.peptide_group pgroup ON ((src.peptide_group_id = pgroup.peptide_group_id))) LEFT JOIN pepdb.peptides p ON ((src.peptide_id = p.peptide_id)));

CREATE VIEW pepdb.parent_child_details AS
    SELECT par.peptide_id AS child_id, pchild.peptide_sequence AS child_sequence, pchild.protein_cat_id AS child_protein, pgchild.peptide_group_id AS child_group, pgchild.peptide_id_in_group AS child_lab_id, pchild.sequence_length AS child_seq_length, pchild.amino_acid_start_pos AS child_aastart, pchild.amino_acid_end_pos AS child_aaend, pchild.optimal_epitope_list_id AS child_optimal_epitope_list_id, pchild.hla_restriction AS child_hla_restriction, pchild.peptide_flag AS child_peptide_flag, pchild.peptide_notes AS child_peptide_notes, par.linked_parent AS parent_id, pparent.peptide_sequence AS parent_sequence, pparent.protein_cat_id AS parent_protein, pgparent.peptide_group_id AS parent_group, pgparent.peptide_id_in_group AS parent_lab_id, pparent.sequence_length AS parent_seq_length, pparent.amino_acid_start_pos AS parent_aastart, pparent.amino_acid_end_pos AS parent_aaend, pparent.peptide_flag AS parent_peptide_flag, pparent.peptide_notes AS parent_peptide_notes FROM ((((pepdb.parent par LEFT JOIN pepdb.peptides pchild ON ((par.peptide_id = pchild.peptide_id))) LEFT JOIN pepdb.peptides pparent ON ((par.linked_parent = pparent.peptide_id))) LEFT JOIN pepdb.peptide_group_assignment pgchild ON ((par.peptide_id = pgchild.peptide_id))) LEFT JOIN pepdb.peptide_group_assignment pgparent ON ((par.linked_parent = pgparent.peptide_id)));

CREATE VIEW pepdb.pool_details AS
    SELECT peptide_pool._ts, peptide_pool.createdby, peptide_pool.created, peptide_pool.modifiedby, peptide_pool.modified, peptide_pool.peptide_pool_id, peptide_pool.peptide_pool_name, peptide_pool.pool_type_id, pt.pool_type_desc, peptide_pool.comment, peptide_pool.archived, peptide_pool.parent_pool_id, peptide_pool.matrix_peptide_pool_id, p.peptide_pool_name AS parent_pool_name FROM ((pepdb.peptide_pool LEFT JOIN pepdb.peptide_pool p ON ((peptide_pool.parent_pool_id = p.peptide_pool_id))) LEFT JOIN pepdb.pool_type pt ON ((peptide_pool.pool_type_id = pt.pool_type_id)));

CREATE VIEW pepdb.pool_peptides AS
    SELECT src.peptide_pool_assignment_id, src.peptide_id, src.peptide_pool_id, p.peptide_sequence, p.protein_cat_id, pg.peptide_group_id, pg.peptide_id_in_group, p.sequence_length, p.amino_acid_start_pos, p.amino_acid_end_pos, p.child, p.parent, p.peptide_flag, p.peptide_notes, pp.pool_type_id, pp.peptide_pool_name, pt.pool_type_desc, pp.archived FROM ((pepdb.peptide_pool_assignment src LEFT JOIN (pepdb.peptide_pool pp LEFT JOIN pepdb.pool_type pt ON ((pp.pool_type_id = pt.pool_type_id))) ON ((src.peptide_pool_id = pp.peptide_pool_id))) LEFT JOIN pepdb.peptide_group_assignment pg ON ((src.peptide_group_assignment_id = pg.peptide_group_assignment_id))), pepdb.peptides p WHERE (src.peptide_id = p.peptide_id);

CREATE VIEW pepdb.peptideGroupRollup AS
    SELECT
      pg.created,
      pg.createdBy,
      pg.modified,
      pg.modifiedBy,
      pg.peptide_group_id,
      pg.peptide_group_name AS name,
      seq_ref,
      p.pathogen_desc AS Pathogen,
      c.clade_desc AS Clade,
      gt.group_type_desc AS GroupType,
      ar.pep_align_ref_desc AS AlignRef
    FROM pepdb.peptide_group pg
      LEFT JOIN pepdb.pathogen p ON pg.pathogen_id = p.pathogen_id
      LEFT JOIN pepdb.clade c ON pg.clade_id = c.clade_id
      LEFT JOIN pepdb.group_type gt ON pg.group_type_id = gt.group_type_id
      LEFT JOIN pepdb.pep_align_ref ar ON pg.pep_align_ref_id = ar.pep_align_ref_id;

CREATE VIEW pepdb.peptidePoolRollup AS
    SELECT
      pp.created,
      pp.createdBy,
      pp.modified,
      pp.modifiedBy,
      pp.peptide_pool_id,
      pp.peptide_pool_name AS Name,
      comment,
      pt.pool_type_desc AS PoolType,
      archived,
      parent_pool_id,
      matrix_peptide_pool_id
    FROM pepdb.peptide_pool pp
      JOIN pepdb.pool_type pt ON pp.pool_type_id = pt.pool_type_id;

CREATE VIEW pepdb.peptideRollup AS
    SELECT
      p.created,
      p.createdBy,
      p.modified,
      p.modifiedBy,
      p.peptide_id,
      peptide_sequence,
      protein_cat_desc AS ProteinCategory,
      amino_acid_start_pos,
      amino_acid_end_pos,
      sequence_length,
      child,
      parent,
      src_file_name,
      storage_location,
      optimal_epitope_list_desc AS OptimalEpitopeList,
      hla_restriction,
      peptide_flag,
      peptide_notes
    FROM pepdb.peptides p
      LEFT JOIN pepdb.protein_category pc ON p.protein_cat_id = pc.protein_cat_id
      LEFT JOIN pepdb.optimal_epitope_list el ON p.optimal_epitope_list_id = el.optimal_epitope_list_id;
