/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

SELECT
  r.MS2Details AS ms2Run,
  COALESCE(X.totalPeptides, 0) AS totalPeptides,
  COALESCE(X.distinctPeptides, 0) as distinctPeptides
FROM
  ms2.MS2SearchRuns r
LEFT OUTER JOIN
  (SELECT
    P.Fraction.Run.run AS ms2Run,
    COUNT(*) AS totalPeptides,
    COUNT(DISTINCT P.Peptide) AS distinctPeptides,
  FROM ms2.XTandemPeptides P
  WHERE (Expect <= 0.1) AND ((Charge=1 AND hyper >= 230)
    OR (Charge=2 AND hyper >= 400)
    OR (Charge=3 AND hyper >= 550))
  GROUP BY P.Fraction.Run.run
) X
ON r.MS2Details = X.ms2Run
