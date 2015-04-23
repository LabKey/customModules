ALTER TABLE hdrl.InboundSpecimen DROP CONSTRAINT IF EXISTS UQ_InboundSpecimen;
ALTER TABLE hdrl.InboundSpecimen DROP CONSTRAINT IF EXISTS FK_InboundSpecimen_Request, ADD CONSTRAINT FK_InboundSpecimen_Request FOREIGN KEY (InboundRequestId) REFERENCES hdrl.InboundRequest (RequestId) ON DELETE CASCADE;
