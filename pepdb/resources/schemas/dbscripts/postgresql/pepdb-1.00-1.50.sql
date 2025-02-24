ALTER TABLE pepdb.peptide_pool ADD COLUMN archived boolean default false;
ALTER TABLE pepdb.peptide_pool_assignment ADD COLUMN peptide_group_assignment_id integer;
ALTER TABLE pepdb.peptide_pool_assignment ADD CONSTRAINT FK_peptide_pool_assignment3 FOREIGN KEY(peptide_group_assignment_id) REFERENCES pepdb.peptide_group_assignment(peptide_group_assignment_id);
ALTER TABLE pepdb.peptides ADD COLUMN peptide_flag boolean default false;
ALTER TABLE pepdb.peptides ADD COLUMN peptide_notes text;
