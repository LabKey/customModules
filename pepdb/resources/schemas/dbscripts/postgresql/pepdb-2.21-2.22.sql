
INSERT INTO pepdb.clade VALUES (1, 'A');
INSERT INTO pepdb.clade VALUES (2, 'B');
INSERT INTO pepdb.clade VALUES (3, 'C');
INSERT INTO pepdb.clade VALUES (4, 'D');
INSERT INTO pepdb.clade VALUES (5, 'E');
INSERT INTO pepdb.clade VALUES (6, 'G');
INSERT INTO pepdb.clade VALUES (7, 'M');
INSERT INTO pepdb.clade VALUES (8, 'Other');
INSERT INTO pepdb.clade VALUES (9, 'Unknown');
INSERT INTO pepdb.clade VALUES (10, 'A1');
INSERT INTO pepdb.clade VALUES (11, 'C/A1/D');
INSERT INTO pepdb.clade VALUES (12, 'D/A1');

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('pepdb.clade', 'clade_id'), 12, true);

INSERT INTO pepdb.pathogen VALUES (1, 'HIV-1');
INSERT INTO pepdb.pathogen VALUES (2, 'HIV-2');
INSERT INTO pepdb.pathogen VALUES (3, 'TB');
INSERT INTO pepdb.pathogen VALUES (4, 'Malaria');
INSERT INTO pepdb.pathogen VALUES (5, 'Flu');
INSERT INTO pepdb.pathogen VALUES (6, 'FEC');
INSERT INTO pepdb.pathogen VALUES (7, 'EBV');
INSERT INTO pepdb.pathogen VALUES (8, 'Other');
INSERT INTO pepdb.pathogen VALUES (9, 'CMV');
INSERT INTO pepdb.pathogen VALUES (10, 'AD5');
INSERT INTO pepdb.pathogen VALUES (11, 'Pneumococcal');
INSERT INTO pepdb.pathogen VALUES (12, 'EBOV');

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('pepdb.pathogen', 'pathogen_id'), 12, true);

INSERT INTO pepdb.group_type VALUES (1, 'Consensus');
INSERT INTO pepdb.group_type VALUES (2, 'Autologous');
INSERT INTO pepdb.group_type VALUES (3, 'Mosaic');
INSERT INTO pepdb.group_type VALUES (4, 'Toggle');
INSERT INTO pepdb.group_type VALUES (5, 'LABL CTL Epitope');
INSERT INTO pepdb.group_type VALUES (6, 'Other');
INSERT INTO pepdb.group_type VALUES (7, 'Vaccine-matched');
INSERT INTO pepdb.group_type VALUES (8, 'PTE');

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('pepdb.group_type', 'group_type_id'), 8, true);

INSERT INTO pepdb.pep_align_ref VALUES (1,'HXB2');

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('pepdb.pep_align_ref', 'pep_align_ref_id'), 1, true);

INSERT INTO pepdb.protein_category VALUES (1, 'GAG');
INSERT INTO pepdb.protein_category VALUES (2, 'POL');
INSERT INTO pepdb.protein_category VALUES (3, 'VIF');
INSERT INTO pepdb.protein_category VALUES (4, 'VPR');
INSERT INTO pepdb.protein_category VALUES (5, 'TAT');
INSERT INTO pepdb.protein_category VALUES (6, 'REV');
INSERT INTO pepdb.protein_category VALUES (7, 'VPU');
INSERT INTO pepdb.protein_category VALUES (8, 'ENV');
INSERT INTO pepdb.protein_category VALUES (9, 'NEF');
INSERT INTO pepdb.protein_category VALUES (10, 'gp160');
INSERT INTO pepdb.protein_category VALUES (11, 'Integrase');
INSERT INTO pepdb.protein_category VALUES (12, 'p17');
INSERT INTO pepdb.protein_category VALUES (13, 'Antigen 85A');
INSERT INTO pepdb.protein_category VALUES (14, 'BZLF 1');
INSERT INTO pepdb.protein_category VALUES (15, 'CFP10');
INSERT INTO pepdb.protein_category VALUES (16, 'EBNA3A');
INSERT INTO pepdb.protein_category VALUES (17, 'ESAT-6');
INSERT INTO pepdb.protein_category VALUES (18, 'IE1');
INSERT INTO pepdb.protein_category VALUES (19, 'p24');
INSERT INTO pepdb.protein_category VALUES (20, 'p2p7p1p6');
INSERT INTO pepdb.protein_category VALUES (21, 'pp65');
INSERT INTO pepdb.protein_category VALUES (22, 'Protease');
INSERT INTO pepdb.protein_category VALUES (23, 'RT-Integrase');
INSERT INTO pepdb.protein_category VALUES (24, 'Other');
INSERT INTO pepdb.protein_category VALUES (25, 'Gag_Pol_TF');
INSERT INTO pepdb.protein_category VALUES (26, 'RT');
INSERT INTO pepdb.protein_category VALUES (27, 'Protease-RT');
INSERT INTO pepdb.protein_category VALUES (28, 'p17-p24');
INSERT INTO pepdb.protein_category VALUES (29, 'p24-p2p7p1p6');
INSERT INTO pepdb.protein_category VALUES (30, 'Gag_Pol_TF-Protease');


SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('pepdb.protein_category', 'protein_cat_id'), 30, true);

INSERT INTO pepdb.pool_type VALUES (1, 'Pool');
INSERT INTO pepdb.pool_type VALUES (2, 'Sub-Pool');
INSERT INTO pepdb.pool_type VALUES (3, 'Matrix');
INSERT INTO pepdb.pool_type VALUES (4, 'Other');

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('pepdb.pool_type', 'pool_type_id'), 4, true);

INSERT INTO pepdb.optimal_epitope_list VALUES (1, 'A');
INSERT INTO pepdb.optimal_epitope_list VALUES (2, 'B');
INSERT INTO pepdb.optimal_epitope_list VALUES (3, 'None');

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('pepdb.optimal_epitope_list', 'optimal_epitope_list_id'), 3, true);
