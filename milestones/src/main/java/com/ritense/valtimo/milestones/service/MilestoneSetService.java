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
import com.ritense.valtimo.milestones.service.exception.IllegalMilestoneSetDeletionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

public class MilestoneSetService {

    private static final Logger logger = LoggerFactory.getLogger(MilestoneSetService.class);
    private final MilestoneSetRepository milestoneSetRepository;
    private final MilestoneRepository milestoneRepository;

    public MilestoneSetService(MilestoneSetRepository milestoneSetRepository, MilestoneRepository milestoneRepository) {
        this.milestoneSetRepository = milestoneSetRepository;
        this.milestoneRepository = milestoneRepository;
    }

    public MilestoneSet saveMilestoneSet(MilestoneSet milestoneSet) {
        logger.debug("Service request to save milestone set {}", milestoneSet.getTitle());
        return milestoneSetRepository.save(milestoneSet);
    }

    public List<MilestoneSet> listMilestoneSets() {
        logger.debug("Service request to get all milestone sets");
        return milestoneSetRepository.findAll();
    }

    /**
     * Deletes the milestone set when it exists, otherwise it does nothing.
     */
    public void delete(Long id) {
        Optional<MilestoneSet> milestoneSet = milestoneSetRepository.findById(id);
        if (milestoneSet.isEmpty()) {
            return;
        }
        List<Milestone> milestones = milestoneRepository.findMilestonesByMilestoneSet(milestoneSet.get());
        if (milestones.size() == 0) {
            milestoneSetRepository.deleteById(id);
        } else {
            throw new IllegalMilestoneSetDeletionException();
        }
    }

}