SELECT SampleID, SUM(Pass) as NumSuccessfulGrowthTests
FROM pass_allfoldtests
GROUP BY SampleID