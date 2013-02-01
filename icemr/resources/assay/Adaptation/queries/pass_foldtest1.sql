SELECT
fi.SampleID,
GrowthFoldTest,
Increase,
CASE WHEN (Increase  >= FoldIncrease1) THEN (1)
ELSE (0) END As Pass
FROM
parasitemia_foldincrease1 as fi, Samples.Flasks as f
WHERE
fi.SampleID = f.SampleID

-- need to repeat for tests 1 to 3

