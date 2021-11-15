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

package com.ritense.valtimo.milestones.web.rest;

import com.ritense.valtimo.milestones.domain.MilestoneSet;
import com.ritense.valtimo.milestones.repository.MilestoneSetRepository;
import com.ritense.valtimo.milestones.service.MilestoneSetService;
import com.ritense.valtimo.web.rest.util.HeaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class MilestoneSetResource {

    private static final String ENTITY_NAME = "milestoneSet";

    private final MilestoneSetService milestoneSetService;
    private final MilestoneSetRepository milestoneSetRepository;

    @GetMapping("/milestone-sets/{id}")
    public ResponseEntity<MilestoneSet> getMilestoneSet(@PathVariable Long id) {
        logger.debug("REST request to get Milestone set : {}", id);
        Optional<MilestoneSet> milestoneSetOptional = milestoneSetRepository.findById(id);
        return milestoneSetOptional
            .map(milestoneSet -> new ResponseEntity<>(milestoneSet, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/milestone-sets")
    public ResponseEntity<List<MilestoneSet>> listMilestoneSets() {
        logger.debug("REST request to get all milestone sets");
        List<MilestoneSet> milestoneSetList = milestoneSetService.listMilestoneSets();
        return ResponseEntity.ok(milestoneSetList);
    }

    @PostMapping("/milestone-sets")
    public ResponseEntity<MilestoneSet> saveMilestoneSet(@Valid @RequestBody MilestoneSet milestoneSet) {
        logger.debug("REST request to save Milestone set : {}", milestoneSet);

        boolean newMilestoneSet = false;
        if (milestoneSet.getId() == null) {
            newMilestoneSet = true;
        }

        MilestoneSet savedMilestoneSet = milestoneSetService.saveMilestoneSet(milestoneSet);

        if (newMilestoneSet && savedMilestoneSet.getId() != null) {
            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, savedMilestoneSet.getId().toString()))
                .body(savedMilestoneSet);
        } else {
            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, savedMilestoneSet.getId().toString()))
                .body(savedMilestoneSet);
        }

    }

    @DeleteMapping("/milestone-sets/{id}")
    public ResponseEntity<Void> deleteMilestoneSet(@PathVariable Long id) {
        logger.debug("REST request to delete Milestone set : {}", id);
        milestoneSetService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}