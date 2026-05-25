/*
 * Copyright (c) 2026 LabKey Corporation
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
-- Converting uq_lotassignment [peptideId, container, lotNumber] from unique to non-unique index because pk_lotassignment [peptideId, container] overlaps it with a smaller column set
ALTER TABLE peptideinventory.lotAssignment DROP CONSTRAINT uq_lotassignment;
CREATE INDEX ix_lotassignment ON peptideinventory.lotAssignment(peptideId, container, lotNumber);
-- Converting uq_rcpoolassignment [peptideId, container, rcPoolId] from unique to non-unique index because pk_rcpoolassignment [peptideId, container] overlaps it with a smaller column set
ALTER TABLE peptideinventory.rcPoolAssignment DROP CONSTRAINT uq_rcpoolassignment;
CREATE INDEX ix_rcpoolassignment ON peptideinventory.rcPoolAssignment(peptideId, container, rcPoolId);
