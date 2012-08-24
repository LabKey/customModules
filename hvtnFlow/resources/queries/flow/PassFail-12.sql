/*
 * Copyright (c) 2011-2012 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        negctrl_CD4_CD107a_Freq <= 0.1 AND
        negctrl_CD4_TNFa_Freq <= 0.1 AND
        negctrl_CD8_IL2_Freq <= 0.1 AND
        negctrl_CD8_IFNg_Freq <= 0.1 AND
        negctrl_CD8_CD154_Freq <= 0.1 AND
        negctrl_CD8_CD107a_Freq <= 0.1 AND
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
        negctrl_CD4_CD107a_Freq > 0.1 OR
        negctrl_CD4_TNFa_Freq > 0.1 OR
        negctrl_CD8_IL2_Freq > 0.1 OR
        negctrl_CD8_IFNg_Freq > 0.1 OR
        negctrl_CD8_CD154_Freq > 0.1 OR
        negctrl_CD8_CD107a_Freq > 0.1 OR
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
  negctrl_CD4_CD107a_Freq,
  negctrl_CD4_TNFa_Freq,

  negctrl_CD8_IL2_Freq,
  negctrl_CD8_IFNg_Freq,
  negctrl_CD8_CD154_Freq,
  negctrl_CD8_CD107a_Freq,
  negctrl_CD8_TNFa_Freq,

  sebctrl_CD4_IL2_Freq,
  sebctrl_CD4_IFNg_Freq,
  sebctrl_CD4_CD154_Freq,
  sebctrl_CD4_CD107a_Freq,
  sebctrl_CD4_TNFa_Freq,

  sebctrl_CD8_IL2_Freq,
  sebctrl_CD8_IFNg_Freq,
  sebctrl_CD8_CD154_Freq,
  sebctrl_CD8_CD107a_Freq,
  sebctrl_CD8_TNFa_Freq
FROM
(
  -- to make this easier to read, we separate out the aggregates from the verdict expressions
  SELECT
    Min(Key) AS Key,
    CASE WHEN (COUNT(LO_CD4) > 0) THEN 'LO_CD4 ' END AS LO_CD4,
    CASE WHEN (COUNT(LO_CD8) > 0) THEN 'LO_CD8 ' END AS LO_CD8,
    CASE WHEN (COUNT(LO_SEB) > 0) THEN 'LO_SEB ' END AS LO_SEB,
    -- negctrl_CD4_Resp_Count and negctrl_CD8_Resp_Count should both be NULL or both NOT NULL
    -- that's why we don't need to COUNT both of them
    CASE WHEN (COUNT(negctrl_CD4_IL2_Count) = 0) THEN 'NO_BKG ' END AS NO_BKG,

    100*SUM(negctrl_CD4_IL2_Count)/SUM(negctrl_CD4_Count) AS negctrl_CD4_IL2_Freq,
    100*SUM(negctrl_CD4_IFNg_Count)/SUM(negctrl_CD4_Count) AS negctrl_CD4_IFNg_Freq,
    100*SUM(negctrl_CD4_CD154_Count)/SUM(negctrl_CD4_Count) AS negctrl_CD4_CD154_Freq,
    100*SUM(negctrl_CD4_CD107a_Count)/SUM(negctrl_CD4_Count) AS negctrl_CD4_CD107a_Freq,
    100*SUM(negctrl_CD4_TNFa_Count)/SUM(negctrl_CD4_Count) AS negctrl_CD4_TNFa_Freq,

    100*SUM(negctrl_CD8_IL2_Count)/SUM(negctrl_CD8_Count) AS negctrl_CD8_IL2_Freq,
    100*SUM(negctrl_CD8_IFNg_Count)/SUM(negctrl_CD8_Count) AS negctrl_CD8_IFNg_Freq,
    100*SUM(negctrl_CD8_CD154_Count)/SUM(negctrl_CD8_Count) AS negctrl_CD8_CD154_Freq,
    100*SUM(negctrl_CD8_CD107a_Count)/SUM(negctrl_CD8_Count) AS negctrl_CD8_CD107a_Freq,
    100*SUM(negctrl_CD8_TNFa_Count)/SUM(negctrl_CD8_Count) AS negctrl_CD8_TNFa_Freq,

    CAST(MIN(sebctrl_CD4_IL2_Freq) AS DOUBLE) AS sebctrl_CD4_IL2_Freq,
    CAST(MIN(sebctrl_CD4_IFNg_Freq) AS DOUBLE) AS sebctrl_CD4_IFNg_Freq,
    CAST(MIN(sebctrl_CD4_CD154_Freq) AS DOUBLE) AS sebctrl_CD4_CD154_Freq,
    CAST(MIN(sebctrl_CD4_CD107a_Freq) AS DOUBLE) AS sebctrl_CD4_CD107a_Freq,
    CAST(MIN(sebctrl_CD4_TNFa_Freq) AS DOUBLE) AS sebctrl_CD4_TNFa_Freq,

    CAST(MIN(sebctrl_CD8_IL2_Freq) AS DOUBLE) AS sebctrl_CD8_IL2_Freq,
    CAST(MIN(sebctrl_CD8_IFNg_Freq) AS DOUBLE) AS sebctrl_CD8_IFNg_Freq,
    CAST(MIN(sebctrl_CD8_CD154_Freq) AS DOUBLE) AS sebctrl_CD8_CD154_Freq,
    CAST(MIN(sebctrl_CD8_CD107a_Freq) AS DOUBLE) AS sebctrl_CD8_CD107a_Freq,
    CAST(MIN(sebctrl_CD8_TNFa_Freq) AS DOUBLE) AS sebctrl_CD8_TNFa_Freq,

    Run,
    "EXPERIMENT NAME",
    SampleOrder,
    Min(Sample) AS Sample,
    Count(FCSAnalysis) AS FileCount,
  FROM "PassFailDetails-12"
  GROUP BY Run, "EXPERIMENT NAME", SampleOrder
) D
