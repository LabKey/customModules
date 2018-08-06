/*
 * Copyright (c) 2016 LabKey Corporation
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

/* hdrl-16.10-16.11.sql */

ALTER TABLE hdrl.requestResult DROP COLUMN Container;
ALTER TABLE hdrl.specimenResult DROP COLUMN Container;

/* hdrl-16.11-16.12.sql */

ALTER TABLE hdrl.labwareoutboundrequests ALTER COLUMN date_received TYPE timestamp;
ALTER TABLE hdrl.labwareoutboundrequests ALTER COLUMN date_completed TYPE timestamp;
ALTER TABLE hdrl.labwareoutboundrequests ALTER COLUMN date_modified TYPE timestamp;
ALTER TABLE hdrl.labwareoutboundspecimens ALTER COLUMN date_received TYPE timestamp;
ALTER TABLE hdrl.labwareoutboundspecimens ALTER COLUMN date_completed TYPE timestamp;
ALTER TABLE hdrl.labwareoutboundspecimens ALTER COLUMN date_modified TYPE timestamp;