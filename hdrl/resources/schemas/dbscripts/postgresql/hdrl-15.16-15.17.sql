
UPDATE hdrl.InboundSpecimen SET SSN=NULL WHERE CHAR_LENGTH(SSN) > 9;
ALTER TABLE hdrl.InboundSpecimen ALTER SSN TYPE VARCHAR(9);

ALTER TABLE hdrl.FamilyMemberPrefix ALTER CODE TYPE CHAR(2);
UPDATE hdrl.FamilyMemberPrefix SET CODE='00' where CODE='0';
UPDATE hdrl.FamilyMemberPrefix SET CODE='01' where CODE='1';
UPDATE hdrl.FamilyMemberPrefix SET CODE='02' where CODE='2';
UPDATE hdrl.FamilyMemberPrefix SET CODE='03' where CODE='3';
UPDATE hdrl.FamilyMemberPrefix SET CODE='04' where CODE='4';
UPDATE hdrl.FamilyMemberPrefix SET CODE='05' where CODE='5';
UPDATE hdrl.FamilyMemberPrefix SET CODE='06' where CODE='6';
UPDATE hdrl.FamilyMemberPrefix SET CODE='07' where CODE='7';
UPDATE hdrl.FamilyMemberPrefix SET CODE='08' where CODE='8';
UPDATE hdrl.FamilyMemberPrefix SET CODE='09' where CODE='9';

