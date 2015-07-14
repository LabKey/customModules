ALTER TABLE hdrl.specimenResult ADD COLUMN RequestId INT;
UPDATE hdrl.specimenResult res SET RequestId = req.InboundRequestId
  FROM hdrl.InboundSpecimen req
  WHERE res.SpecimenId = req.RowId;
ALTER TABLE hdrl.specimenResult ALTER COLUMN RequestId SET NOT NULL;

ALTER TABLE hdrl.specimenResult ADD COLUMN ReportFileName VARCHAR(20);
ALTER TABLE hdrl.specimenResult ADD CONSTRAINT FK_SpecimenResult_InboundRequest FOREIGN KEY (RequestId) REFERENCES hdrl.InboundRequest(RequestId) ON DELETE CASCADE;

ALTER TABLE hdrl.specimenResult ALTER COLUMN modifiedResultFlag SET DEFAULT 'F';