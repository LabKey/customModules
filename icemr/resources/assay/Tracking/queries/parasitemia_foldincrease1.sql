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
SELECT pi.SampleID, pi.parasitemia as ParasitemiaStart, pf.parasitemia as ParasitemiaFinish,
  pf.growthfoldtestfinished as GrowthFoldTest, (pf.parasitemia/pi.parasitemia) as Increase
FROM
  (SELECT  SampleID,  Parasitemia,  GrowthFoldTestInitiated  FROM  Data  WHERE  GrowthFoldTestInitiated=1) as pi,
  (SELECT  SampleID,  Parasitemia,  GrowthFoldTestFinished  FROM  Data  WHERE  GrowthFoldTestFinished=1)as pf
WHERE pi.SampleID = pf.SampleID