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

package com.ritense.form.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.service.DocumentService;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.repository.FormDefinitionRepository;
import com.ritense.form.service.FormLoaderService;
import java.util.Optional;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class FormIoFormLoaderService implements FormLoaderService {

    private final DocumentService<JsonSchemaDocument> documentService;
    private final FormDefinitionRepository formDefinitionRepository;

    public FormIoFormLoaderService(
        final DocumentService<JsonSchemaDocument> documentService,
        final FormDefinitionRepository formDefinitionRepository
    ) {
        this.documentService = documentService;
        this.formDefinitionRepository = formDefinitionRepository;
    }

    @Override
    public Optional<JsonNode> getFormDefinitionByName(final String formDefinitionName) {
        assertArgumentNotNull(formDefinitionName, "formDefinitionName is required");
        return formDefinitionRepository.findByName(formDefinitionName).map(FormIoFormDefinition::asJson);
    }

    @Override
    public Optional<JsonNode> getFormDefinitionByNamePreFilled(final String formDefinitionName, final Document.Id documentId) {
        assertArgumentNotNull(documentId, "documentId is required");
        return AuthorizationContext.runWithoutAuthorization(() -> documentService.findBy(documentId))
            .flatMap(jsonSchemaDocument -> formDefinitionRepository.findByName(formDefinitionName)
                .map(formIoFormDefinition -> {
                    formIoFormDefinition.preFill(jsonSchemaDocument.content().asJson());
                    return formIoFormDefinition.asJson();
                })
            );
    }

}
