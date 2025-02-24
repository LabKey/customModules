DROP VIEW pepdb.group_peptides;
ALTER TABLE pepdb.peptide_group_assignment ALTER COLUMN peptide_id_in_group TYPE float;
CREATE VIEW pepdb.group_peptides AS
SELECT src.peptide_group_assignment_id,src.peptide_id, src.peptide_group_id, src.peptide_id_in_group,
    pgroup.peptide_group_name,pgroup.pathogen_id,
    p.peptide_sequence,p.protein_cat_id,
    p.sequence_length,p.amino_acid_start_pos,p.amino_acid_end_pos,
     p.child, p.parent,p.optimal_epitope_list_id,p.hla_restriction,
     src.frequency_number,src.frequency_number_date,src.in_current_file
FROM ((pepdb.peptide_group_assignment src LEFT JOIN pepdb.peptide_group pgroup ON ((src.peptide_group_id
    = pgroup.peptide_group_id))) LEFT JOIN pepdb.peptides p ON ((src.peptide_id =
    p.peptide_id)));

DROP VIEW pepdb.pool_peptides;
CREATE VIEW pepdb.pool_peptides AS
SELECT src.peptide_pool_assignment_id,src.peptide_id,src.peptide_pool_id, 
p.peptide_sequence,p.protein_cat_id,
p.sequence_length,p.amino_acid_start_pos,p.amino_acid_end_pos,
p.child, p.parent,
 pp.pool_type_id,pp.peptide_pool_name,pt.pool_type_desc
FROM pepdb.peptide_pool_assignment src LEFT JOIN pepdb.peptide_pool pp LEFT JOIN pepdb.pool_type pt ON(pp.pool_type_id = pt.pool_type_id) ON (src.peptide_pool_id = pp.peptide_pool_id), pepdb.peptides p
WHERE (src.peptide_id = p.peptide_id);

