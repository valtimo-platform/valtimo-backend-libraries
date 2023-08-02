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
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.EveritSchemaAllowsPropertyKt;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionRole;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionRoleId;
import com.ritense.document.exception.DocumentDefinitionDeploymentException;
import com.ritense.document.exception.UnknownDocumentDefinitionException;
import com.ritense.document.repository.DocumentDefinitionRepository;
import com.ritense.document.repository.DocumentDefinitionRoleRepository;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.result.DeployDocumentDefinitionResult;
import com.ritense.document.service.result.DeployDocumentDefinitionResultFailed;
import com.ritense.document.service.result.DeployDocumentDefinitionResultSucceeded;
import com.ritense.document.service.result.error.DocumentDefinitionError;
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import javax.validation.ValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Transactional
public class JsonSchemaDocumentDefinitionService implements DocumentDefinitionService<JsonSchemaDocumentDefinition> {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaDocumentDefinitionService.class);
    private static final String PATH = "classpath*:config/document/definition/*.json";

    private final ResourceLoader resourceLoader;
    private final DocumentDefinitionRepository<JsonSchemaDocumentDefinition> documentDefinitionRepository;
    private final DocumentDefinitionRoleRepository<JsonSchemaDocumentDefinitionRole> documentDefinitionRoleRepository;

    public JsonSchemaDocumentDefinitionService(ResourceLoader resourceLoader, DocumentDefinitionRepository<JsonSchemaDocumentDefinition> documentDefinitionRepository, DocumentDefinitionRoleRepository<JsonSchemaDocumentDefinitionRole> documentDefinitionRoleRepository) {
        this.resourceLoader = resourceLoader;
        this.documentDefinitionRepository = documentDefinitionRepository;
        this.documentDefinitionRoleRepository = documentDefinitionRoleRepository;
    }

    @Override
    public Page<JsonSchemaDocumentDefinition> findAll(Pageable pageable) {
        return documentDefinitionRepository.findAll(pageable);
    }

    @Override
    public Page<JsonSchemaDocumentDefinition> findForUser(boolean filteredOnRole, Pageable pageable) {
        List<String> roles = SecurityUtils.getCurrentUserRoles();
        if (!filteredOnRole && roles.contains(AuthoritiesConstants.ADMIN)) {
            return documentDefinitionRepository.findAll(pageable);
        } else {
            return documentDefinitionRepository.findAllForRoles(roles, pageable);
        }
    }

    @Override
    public JsonSchemaDocumentDefinitionId findIdByName(String name) {
        return findLatestByName(name)
            .orElseThrow(() -> new UnknownDocumentDefinitionException(name))
            .getId();
    }

    @Override
    public Optional<JsonSchemaDocumentDefinition> findBy(DocumentDefinition.Id id) {
        return documentDefinitionRepository.findById(id);
    }

    @Override
    public Optional<JsonSchemaDocumentDefinition> findLatestByName(String documentDefinitionName) {
        return documentDefinitionRepository.findFirstByIdNameOrderByIdVersionDesc(documentDefinitionName);
    }

    @Override
    public void deployAll() {
        deployAll(true, false);
    }

    @Override
    public void deploy(InputStream inputStream) throws IOException {
        deploy(inputStream, true, false);
    }

    @Override
    public DeployDocumentDefinitionResult<JsonSchemaDocumentDefinition> deploy(String schema) {
        return deploy(schema, false, false);
    }

    @Override
    public void deployAll(boolean readOnly, boolean force) {
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
        var jsonSchema = JsonSchema.fromString(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
        deploy(jsonSchema, readOnly, force);
    }

    @Override
    public DeployDocumentDefinitionResult<JsonSchemaDocumentDefinition> deploy(String schema, boolean readOnly, boolean force) {
        try {
            var jsonSchema = JsonSchema.fromString(schema);
            return deploy(jsonSchema, readOnly, force);
        } catch (Exception ex) {
            DocumentDefinitionError error = ex::getMessage;
            return new DeployDocumentDefinitionResultFailed<>(List.of(error));
        }
    }

    private DeployDocumentDefinitionResult<JsonSchemaDocumentDefinition> deploy(JsonSchema jsonSchema, boolean readOnly, boolean force) {
        try {
            var documentDefinitionName = jsonSchema.getSchema().getId().replace(".schema", "");
            var existingDefinition = findLatestByName(documentDefinitionName);
            var documentDefinitionId = JsonSchemaDocumentDefinitionId.newId(documentDefinitionName);

            if (existingDefinition.isPresent()) {
                var existingDocumentDefinition = existingDefinition.get();

                // Check read-only of previous definition
                if (existingDocumentDefinition.isReadOnly() && !force) {
                    DocumentDefinitionError error = () -> "This schema cannot be updated, because its readonly in previous versions";
                    return new DeployDocumentDefinitionResultFailed<>(List.of(error));
                }

                if (existingDocumentDefinition.getSchema().equals(jsonSchema)) {
                    logger.info("Schema already deployed - {} - {} ", existingDocumentDefinition.getId(), jsonSchema.getSchema().getId());
                    DocumentDefinitionError error = () -> "This exact schema is already deployed";
                    return new DeployDocumentDefinitionResultFailed<>(List.of(error));
                } else {
                    // Definition changed increase version
                    documentDefinitionId = JsonSchemaDocumentDefinitionId.nextVersion(existingDocumentDefinition.id());
                    logger.info("Schema changed. Deploying next version - {} - {} ", documentDefinitionId, jsonSchema.getSchema().getId());
                }
            }

            var documentDefinition = new JsonSchemaDocumentDefinition(documentDefinitionId, jsonSchema);

            if (readOnly) {
                documentDefinition.markReadOnly();
            }

            store(documentDefinition);
            logger.info("Deployed schema - {} - {} ", documentDefinitionId, jsonSchema.getSchema().getId());
            return new DeployDocumentDefinitionResultSucceeded<>(documentDefinition);
        } catch (Exception ex) {
            DocumentDefinitionError error = ex::getMessage;
            logger.warn(ex.getMessage());
            return new DeployDocumentDefinitionResultFailed<>(List.of(error));
        }
    }

    @Override
    public void store(JsonSchemaDocumentDefinition documentDefinition) {
        assertArgumentNotNull(documentDefinition, "documentDefinition is required");
        documentDefinitionRepository.findById(documentDefinition.id())
            .ifPresentOrElse(
                existingDocumentDefinition -> {
                    if (!existingDocumentDefinition.equals(documentDefinition)) {
                        throw new UnsupportedOperationException("Schema already deployed, will cannot redeploy");
                    }
                }, () -> documentDefinitionRepository.saveAndFlush(documentDefinition)
            );
    }

    @Override
    public void removeDocumentDefinition(String documentDefinitionName) {
        documentDefinitionRepository.deleteByIdName(documentDefinitionName);
    }


    @Override
    public boolean currentUserCanAccessDocumentDefinition(String documentDefinitionName) {
        return currentUserCanAccessDocumentDefinition(false, documentDefinitionName);
    }

    @Override
    public boolean currentUserCanAccessDocumentDefinition(boolean allowPrivilegedRoles, String documentDefinitionName) {
        List<String> roles = SecurityUtils.getCurrentUserRoles();
        return (allowPrivilegedRoles && roles.contains(AuthoritiesConstants.ADMIN))
            || getDocumentDefinitionRoles(documentDefinitionName).stream().anyMatch(roles::contains);
    }


    @Override
    public Set<String> getDocumentDefinitionRoles(String documentDefinitionName) {
        return documentDefinitionRoleRepository.findAllByIdDocumentDefinitionName(documentDefinitionName)
            .stream()
            .map(role -> role.id().role())
            .collect(Collectors.toSet());
    }

    @Override
    public void putDocumentDefinitionRoles(String documentDefinitionName, Set<String> roles) {
        List<JsonSchemaDocumentDefinitionRole> documentDefinitionRoles = roles.stream().map(it -> new JsonSchemaDocumentDefinitionRole(new JsonSchemaDocumentDefinitionRoleId(
            documentDefinitionName,
            it
        ))).toList();
        documentDefinitionRoleRepository.deleteByIdDocumentDefinitionName(documentDefinitionName);
        documentDefinitionRoleRepository.saveAll(documentDefinitionRoles);
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
