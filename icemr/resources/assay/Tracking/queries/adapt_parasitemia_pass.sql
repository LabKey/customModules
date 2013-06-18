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
SELECT f.SampleID, IncreaseTest1, IncreaseTest2, IncreaseTest3,
PassTest1, PassTest2, PassTest3,
CASE WHEN ((PassTest1 + PassTest2 + PassTest3) >= f.AdaptationCriteria) THEN
'Yes'
ELSE
'No'
END As SuccessfulAdaptation
FROM
Samples."Adaptation Flasks" as f,
adapt_parasitemia_increase as p
WHERE f.SampleID = p.SampleID

