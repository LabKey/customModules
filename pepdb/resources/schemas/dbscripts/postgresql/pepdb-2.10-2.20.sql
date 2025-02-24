CREATE OR REPLACE VIEW pepdb.pool_details AS
SELECT peptide_pool._ts,peptide_pool.createdby,peptide_pool.created,peptide_pool.modifiedby,peptide_pool.modified,
peptide_pool.peptide_pool_id,peptide_pool.peptide_pool_name,
peptide_pool.pool_type_id,pt.pool_type_desc,
peptide_pool.comment,peptide_pool.archived,
peptide_pool.parent_pool_id,peptide_pool.matrix_peptide_pool_id,p.peptide_pool_name AS parent_pool_name
from pepdb.peptide_pool LEFT JOIN pepdb.peptide_pool p ON(peptide_pool.parent_pool_id = p.peptide_pool_id)
LEFT JOIN pepdb.pool_type pt ON (peptide_pool.pool_type_id = pt.pool_type_id);