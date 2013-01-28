PARAMETERS(start TIMESTAMP, finish TIMESTAMP)
SELECT f.*, i.ParasitemiaStart, i.ParasitemiaFinish, i.Increase
FROM
  (SELECT ps.SampleID, ps.Parasitemia as ParasitemiaStart, pf.Parasitemia as ParasitemiaFinish,
  round((pf.parasitemia/ps.parasitemia),2)  As Increase
    FROM
      (SELECT  SampleID,  Parasitemia FROM  Data  WHERE  (timestampdiff('SQL_TSI_DAY', MeasurementDate, start)=0))  as ps,
      (SELECT  SampleID,  Parasitemia FROM  Data  WHERE  (timestampdiff('SQL_TSI_DAY', MeasurementDate, finish)=0))as pf
    WHERE ps.SampleID = pf.SampleID) as i,
  Samples.Flasks as f
WHERE i.SampleID = f.SampleID
