DROP VIEW pepdb.pool_peptides;
CREATE VIEW pepdb.pool_peptides AS
SELECT src.peptide_pool_assignment_id,src.peptide_id,src.peptide_pool_id,
p.peptide_sequence,p.protein_cat_id,pg.peptide_group_id,pg.peptide_id_in_group,
p.sequence_length,p.amino_acid_start_pos,p.amino_acid_end_pos,
p.child, p.parent,p.peptide_flag,p.peptide_notes,
 pp.pool_type_id,pp.peptide_pool_name,pt.pool_type_desc,pp.archived
FROM pepdb.peptide_pool_assignment src
LEFT JOIN pepdb.peptide_pool pp LEFT JOIN pepdb.pool_type pt ON(pp.pool_type_id = pt.pool_type_id) ON (src.peptide_pool_id = pp.peptide_pool_id)
LEFT JOIN pepdb.peptide_group_assignment pg ON(src.peptide_group_assignment_id = pg.peptide_group_assignment_id),
pepdb.peptides p
WHERE (src.peptide_id = p.peptide_id);