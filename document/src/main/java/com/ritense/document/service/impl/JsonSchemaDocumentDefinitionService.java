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

package com.ritense.document.service.impl;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.internal.path.ArrayPathToken;
import com.jayway.jsonpath.internal.path.CompiledPath;
import com.jayway.jsonpath.internal.path.FunctionPathToken;
import com.jayway.jsonpath.internal.path.PathCompiler;
import com.jayway.jsonpath.internal.path.PathToken;
import com.jayway.jsonpath.internal.path.PredicatePathToken;
import com.jayway.jsonpath.internal.path.RootPathToken;
import com.jayway.jsonpath.internal.path.ScanPathToken;
import com.jayway.jsonpath.internal.path.WildcardPathToken;
import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.EveritSchemaAllowsPropertyKt;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.exception.DocumentDefinitionDeploymentException;
import com.ritense.document.exception.UnknownDocumentDefinitionException;
import com.ritense.document.repository.DocumentDefinitionRepository;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.JsonSchemaDocumentDefinitionSpecification;
import com.ritense.document.service.result.DeployDocumentDefinitionResult;
import com.ritense.document.service.result.DeployDocumentDefinitionResultFailed;
import com.ritense.document.service.result.DeployDocumentDefinitionResultSucceeded;
import com.ritense.document.service.result.error.DocumentDefinitionError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import javax.validation.ValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import static com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.CREATE;
import static com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.DELETE;
import static com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.MODIFY;
import static com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.VIEW;
import static com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.VIEW_LIST;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Transactional
public class JsonSchemaDocumentDefinitionService implements DocumentDefinitionService {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaDocumentDefinitionService.class);
    private static final String PATH = "classpath*:config/document/definition/*.json";

    private final ResourceLoader resourceLoader;
    private final DocumentDefinitionRepository<JsonSchemaDocumentDefinition> documentDefinitionRepository;
    private final AuthorizationService authorizationService;

    public JsonSchemaDocumentDefinitionService(
        ResourceLoader resourceLoader,
        DocumentDefinitionRepository<JsonSchemaDocumentDefinition> documentDefinitionRepository,
        AuthorizationService authorizationService
    ) {
        this.resourceLoader = resourceLoader;
        this.documentDefinitionRepository = documentDefinitionRepository;
        this.authorizationService = authorizationService;
    }

    @Override
    public Page<JsonSchemaDocumentDefinition> findAll(Pageable pageable) {
        Specification<JsonSchemaDocumentDefinition> spec = authorizationService
            .getAuthorizationSpecification(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocumentDefinition.class,
                    VIEW_LIST,
                    null
                ),
                null
            ).and(
                JsonSchemaDocumentDefinitionSpecification.byLatestVersion()
            );
        return documentDefinitionRepository.findAll(spec, pageable);
    }

    @Override
    public Page<JsonSchemaDocumentDefinition> findAllForAdmin(Pageable pageable) {
        authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocumentDefinition.class,
                    Action.deny(),
                    null
                ));

        var spec = JsonSchemaDocumentDefinitionSpecification.byLatestVersion();
        return documentDefinitionRepository.findAll(spec, pageable);
    }

    @Override
    public JsonSchemaDocumentDefinitionId findIdByName(String name) {
        return findLatestByName(name)
            .orElseThrow(() -> new UnknownDocumentDefinitionException(name))
            .getId();
    }

    @Override
    public Optional<JsonSchemaDocumentDefinition> findBy(DocumentDefinition.Id id) {
        Optional<JsonSchemaDocumentDefinition> optionalDefinition = documentDefinitionRepository.findById(id);
        optionalDefinition.ifPresent(definition -> {
            authorizationService
                .requirePermission(
                    new EntityAuthorizationRequest<>(
                        JsonSchemaDocumentDefinition.class,
                        VIEW,
                        definition
                    )
                );
        });
        return optionalDefinition;
    }

    @Override
    public Optional<JsonSchemaDocumentDefinition> findLatestByName(String documentDefinitionName) {
        Optional<JsonSchemaDocumentDefinition> optionalDefinition = documentDefinitionRepository.findFirstByIdNameOrderByIdVersionDesc(
            documentDefinitionName);

        optionalDefinition.ifPresent(definition -> {
            authorizationService
                .requirePermission(
                    new EntityAuthorizationRequest<>(
                        JsonSchemaDocumentDefinition.class,
                        VIEW,
                        definition
                    )
                );
        });

        return optionalDefinition;
    }

    @Override
    public void deployAll() {
        //Authorization check is delegated to the store() method
        deployAll(true, false);
    }

    @Override
    public void deploy(InputStream inputStream) throws IOException {
        //Authorization check is delegated to the store() method
        deploy(inputStream, true, false);
    }

    @Override
    public DeployDocumentDefinitionResult deploy(String schema) {
        //Authorization check is delegated to the store() method
        return deploy(schema, false, false);
    }

    @Override
    public void deployAll(boolean readOnly, boolean force) {
        //Authorization check is delegated to the store() method
        logger.info("Deploy all schema's");
        try {
            final Resource[] resources = loadResources();
            for (Resource resource : resources) {
                if (resource.getFilename() != null) {
                    deploy(resource.getInputStream(), readOnly, force);
                }
            }
        } catch (Exception ex) {
            throw new DocumentDefinitionDeploymentException("Error deploying document schema's", ex);
        }
    }

    @Override
    public void deploy(InputStream inputStream, boolean readOnly, boolean force) throws IOException {
        //Authorization check is delegated to the store() method
        var jsonSchema = JsonSchema.fromString(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
        deploy(jsonSchema, readOnly, force);
    }

    @Override
    public DeployDocumentDefinitionResult deploy(String schema, boolean readOnly, boolean force) {
        //Authorization check is delegated to the store() method
        try {
            var jsonSchema = JsonSchema.fromString(schema);
            return deploy(jsonSchema, readOnly, force);
        } catch (AccessDeniedException accessDeniedException) {
            throw accessDeniedException;
        } catch (Exception ex) {
            DocumentDefinitionError error = ex::getMessage;
            return new DeployDocumentDefinitionResultFailed(List.of(error));
        }
    }

    private DeployDocumentDefinitionResult deploy(JsonSchema jsonSchema, boolean readOnly, boolean force) {
        //Authorization check is delegated to the store() method
        try {
            var documentDefinitionName = jsonSchema.getSchema().getId().replace(".schema", "");
            var existingDefinition = findLatestByName(documentDefinitionName);

            final JsonSchemaDocumentDefinitionId documentDefinitionId;
            if (existingDefinition.isPresent()) {
                var existingDocumentDefinition = existingDefinition.get();

                // Check read-only of previous definition
                if (existingDocumentDefinition.isReadOnly() && !force) {
                    DocumentDefinitionError error = () -> "This schema cannot be updated, because its readonly in previous versions";
                    return new DeployDocumentDefinitionResultFailed(List.of(error));
                }

                if (existingDocumentDefinition.getSchema().equals(jsonSchema)) {
                    logger.info("Schema already deployed - {} - {} ", existingDocumentDefinition.getId(), jsonSchema.getSchema().getId());
                    DocumentDefinitionError error = () -> "This exact schema is already deployed";
                    return new DeployDocumentDefinitionResultFailed(List.of(error));
                } else {
                    // Definition changed increase version
                    documentDefinitionId = JsonSchemaDocumentDefinitionId.nextVersion(existingDocumentDefinition.id());
                    logger.info("Schema changed. Deploying next version - {} - {} ", documentDefinitionId, jsonSchema.getSchema().getId());
                }
            } else {
                documentDefinitionId = JsonSchemaDocumentDefinitionId.newId(documentDefinitionName);
            }

            var documentDefinition = new JsonSchemaDocumentDefinition(documentDefinitionId, jsonSchema);

            if (readOnly) {
                documentDefinition.markReadOnly();
            }

            store(documentDefinition);
            logger.info("Deployed schema - {} - {} ", documentDefinitionId, jsonSchema.getSchema().getId());
            return new DeployDocumentDefinitionResultSucceeded(documentDefinition);
        } catch (AccessDeniedException accessDeniedException) {
            throw accessDeniedException;
        } catch (Exception ex) {
            DocumentDefinitionError error = ex::getMessage;
            logger.warn(ex.getMessage());
            return new DeployDocumentDefinitionResultFailed(List.of(error));
        }
    }

    @Override
    public void store(JsonSchemaDocumentDefinition documentDefinition) {
        assertArgumentNotNull(documentDefinition,   "documentDefinition is required");

        Optional<JsonSchemaDocumentDefinition> optionalDefinition = documentDefinitionRepository.findById(documentDefinition.id());
        // So much TODO:, I've got so much TODO:
        // - get the latest definition instead of the versioned one?
        // - check if new version is incremental?
        // - clean up this optional structure
        optionalDefinition.ifPresentOrElse(
                existingDocumentDefinition -> {
                    if (!existingDocumentDefinition.equals(documentDefinition)) {
                        throw new UnsupportedOperationException("Schema already deployed, will cannot redeploy");
                    }
                }, () -> {
                    JsonSchemaDocumentDefinitionId latestDefinitionId = documentDefinitionRepository.findFirstByIdNameOrderByIdVersionDesc(
                        documentDefinition.id().name()).map(JsonSchemaDocumentDefinition::getId).orElse(null);
                    authorizationService.requirePermission(
                            new EntityAuthorizationRequest<>(
                                JsonSchemaDocumentDefinition.class,
                                latestDefinitionId == null ? CREATE : MODIFY,
                                documentDefinition
                            )
                    );

                    documentDefinitionRepository.saveAndFlush(documentDefinition);
                }
            );
    }

    @Override
    public void removeDocumentDefinition(String documentDefinitionName) {
        findLatestByName(documentDefinitionName).ifPresent(documentDefinition -> {
            authorizationService
                .requirePermission(
                    new EntityAuthorizationRequest<>(
                        JsonSchemaDocumentDefinition.class,
                        DELETE,
                        documentDefinition
                    )
                );
        });

        documentDefinitionRepository.deleteByIdName(documentDefinitionName);
    }


    @Override
    public boolean currentUserCanAccessDocumentDefinition(String documentDefinitionName) {
        return findLatestByName(documentDefinitionName)
            .map(documentDefinition -> authorizationService.hasPermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocumentDefinition.class,
                    VIEW,
                    documentDefinition
                )
            )).orElse(false);
    }

    @Override
    public void validateJsonPath(String documentDefinitionName, String jsonPathExpression) {
        var definition = findLatestByName(documentDefinitionName)
            .orElseThrow(() -> new UnknownDocumentDefinitionException(documentDefinitionName));
        if (jsonPathExpression.startsWith("doc:")) {
            jsonPathExpression = "$." + jsonPathExpression.substring("doc:".length());
        } else if (jsonPathExpression.startsWith("case:")) {
            return;
        }
        try {
            PathCompiler.compile(jsonPathExpression);
        } catch (InvalidPathException e) {
            throw new ValidationException("Failed to compile JsonPath '" + jsonPathExpression + "' for document definition '" + documentDefinitionName + "'", e);
        }
        if (!isValidJsonPath(definition, jsonPathExpression)) {
            throw new ValidationException("JsonPath '" + jsonPathExpression + "' doesn't point to any property inside document definition '" + documentDefinitionName + "'");
        }
    }

    @Override
    public boolean isValidJsonPath(JsonSchemaDocumentDefinition definition, String jsonPathExpression) {
        CompiledPath compiledJsonPath;
        try {
            compiledJsonPath = (CompiledPath) PathCompiler.compile(jsonPathExpression);
        } catch (InvalidPathException e) {
            logger.error("Failed to compile JsonPath '{}' for document definition '{}'", jsonPathExpression, definition.id().name(), e);
            return false;
        }
        var jsonPointer = toJsonPointerRecursive(compiledJsonPath.getRoot());
        return isValidJsonPointer(definition, jsonPointer);
    }

    @Override
    public void validateJsonPointer(String documentDefinitionName, String jsonPointer) {
        var definition = findLatestByName(documentDefinitionName)
            .orElseThrow(() -> new UnknownDocumentDefinitionException(documentDefinitionName));
        if (!isValidJsonPointer(definition, jsonPointer)) {
            throw new ValidationException("JsonPointer '" + jsonPointer + "' doesn't point to any property inside document definition '" + documentDefinitionName + "'");
        }
    }

    private boolean isValidJsonPointer(JsonSchemaDocumentDefinition definition, String jsonPointer) {
        return EveritSchemaAllowsPropertyKt.allowsProperty(definition.getSchema().getSchema(), jsonPointer);
    }

    private String toJsonPointerRecursive(PathToken pathToken) {
        var jsonPointerPart = toJsonPointerPart(pathToken);
        if (jsonPointerPart == null) {
            return "";
        } else {
            return jsonPointerPart + toJsonPointerRecursive(pathToken.getNext());
        }
    }

    private String toJsonPointerPart(PathToken pathToken) {
        if (pathToken == null
            || pathToken instanceof PredicatePathToken
            || pathToken instanceof WildcardPathToken
            || pathToken instanceof FunctionPathToken
            || pathToken instanceof ScanPathToken) {
            return null;
        } else if (pathToken instanceof RootPathToken) {
            return "";
        } else if (pathToken instanceof ArrayPathToken) {
            return "/0";
        } else {
            return "/" + trim(getPathFragment(pathToken), "['", "']", "[", "]");
        }
    }

    private String getPathFragment(PathToken pathToken) {
        try {
            var getPathFragmentMethod = PathToken.class.getDeclaredMethod("getPathFragment");
            getPathFragmentMethod.setAccessible(true);
            return (String) getPathFragmentMethod.invoke(pathToken);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Broken method. The Jayway JsonPath library might've changed.", e);
        }
    }

    private String trim(String value, String... trims) {
        for (var trim : trims) {
            if (value.startsWith(trim)) {
                value = value.substring(trim.length());
            }
            if (value.endsWith(trim)) {
                value = value.substring(0, value.length() - trim.length());
            }
        }
        return value;
    }

    private Resource[] loadResources() throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PATH);
    }

}
