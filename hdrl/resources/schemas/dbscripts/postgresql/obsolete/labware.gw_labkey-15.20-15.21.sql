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

/*
 * The tables below mimic the Oracle tables used for communication with LabWare.  We use them for 
 * automated tests only.
 */
CREATE SCHEMA gw_labkey;

CREATE TABLE gw_labkey.X_LK_INBND_REQUESTS 
(
  BATCH_ID INT NOT NULL 
, CARRIER VARCHAR(40) 
, TRACKING_NUMBER VARCHAR(40) 
, CUSTOMER VARCHAR(100) NOT NULL
, STATUS VARCHAR(20) NOT NULL
, CONSTRAINT X_LK_INBND_REQUESTS_PK PRIMARY KEY 
  (
    BATCH_ID 
  )
);

CREATE TABLE gw_labkey.X_LK_INBND_SPECIMENS 
(
  TEST_REQUEST_ID INT NOT NULL 
, BATCH_ID INT NOT NULL
, TEST_REQUESTED VARCHAR(100) NOT NULL
, CUST_BARCODE VARCHAR(40) 
, FMP VARCHAR(2) NOT NULL
, SSN VARCHAR(9) NOT NULL
, DRAW_DATE DATE NOT NULL
, SPECIMEN_TYPE VARCHAR(20) 
, NUM_CONTAINERS INT 
, SOT VARCHAR(1) 
, DUC VARCHAR(5) 
, DOD_ID VARCHAR(20) 
, FIRST_NAME VARCHAR(40) 
, MIDDLE_NAME VARCHAR(40) 
, LAST_NAME VARCHAR(80) 
, BIRTH_DATE DATE 
, GENDER VARCHAR(20) 
, INITIALS VARCHAR(5) 
, CONSTRAINT X_LK_INBND_SPECIMENS_PK PRIMARY KEY 
  (
    TEST_REQUEST_ID 
  )
);

CREATE TABLE gw_labkey.X_LK_OUTBD_REQUESTS 
(
  BATCH_ID INT NOT NULL 
, HDRL_STATUS VARCHAR(20) NOT NULL
, CUSTOMER_NOTE VARCHAR(254) 
, DATE_RECEIVED DATE NOT NULL
, DATE_COMPLETED DATE 
, CONSTRAINT X_LK_OUTBD_REQUESTS_PK PRIMARY KEY 
  (
    BATCH_ID 
  )
);

CREATE TABLE gw_labkey.X_LK_OUTBD_SPECIMENS 
(
  TEST_REQUEST_ID INT NOT NULL 
, BATCH_ID INT NOT NULL
, DATE_RECEIVED DATE NOT NULL
, DATE_COMPLETED DATE 
, SAMPLE_INTEGRITY VARCHAR(80) 
, TEST_RESULT VARCHAR(254) 
, CUSTOMER_CODE VARCHAR(80) 
, CLINICAL_REPORT TEXT 
, CONSTRAINT X_LK_OUTBD_SPECIMENS_PK PRIMARY KEY 
  (
    TEST_REQUEST_ID 
  )
);
