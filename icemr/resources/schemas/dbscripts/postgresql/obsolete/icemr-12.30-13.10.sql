/*
 * Copyright (c) 2013-2016 LabKey Corporation
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

CREATE SCHEMA icemr;

-- create the lookup tables
CREATE TABLE icemr.lk_compound (
    compound     VARCHAR(20) NOT NULL,
  CONSTRAINT PK_lk_compound PRIMARY KEY (compound)
);

-- prepopulate compound values
INSERT INTO icemr.lk_compound (compound) VALUES
('1843U89'),
('BMS-388891'),
('GSK 2645947'),
('DSM1'),
('DMSO')
;

