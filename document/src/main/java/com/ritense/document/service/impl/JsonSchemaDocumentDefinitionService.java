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

package com.ritense.document.service.impl;

import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.exception.DocumentDefinitionNameMismatchException;
import com.ritense.document.exception.UnknownDocumentDefinitionException;
import com.ritense.document.repository.DocumentDefinitionRepository;
import com.ritense.document.service.DocumentDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Slf4j
@RequiredArgsConstructor
@Transactional
public class JsonSchemaDocumentDefinitionService implements DocumentDefinitionService {

    private static final String PATH = "classpath*:config/document/definition/*.json";

    private final ResourceLoader resourceLoader;
    private final DocumentDefinitionRepository<JsonSchemaDocumentDefinition> documentDefinitionRepository;

    @Override
    public Page<JsonSchemaDocumentDefinition> findAll(Pageable pageable) {
        return documentDefinitionRepository.findAll(pageable);
    }

    @Override
    public JsonSchemaDocumentDefinitionId findIdByNameAndVersion(String name, Long version) {
        if (version == null) {
            return findLatestByName(name)
                .orElseThrow(() -> new UnknownDocumentDefinitionException(name))
                .getId();
        } else {
            return JsonSchemaDocumentDefinitionId.existingId(name, version);
        }
    }

    @Override
    public Optional<JsonSchemaDocumentDefinition> findBy(DocumentDefinition.Id id) {
        return documentDefinitionRepository.findById(id);
    }

    @Override
    public Optional<JsonSchemaDocumentDefinition> findLatestByName(String documentDefinitionName) {
        return documentDefinitionRepository.findFirstByIdNameOrderByIdVersionDesc(documentDefinitionName);
    }

    public void deployAll() {
        logger.info("Deploy all schema's");
        try {
            final Resource[] resources = loadResources();
            for (Resource resource : resources) {
                if (resource.getFilename() != null) {
                    final var schema = JsonSchema.fromStream(resource.getInputStream());

                    if (!resource.getFilename().equals(schema.getSchema().getId() + ".json")) {
                        throw new DocumentDefinitionNameMismatchException(
                            "Id schema doesn't correspond with file name.");
                    }

                    deploy(schema);
                }
            }
        } catch (Exception e) {
            logger.error("Error deploying document schema's", e);
        }
    }

    public void deploy(JsonSchema schema) {
        final var definitionName = schema.getSchema().getId().replace(".schema", "");
        final var existingDefinition = findLatestByName(definitionName);
        var definitionId = JsonSchemaDocumentDefinitionId.newId(definitionName);
        if (existingDefinition.isPresent()) {
            if (JsonSchema.fromString(existingDefinition.get().schema().toString()).equals(schema)) {
                logger.info("Schema already deployed - {} - {} ", existingDefinition.get().getId(), schema.getSchema().getId());
                return;
            } else {
                definitionId = JsonSchemaDocumentDefinitionId.nextVersion(existingDefinition.get().id());
                logger.info("Schema changed. Deploying next version - {} - {} ", definitionId.toString(), schema.getSchema().getId());
            }
        }
        final var definition = new JsonSchemaDocumentDefinition(definitionId, schema);

        deploy(definition);
        logger.info("Deployed schema - {} - {} ", definitionId.toString(), schema.getSchema().getId());
    }

    public void deploy(JsonSchemaDocumentDefinition newDocumentDefinition) {
        assertArgumentNotNull(newDocumentDefinition, "documentDefinition is required");
        documentDefinitionRepository.findById(newDocumentDefinition.id())
            .ifPresentOrElse(
                documentDefinition -> {
                    if (!documentDefinition.equals(newDocumentDefinition)) {
                        throw new UnsupportedOperationException("Schema already deployed, cannot redeploy");
                    }
                }, () -> documentDefinitionRepository.saveAndFlush(newDocumentDefinition)
            );
    }

    private Resource[] loadResources() throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PATH);
    }

}