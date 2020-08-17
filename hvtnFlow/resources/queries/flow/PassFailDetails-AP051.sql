/*
 * Copyright (c) 2020 LabKey Corporation
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
 * Ticket 41061: New pass-fail query for 27-color ICS assay used for Analysis Plan 051
 */
SELECT
  B.RowId AS FCSAnalysis,
  B.Stim,
  B.CD4_Count,
  B.CD8_Count,
  B.Run,
  B."EXPERIMENT NAME",
  B.SampleOrder,
  B.Sample,

  CASE WHEN B.negctrl IS NULL     AND B.CD4_Count < (B.Cutoff * 35000) THEN 'LO_CD4' END AS LO_CD4,
  CASE WHEN B.negctrl IS NULL     AND B.CD8_Count < (B.Cutoff * 15000) THEN 'LO_CD8' END AS LO_CD8,
  CASE WHEN B.negctrl IS NOT NULL AND B.CD4_Count < 35000 THEN 'negctrl_LO_CD4' END AS negctrl_LO_CD4,
  CASE WHEN B.negctrl IS NOT NULL AND B.CD8_Count < 15000 THEN 'negctrl_LO_CD8' END AS negctrl_LO_CD8,

  CASE
    WHEN (B.CD4_Count >= 35000 AND B.CD8_Count >= 15000) THEN B.negctrl
    WHEN (B.negctrl IS NOT NULL) THEN 'excluded'
  END AS negctrl,
  B.posctrl,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 35000 AND B.CD8_Count >= 15000) THEN B.CD4_Count END AS negctrl_CD4_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 35000 AND B.CD8_Count >= 15000) THEN B.CD8_Count END AS negctrl_CD8_Count,
  B.Run.Name||'-' ||B."EXPERIMENT NAME"||'-'||B.SampleOrder AS Key
FROM
(
  SELECT A.RowId,
    A.Run,
    A.FCSFile.Keyword."EXPERIMENT NAME",
    A.FCSFile.Sample,
    A.FCSFile.Keyword."Sample Order" AS SampleOrder,
    A.FCSFile.Keyword.Stim AS Stim,
    CASE
        WHEN (A.FCSFile.Keyword.Stim IN ('SEB', 'sebctrl', 'PHA', 'phactrl', 'CMV')) THEN 0
        WHEN (A.FCSFile.Keyword.Stim IN ('Env1', 'Env2', 'Env3', 'ENV-1-PTEG', 'ENV-2-PTEG', 'ENV-3-PTEG')) THEN 1
        WHEN (A.FCSFile.Keyword.Stim NOT IN ('negctrl', 'Neg Cont')) THEN 1 END AS Cutoff,
    CASE WHEN (A.FCSFile.Keyword.Stim IN ('negctrl', 'Neg Cont')) THEN 'negctrl' END AS negctrl,
    CASE
        WHEN (A.FCSFile.Keyword.Stim IN ('SEB', 'sebctrl')) THEN 'sebctrl'
        WHEN (A.FCSFile.Keyword.Stim IN ('PHA', 'phactrl')) THEN 'phactrl' END AS posctrl,
    COALESCE(
             -- AP051
             A.Statistic('Time/S/Lv/K1/K2/K3/K4/K5/K6/K7/K8/14-/S/L/19-/3+/3+excl 16br/56-16-/4+:Count')
             ) AS CD4_Count,

    COALESCE(
             -- AP051
             A.Statistic('Time/S/Lv/K1/K2/K3/K4/K5/K6/K7/K8/14-/S/L/19-/3+/3+excl 16br/56-16-/8+:Count')
             ) AS CD8_Count

  FROM FCSAnalyses AS A
  WHERE A.FCSFile.Keyword.Stim NOT IN ('PBS','Comp')
) AS B