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

ALTER TABLE gw_labkey.X_LK_INBND_REQUESTS ALTER COLUMN STATUS DROP NOT NULL;

ALTER TABLE gw_labkey.X_LK_INBND_REQUESTS ADD CONSTRAINT INBND_SPECIMENS_BATCH_ID_FK
  FOREIGN KEY (BATCH_ID) REFERENCES gw_labkey.X_LK_INBND_REQUESTS(BATCH_ID);

ALTER TABLE gw_labkey.X_LK_OUTBD_REQUESTS ADD COLUMN DATE_MODIFIED DATE NOT NULL;
ALTER TABLE gw_labkey.X_LK_OUTBD_REQUESTS ALTER COLUMN HDRL_STATUS DROP NOT NULL;

ALTER TABLE gw_labkey.X_LK_OUTBD_SPECIMENS ADD COLUMN HDRL_STATUS VARCHAR(20);
ALTER TABLE gw_labkey.X_LK_OUTBD_SPECIMENS ADD COLUMN DATE_MODIFIED DATE NOT NULL;
ALTER TABLE gw_labkey.X_LK_OUTBD_SPECIMENS ADD COLUMN MODIFIED_RESULT_FLAG VARCHAR(1);
ALTER TABLE gw_labkey.X_LK_OUTBD_SPECIMENS ADD CONSTRAINT OUTBD_SPECIMENS_BATCH_ID_FK
  FOREIGN KEY (BATCH_ID) REFERENCES gw_labkey.X_LK_OUTBD_REQUESTS(BATCH_ID);