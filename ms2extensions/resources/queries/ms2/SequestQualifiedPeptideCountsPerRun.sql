/*
 * Copyright (c) 2012-2013 LabKey Corporation
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
SELECT P.Fraction.Run.run AS MS2Run, COUNT(*) AS TotalPeptides
, COUNT(DISTINCT P.Peptide) AS DistinctPeptides
FROM SequestPeptides P 
WHERE ((Charge=1 AND Xcorr >= 1.8)
    OR (Charge=2 AND Xcorr >= 2.3) 
    OR (Charge=3 AND Xcorr >= 2.8)
    OR (Charge=4 AND Xcorr >= 3.3))
AND (DeltaCn >= 0.1)
GROUP BY P.Fraction.Run.run;
