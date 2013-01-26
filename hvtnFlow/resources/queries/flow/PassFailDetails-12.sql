/*
 * Copyright (c) 2011-2013 LabKey Corporation
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
  B.CD4_IL2_Count,
  B.CD4_IFNg_Count,
  B.CD4_CD154_Count,
  B.CD4_CD107a_Count,
  B.CD4_TNFa_Count,

  B.CD8_Count,
  B.CD8_IL2_Count,
  B.CD8_IFNg_Count,
  B.CD8_CD154_Count,
  B.CD8_CD107a_Count,
  B.CD8_TNFa_Count,

  B.Run,
  B."EXPERIMENT NAME",
  B.SampleOrder,
  B.Sample,
  CASE WHEN B.CD4_Count < B.Cutoff THEN 'LO_CD4' END AS LO_CD4,
  CASE WHEN B.CD8_Count < B.Cutoff THEN 'LO_CD8' END AS LO_CD8,
  CASE WHEN (B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.negctrl
    WHEN (B.negctrl IS NOT NULL) THEN 'excluded' END AS negctrl,
  B.posctrl,
  CASE WHEN (B.posctrl IS NOT NULL AND (
      B.CD4_IL2_Freq < 1.2 OR
      B.CD4_IFNg_Freq < 1.2 OR
      B.CD4_CD154_Freq < 1.2 OR
      B.CD4_CD107a_Freq < 1.2 OR
      B.CD4_TNFa_Freq < 1.2 OR
      B.CD8_IL2_Freq < 1.2 OR
      B.CD8_IFNg_Freq < 1.2 OR
      B.CD8_CD154_Freq < 1.2 OR
      B.CD8_CD107a_Freq < 1.2 OR
      B.CD8_TNFa_Freq < 1.2
    )) THEN 'LO_POS' END AS LO_POS,

  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD4_IL2_Freq    END AS posctrl_CD4_IL2_Freq,
  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD4_IFNg_Freq   END AS posctrl_CD4_IFNg_Freq,
  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD4_CD154_Freq  END AS posctrl_CD4_CD154_Freq,
  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD4_CD107a_Freq END AS posctrl_CD4_CD107a_Freq,
  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD4_TNFa_Freq   END AS posctrl_CD4_TNFa_Freq,

  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD8_IL2_Freq    END AS posctrl_CD8_IL2_Freq,
  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD8_IFNg_Freq   END AS posctrl_CD8_IFNg_Freq,
  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD8_CD154_Freq  END AS posctrl_CD8_CD154_Freq,
  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD8_CD107a_Freq END AS posctrl_CD8_CD107a_Freq,
  CASE WHEN (B.posctrl IS NOT NULL) THEN B.CD8_TNFa_Freq   END AS posctrl_CD8_TNFa_Freq,

  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD4_Count END AS negctrl_CD4_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD4_IL2_Count END AS negctrl_CD4_IL2_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD4_IFNg_Count END AS negctrl_CD4_IFNg_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD4_CD154_Count END AS negctrl_CD4_CD154_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD4_CD107a_Count END AS negctrl_CD4_CD107a_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD4_TNFa_Count END AS negctrl_CD4_TNFa_Count,

  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD8_Count END AS negctrl_CD8_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD8_IL2_Count END AS negctrl_CD8_IL2_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD8_IFNg_Count END AS negctrl_CD8_IFNg_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD8_CD154_Count END AS negctrl_CD8_CD154_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD8_CD107a_Count END AS negctrl_CD8_CD107a_Count,
  CASE WHEN (B.negctrl IS NOT NULL AND B.CD4_Count >= 5000 AND B.CD8_Count >= 5000) THEN B.CD8_TNFa_Count END AS negctrl_CD8_TNFa_Count,
  B.Run.Name||'-' ||B."EXPERIMENT NAME"||'-'||B.SampleOrder AS Key,
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

    A.Statistic('S/Exclude/14-/Lv/L/3+/4+:Count') AS CD4_Count,
    A.Statistic('S/Exclude/14-/Lv/L/3+/4+/IL2+:Count') AS CD4_IL2_Count,
    A.Statistic('S/Exclude/14-/Lv/L/3+/4+/IFNg+:Count') AS CD4_IFNg_Count,
    A.Statistic('S/Exclude/14-/Lv/L/3+/4+/154+:Count') AS CD4_CD154_Count,
    A.Statistic('S/Exclude/14-/Lv/L/3+/4+/107a+:Count') AS CD4_CD107a_Count,
    A.Statistic('S/Exclude/14-/Lv/L/3+/4+/TNFa+:Count') AS CD4_TNFa_Count,

    A.Statistic('S/Exclude/14-/Lv/L/3+/4+/IL2+:Freq_Of_Parent') AS CD4_IL2_Freq,
    A.Statistic('S/Exclude/14-/Lv/L/3+/4+/IFNg+:Freq_Of_Parent') AS CD4_IFNg_Freq,
    A.Statistic('S/Exclude/14-/Lv/L/3+/4+/154+:Freq_Of_Parent') AS CD4_CD154_Freq,
    A.Statistic('S/Exclude/14-/Lv/L/3+/4+/107a+:Freq_Of_Parent') AS CD4_CD107a_Freq,
    A.Statistic('S/Exclude/14-/Lv/L/3+/4+/TNFa+:Freq_Of_Parent') AS CD4_TNFa_Freq,

    A.Statistic('S/Exclude/14-/Lv/L/3+/8+:Count') AS CD8_Count,
    A.Statistic('S/Exclude/14-/Lv/L/3+/8+/IL2+:Count') AS CD8_IL2_Count,
    A.Statistic('S/Exclude/14-/Lv/L/3+/8+/IFNg+:Count') AS CD8_IFNg_Count,
    A.Statistic('S/Exclude/14-/Lv/L/3+/8+/154+:Count') AS CD8_CD154_Count,
    A.Statistic('S/Exclude/14-/Lv/L/3+/8+/107a+:Count') AS CD8_CD107a_Count,
    A.Statistic('S/Exclude/14-/Lv/L/3+/8+/TNFa+:Count') AS CD8_TNFa_Count,

    A.Statistic('S/Exclude/14-/Lv/L/3+/8+/IL2+:Freq_Of_Parent') AS CD8_IL2_Freq,
    A.Statistic('S/Exclude/14-/Lv/L/3+/8+/IFNg+:Freq_Of_Parent') AS CD8_IFNg_Freq,
    A.Statistic('S/Exclude/14-/Lv/L/3+/8+/154+:Freq_Of_Parent') AS CD8_CD154_Freq,
    A.Statistic('S/Exclude/14-/Lv/L/3+/8+/107a+:Freq_Of_Parent') AS CD8_CD107a_Freq,
    A.Statistic('S/Exclude/14-/Lv/L/3+/8+/TNFa+:Freq_Of_Parent') AS CD8_TNFa_Freq,
  FROM FCSAnalyses AS A
  WHERE A.FCSFile.Keyword.Stim NOT IN ('PBS','Comp')
) AS B
