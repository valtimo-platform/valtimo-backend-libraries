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

package com.ritense.valtimo.milestones.process;

import com.ritense.valtimo.milestones.domain.MilestoneSet;
import com.ritense.valtimo.milestones.repository.MilestoneSetRepository;
import com.ritense.valtimo.milestones.service.MilestoneInstanceService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import java.time.LocalDate;

public class MilestoneProcessActions {
    private final MilestoneSetRepository milestoneSetRepository;
    private final MilestoneInstanceService milestoneInstanceService;

    public MilestoneProcessActions(
        final MilestoneSetRepository milestoneSetRepository,
        final MilestoneInstanceService milestoneInstanceService
    ) {
        this.milestoneSetRepository = milestoneSetRepository;
        this.milestoneInstanceService = milestoneInstanceService;
    }

    /**
     * The {@link MilestoneInstanceService#generateMilestoneInstancesFromSetIgnoreOnDuplicate(MilestoneSet, String, LocalDate)}
     * that is called in this function
     * most likely should be called from the project itself so they can decide the right reference date.
     * TODO: Add version for both ignore on duplicate and not
     *
     * @param setTitle The name of the set.
     */
    public void generateMilestoneInstances(DelegateExecution delegateExecution, String setTitle) {
        MilestoneSet milestoneSet = milestoneSetRepository.getMilestoneSetByTitle(setTitle);
        milestoneInstanceService.generateMilestoneInstancesFromSetIgnoreOnDuplicate(
            milestoneSet,
            delegateExecution.getProcessInstanceId(),
            LocalDate.now());
    }

    /**
     * The {@link MilestoneInstanceService#generateMilestoneInstancesFromSetIgnoreOnDuplicate(MilestoneSet, String, LocalDate)}
     * that is called in this function
     * most likely should be called from the project itself so they can decide the right reference date.
     * TODO: Add version for both ignore on duplicate and not
     *
     * @param setTitle  The name of the set.
     * @param localDate The localDate in yyyy-MM-dd format. (This is returned by the {@link LocalDate#toString()}).
     */
    public void generateMilestoneInstances(DelegateExecution delegateExecution, String setTitle, String localDate) {
        MilestoneSet milestoneSet = milestoneSetRepository.getMilestoneSetByTitle(setTitle);
        milestoneInstanceService.generateMilestoneInstancesFromSetIgnoreOnDuplicate(
            milestoneSet,
            delegateExecution.getProcessInstanceId(),
            LocalDate.parse(localDate));
    }

    /**
     * THIS IS A TEMPORARILY SOLUTION
     * This should be a global execution listener.
     * TODO: Solve this
     */
    public void completeApplicableMilestones(DelegateExecution delegateExecution) {
        milestoneInstanceService.completeMilestoneInstanceByActivityIfExists(
            delegateExecution.getCurrentActivityId(),
            delegateExecution.getProcessInstanceId());
    }

}