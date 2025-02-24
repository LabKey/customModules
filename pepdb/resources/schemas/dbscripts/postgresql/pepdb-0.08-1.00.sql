DROP VIEW pepdb.pool_peptides;
CREATE VIEW pepdb.pool_peptides AS
SELECT src.peptide_pool_assignment_id,src.peptide_id,src.peptide_pool_id, 
p.peptide_sequence,p.protein_cat_id,pg.peptide_group_id,pg.peptide_id_in_group,
p.sequence_length,p.amino_acid_start_pos,p.amino_acid_end_pos,
p.child, p.parent,
 pp.pool_type_id,pp.peptide_pool_name,pt.pool_type_desc
FROM pepdb.peptide_pool_assignment src LEFT JOIN pepdb.peptide_pool pp
LEFT JOIN pepdb.pool_type pt ON(pp.pool_type_id = pt.pool_type_id)
ON (src.peptide_pool_id = pp.peptide_pool_id), pepdb.peptides p, pepdb.peptide_group_assignment pg
WHERE (src.peptide_id = p.peptide_id) and (pg.peptide_id = p.peptide_id);

DROP VIEW pepdb.parent_child_details;
CREATE VIEW pepdb.parent_child_details AS
select par.peptide_id AS child_id,pchild.peptide_sequence AS child_sequence,
pchild.protein_cat_id AS child_protein,pgchild.peptide_group_id AS child_group,
pgchild.peptide_id_in_group AS child_lab_id,pchild.sequence_length AS child_seq_length,
pchild.amino_acid_start_pos AS child_AAStart,pchild.amino_acid_end_pos AS child_AAEnd,
pchild.optimal_epitope_list_id AS child_optimal_epitope_list_id,pchild.hla_restriction AS child_hla_restriction,
par.linked_parent AS parent_id,pparent.peptide_sequence AS parent_sequence,
pparent.protein_cat_id AS parent_protein,pgparent.peptide_group_id AS parent_group,
pgparent.peptide_id_in_group AS parent_lab_id,pparent.sequence_length AS parent_seq_length,
pparent.amino_acid_start_pos AS parent_AAStart,pparent.amino_acid_end_pos AS parent_AAEnd 
from pepdb.parent par LEFT JOIN pepdb.peptides pchild ON (par.peptide_id = pchild.peptide_id)
LEFT JOIN pepdb.peptides pparent ON(par.linked_parent = pparent.peptide_id)
LEFT JOIN pepdb.peptide_group_assignment pgchild ON(par.peptide_id = pgchild.peptide_id)
LEFT JOIN pepdb.peptide_group_assignment pgparent ON(par.linked_parent = pgparent.peptide_id);