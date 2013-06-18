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
PARAMETERS(start TIMESTAMP, finish TIMESTAMP, flaskId VARCHAR)
SELECT f.*, i.ParasitemiaStart, i.ParasitemiaFinish, i.Increase
FROM
  (SELECT ps.SampleID, ps.Parasitemia as ParasitemiaStart, pf.Parasitemia as ParasitemiaFinish,
  round((pf.parasitemia/ps.parasitemia),2)  As Increase
    FROM
      (SELECT  SampleID,  Parasitemia FROM  Data  WHERE  (timestampdiff('SQL_TSI_DAY', MeasurementDate, start)=0)
        AND SampleID = flaskId)  as ps,
      (SELECT  SampleID,  Parasitemia FROM  Data  WHERE  (timestampdiff('SQL_TSI_DAY', MeasurementDate, finish)=0)
        AND SampleID = flaskId) as pf
    WHERE ps.SampleID = pf.SampleID) as i,
  adapt_flasks as f
WHERE i.SampleID = f.SampleID
