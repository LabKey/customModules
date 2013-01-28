SELECT
fi.SampleID,
GrowthFoldTest,
Increase,
cast((Increase >= FoldIncrease1) as INTEGER) As Pass
FROM
parasitemia_foldincrease1 as fi, Samples.Flasks as f
WHERE
fi.SampleID = f.SampleID

-- need to repeat for tests 1 to 3

