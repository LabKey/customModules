SELECT pi.SampleID, pi.parasitemia as ParasitemiaStart, pf.parasitemia as ParasitemiaFinish,
  pf.growthfoldtestfinished as GrowthFoldTest, (pf.parasitemia/pi.parasitemia) as Increase
FROM
  (SELECT  SampleID,  Parasitemia,  GrowthFoldTestInitiated  FROM  Data  WHERE  GrowthFoldTestInitiated=2) as pi,
  (SELECT  SampleID,  Parasitemia,  GrowthFoldTestFinished  FROM  Data  WHERE  GrowthFoldTestFinished=2)as pf
WHERE pi.SampleID = pf.SampleID
