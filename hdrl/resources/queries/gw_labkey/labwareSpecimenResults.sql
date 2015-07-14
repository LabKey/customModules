SELECT test_request_id AS SpecimenId,
       batch_id as RequestId,
       date_received AS Received,
        date_completed as Completed,
        sample_integrity as SampleIntegrity,
        test_result as TestResult,
        customer_code as CustomerCode,
        date_modified as Modified,
        modified_result_flag as ModifiedResultFlag,
        hs.RowId AS RequestStatusId,
        r.Container as Container,
        report_file_name as ReportFileName
        FROM X_LK_OUTBD_SPECIMENS
        LEFT JOIN hdrl.InboundRequest r on r.RequestId = batch_id
        LEFT JOIN hdrl.RequestStatus hs ON hs.name = hdrl_status;