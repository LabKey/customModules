SELECT SampleID, IncreaseTest1, IncreaseTest2, IncreaseTest3,
PassTest1, PassTest2, PassTest3,
CASE WHEN ((PassTest1 + PassTest2 + PassTest3) >= AdaptationCriteria) THEN
'Yes'
ELSE
'No'
END As SuccessfulAdaptation
FROM
stored_parasitemia_alltests_increase

