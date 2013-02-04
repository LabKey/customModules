-- combines the results from the two calculated queries and combines them with the
-- over flask sample set data
SELECT
f.PatientID, f.SampleID, f.Scientist, f.Stage, f.Parasitemia, f.Gametocytemia, f.PatientpRBCs, f.Hematocrit,
f.CultureMedia, f.SerumBatchID, f.AlbumaxBatchID, f.FoldIncrease1, f.FoldIncrease2, f.FoldIncrease3,
f.AdaptationCriteria,f.Comments, f.MaintenanceStopped, f.MaintenanceDate,
a.IncreaseTest1, a.IncreaseTest2, a.IncreaseTest3, a.PassTest1, a.PassTest2, a.PassTest3, a.SuccessfulAdaptation,
f.AdaptationDate, d.NumDaysInCulture, d.NumDaysToGrowthTestStart, d.NumDaysFromGrowthTestFinishToAdaptation, d.NumDaysToAdaptation,
FROM
Samples.Flasks as f,
stored_parasitemia_flask_adapted as a,
stored_dates_numdays as d
WHERE f.SampleID = d.SampleID AND d.SampleID = a.SampleID


