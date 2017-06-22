/*
 * Copyright (c) 2011-2017 LabKey Corporation
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
 * TODO EXPLAIN THIS HERE
 */

SELECT
  Key,
  CASE
    WHEN (LO_CD4 IS NULL AND
      LO_CD8 IS NULL AND
      NO_BKG IS NULL AND
      negctrl_CD4_Resp <= 0.1 AND negctrl_CD8_Resp <= 0.1)
      THEN 'PASS'
    ELSE
      (COALESCE(LO_CD4, '') ||
      COALESCE(LO_CD8, '') ||
      COALESCE(NO_BKG, '') ||
      CASE WHEN (negctrl_CD4_Resp > 0.1 OR negctrl_CD8_Resp > 0.1) THEN 'HI_BKG ' ELSE '' END)
    END AS Verdict,
  Run,
  "EXPERIMENT NAME",
  SampleOrder,
  Sample,
  FileCount,
  negctrl_CD4_Resp,
  negctrl_CD8_Resp,
  CAST(posctrl_CD4_Resp AS DOUBLE) AS posctrl_CD4_Resp,
  CAST(posctrl_CD8_Resp AS DOUBLE) AS posctrl_CD8_Resp
FROM
(
  -- to make this easier to read, we separate out the aggregates from the verdict expressions
  SELECT
    Min(Key) AS Key,
    CASE WHEN (COUNT(LO_CD4) > 0) THEN 'LO_CD4 ' END AS LO_CD4,
    CASE WHEN (COUNT(LO_CD8) > 0) THEN 'LO_CD8 ' END AS LO_CD8,
    -- NOTE: LO_POS is no longer included in the PassFail verdict, but I'm keeping it available so it can be easily re-added if it is needed again
    CASE WHEN (COUNT(LO_POS) > 0) THEN 'LO_POS ' END AS LO_POS,
    -- negctrl_CD4_Resp_Count and negctrl_CD8_Resp_Count should both be NULL or both NOT NULL
    -- that's why we don't need to COUNT both of them
    CASE WHEN (COUNT(negctrl_CD4_Resp_Count) = 0) THEN 'NO_BKG ' END AS NO_BKG,
    100*SUM(negctrl_CD4_Resp_Count)/SUM(negctrl_CD4_Count) AS negctrl_CD4_Resp,
    100*SUM(negctrl_CD8_Resp_Count)/SUM(negctrl_CD8_Count) AS negctrl_CD8_Resp,
    Run,
    "EXPERIMENT NAME",
    SampleOrder,
    Min(Sample) AS Sample,
    Count(FCSAnalysis) AS FileCount,
    MIN(posctrl_CD4_Resp) AS posctrl_CD4_Resp,
    MIN(posctrl_CD8_Resp) AS posctrl_CD8_Resp,
  FROM PassFailDetails
  GROUP BY Run, "EXPERIMENT NAME", SampleOrder
) D
