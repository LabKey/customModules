/*
 * Copyright (c) 2011 LabKey Corporation
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
CONVERT('HVTN', 'VARCHAR') AS NETWORK,
FCSAnalyses.FCSFile.Sample.Property.PROTOCOL AS PROTOCOL,
'FH' AS LABID,
FCSAnalyses.FCSFile.Run AS ASSAYID,
CASE
  WHEN FCSAnalyses.FCSFile.Sample.Property.PTID LIKE 'FH%' THEN 'QC'
  ELSE 'Sample'
END AS SPECROLE,
FCSAnalyses.FCSFile.Sample.Property.PTID,
'S' AS PTIDTYPE,
NULL AS STDY_DESC,
FCSAnalyses.FCSFile.Sample.Property.VISITNO,
FCSAnalyses.FCSFile.Sample.Property.DRAWDT,
FCSAnalyses.FCSFile.Sample.Property.TESTDT,
FCSAnalyses.FCSFile.Keyword.Plate AS PLATE,
FCSAnalyses.FCSFile.Sample.Property.SAMP_ORD,
FCSAnalyses.FCSFile.Keyword."WELL ID" AS WELL_ID,
FCSAnalyses.FCSFile.Keyword.Stim AS ANTIGEN,
FCSAnalyses.FCSFile.Keyword.Replicate AS NREPL,
Subsets.AnalysisPlanId AS ANALYSIS_PLAN_ID,
FCSAnalyses.FCSFile.Keyword.EXP_ASSAY_ID AS EXP_ASSAY_ID,

FCSAnalyses.Statistic."Count" AS COLLECTCT,
Subsets.NAME1 AS SUBSET1, FCSAnalyses.Statistic(Subsets.STAT1) AS SUBSET1_NUM,
Subsets.NAME2 AS SUBSET2, FCSAnalyses.Statistic(Subsets.STAT2) AS SUBSET2_NUM,
Subsets.NAME3 AS SUBSET3, FCSAnalyses.Statistic(Subsets.STAT3) AS SUBSET3_NUM,
Subsets.NAME4 AS SUBSET4, FCSAnalyses.Statistic(Subsets.STAT4) AS SUBSET4_NUM,
Subsets.NAME5 AS SUBSET5, FCSAnalyses.Statistic(Subsets.STAT5) AS SUBSET5_NUM,
Subsets.NAME6 AS SUBSET6, FCSAnalyses.Statistic(Subsets.STAT6) AS SUBSET6_NUM,
Subsets.NAME7 AS SUBSET7, FCSAnalyses.Statistic(Subsets.STAT7) AS SUBSET7_NUM,
Subsets.NAME8 AS SUBSET8, FCSAnalyses.Statistic(Subsets.STAT8) AS SUBSET8_NUM,
Subsets.NAME9 AS SUBSET9, FCSAnalyses.Statistic(Subsets.STAT9) AS SUBSET9_NUM,
Subsets.NAME10 AS SUBSET10, FCSAnalyses.Statistic(Subsets.STAT10) AS SUBSET10_NUM,

NULL AS NUMVIALS,
NULL AS VIAL1_ID,
NULL AS VIAL2_ID,
NULL AS VIAL3_ID,
NULL AS VIAL4_ID,
NULL AS PREFRZCT,
FCSAnalyses.FCSFile.Sample.Property.VIABL1,
FCSAnalyses.FCSFile.Sample.Property.RECOVR1,
FCSAnalyses.FCSFile.Sample.Property.VIABL2,
FCSAnalyses.FCSFile.Sample.Property.RECOVR2,
1 AS METHOD,
'N' AS REPLACE,
NULL AS MODDT,
FCSAnalyses.Flag.Comment AS ANALYSIS_COMMENT,
FCSAnalyses.FCSFile.Flag.Comment AS FCS_COMMENT,
FCSAnalyses.FCSFile.Run.Flag.Comment AS RUN_COMMENT,
FCSAnalyses.FCSFile.Sample.Flag.Comment AS SAMPLE_COMMENT,

NULL AS SPECID,
FCSAnalyses.FCSFile.Sample AS Sample,
NULL AS VISITDAY,
'Y' AS ASSAYRUN,
'Y' AS RELIABLE,
--FCSAnalyses.FCSFile.Sample.Property.PLT_TEMPLATE,
' ' AS COLORS,
'S' AS STAIN,
FCSAnalyses.RowId AS AnalysisResults,
FCSAnalyses.FCSFile.Name As FCSFileName,
-- make sample available by selecting fcsfile
FCSAnalyses.FCSFile as _fcsfile,
FCSAnalyses.RowId as _well,
FCSAnalyses.FCSFile.Sample as _sample

FROM FCSAnalyses INNER JOIN Project.lists.AnalysisPlans AS Subsets ON 1=1

WHERE
  FCSAnalyses.FCSFile.Keyword."Sample Order" NOT IN ('PBS','Comp')