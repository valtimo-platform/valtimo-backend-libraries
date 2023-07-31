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

import com.ritense.valtimo.domain.choicefield.ChoiceFieldValue;
import com.ritense.valtimo.service.ChoiceFieldValueService;
import com.ritense.valtimo.web.rest.dto.ChoiceFieldValueCreateRequestDTO;
import com.ritense.valtimo.web.rest.dto.ChoiceFieldValueUpdateRequestDTO;
import com.ritense.valtimo.web.rest.util.HeaderUtil;
import com.ritense.valtimo.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class ChoiceFieldValueResource {

    private static final Logger logger = LoggerFactory.getLogger(ChoiceFieldValueResource.class);
    private final ChoiceFieldValueService choiceFieldValueService;
    private final static String CHOICE_FIELD_VALUE = "choiceFieldValue";

    public ChoiceFieldValueResource(
        ChoiceFieldValueService choiceFieldValueService
    ) {
        this.choiceFieldValueService = choiceFieldValueService;
    }

    @PostMapping("/v1/choice-field-values")
    public ResponseEntity<ChoiceFieldValue> createChoiceFieldValue(
        @Valid @RequestBody ChoiceFieldValueCreateRequestDTO requestDTO,
        @RequestParam("choice_field_name") String choiceFieldName
    ) throws URISyntaxException {
        logger.debug("REST request to create ChoiceFieldValue : {}", requestDTO);
        ChoiceFieldValue result = choiceFieldValueService.create(requestDTO, choiceFieldName);
        return ResponseEntity.created(new URI("/api/v1/choice-field-values/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(CHOICE_FIELD_VALUE, result.getName()))
            .body(result);
    }

    @PutMapping("/v1/choice-field-values")
    public ResponseEntity<ChoiceFieldValue> updateChoiceFieldValue(
        @Valid @RequestBody ChoiceFieldValueUpdateRequestDTO requestDTO,
        @RequestParam("choice_field_name") String choiceFieldName
    ) {
        logger.debug("REST request to update ChoiceFieldValue : {}", requestDTO);
        ChoiceFieldValue result = choiceFieldValueService.update(requestDTO, choiceFieldName);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(CHOICE_FIELD_VALUE, result.getId().toString()))
            .body(result);
    }

    @GetMapping("/v1/choice-field-values")
    public ResponseEntity<List<ChoiceFieldValue>> getAllChoiceFieldValues(Pageable pageable) {
        logger.debug("REST request to get a page of ChoiceFieldValues");
        final Page<ChoiceFieldValue> page = choiceFieldValueService.findAll(pageable);
        final HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/v1/choice-field-values");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/v1/choice-field-values/{id}")
    public ResponseEntity<ChoiceFieldValue> getChoiceFieldValue(@PathVariable Long id) {
        logger.debug("REST request to get ChoiceFieldValue : {}", id);
        return choiceFieldValueService.findOne(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/v1/choice-field-values/{id}")
    public ResponseEntity<Void> deleteChoiceFieldValue(@PathVariable Long id) {
        logger.debug("REST request to delete ChoiceFieldValue : {}", id);
        choiceFieldValueService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(CHOICE_FIELD_VALUE, id.toString())).build();
    }

    @GetMapping("/v1/choice-field-values/choice-field/{choicefield_name}/value/{value}")
    public ResponseEntity<ChoiceFieldValue> getChoiceFieldValuesByChoiceField(
        @PathVariable(name = "choicefield_name") String choiceFieldName,
        @PathVariable(name = "value") String value
    ) {
        logger.debug("REST request to get ChoiceFieldValue with choicefield name: {} and value: {}", choiceFieldName, value);
        final ChoiceFieldValue choiceFieldValue = choiceFieldValueService.findOneByChoiceFieldNameAndValue(choiceFieldName, value);
        return ResponseEntity.ok(choiceFieldValue);
    }

    @GetMapping("/v1/choice-field-values/{choice_field_name}/values")
    public ResponseEntity<List<ChoiceFieldValue>> getChoiceFieldValuesByChoiceField(
        Pageable pageable,
        @PathVariable(name = "choice_field_name") String choiceFieldName
    ) {
        logger.debug("REST request to get ChoiceField : {}", choiceFieldName);
        final Page<ChoiceFieldValue> page = choiceFieldValueService.findAllByChoiceFieldKeyName(pageable, choiceFieldName);
        final HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/v1/choice-field-values");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

}
