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
import com.ritense.valtimo.milestones.domain.MilestoneSet;
import com.ritense.valtimo.milestones.repository.MilestoneRepository;
import com.ritense.valtimo.milestones.repository.MilestoneSetRepository;
import com.ritense.valtimo.milestones.service.exception.MultipleProcessesWithinMilestoneSetException;
import com.ritense.valtimo.milestones.service.mapper.MilestoneMapper;
import com.ritense.valtimo.milestones.web.rest.dto.MilestoneDTO;
import com.ritense.valtimo.milestones.web.rest.dto.MilestoneSaveDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

public class MilestoneService {

    private static final Logger logger = LoggerFactory.getLogger(MilestoneService.class);
    private final MilestoneSetRepository milestoneSetRepository;
    private final MilestoneRepository milestoneRepository;
    private final MilestoneMapper milestoneMapper;

    public MilestoneService(MilestoneSetRepository milestoneSetRepository, MilestoneRepository milestoneRepository, MilestoneMapper milestoneMapper) {
        this.milestoneSetRepository = milestoneSetRepository;
        this.milestoneRepository = milestoneRepository;
        this.milestoneMapper = milestoneMapper;
    }

    /**
     * Saves the milestone. It creates a new one if the id is not present, otherwise it updates the existing one.
     *
     * @return DTO Of the saved entity
     * @throws MultipleProcessesWithinMilestoneSetException when trying to add a {@link Milestone}
     *                                                      to a {@link com.ritense.valtimo.milestones.domain.MilestoneSet}
     *                                                      when another milestone in the same set is already attached to a different process instance.
     * @throws IllegalStateException                        when the milestoneSaveDTO has an id who's entity can't be found.
     * @throws IllegalArgumentException                     When the milestone set cannot be found
     */
    public MilestoneDTO saveMilestone(MilestoneSaveDTO milestoneSaveDTO)
        throws MultipleProcessesWithinMilestoneSetException, IllegalStateException, IllegalArgumentException {
        logger.debug("Service request to update milestone {}", milestoneSaveDTO);

        final MilestoneSet targetMilestoneSet = getMilestoneSet(milestoneSaveDTO);

        assertSingleProcessWithinMilestoneSet(milestoneSaveDTO);

        final Milestone milestone = getMilestone(milestoneSaveDTO.getId());

        milestone.setTitle(milestoneSaveDTO.getTitle());
        milestone.setProcessDefinitionKey(milestoneSaveDTO.getProcessDefinitionKey());
        milestone.setTaskDefinitionKey(milestoneSaveDTO.getTaskDefinitionKey());
        milestone.setPlannedIntervalInDays(milestoneSaveDTO.getPlannedIntervalInDays());
        milestone.setColor(milestoneSaveDTO.getColor());
        milestone.setMilestoneSet(targetMilestoneSet);

        Milestone savedMilestone = milestoneRepository.save(milestone);
        return milestoneMapper.milestoneToMilestoneDTO(savedMilestone);
    }

    private MilestoneSet getMilestoneSet(MilestoneSaveDTO milestoneSaveDTO) {
        MilestoneSet targetMilestoneSet = milestoneSetRepository.getMilestoneSetById(milestoneSaveDTO.getMilestoneSet());

        if (targetMilestoneSet == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Unable to get the milestone set with id %s while saving milestone %s",
                    milestoneSaveDTO.getMilestoneSet(),
                    milestoneSaveDTO.getTitle()));
        }

        return targetMilestoneSet;
    }

    private Milestone getMilestone(Long id) throws IllegalStateException {
        if (id == null) {
            return new Milestone();
        }

        Optional<Milestone> milestone = milestoneRepository.findById(id);

        if (milestone.isEmpty()) {
            throw new IllegalStateException("Unable to find a milestone with the provided id: " + id);
        }

        return milestone.get();
    }

    private void assertSingleProcessWithinMilestoneSet(MilestoneSaveDTO milestoneSaveDTO) throws MultipleProcessesWithinMilestoneSetException {
        Optional<Milestone> existingMilestoneInSet = milestoneRepository.findFirstByMilestoneSetId(milestoneSaveDTO.getMilestoneSet());

        if (existingMilestoneInSet.isPresent()
            && !existingMilestoneInSet.get().getProcessDefinitionKey().equals(milestoneSaveDTO.getProcessDefinitionKey())) {
            throw new MultipleProcessesWithinMilestoneSetException(
                "Milestones within a set must all have the same process. " +
                    "Selected process differs from existing milestones in this Set.",
                "incorrectProcessSelected",
                "Milestone");
        }
    }

    public List<MilestoneDTO> listMilestones() {
        logger.debug("Service request to get all milestones");
        return milestoneMapper.milestonesToMilestoneDtos(milestoneRepository.findAll());
    }

    /**
     * Deletes the milestone when it exists, otherwise it does nothing.
     */
    public void delete(Long id) {
        Optional<Milestone> milestone = milestoneRepository.findById(id);

        if (milestone.isPresent()) {
            milestoneRepository.deleteById(id);
        }
    }
}
