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

package com.ritense.form.web.rest.impl;

import com.ritense.form.domain.FormDefinition;
import com.ritense.form.domain.request.CreateFormDefinitionRequest;
import com.ritense.form.domain.request.ModifyFormDefinitionRequest;
import com.ritense.form.service.FormDefinitionService;
import com.ritense.form.web.rest.FormManagementResource;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public class FormIoFormManagementResource implements FormManagementResource {

    private final FormDefinitionService formDefinitionService;

    public FormIoFormManagementResource(FormDefinitionService formDefinitionService) {
        this.formDefinitionService = formDefinitionService;
    }

    @Override
    public ResponseEntity<Page<? extends FormDefinition>> getAll(Pageable pageable) {
        return ResponseEntity.ok(formDefinitionService.getAllFormOptions(pageable));
    }

    @Override
    public ResponseEntity<Page<? extends FormDefinition>> queryFormDefinitions(@RequestParam String searchTerm, Pageable pageable) {
        return ResponseEntity.ok(formDefinitionService.queryFormDefinitions(searchTerm, pageable));
    }

    @Override
    public ResponseEntity<? extends FormDefinition> getFormDefinitionById(@PathVariable String formDefinitionId) {
        return formDefinitionService.getFormDefinitionById(UUID.fromString(formDefinitionId))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<? extends Boolean> getExistsByName(@PathVariable String name) {
        return ResponseEntity.ok(formDefinitionService.getFormDefinitionByName(name).isPresent());
    }

    @Override
    public ResponseEntity<? extends FormDefinition> createFormDefinition(@Valid @RequestBody CreateFormDefinitionRequest request) {
        return parseResult(formDefinitionService.createFormDefinition(request));
    }

    @Override
    public ResponseEntity<? extends FormDefinition> modifyFormDefinition(@Valid @RequestBody ModifyFormDefinitionRequest request) {
        return parseResult(formDefinitionService.modifyFormDefinition(request));
    }

    @Override
    public ResponseEntity<Void> deleteFormDefinition(@PathVariable String formDefinitionId) {
        formDefinitionService.deleteFormDefinition(UUID.fromString(formDefinitionId));
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<FormDefinition> parseResult(FormDefinition formDefinition) {
        if (formDefinition != null) {
            return ResponseEntity.ok(formDefinition);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

}