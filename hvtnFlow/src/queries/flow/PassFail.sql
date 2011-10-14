/*
EXPLAIN THIS HERE

 */
SELECT Min(D.Key) AS Key,
CASE WHEN (Count(D.LO_CD4) = 0 AND
 COUNT(D.LO_CD8) = 0 AND
 COUNT(D.LO_SEB) = 0 AND
 SUM(D.negctrl_CD4_Resp_Count)/SUM(D.negctrl_CD4_Count) <= 0.001 AND
 SUM(D.negctrl_CD8_Resp_Count)/SUM(D.negctrl_CD8_Count) <= 0.001) THEN 'PASS' ELSE
 (
 CASE WHEN (Count(D.LO_CD4) > 0) THEN 'LO_CD4 ' ELSE '' END ||
 CASE WHEN (Count(D.LO_CD8) > 0) THEN 'LO_CD8 ' ELSE '' END ||
 CASE WHEN (Count(D.LO_SEB) > 0) THEN 'LO_SEB ' ELSE '' END ||
 CASE WHEN (COUNT(D.negctrl_CD4_Resp_Count) = 0) THEN 'NO_BKG' ELSE '' END ||
 CASE WHEN (SUM(D.negctrl_CD4_Resp_Count)/SUM(D.negctrl_CD4_Count) > 0.001 OR SUM(D.negctrl_CD8_Resp_Count)/SUM(D.negctrl_CD8_Count) > 0.001) THEN 'HI_BKG' ELSE '' END
 ) END AS Verdict,
D.Run,
D."EXPERIMENT NAME",
D.SampleOrder,
Min(D.Sample) AS Sample,
Count(D.FCSAnalysis) AS FileCount,
100.0*SUM(D.negctrl_CD4_Resp_Count)/SUM(D.negctrl_CD4_Count) AS negctrl_CD4_Resp,
100.0*SUM(D.negctrl_CD8_Resp_Count)/SUM(D.negctrl_CD8_Count) AS negctrl_CD8_Resp,
MIN(D.sebctrl_CD4_Resp) AS sebctrl_CD4_Resp,
MIN(D.sebctrl_CD8_Resp) AS sebctrl_CD8_Resp,
FROM PassFailDetails AS D
GROUP BY D.Run, D."EXPERIMENT NAME", D.SampleOrder


-- (
--   SELECT
--     FCSAnalysis, Stim CD4_Count, CD8_Count, CD4_Resp_Count, CD8_Resp_Count,
--     CD4_Resp, CD8_Resp, Run, "EXPERIMENT NAME", SampleOrder, Sample,
--     LO_CD4, LO_CD8, LO_SEB,
--     negctrl_CD4_Resp_Count, negctrl_CD8_Resp_Count,
--     sebctrl_CD4_Resp, sebctrl_CD8_Resp,
--     Key
--   FROM PassFailDetails AS PFD
-- ) AS D