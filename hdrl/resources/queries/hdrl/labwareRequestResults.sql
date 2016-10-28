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
SELECT batch_id as RequestId,
       customer_note as CustomerNote,
       date_received as Received,
       date_completed as Completed,
       date_modified as Modified,
       s.RowId as RequestStatusId
       FROM labwareOutboundRequests
       LEFT JOIN RequestStatus s ON s.name = hdrl_status;
