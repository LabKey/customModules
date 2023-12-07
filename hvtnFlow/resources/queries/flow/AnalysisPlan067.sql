/*
 * Copyright (c) 2015 LabKey Corporation
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
--A._sample.property.NETWORK as NETWORK,
A.NETWORK,
A.PROTOCOL,
A.LABID,
A.ASSAY,
A.ASSAY_METHOD,
A.ASSAYID,
A._well.Run AS FLOWJO_XML_FILENAME,
REPLACE(A._well.Name, '.fcs', '') AS FCS_FILENAME,
A.SPECROLE,
A.PTID,
A.SID,
A.GUSPEC,
--A._sample.property.PTIDTYPE as PTIDTYPE,
A.PTIDTYPE,
A.SPECTYPE,
A.ISOLATION,
A.ADDITIVE,
A.CRYOSTAT,
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
NULL AS ANTIGENID,
A.NREPL,
A.ANALYSIS_PLAN_ID,
A.EXP_ASSAY_ID,
A.COLLECTCT,
A.SUBSET1 AS SUBSET,
A.SUBSET1_NUM AS SUBSET_NUM,

A.NUMVIALS,
A.RUNGROUPID AS GUAVA_MUSE_ID,
A.VIABL1,
A.RECOVR1,
A.VIABL2,
A.RECOVR2,
A.METHOD AS COUNT_METHOD,
A.REPLACE,
A.MODDT,
A.RUNNUM,
A.RELIABLE,
A.COMMENTS,
A._well @hidden, A._fcsfile @hidden, A._sample @hidden

FROM AnalysisPlanTemplate A
WHERE A.ANALYSIS_PLAN_ID = '67'
