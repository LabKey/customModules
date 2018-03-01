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
A.NETWORK,
A.PROTOCOL,
A.LABID,
A.ASSAYID,
A.SPECROLE,
CASE WHEN A.SPECROLE = 'QC' THEN NULL ELSE A.PTID END AS PTID,
A.PTIDTYPE,
A.SPECTYPE,
A.ISOLATION,
A.ADDITIVE,
A.CRYOSTAT,
CASE WHEN A.SPECROLE = 'QC' THEN A.PTID ELSE NULL END AS CTRSAMPNAME,
A.STDY_DESC,
A.VISITNO,
A.DRAWDT,
A.TESTDT,
A.PLATE,
A.SAMP_ORD,
A.WELL_ID,
CASE
WHEN IFDEFINED(A._fcsfile.Keyword.WELLROLE) IS NOT NULL THEN IFDEFINED(A._fcsfile.Keyword.WELLROLE)
WHEN A.SPECROLE = 'QC' THEN
   CASE
    WHEN A.ANTIGEN = 'CMV' THEN 'AgCtrl-Expt'
    WHEN A.ANTIGEN IN ('negctrl') THEN 'NegCtrl-CellsOnly-Expt'
    WHEN A.ANTIGEN IN ('GTS negctrl', 'GTS Buffer') THEN 'NegCtrl-CellsOnly-Ad'
    WHEN A.ANTIGEN IN ('SEB', 'sebctrl', 'PHA', 'phactrl') THEN 'PosCtrl-Expt'
    ELSE ''
   END
ELSE
   CASE
    WHEN A.ANTIGEN = 'CMV' THEN 'AgCtrl-Specimen'
    WHEN A.ANTIGEN IN ('negctrl') THEN 'NegCtrl-CellsOnly-Specimen'
    WHEN A.ANTIGEN IN ('GTS negctrl', 'GTS Buffer') THEN 'NegCtrl-CellsOnly-Ad'
    WHEN A.ANTIGEN IN ('SEB', 'sebctrl', 'PHA', 'phactrl') THEN 'PosCtrl-Specimen'
    WHEN A.ANTIGEN IN ('Empty Ad5 (VRC)') THEN 'TestAg-Ad'
    ELSE 'TestAg-Specimen'
  END
END
AS WELLROLE,
A.ANTIGEN,
A.NREPL,
A.ANALYSIS_PLAN_ID,
A.EXP_ASSAY_ID,
A.COLLECTCT,
A.SUBSET1, A.SUBSET1_NUM,
A.SUBSET2, A.SUBSET2_NUM,
A.SUBSET3, A.SUBSET3_NUM,
A.SUBSET4, A.SUBSET4_NUM,
A.SUBSET5, A.SUBSET5_NUM,
A.SUBSET6, A.SUBSET6_NUM,
A.SUBSET7, A.SUBSET7_NUM,
A.SUBSET8, A.SUBSET8_NUM,
A.SUBSET9, A.SUBSET9_NUM,
A.SUBSET10, A.SUBSET10_NUM,

A.NUMVIALS,
A.RUNGROUPID,
A.VIAL1_ID,
A.VIAL2_ID,
A.VIAL3_ID,
A.VIAL4_ID,
A.PREFRZCT,
A.VIABL1,
A.RECOVR1,
A.VIABL2,
A.RECOVR2,
A.METHOD,
A.REPLACE,
A.MODDT,
A.RUNNUM,
A.RELIABLE,
BTRIM(
 CASE WHEN (A.ANALYSIS_COMMENT IS NULL) THEN('') ELSE (chr(10) || '(Analysis:)' || A.ANALYSIS_COMMENT) END ||
 CASE WHEN (A.FCS_COMMENT IS NULL) THEN('') ELSE (chr(10) || '(File:)' || A.FCS_COMMENT) END ||
 CASE WHEN (A.RUN_COMMENT IS NULL) THEN('') ELSE (chr(10) || '(Run:)' || A.RUN_COMMENT) END ||
 CASE WHEN (A.SAMPLE_COMMENT IS NULL) THEN('') ELSE (chr(10) || '(Sample:)' || A.SAMPLE_COMMENT) END ||
 CASE WHEN (A.SAMPLE_COMMENTS IS NULL) THEN('') ELSE (chr(10) || '(Sample:)' || A.SAMPLE_COMMENTS) END,
 chr(10)
)
AS COMMENTS,
-- pass through
A._fcsfile,
A._well,
A._sample

FROM AnalysisPlanBase A
  -- INNER JOIN FirstLastTestDate TD ON A.TESTDT=TD.LastTestDate AND A.PTID=TD.PTID AND (A.VISITNO = TD.VISITNO OR A.VISITNO IS NULL AND TD.VISITNO IS NULL)
