/*
 * Copyright (c) 2015-2019 LabKey Corporation
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

CREATE SCHEMA ms2extensions;
GO

CREATE TABLE ms2extensions.Ms2RunAggregates 
(
    MS2Run INT NOT NULL,
    TotalPeptides INT NULL,
    DistinctPeptides INT NULL,
    Container ENTITYID NOT NULL,

    CONSTRAINT PK_Ms2RunAggregates PRIMARY KEY ( MS2Run )
);

CREATE INDEX IDX_MS2RunAggregates_MS2Run ON ms2extensions.Ms2RunAggregates(MS2Run);
CREATE INDEX IDX_MS2RunAggregates_Container ON ms2extensions.Ms2RunAggregates(Container);
