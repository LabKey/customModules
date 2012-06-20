SELECT P.Fraction.Run.run AS MS2Run, COUNT(*) AS TotalPeptides,
COUNT(DISTINCT P.Peptide) AS DistinctPeptides
FROM XTandemPeptides P 
WHERE ((Charge=1 AND hyper >= 230)
    OR (Charge=2 AND hyper >= 400) 
    OR (Charge=3 AND hyper >= 550))
AND (Expect <= 0.1)
GROUP BY P.Fraction.Run.run;
