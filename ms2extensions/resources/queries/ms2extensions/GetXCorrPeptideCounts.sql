/*
 * Copyright (c) 2013-2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

SELECT
  ms2Run,
  MAX(X.totalPeptides) AS totalPeptides,
  MAX(X.distinctPeptides) as distinctPeptides
FROM
  (SELECT ms2Run, totalPeptides, distinctPeptides FROM GetCometPeptideCounts WHERE ms2Run.StatusId = 1 UNION
  SELECT ms2Run, totalPeptides, distinctPeptides FROM GetSequestPeptideCounts WHERE ms2Run.StatusId = 1) X
GROUP BY ms2Run
