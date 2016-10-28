/*
 * Copyright (c) 2015-2016 LabKey Corporation
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
        report_file_name as ReportFileName
        FROM labwareOutboundSpecimens
        LEFT JOIN RequestStatus hs ON hs.name = hdrl_status;