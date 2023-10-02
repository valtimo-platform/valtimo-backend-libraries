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

import com.ritense.valtimo.milestones.domain.Milestone;
import com.ritense.valtimo.milestones.repository.MilestoneRepository;
import com.ritense.valtimo.milestones.service.MilestoneService;
import com.ritense.valtimo.milestones.service.mapper.MilestoneMapper;
import com.ritense.valtimo.milestones.web.rest.dto.MilestoneDTO;
import com.ritense.valtimo.milestones.web.rest.dto.MilestoneSaveDTO;
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
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class MilestoneResource {

    private static final String ENTITY_NAME = "milestone";
    private static final Logger logger = LoggerFactory.getLogger(MilestoneResource.class);

    private final MilestoneService milestoneService;
    private final MilestoneRepository milestoneRepository;
    private final MilestoneMapper milestoneMapper;

    public MilestoneResource(MilestoneService milestoneService, MilestoneRepository milestoneRepository, MilestoneMapper milestoneMapper) {
        this.milestoneService = milestoneService;
        this.milestoneRepository = milestoneRepository;
        this.milestoneMapper = milestoneMapper;
    }

    @GetMapping("/v1/milestones/{id}")
    public ResponseEntity<MilestoneDTO> getMilestone(@PathVariable Long id) {
        logger.debug("REST request to get Milestone : {}", id);
        Optional<Milestone> milestone = milestoneRepository.findById(id);
        return milestone
            .map(milestoneMapper::milestoneToMilestoneDTO)
            .map(milestoneDTO -> new ResponseEntity<>(milestoneDTO, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/v1/milestones")
    public ResponseEntity<List<MilestoneDTO>> listMilestones() {
        logger.debug("REST request to get all milestones");
        List<MilestoneDTO> milestoneDTOList = milestoneService.listMilestones();
        return ResponseEntity.ok(milestoneDTOList);
    }

    @PostMapping("/v1/milestones")
    public ResponseEntity<MilestoneDTO> saveMilestone(@Valid @RequestBody MilestoneSaveDTO milestoneSaveDTO) throws Exception {
        logger.debug("REST request to save Milestone : {}", milestoneSaveDTO);
        MilestoneDTO savedMilestoneDTO;
        try {
            savedMilestoneDTO = milestoneService.saveMilestone(milestoneSaveDTO);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, savedMilestoneDTO.getId().toString()))
            .body(savedMilestoneDTO);
    }

    @DeleteMapping("/v1/milestones/{id}")
    public ResponseEntity<Void> deleteMilestone(@PathVariable Long id) throws IllegalStateException {
        logger.debug("REST request to delete Milestone : {}", id);
        milestoneService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
