SELECT *,
CASE WHEN (FinishParasitemia1 IS NOT NULL AND StartParasitemia1 IS NOT NULL) THEN
  round(FinishParasitemia1/StartParasitemia1, 2)
ELSE 0 END As IncreaseTest1,
CASE WHEN (FinishParasitemia1 IS NOT NULL AND StartParasitemia1 IS NOT NULL) THEN
  CASE WHEN (FinishParasitemia1/StartParasitemia1 >= FoldIncrease1) THEN (1)
  ELSE (0) END
ELSE (0) END As PassTest1,
CASE WHEN (FinishParasitemia2 IS NOT NULL AND StartParasitemia2 IS NOT NULL) THEN
  round(FinishParasitemia2/StartParasitemia2, 2)
ELSE 0 END As IncreaseTest2,
CASE WHEN (FinishParasitemia2 IS NOT NULL AND StartParasitemia2 IS NOT NULL) THEN
  CASE WHEN (FinishParasitemia2/StartParasitemia2 >= FoldIncrease2) THEN (1)
  ELSE (0) END
ELSE 0 END As PassTest2,
CASE WHEN (FinishParasitemia3 IS NOT NULL AND StartParasitemia3 IS NOT NULL) THEN
  round(FinishParasitemia3/StartParasitemia3, 2)
ELSE 0 END As IncreaseTest3,
CASE WHEN (FinishParasitemia3 IS NOT NULL AND StartParasitemia3 IS NOT NULL) THEN
  CASE WHEN (FinishParasitemia3/StartParasitemia3 >= FoldIncrease3) THEN (1)
  ELSE (0) END
ELSE 0 END As PassTest3,
FROM
Samples.Flasks


