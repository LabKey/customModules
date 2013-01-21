CREATE SCHEMA ms2extensions;

CREATE TABLE ms2extensions.Ms2RunAggregates 
(
	ms2Run int not null,
	totalPeptides int null,
	distinctPeptides int null
	, container entityid NOT NULL
	, CONSTRAINT PK_Ms2RunAggregates PRIMARY KEY ( ms2run )
);

CREATE INDEX IDX_MS2RunAggregates_MS2Run ON ms2extensions.Ms2RunAggregates(ms2Run);
CREATE INDEX IDX_MS2RunAggregates_Container ON ms2extensions.Ms2RunAggregates(Container);
