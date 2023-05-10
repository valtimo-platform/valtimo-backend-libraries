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

package com.ritense.valtimo.milestones.service;

import com.ritense.valtimo.milestones.domain.MilestoneInstance;
import com.ritense.valtimo.milestones.domain.MilestoneSet;
import com.ritense.valtimo.milestones.repository.MilestoneInstanceRepository;
import com.ritense.valtimo.milestones.service.mapper.MilestoneInstanceMapper;
import com.ritense.valtimo.milestones.web.rest.dto.MilestoneInstanceDTO;
import org.springframework.cache.annotation.Cacheable;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public class MilestoneInstanceService {

    private final MilestoneInstanceRepository milestoneInstanceRepository;
    private final MilestoneInstanceMapper milestoneInstanceMapper;
    private final MilestoneInstanceGenerator milestoneInstanceGenerator;

    public MilestoneInstanceService(
        final MilestoneInstanceRepository milestoneInstanceRepository,
        final MilestoneInstanceMapper milestoneInstanceMapper,
        final MilestoneInstanceGenerator milestoneInstanceGenerator
    ) {
        this.milestoneInstanceRepository = milestoneInstanceRepository;
        this.milestoneInstanceMapper = milestoneInstanceMapper;
        this.milestoneInstanceGenerator = milestoneInstanceGenerator;
    }

    public List<MilestoneInstanceDTO> getAllMilestoneInstances() {
        return milestoneInstanceMapper.milestoneInstancesToMilestoneInstanceDtos(
            milestoneInstanceRepository.findAll());
    }

    /**
     * Returns the amount of milestones that are overdue for reporting purposes.
     * This is a total of ALL milestones, regardless of the milestone set, process instance, task, etc.
     */
    @Cacheable("milestones.overDueMilestoneCount")
    public int getOverdueMilestoneCount() {
        return milestoneInstanceRepository.countOverdueMilestones();
    }

    /**
     * Marks the milestone as completed and sets the reached date and whether it was reached in time or not.
     */
    public void completeMilestoneInstanceByActivityIfExists(String activityId, String processInstanceId) {
        MilestoneInstance milestoneInstance =
            milestoneInstanceRepository.findByTaskDefinitionKeyAndProcessInstanceId(
                activityId,
                processInstanceId);
        if (milestoneInstance == null) {
            return;
        }
        milestoneInstance.completeMilestone(ZonedDateTime.now(ZoneOffset.UTC));
        milestoneInstanceRepository.save(milestoneInstance);
    }

    public void generateMilestoneInstancesFromSetIgnoreOnDuplicate(
        final MilestoneSet milestoneSet,
        final String processInstanceId,
        final LocalDate referenceDate
    ) {
        milestoneInstanceGenerator.generateMilestoneInstancesFromSetIgnoreOnDuplicate(milestoneSet, processInstanceId, referenceDate);
    }

}