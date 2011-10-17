/*
EXPLAIN THIS HERE

 */

SELECT
  Key,
  CASE
    WHEN (LO_CD4 IS NULL AND
      LO_CD8 IS NULL AND
      LO_SEB IS NULL AND
      NO_BKG IS NULL AND
        negctrl_CD4_IL2_Freq <= 0.1 AND
        negctrl_CD4_IFNg_Freq <= 0.1 AND
        negctrl_CD4_CD154_Freq <= 0.1 AND
        negctrl_CD4_TNFa_Freq <= 0.1 AND
        negctrl_CD8_IL2_Freq <= 0.1 AND
        negctrl_CD8_IFNg_Freq <= 0.1 AND
        negctrl_CD8_CD154_Freq <= 0.1 AND
        negctrl_CD8_TNFa_Freq <= 0.1
        )
      THEN 'PASS'
    ELSE
      (COALESCE(LO_CD4, '') ||
      COALESCE(LO_CD8, '') ||
      COALESCE(LO_SEB, '') ||
      COALESCE(NO_BKG, '') ||
      CASE WHEN (
        negctrl_CD4_IL2_Freq > 0.1 OR
        negctrl_CD4_IFNg_Freq > 0.1 OR
        negctrl_CD4_CD154_Freq > 0.1 OR
        negctrl_CD4_TNFa_Freq > 0.1 OR
        negctrl_CD8_IL2_Freq > 0.1 OR
        negctrl_CD8_IFNg_Freq > 0.1 OR
        negctrl_CD8_CD154_Freq > 0.1 OR
        negctrl_CD8_TNFa_Freq > 0.1) THEN 'HI_BKG ' ELSE '' END)
    END AS Verdict,
  Run,
  "EXPERIMENT NAME",
  SampleOrder,
  Sample,
  FileCount,
  negctrl_CD4_IL2_Freq,
  negctrl_CD4_IFNg_Freq,
  negctrl_CD4_CD154_Freq,
  negctrl_CD4_TNFa_Freq,
  negctrl_CD8_IL2_Freq,
  negctrl_CD8_IFNg_Freq,
  negctrl_CD8_CD154_Freq,
  negctrl_CD8_TNFa_Freq,

  sebctrl_CD4_IL2_Freq,
  sebctrl_CD4_IFNg_Freq,
  sebctrl_CD4_CD154_Freq,
  sebctrl_CD4_TNFa_Freq,
  sebctrl_CD8_IL2_Freq,
  sebctrl_CD8_IFNg_Freq,
  sebctrl_CD8_CD154_Freq,
  sebctrl_CD8_TNFa_Freq
FROM
(
  -- to make this easier to read, we separate out the aggregates from the verdict expressions
  SELECT
    Min(Key) AS Key,
    CASE WHEN (COUNT(LO_CD4) > 0) THEN 'LO_CD4 ' END AS LO_CD4,
    CASE WHEN (COUNT(LO_CD8) > 0) THEN 'LO_CD8 ' END AS LO_CD8,
    CASE WHEN (COUNT(LO_SEB) > 0) THEN 'LO_SEB ' END AS LO_SEB,
    CASE WHEN (COUNT(negctrl_CD4_IL2_Count) = 0) THEN 'NO_BKG ' END AS NO_BKG,

    100*SUM(negctrl_CD4_IL2_Count)/SUM(negctrl_CD4_Count) AS negctrl_CD4_IL2_Freq,
    100*SUM(negctrl_CD4_IFNg_Count)/SUM(negctrl_CD8_Count) AS negctrl_CD4_IFNg_Freq,
    100*SUM(negctrl_CD4_CD154_Count)/SUM(negctrl_CD4_Count) AS negctrl_CD4_CD154_Freq,
    100*SUM(negctrl_CD4_TNFa_Count)/SUM(negctrl_CD8_Count) AS negctrl_CD4_TNFa_Freq,
    100*SUM(negctrl_CD8_IL2_Count)/SUM(negctrl_CD4_Count) AS negctrl_CD8_IL2_Freq,
    100*SUM(negctrl_CD8_IFNg_Count)/SUM(negctrl_CD8_Count) AS negctrl_CD8_IFNg_Freq,
    100*SUM(negctrl_CD8_CD154_Count)/SUM(negctrl_CD4_Count) AS negctrl_CD8_CD154_Freq,
    100*SUM(negctrl_CD8_TNFa_Count)/SUM(negctrl_CD8_Count) AS negctrl_CD8_TNFa_Freq,

    MIN(sebctrl_CD4_IL2_Freq) AS sebctrl_CD4_IL2_Freq,
    MIN(sebctrl_CD4_IFNg_Freq) AS sebctrl_CD4_IFNg_Freq,
    MIN(sebctrl_CD4_CD154_Freq) AS sebctrl_CD4_CD154_Freq,
    MIN(sebctrl_CD4_TNFa_Freq) AS sebctrl_CD4_TNFa_Freq,
    MIN(sebctrl_CD8_IL2_Freq) AS sebctrl_CD8_IL2_Freq,
    MIN(sebctrl_CD8_IFNg_Freq) AS sebctrl_CD8_IFNg_Freq,
    MIN(sebctrl_CD8_CD154_Freq) AS sebctrl_CD8_CD154_Freq,
    MIN(sebctrl_CD8_TNFa_Freq) AS sebctrl_CD8_TNFa_Freq,

    Run,
    "EXPERIMENT NAME",
    SampleOrder,
    Min(Sample) AS Sample,
    Count(FCSAnalysis) AS FileCount,
  FROM "PassFailDetails-12"
  GROUP BY Run, "EXPERIMENT NAME", SampleOrder
) D
