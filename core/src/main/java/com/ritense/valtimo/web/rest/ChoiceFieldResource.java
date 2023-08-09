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

package com.ritense.valtimo.web.rest;

import com.ritense.valtimo.domain.choicefield.ChoiceField;
import com.ritense.valtimo.service.ChoiceFieldService;
import com.ritense.valtimo.web.rest.dto.ChoiceFieldCreateRequestDTO;
import com.ritense.valtimo.web.rest.dto.ChoiceFieldDTO;
import com.ritense.valtimo.web.rest.dto.ChoiceFieldUpdateRequestDTO;
import com.ritense.valtimo.web.rest.util.HeaderUtil;
import com.ritense.valtimo.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class ChoiceFieldResource {

    private static final Logger logger = LoggerFactory.getLogger(ChoiceFieldResource.class);
    private static final String CHOICEFIELD_ENTITY_NAME = "choiceField";
    private final ChoiceFieldService choiceFieldService;

    public ChoiceFieldResource(ChoiceFieldService choiceFieldService) {
        this.choiceFieldService = choiceFieldService;
    }

    @PostMapping("/v1/choice-fields")
    public ResponseEntity<ChoiceField> createChoiceField(@Valid @RequestBody ChoiceFieldCreateRequestDTO choiceFieldCreateRequestDTO) throws URISyntaxException {
        logger.debug("REST request to save ChoiceField : {}", choiceFieldCreateRequestDTO);
        ChoiceField result = choiceFieldService.create(choiceFieldCreateRequestDTO);
        return ResponseEntity.created(new URI("/api/v1/choice-fields/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(CHOICEFIELD_ENTITY_NAME, result.getKeyName()))
            .body(result);
    }

    @PutMapping("/v1/choice-fields")
    public ResponseEntity<ChoiceField> updateChoiceField(@Valid @RequestBody ChoiceFieldUpdateRequestDTO choiceFieldUpdateRequestDTO) {
        logger.debug("REST request to update ChoiceField : {}", choiceFieldUpdateRequestDTO);
        ChoiceField result = choiceFieldService.update(choiceFieldUpdateRequestDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(CHOICEFIELD_ENTITY_NAME, result.getKeyName()))
            .body(result);
    }

    @GetMapping("/v1/choice-fields")
    public ResponseEntity<List<ChoiceField>> getAllChoiceFields(Pageable pageable) {
        logger.debug("REST request to get a page of ChoiceFields");
        Page<ChoiceField> page = choiceFieldService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/v1/choice-fields");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/v1/choice-fields/{id}")
    public ResponseEntity<ChoiceField> getChoiceField(@PathVariable Long id) {
        logger.debug("REST request to get ChoiceField : {}", id);
        Optional<ChoiceField> choiceFieldOptional = choiceFieldService.findOneById(id);
        return choiceFieldOptional
            .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/v1/choice-fields/name/{name}")
    public ResponseEntity<ChoiceFieldDTO> getChoiceFieldByName(@PathVariable String name) {
        logger.debug("REST request to get ChoiceField : {}", name);
        ChoiceFieldDTO choiceFieldDTO = choiceFieldService.findOneByName(name);
        return Optional.ofNullable(choiceFieldDTO)
            .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/v1/choice-fields/{id}")
    public ResponseEntity<Void> deleteChoiceField(@PathVariable Long id) {
        logger.debug("REST request to delete ChoiceField : {}", id);
        choiceFieldService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(CHOICEFIELD_ENTITY_NAME, id.toString())).build();
    }

}
