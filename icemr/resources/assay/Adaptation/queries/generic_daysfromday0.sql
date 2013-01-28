PARAMETERS(finish TIMESTAMP)
SELECT DISTINCT f.*,
cast(timestampdiff('SQL_TSI_DAY', d.Run.StartDate, finish) as INTEGER) As NumDaysInCulture,
FROM
Samples.Flasks as f
LEFT JOIN Data as d
-- consider doing an inner join if they don't care about flasks
-- that aren't involved in any experiments
On f.SampleID = d.SampleID
