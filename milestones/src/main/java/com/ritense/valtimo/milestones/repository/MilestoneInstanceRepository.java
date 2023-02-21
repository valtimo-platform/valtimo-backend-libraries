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

package com.ritense.valtimo.milestones.repository;

import com.ritense.valtimo.milestones.domain.MilestoneInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MilestoneInstanceRepository extends JpaRepository<MilestoneInstance, Long> {
    MilestoneInstance findByTaskDefinitionKeyAndProcessInstanceId(String taskDefinitionKey, String processInstanceId);

    List<MilestoneInstance> findAllByProcessInstanceIdIn(List<String> processInstanceIds);

    @Query("SELECT COUNT(mi) FROM MilestoneInstance mi WHERE mi.reached = false and mi.expectedDate < current_date")
    Integer countOverdueMilestones();
}
