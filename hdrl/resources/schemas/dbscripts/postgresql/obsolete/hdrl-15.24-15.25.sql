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
CREATE TABLE hdrl.labwareOutboundRequests
(
  batch_id INT NOT NULL,
  hdrl_status VARCHAR(20),
  customer_note VARCHAR(254),
  date_received DATE NOT NULL,
  date_completed DATE,
  date_modified DATE NOT NULL,
  CONSTRAINT PK_LabwareOutboundRequests PRIMARY KEY (batch_id),
  CONSTRAINT FK_LabwareOutboundRequests_InboundRequest FOREIGN KEY (batch_id) REFERENCES hdrl.InboundRequest (RequestId) ON DELETE CASCADE
);

CREATE TABLE hdrl.labwareOutboundSpecimens
(
  test_request_id INT NOT NULL,
  batch_id INT NOT NULL,
  date_received DATE NOT NULL,
  date_completed DATE,
  sample_integrity VARCHAR(80),
  test_result VARCHAR(254),
  customer_code VARCHAR(80),
  hdrl_status VARCHAR(20),
  date_modified DATE NOT NULL,
  modified_result_flag VARCHAR(1),
  report_file_name VARCHAR(20),

  CONSTRAINT PK_LabwareOutboundSpecimens PRIMARY KEY (test_request_id),
  CONSTRAINT FK_LabwareOutboundSpecimens_LabwareOutboundRequests FOREIGN KEY (batch_id) REFERENCES hdrl.labwareOutboundRequests (batch_id) ON DELETE CASCADE,
  CONSTRAINT FK_LabwareOutboundSpecimens_InboundSpecimen FOREIGN KEY (test_request_id) REFERENCES hdrl.InboundSpecimen (RowId) ON DELETE CASCADE
);