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

/* labware.gw_labkey-16.11-16.12.sql */

ALTER TABLE gw_labkey.X_LK_INBND_SPECIMENS ALTER draw_date TYPE timestamp;
ALTER TABLE gw_labkey.X_LK_INBND_SPECIMENS ALTER birth_date TYPE timestamp;
ALTER TABLE gw_labkey.X_LK_OUTBD_REQUESTS ALTER date_received TYPE timestamp;
ALTER TABLE gw_labkey.X_LK_OUTBD_REQUESTS ALTER date_completed TYPE timestamp;
ALTER TABLE gw_labkey.X_LK_OUTBD_REQUESTS ALTER date_modified TYPE timestamp;
ALTER TABLE gw_labkey.X_LK_OUTBD_SPECIMENS ALTER date_received TYPE timestamp;
ALTER TABLE gw_labkey.X_LK_OUTBD_SPECIMENS ALTER date_completed TYPE timestamp;
ALTER TABLE gw_labkey.X_LK_OUTBD_SPECIMENS ALTER date_modified TYPE timestamp;