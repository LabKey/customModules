
ALTER TABLE pepdb.peptide_pool ADD COLUMN parent_pool_id integer;
ALTER TABLE pepdb.peptide_pool ADD CONSTRAINT FK_peptide_pool2 FOREIGN KEY(parent_pool_id) REFERENCES pepdb.peptide_pool(peptide_pool_id);
ALTER TABLE pepdb.peptide_pool ADD COLUMN matrix_peptide_pool_id text;
