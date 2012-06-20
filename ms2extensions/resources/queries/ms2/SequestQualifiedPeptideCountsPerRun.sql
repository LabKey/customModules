SELECT P.Fraction.Run.run AS MS2Run, COUNT(*) AS TotalPeptides
, COUNT(DISTINCT P.Peptide) AS DistinctPeptides
FROM SequestPeptides P 
WHERE ((Charge=1 AND Xcorr >= 1.8)
    OR (Charge=2 AND Xcorr >= 2.3) 
    OR (Charge=3 AND Xcorr >= 2.8))
AND (DeltaCn >= 0.1)
GROUP BY P.Fraction.Run.run;
