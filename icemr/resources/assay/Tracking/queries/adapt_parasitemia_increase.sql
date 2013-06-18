/*
 * Copyright (c) 2013 LabKey Corporation
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
SELECT *,
CASE WHEN (FinishParasitemia1 IS NOT NULL AND StartParasitemia1 IS NOT NULL AND StartParasitemia1 <> 0) THEN
  round(FinishParasitemia1/StartParasitemia1, 2)
ELSE 0 END As IncreaseTest1,
CASE WHEN (FinishParasitemia1 IS NOT NULL AND StartParasitemia1 IS NOT NULL AND StartParasitemia1 <> 0) THEN
  CASE WHEN (FinishParasitemia1/StartParasitemia1 >= FoldIncrease1) THEN (1)
  ELSE (0) END
ELSE (0) END As PassTest1,
CASE WHEN (FinishParasitemia2 IS NOT NULL AND StartParasitemia2 IS NOT NULL AND StartParasitemia2 <> 0) THEN
  round(FinishParasitemia2/StartParasitemia2, 2)
ELSE 0 END As IncreaseTest2,
CASE WHEN (FinishParasitemia2 IS NOT NULL AND StartParasitemia2 IS NOT NULL AND StartParasitemia2 <> 0) THEN
  CASE WHEN (FinishParasitemia2/StartParasitemia2 >= FoldIncrease2) THEN (1)
  ELSE (0) END
ELSE 0 END As PassTest2,
CASE WHEN (FinishParasitemia3 IS NOT NULL AND StartParasitemia3 IS NOT NULL AND StartParasitemia3 <> 0) THEN
  round(FinishParasitemia3/StartParasitemia3, 2)
ELSE 0 END As IncreaseTest3,
CASE WHEN (FinishParasitemia3 IS NOT NULL AND StartParasitemia3 IS NOT NULL AND StartParasitemia3 <> 0) THEN
  CASE WHEN (FinishParasitemia3/StartParasitemia3 >= FoldIncrease3) THEN (1)
  ELSE (0) END
ELSE 0 END As PassTest3,
FROM
adapt_flasks
