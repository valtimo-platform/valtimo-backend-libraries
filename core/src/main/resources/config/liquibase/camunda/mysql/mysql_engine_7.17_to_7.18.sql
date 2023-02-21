/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

--
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

insert into ACT_GE_SCHEMA_LOG
values ('700', CURRENT_TIMESTAMP, '7.18.0');

-- https://jira.camunda.com/browse/CAM-14303 --
ALTER TABLE ACT_RU_TASK
  ADD COLUMN LAST_UPDATED_ datetime;
create index ACT_IDX_TASK_LAST_UPDATED on ACT_RU_TASK(LAST_UPDATED_);

-- https://jira.camunda.com/browse/CAM-14721
ALTER TABLE ACT_RU_BATCH
    ADD COLUMN START_TIME_ datetime;

-- https://jira.camunda.com/browse/CAM-14722
ALTER TABLE ACT_RU_BATCH
    ADD COLUMN EXEC_START_TIME_ datetime;
ALTER TABLE ACT_HI_BATCH
    ADD COLUMN EXEC_START_TIME_ datetime;