SELECT SampleID, GrowthFoldTest, Increase, Pass
FROM pass_foldtest1
UNION ALL
SELECT SampleID, GrowthFoldTest, Increase, Pass
FROM pass_foldtest2
UNION ALL
SELECT SampleID, GrowthFoldTest, Increase, Pass
FROM pass_foldtest3
