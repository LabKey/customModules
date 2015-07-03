-- =============================================
-- Author:      C. Schoening
-- Create date: 24-Jun-2015
-- Description: Creates the LabKey interface tables in the GW_LABKEY schema
--    This script must be run Logged in with the "gw_labkey" user previously created 
-- =============================================

--------------------------------------------------------
--  DDL for Table X_LK_INBND_REQUESTS
--------------------------------------------------------
CREATE TABLE X_LK_INBND_REQUESTS 
(
  BATCH_ID NUMBER NOT NULL 
, CARRIER VARCHAR2(40) 
, TRACKING_NUMBER VARCHAR2(40) 
, CUSTOMER VARCHAR2(100) NOT NULL
, STATUS VARCHAR2(20) NOT NULL
, CONSTRAINT X_LK_INBND_REQUESTS_PK PRIMARY KEY 
  (
    BATCH_ID 
  )
  ENABLE 
);

--------------------------------------------------------
--  DDL for Table X_LK_INBND_SPECIMENS
--------------------------------------------------------
CREATE TABLE X_LK_INBND_SPECIMENS 
(
  TEST_REQUEST_ID NUMBER NOT NULL 
, BATCH_ID NUMBER NOT NULL
, TEST_REQUESTED VARCHAR2(100) NOT NULL
, CUST_BARCODE VARCHAR2(40) 
, FMP VARCHAR2(2) NOT NULL
, SSN VARCHAR2(9) NOT NULL
, DRAW_DATE DATE NOT NULL
, SPECIMEN_TYPE VARCHAR2(20) 
, NUM_CONTAINERS NUMBER 
, SOT VARCHAR2(1) 
, DUC VARCHAR2(5) 
, DOD_ID VARCHAR2(20) 
, FIRST_NAME VARCHAR2(40) 
, MIDDLE_NAME VARCHAR2(40) 
, LAST_NAME VARCHAR2(80) 
, BIRTH_DATE DATE 
, GENDER VARCHAR2(20) 
, INITIALS VARCHAR2(5) 
, CONSTRAINT X_LK_INBND_SPECIMENS_PK PRIMARY KEY 
  (
    TEST_REQUEST_ID 
  )
  ENABLE 
);

--------------------------------------------------------
--  DDL for Table X_LK_OUTBD_REQUESTS
--------------------------------------------------------
CREATE TABLE X_LK_OUTBD_REQUESTS 
(
  BATCH_ID NUMBER NOT NULL 
, HDRL_STATUS VARCHAR2(20) NOT NULL
, CUSTOMER_NOTE VARCHAR2(254) 
, DATE_RECEIVED DATE NOT NULL
, DATE_COMPLETED DATE 
, CONSTRAINT X_LK_OUTBD_REQUESTS_PK PRIMARY KEY 
  (
    BATCH_ID 
  )
  ENABLE 
);

--------------------------------------------------------
--  DDL for Table X_LK_OUTBD_SPECIMENS 
--------------------------------------------------------
CREATE TABLE X_LK_OUTBD_SPECIMENS 
(
  TEST_REQUEST_ID NUMBER NOT NULL 
, BATCH_ID NUMBER NOT NULL
, DATE_RECEIVED DATE NOT NULL
, DATE_COMPLETED DATE 
, SAMPLE_INTEGRITY VARCHAR2(80) 
, TEST_RESULT VARCHAR2(254) 
, CUSTOMER_CODE VARCHAR2(80) 
, CLINICAL_REPORT CLOB 
, CONSTRAINT X_LK_OUTBD_SPECIMENS_PK PRIMARY KEY 
  (
    TEST_REQUEST_ID 
  )
  ENABLE 
);

commit;
