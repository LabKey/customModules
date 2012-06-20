SELECT  P.RowId
, CASE WHEN P.Charge=1 THEN P.Hyper ELSE 999999 END AS HyperChgFilt1
, CASE WHEN P.Charge=2 THEN P.Hyper ELSE 999999 END AS HyperChgFilt2
, CASE WHEN P.Charge=3 THEN P.Hyper ELSE 999999 END AS HyperChgFilt3
, CASE WHEN P.Charge=4 THEN P.Hyper ELSE 999999 END AS HyperChgFilt4	
FROM XTandemPeptides P 
