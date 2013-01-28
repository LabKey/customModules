SELECT gt.SampleID, gt.NumSuccessfulGrowthTests, sf.AdaptationCriteria,
(gt.NumSuccessfulGrowthTests >= sf.AdaptationCriteria) As AdaptationSuccess
FROM calc_growthtests as gt, Samples.Flasks as sf
WHERE sf.SampleID = gt.SampleID
