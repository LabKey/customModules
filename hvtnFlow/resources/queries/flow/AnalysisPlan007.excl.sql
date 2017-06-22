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
A.PTID,
A.PTIDTYPE,
A.CTRSAMPNAME,
A.STDY_DESC,
A.VISITNO,
A.DRAWDT,
A.TESTDT,
A.PLATE,
A.SAMP_ORD,
A.WELL_ID,
A.WELLROLE,
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
A.COMMENTS,
A._well @hidden, A._fcsfile @hidden, A._sample @hidden

FROM AnalysisPlanTemplate A
WHERE A.ANALYSIS_PLAN_ID = '7.excl'
