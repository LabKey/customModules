SELECT DISTINCT f.SampleID,
cast(timestampdiff('SQL_TSI_DAY', d.Run.StartDate, f.MaintenanceDate) as INTEGER) As NumDaysInCulture,
cast(timestampdiff('SQL_TSI_DAY', d.Run.StartDate, f.StartDate1) as INTEGER) As NumDaysToGrowthTestStart,
cast(timestampdiff('SQL_TSI_DAY', f.FinishDate1, f.AdaptationDate) as INTEGER) As NumDaysFromGrowthTestFinishToAdaptation,
cast(timestampdiff('SQL_TSI_DAY', d.Run.StartDate, f.AdaptationDate) as INTEGER) As NumDaysToAdaptation,
FROM
Samples.Flasks as f
LEFT JOIN Data as d
On f.SampleID = d.SampleID
