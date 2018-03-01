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
SELECT
  B.RowId AS FCSAnalysis,
  B.Stim,
  B.CD4_Count,
  B.CD8_Count,
  B.CD4_Resp_Count,
  B.CD8_Resp_Count,
  B.CD4_Resp,
  B.CD8_Resp,
  B.Run,
  B."EXPERIMENT NAME",
  B.SampleOrder,
  B.Sample,
  CASE WHEN B.CD4_Count < B.Cutoff THEN 'LO_CD4' END AS LO_CD4,
  CASE WHEN B.CD8_Count < B.Cutoff THEN 'LO_CD8' END AS LO_CD8,
  CASE WHEN (B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.negctrl
  WHEN (B.negctrl IS NOT NULL) THEN 'excluded' END AS negctrl,
  B.posctrl,
  -- NOTE: LO_POS is no longer included in the PassFail verdict, but I'm keeping it available for adding to the grid via customize view
  CASE WHEN (B.posctrl IS NOT NULL AND (B.CD4_Resp < 1.2 OR B.CD8_Resp < 1.2)) THEN 'LO_POS' END AS LO_POS,
  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD4_Resp END AS posctrl_CD4_Resp,
  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD8_Resp END AS posctrl_CD8_Resp,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD4_Count END AS negctrl_CD4_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD8_Count END AS negctrl_CD8_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD4_Resp_Count END AS negctrl_CD4_Resp_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD8_Resp_Count END AS negctrl_CD8_Resp_Count,
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
        WHEN (A.FCSFile.Keyword.Stim IN ('Env1', 'Env2', 'Env3', 'ENV-1-PTEG', 'ENV-2-PTEG', 'ENV-3-PTEG')) THEN 5000
        WHEN (A.FCSFile.Keyword.Stim NOT IN ('negctrl', 'Neg Cont')) THEN 5000 END AS Cutoff,
    CASE WHEN (A.FCSFile.Keyword.Stim IN ('negctrl', 'Neg Cont')) THEN 'negctrl' END AS negctrl,
    CASE
        WHEN (A.FCSFile.Keyword.Stim IN ('SEB', 'sebctrl')) THEN 'sebctrl'
        WHEN (A.FCSFile.Keyword.Stim IN ('PHA', 'phactrl')) THEN 'phactrl' END AS posctrl,
    COALESCE(A.Statistic('S/Lv/L/3+/4+:Count'),
             A.Statistic('S/Lv/L/3+/Excl/4+:Count'),
             A.Statistic('S/Exclude/Lv/L/3+/4+:Count'),
             A.Statistic('S/Exclude/Lv/CD14-/L/CD3+/CD4+:Count'),
             A.Statistic('S/Exclude/14-/Lv/L/3+/4+:Count'),
             A.Statistic('S/Time/Lv/14-SSlo/Keeper/L/16-56-/4+:Count')
             ) AS CD4_Count,

    COALESCE(A.Statistic('S/Lv/L/3+/8+:Count'),
             A.Statistic('S/Lv/L/3+/Excl/8+:Count'),
             A.Statistic('S/Exclude/Lv/L/3+/8+:Count'),
             A.Statistic('S/Exclude/Lv/CD14-/L/CD3+/CD8+:Count'),
             A.Statistic('S/Exclude/14-/Lv/L/3+/8+:Count'),
             A.Statistic('S/Time/Lv/14-SSlo/Keeper/L/16-56-/8+:Count')
             ) AS CD8_Count,

    COALESCE(A.Statistic('S/Lv/L/3+/4+/IFNg\IL2:Count'),
             A.Statistic('S/Lv/L/3+/4+/(IFNg+|IL2+):Count'),

             A.Statistic('S/Lv/L/3+/Excl/4+/IFNg\IL2:Count'),
             A.Statistic('S/Lv/L/3+/Excl/4+/(IFNg+|IL2+):Count'),

             A.Statistic('S/Exclude/Lv/L/3+/4+/IFNg\IL2:Count'),
             A.Statistic('S/Exclude/Lv/L/3+/4+/(IFNg+|IL2+):Count'),

             A.Statistic('S/Exclude/Lv/CD14-/L/CD3+/CD4+/IFNg\IL2:Count'),
             A.Statistic('S/Exclude/Lv/CD14-/L/CD3+/CD4+/(IFNg+|IL2+):Count'),

             A.Statistic('S/Exclude/14-/Lv/L/3+/4+/IFNg\IL2:Count'),
             A.Statistic('S/Exclude/14-/Lv/L/3+/4+/(IFNg+|IL2+):Count'),

             A.Statistic('S/Time/Lv/14-SSlo/Keeper/L/16-56-/4+/IFNg_OR_IL2:Count')
             ) AS CD4_Resp_Count,
   
    COALESCE(A.Statistic('S/Lv/L/3+/8+/IFNg\IL2:Count'),
             A.Statistic('S/Lv/L/3+/8+/(IFNg+|IL2+):Count'),

             A.Statistic('S/Lv/L/3+/Excl/8+/IFNg\IL2:Count'),
             A.Statistic('S/Lv/L/3+/Excl/8+/(IFNg+|IL2+):Count'),

             A.Statistic('S/Exclude/Lv/L/3+/8+/IFNg\IL2:Count'),
             A.Statistic('S/Exclude/Lv/L/3+/8+/(IFNg+|IL2+):Count'),

             A.Statistic('S/Exclude/Lv/CD14-/L/CD3+/CD8+/IFNg\IL2:Count'),
             A.Statistic('S/Exclude/Lv/CD14-/L/CD3+/CD8+/(IFNg+|IL2+):Count'),

             A.Statistic('S/Exclude/14-/Lv/L/3+/8+/IFNg\IL2:Count'),
             A.Statistic('S/Exclude/14-/Lv/L/3+/8+/(IFNg+|IL2+):Count'),

             A.Statistic('S/Time/Lv/14-SSlo/Keeper/L/16-56-/8+/IFNg_OR_IL2:Count')
             ) AS CD8_Resp_Count,

    COALESCE(A.Statistic('S/Lv/L/3+/4+/IFNg\IL2:Freq_Of_Parent'),
             A.Statistic('S/Lv/L/3+/4+/(IFNg+|IL2+):Freq_Of_Parent'),

             A.Statistic('S/Lv/L/3+/Excl/4+/IFNg\IL2:Freq_Of_Parent'),
             A.Statistic('S/Lv/L/3+/Excl/4+/(IFNg+|IL2+):Freq_Of_Parent'),

             A.Statistic('S/Exclude/Lv/L/3+/4+/IFNg\IL2:Freq_Of_Parent'),
             A.Statistic('S/Exclude/Lv/L/3+/4+/(IFNg+|IL2+):Freq_Of_Parent'),

             A.Statistic('S/Exclude/Lv/CD14-/L/CD3+/CD4+/IFNg\IL2:Freq_Of_Parent'),
             A.Statistic('S/Exclude/Lv/CD14-/L/CD3+/CD4+/(IFNg+|IL2+):Freq_Of_Parent'),

             A.Statistic('S/Exclude/14-/Lv/L/3+/4+/IFNg\IL2:Freq_Of_Parent'),
             A.Statistic('S/Exclude/14-/Lv/L/3+/4+/(IFNg+|IL2+):Freq_Of_Parent'),

             A.Statistic('S/Time/Lv/14-SSlo/Keeper/L/16-56-/4+/IFNg_OR_IL2:Freq_Of_Parent')
             ) AS CD4_Resp,
   
    COALESCE(A.Statistic('S/Lv/L/3+/8+/IFNg\IL2:Freq_Of_Parent'),
             A.Statistic('S/Lv/L/3+/8+/(IFNg+|IL2+):Freq_Of_Parent'),

             A.Statistic('S/Lv/L/3+/Excl/8+/IFNg\IL2:Freq_Of_Parent'),
             A.Statistic('S/Lv/L/3+/Excl/8+/(IFNg+|IL2+):Freq_Of_Parent'),

             A.Statistic('S/Exclude/Lv/L/3+/8+/IFNg\IL2:Freq_Of_Parent'),
             A.Statistic('S/Exclude/Lv/L/3+/8+/(IFNg+|IL2+):Freq_Of_Parent'),

             A.Statistic('S/Exclude/Lv/CD14-/L/CD3+/CD8+/IFNg\IL2:Freq_Of_Parent'),
             A.Statistic('S/Exclude/Lv/CD14-/L/CD3+/CD8+/(IFNg+|IL2+):Freq_Of_Parent'),

             A.Statistic('S/Exclude/14-/Lv/L/3+/8+/IFNg\IL2:Freq_Of_Parent'),
             A.Statistic('S/Exclude/14-/Lv/L/3+/8+/(IFNg+|IL2+):Freq_Of_Parent'),

             A.Statistic('S/Time/Lv/14-SSlo/Keeper/L/16-56-/8+/IFNg_OR_IL2:Freq_Of_Parent')
             ) AS CD8_Resp,
   
  FROM FCSAnalyses AS A
  WHERE A.FCSFile.Keyword.Stim NOT IN ('PBS','Comp')
) AS B
