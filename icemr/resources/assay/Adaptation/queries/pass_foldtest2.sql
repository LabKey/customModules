SELECT
fi.SampleID,
GrowthFoldTest,
Increase,
cast((Increase >= FoldIncrease2) as INTEGER) As Pass
FROM
parasitemia_foldincrease2 as fi, Samples.Flasks as f
WHERE
fi.SampleID = f.SampleID

-- need to repeat for tests 1 to 3

