-- Converting uq_lotassignment [peptideId, container, lotNumber] from unique to non-unique index because pk_lotassignment [peptideId, container] overlaps it with a smaller column set
ALTER TABLE peptideinventory.lotAssignment DROP CONSTRAINT uq_lotassignment;
CREATE INDEX ix_lotassignment ON peptideinventory.lotAssignment(peptideId, container, lotNumber);
-- Converting uq_rcpoolassignment [peptideId, container, rcPoolId] from unique to non-unique index because pk_rcpoolassignment [peptideId, container] overlaps it with a smaller column set
ALTER TABLE peptideinventory.rcPoolAssignment DROP CONSTRAINT uq_rcpoolassignment;
CREATE INDEX ix_rcpoolassignment ON peptideinventory.rcPoolAssignment(peptideId, container, rcPoolId);
