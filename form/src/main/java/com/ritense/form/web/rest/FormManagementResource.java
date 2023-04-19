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

package com.ritense.form.web.rest;

import com.ritense.form.domain.FormDefinition;
import com.ritense.form.domain.request.CreateFormDefinitionRequest;
import com.ritense.form.domain.request.ModifyFormDefinitionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public interface FormManagementResource {

    @GetMapping("/v1/form-management")
    ResponseEntity<Page<? extends FormDefinition>> getAll(Pageable pageable);

    @GetMapping(value = "/v1/form-management", params = {"searchTerm"})
    ResponseEntity<Page<? extends FormDefinition>> queryFormDefinitions(@RequestParam("searchTerm") String searchTerm, Pageable pageable);

    @GetMapping(value = "/v1/form-management/{formDefinitionId}", consumes = MediaType.ALL_VALUE)
    ResponseEntity<? extends FormDefinition> getFormDefinitionById(String formDefinitionId);

    @GetMapping(value = "/v1/form-management/exists/{name}", consumes = MediaType.ALL_VALUE)
    ResponseEntity<? extends Boolean> getExistsByName(String name);

    @PostMapping(value = "/v1/form-management", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<? extends FormDefinition> createFormDefinition(CreateFormDefinitionRequest request);

    @PutMapping(value = "/v1/form-management", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<? extends FormDefinition> modifyFormDefinition(ModifyFormDefinitionRequest request);

    @DeleteMapping("/v1/form-management/{formDefinitionId}")
    ResponseEntity<Void> deleteFormDefinition(String formDefinitionId);

}
