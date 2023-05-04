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

package com.ritense.formlink.web.rest.impl;

import com.ritense.formlink.domain.FormAssociation;
import com.ritense.formlink.domain.request.CreateFormAssociationRequest;
import com.ritense.formlink.domain.request.ModifyFormAssociationRequest;
import com.ritense.formlink.service.FormAssociationService;
import com.ritense.formlink.web.rest.FormAssociationManagementResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.UUID;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@Deprecated(since = "10.6.0", forRemoval = true)
@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class CamundaFormAssociationManagementResource implements FormAssociationManagementResource {

    private final FormAssociationService formAssociationService;

    public CamundaFormAssociationManagementResource(FormAssociationService formAssociationService) {
        this.formAssociationService = formAssociationService;
    }

    @Override
    @GetMapping(value = "/v1/form-association-management", params = {"processDefinitionKey"})
    public ResponseEntity<Collection<? extends FormAssociation>> getAll(@RequestParam String processDefinitionKey) {
        final var formAssociations = formAssociationService.getAllFormAssociations(
            processDefinitionKey
        );
        if (formAssociations != null) {
            return ResponseEntity.ok(formAssociations);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @GetMapping(value = "/v1/form-association-management/{formAssociationId}", params = {"processDefinitionKey"}, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<? extends FormAssociation> getFormAssociationById(
        @RequestParam String processDefinitionKey,
        @PathVariable String formAssociationId
    ) {
        return formAssociationService.getFormAssociationById(processDefinitionKey, UUID.fromString(formAssociationId))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    @Override
    @GetMapping(value = "/v1/form-association-management", params = {"processDefinitionKey", "formLinkId"}, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<? extends FormAssociation> getFormAssociationByFormLinkId(
        @RequestParam String processDefinitionKey,
        @RequestParam String formLinkId
    ) {
        return formAssociationService.getFormAssociationByFormLinkId(processDefinitionKey, formLinkId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    @Override
    @PostMapping(value = "/v1/form-association-management", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FormAssociation> createFormAssociation(@Valid @RequestBody CreateFormAssociationRequest request) {
        return parseResult(formAssociationService.createFormAssociation(request));
    }

    @Override
    @PutMapping(value = "/v1/form-association-management", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FormAssociation> modifyFormAssociation(@Valid @RequestBody ModifyFormAssociationRequest request) {
        return parseResult(formAssociationService.modifyFormAssociation(request));
    }

    @Override
    @DeleteMapping("/v1/form-association-management/{processDefinitionKey}/{formAssociationId}")
    public ResponseEntity<Void> deleteFormAssociation(
        @PathVariable String processDefinitionKey,
        @PathVariable String formAssociationId
    ) {
        formAssociationService.deleteFormAssociation(processDefinitionKey, UUID.fromString(formAssociationId));
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<FormAssociation> parseResult(FormAssociation formAssociation) {
        if (formAssociation != null) {
            return ResponseEntity.ok(formAssociation);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

}
