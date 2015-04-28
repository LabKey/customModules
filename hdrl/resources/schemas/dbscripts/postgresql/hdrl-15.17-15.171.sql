ALTER TABLE hdrl.InboundSpecimen ADD COLUMN Container ENTITYID;

UPDATE hdrl.InboundSpecimen s SET Container=r.Container from hdrl.InboundRequest r where s.InboundRequestId = r.RequestId;

ALTER TABLE hdrl.InboundSpecimen ALTER Container SET NOT NULL;
