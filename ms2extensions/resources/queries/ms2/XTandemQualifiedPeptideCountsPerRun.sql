/*
 * Copyright (c) 2012 LabKey Corporation
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
SELECT P.Fraction.Run.run AS MS2Run, COUNT(*) AS TotalPeptides,
COUNT(DISTINCT P.Peptide) AS DistinctPeptides
FROM XTandemPeptides P 
WHERE ((Charge=1 AND hyper >= 230)
    OR (Charge=2 AND hyper >= 400) 
    OR (Charge=3 AND hyper >= 550))
AND (Expect <= 0.1)
GROUP BY P.Fraction.Run.run;
