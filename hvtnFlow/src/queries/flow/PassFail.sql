SELECT Min(D.Key) AS Key,
CASE WHEN (Count(D.LO_CD4) = 0 AND
 COUNT(D.LO_CD8) = 0 AND
 COUNT(D.LO_SEB) = 0 AND
 AVG(D.negctrl_CD4_Resp) <= .1 AND
 AVG(D.negctrl_CD8_Resp) <= .1) THEN 'PASS' ELSE
 (
 CASE WHEN (Count(D.LO_CD4) > 0) THEN 'LO_CD4 ' ELSE '' END ||
 CASE WHEN (Count(D.LO_CD8) > 0) THEN 'LO_CD8 ' ELSE '' END ||
 CASE WHEN (Count(D.LO_SEB) > 0) THEN 'LO_SEB ' ELSE '' END ||
 CASE WHEN (COUNT(D.negctrl_CD4_Resp) = 0) THEN 'NO_BKG' ELSE '' END ||
 CASE WHEN (AVG(D.negctrl_CD4_Resp) > .1 OR AVG(D.negctrl_CD8_Resp) > .1) THEN 'HI_BKG' ELSE '' END


 ) END AS Verdict,
D.Run,
D."EXPERIMENT NAME",
D.SampleOrder,
Min(D.Sample) AS Sample,
Count(D.FCSAnalysis) AS FileCount,
AVG(D.negctrl_CD4_Resp) AS negctrl_CD4_Resp,
AVG(D.negctrl_CD8_Resp) AS negctrl_CD8_Resp,
MIN(D.sebctrl_CD4_Resp) AS sebctrl_CD4_Resp,
MIN(D.sebctrl_CD8_Resp) AS sebctrl_CD8_Resp,
FROM PassFailDetails AS D
GROUP BY D.Run, D."EXPERIMENT NAME", D.SampleOrder