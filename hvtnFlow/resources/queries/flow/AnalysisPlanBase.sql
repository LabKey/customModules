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
CONVERT('HVTN', 'VARCHAR') AS NETWORK,
FCSAnalyses.FCSFile.Sample.Property.PROTOCOL AS PROTOCOL,
'FH' AS LABID,
COALESCE(IFDEFINED(FCSAnalyses.FCSFile.Keyword."EXPERIMENT NAME"), FCSAnalyses.FCSFile.Run.Name) AS ASSAYID,
CASE
  WHEN IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.SPECROLE) IS NOT NULL THEN IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.SPECROLE)
  WHEN FCSAnalyses.FCSFile.Sample.Property.PTID LIKE 'FH%' THEN 'QC'
  ELSE 'Sample'
END AS SPECROLE,
COALESCE(IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.PTID), IFDEFINED(FCSAnalyses.FCSFile.Keyword.Sample)) AS PTID,
CASE
  WHEN IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.PTIDTYPE) IS NOT NULL THEN IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.PTIDTYPE)
  WHEN FCSAnalyses.FCSFile.Sample.Property.PTID LIKE 'FH%' THEN NULL
  ELSE 'S'
END AS PTIDTYPE,
COALESCE(CAST(IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.SPECTYPE) AS VARCHAR), 'PBMC') AS SPECTYPE,
COALESCE(CAST(IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.ISOLATION) AS VARCHAR), 'Blood draw') AS ISOLATION,
COALESCE(CAST(IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.ADDITIVE) AS VARCHAR), 'ACD') AS ADDITIVE,
COALESCE(CAST(IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.CRYOSTAT) AS VARCHAR), '4') AS CRYOSTAT,
CAST(IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.STDY_DESC) AS VARCHAR) AS STDY_DESC,
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

IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.GUAVA_DATA_ID) AS RUNGROUPID,
IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.NUMVIALS) AS NUMVIALS,
IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.VIAL1_ID) AS VIAL1_ID,
IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.VIAL2_ID) AS VIAL2_ID,
IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.VIAL3_ID) AS VIAL3_ID,
IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.VIAL4_ID) AS VIAL4_ID,
NULL AS PREFRZCT,
FCSAnalyses.FCSFile.Sample.Property.VIABL1,
FCSAnalyses.FCSFile.Sample.Property.RECOVR1,
FCSAnalyses.FCSFile.Sample.Property.VIABL2,
FCSAnalyses.FCSFile.Sample.Property.RECOVR2,
COALESCE(IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.METHOD), 1) AS METHOD,
'N' AS REPLACE,
NULL AS MODDT,
COALESCE(IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.RUNNUM), IFDEFINED(FCSAnalyses.FCSFile.Sample.Property."Collection Num")) AS RUNNUM,
-- NOTE: When the SampleSet is imported with 'Y' or 'N' values for RELIABLE, the column will be created as a boolean type
CASE WHEN COALESCE(IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.RELIABLE), true) THEN 'Y' ELSE 'N' END AS RELIABLE,

FCSAnalyses.Flag.Comment AS ANALYSIS_COMMENT,
FCSAnalyses.FCSFile.Flag.Comment AS FCS_COMMENT,
FCSAnalyses.FCSFile.Run.Flag.Comment AS RUN_COMMENT,
FCSAnalyses.FCSFile.Sample.Flag.Comment AS SAMPLE_COMMENT,
CAST(IFDEFINED(FCSAnalyses.FCSFile.Sample.Property.COMMENTS) AS VARCHAR) AS SAMPLE_COMMENTS,

NULL AS SPECID,
FCSAnalyses.FCSFile.Sample AS Sample,
NULL AS VISITDAY,
'Y' AS ASSAYRUN,
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
  FCSAnalyses.FCSFile.Keyword."Sample Order" IS NULL OR
  FCSAnalyses.FCSFile.Keyword."Sample Order" NOT IN ('PBS','Comp')

