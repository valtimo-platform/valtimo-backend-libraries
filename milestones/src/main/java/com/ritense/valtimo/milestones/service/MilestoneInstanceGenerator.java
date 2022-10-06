/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

import com.ritense.valtimo.milestones.domain.Milestone;
import com.ritense.valtimo.milestones.domain.MilestoneInstance;
import com.ritense.valtimo.milestones.domain.MilestoneSet;
import com.ritense.valtimo.milestones.repository.MilestoneInstanceRepository;
import com.ritense.valtimo.milestones.repository.MilestoneRepository;
import com.ritense.valtimo.milestones.service.exception.DuplicateMilestoneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class MilestoneInstanceGenerator {

    private static final Logger logger = LoggerFactory.getLogger(MilestoneInstanceGenerator.class);
    private final MilestoneRepository milestoneRepository;
    private final MilestoneInstanceRepository milestoneInstanceRepository;

    public MilestoneInstanceGenerator(MilestoneRepository milestoneRepository, MilestoneInstanceRepository milestoneInstanceRepository) {
        this.milestoneRepository = milestoneRepository;
        this.milestoneInstanceRepository = milestoneInstanceRepository;
    }

    // TODO: Add option that also allows to generate milestones even when there are duplicates.
    public void generateMilestoneInstancesFromSetIgnoreOnDuplicate(
        final MilestoneSet milestoneSet,
        final String processInstanceId,
        final LocalDate referenceDate
    ) {
        logger.debug("Service request to generate milestone instances from milestone-set {}", milestoneSet);

        List<Milestone> milestones = milestoneRepository.findMilestonesByMilestoneSet(milestoneSet);

        try {
            for (Milestone milestone : milestones) {
                generateMilestoneInstance(milestone, processInstanceId, referenceDate);
            }
        } catch (DuplicateMilestoneException ignore) {
            // Ignore if the set is already loaded in
        }
    }

    private void generateMilestoneInstance(
        final Milestone milestone,
        final String processInstanceId,
        final LocalDate referenceDate
    ) throws DuplicateMilestoneException {
        logger.debug("Processing : {}", milestone.getTitle());

        if (milestoneInstanceAlreadyExists(processInstanceId, milestone)) {
            throw new DuplicateMilestoneException();
        }

        MilestoneInstance milestoneInstance = createMilestoneInstance(
            milestone,
            processInstanceId,
            referenceDate);

        milestoneInstance.calculateExpectedDate();

        //TODO: Save as batch to increase performance.
        milestoneInstanceRepository.save(milestoneInstance);
    }

    // TODO: Check if this should only be checked the first iteration. Prevents a lot of queries
    private boolean milestoneInstanceAlreadyExists(String processInstanceId, Milestone milestone) {
        MilestoneInstance existingMilestoneInstance =
            milestoneInstanceRepository.findByTaskDefinitionKeyAndProcessInstanceId(
                milestone.getTaskDefinitionKey(),
                processInstanceId
            );
        return existingMilestoneInstance != null;
    }

    private MilestoneInstance createMilestoneInstance(
        Milestone milestone,
        String processInstanceId,
        LocalDate referenceDate
    ) {
        return MilestoneInstance.create(
            milestone,
            processInstanceId,
            referenceDate,
            false,
            null,
            null
        );
    }

}
