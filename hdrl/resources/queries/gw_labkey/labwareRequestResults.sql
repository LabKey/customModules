SELECT batch_id as RequestId,
       customer_note as CustomerNote,
       date_received as Received,
       date_completed as Completed,
       date_modified as Modified,
       hs.RowId as RequestStatusId,
       r.Container as Container
       FROM X_LK_OUTBD_REQUESTS
       LEFT JOIN hdrl.InboundRequest r on r.RequestId = batch_id
       LEFT JOIN hdrl.RequestStatus hs ON hs.name = hdrl_status;
