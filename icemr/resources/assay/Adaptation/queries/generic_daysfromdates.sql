PARAMETERS(start TIMESTAMP, finish TIMESTAMP)
-- unclear whether they want this query or not - right now we include flasks between these two dates
-- is that what they want?
-- postgress wants to have these explicit casts here
SELECT f.*, d.MeasurementDate, timestampdiff('SQL_TSI_DAY', cast(finish as TIMESTAMP), cast(start as TIMESTAMP)) As NumDays
FROM Samples.Flasks as f, DATA as d
WHERE (f.SampleID = d.SampleID) AND d.MeasurementDate >= start AND d.MeasurementDate <= finish