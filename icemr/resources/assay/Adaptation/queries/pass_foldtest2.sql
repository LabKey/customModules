SELECT
fi.SampleID,
GrowthFoldTest,
Increase,
CASE WHEN (Increase  >= FoldIncrease2) THEN (1)
ELSE (0) END As Pass
FROM
parasitemia_foldincrease2 as fi, Samples.Flasks as f
WHERE
fi.SampleID = f.SampleID

-- need to repeat for tests 1 to 3

