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
 *
 * For the new pass-fail, we do not need to include the background since we
 * will no longer use that, and for the T cells, we simply want to change the
 * thresholds to 35,000 for CD4 and 15,000 for CD8.
 *
 * The CD4 count is in this gate:
 * Time/S/Lv/K1/K2/K3/K4/K5/K6/K7/K8/14-/S/L/19-/3+/3+excl 16br/56-16-/4+:Count
 *
 * The CD8 count is in this gate:
 * Time/S/Lv/K1/K2/K3/K4/K5/K6/K7/K8/14-/S/L/19-/3+/3+excl 16br/56-16-/8+:Count
 *
 * The overall verdict should indicate fail if any of the antigen-specific CD4 or CD8 counts are below the
 * thresholds, or if both replicates of the negctrl are below.  Sebctrl should not be considered.
 */

SELECT
  Key,
  CASE
    WHEN (LO_CD4 IS NULL AND LO_CD8 IS NULL)
      THEN 'PASS'
    ELSE
      (COALESCE(LO_CD4, '') ||
      COALESCE(LO_CD8, ''))
    END AS Verdict,
  Run,
  "EXPERIMENT NAME",
  SampleOrder,
  Sample,
  FileCount
FROM
(
  -- to make this easier to read, we separate out the aggregates from the verdict expressions
  SELECT
    Min(Key) AS Key,
    CASE
       WHEN (COUNT(LO_CD4) > 0) THEN 'LO_CD4 '
       -- show LO_CD4 flag only when both negctrl replicates are LO_CD4
       WHEN (COUNT(negctrl_LO_CD4) = COUNT(negctrl)) THEN 'LO_CD4 '
    END AS LO_CD4,
    CASE
       WHEN (COUNT(LO_CD8) > 0) THEN 'LO_CD8 '
       -- show LO_CD8 flag only when both negctrl replicates are LO_CD8
       WHEN (COUNT(negctrl_LO_CD8) = COUNT(negctrl)) THEN 'LO_CD8 '
    END AS LO_CD8,
    Run,
    "EXPERIMENT NAME",
    SampleOrder,
    Min(Sample) AS Sample,
    Count(FCSAnalysis) AS FileCount
  FROM "PassFailDetails-AP051"
  GROUP BY Run, "EXPERIMENT NAME", SampleOrder
) D
