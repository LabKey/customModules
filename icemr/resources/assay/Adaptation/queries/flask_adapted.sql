SELECT gt.SampleID, gt.NumSuccessfulGrowthTests, sf.AdaptationCriteria,
CASE WHEN (gt.NumSuccessfulGrowthTests >= sf.AdaptationCriteria) THEN 'Yes'
ELSE 'No' END As AdaptationSuccess
FROM calc_growthtests as gt, Samples.Flasks as sf
WHERE sf.SampleID = gt.SampleID
