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

package com.ritense.form.web.rest.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.form.service.FormLoaderService;
import com.ritense.form.web.rest.FormResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/form", produces = MediaType.APPLICATION_JSON_VALUE)
public class FormIoFormResource implements FormResource {

    private final FormLoaderService formLoaderService;

    public FormIoFormResource(FormLoaderService formLoaderService) {
        this.formLoaderService = formLoaderService;
    }

    @Override
    @GetMapping(value = "/{formDefinitionName}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<JsonNode> getFormByName(@PathVariable String formDefinitionName) {
        return formLoaderService.getFormDefinitionByName(formDefinitionName)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    @Override
    @GetMapping(value = "/{formDefinitionName}/document/{documentId}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<JsonNode> getFormPreFilled(
        @PathVariable String formDefinitionName,
        @PathVariable String documentId
    ) {
        return formLoaderService
            .getFormDefinitionByNamePreFilled(formDefinitionName, JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

}