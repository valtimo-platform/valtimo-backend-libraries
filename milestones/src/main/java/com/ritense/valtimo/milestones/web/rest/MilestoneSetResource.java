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

package com.ritense.valtimo.milestones.web.rest;

import com.ritense.valtimo.milestones.domain.MilestoneSet;
import com.ritense.valtimo.milestones.repository.MilestoneSetRepository;
import com.ritense.valtimo.milestones.service.MilestoneSetService;
import com.ritense.valtimo.milestones.web.rest.dto.MilestoneSetSaveDTO;
import com.ritense.valtimo.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class MilestoneSetResource {

    private static final String ENTITY_NAME = "milestoneSet";
    private static final Logger logger = LoggerFactory.getLogger(MilestoneSetResource.class);

    private final MilestoneSetService milestoneSetService;
    private final MilestoneSetRepository milestoneSetRepository;

    public MilestoneSetResource(MilestoneSetService milestoneSetService, MilestoneSetRepository milestoneSetRepository) {
        this.milestoneSetService = milestoneSetService;
        this.milestoneSetRepository = milestoneSetRepository;
    }

    @GetMapping("/v1/milestone-sets/{id}")
    public ResponseEntity<MilestoneSet> getMilestoneSet(@PathVariable Long id) {
        logger.debug("REST request to get Milestone set : {}", id);
        Optional<MilestoneSet> milestoneSetOptional = milestoneSetRepository.findById(id);
        return milestoneSetOptional
            .map(milestoneSet -> new ResponseEntity<>(milestoneSet, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/v1/milestone-sets")
    public ResponseEntity<List<MilestoneSet>> listMilestoneSets() {
        logger.debug("REST request to get all milestone sets");
        List<MilestoneSet> milestoneSetList = milestoneSetService.listMilestoneSets();
        return ResponseEntity.ok(milestoneSetList);
    }

    @PostMapping("/v1/milestone-sets")
    public ResponseEntity<MilestoneSet> saveMilestoneSet(@Valid @RequestBody MilestoneSetSaveDTO dto) {
        logger.debug("REST request to save Milestone set : {}", dto);

        boolean newMilestoneSet = dto.getId() == null;

        MilestoneSet savedMilestoneSet = milestoneSetService.saveMilestoneSet(dto);

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

    @DeleteMapping("/v1/milestone-sets/{id}")
    public ResponseEntity<Void> deleteMilestoneSet(@PathVariable Long id) {
        logger.debug("REST request to delete Milestone set : {}", id);
        milestoneSetService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
