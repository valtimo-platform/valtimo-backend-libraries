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

import com.ritense.authorization.permission.ConditionContainer;
import com.ritense.authorization.permission.Permission;
import com.ritense.authorization.permission.condition.FieldPermissionCondition;
import com.ritense.authorization.permission.condition.PermissionConditionOperator;
import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@Transactional
public class JsonSchemaDocumentDefinitionServiceIntTest extends BaseIntegrationTest {

    @Inject
    private ResourceLoader resourceLoader;

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    public void shouldDeployFromString() {
        final var result = documentDefinitionService.deploy("""
                {
                    "$id": "testing.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#"
                }
            """);

        assertThat(result.documentDefinition()).isNotNull();
        assertThat(result.errors()).isEmpty();

        final var documentDefinition = (JsonSchemaDocumentDefinition) documentDefinitionService
            .findLatestByName("testing").orElseThrow();
        assertThat(documentDefinition.isReadOnly()).isFalse();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    public void shouldDeployResourceAsReadOnly() throws IOException {

        final var resource = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:config/document/definition/noautodeploy/giraffe.schema.json");

        documentDefinitionService.deploy(resource.getInputStream());

        final var documentDefinition = (JsonSchemaDocumentDefinition) documentDefinitionService
            .findLatestByName("giraffe").orElseThrow();
        assertThat(documentDefinition.isReadOnly()).isTrue();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    public void shouldNotDeployASchemaThatChangedReadOnlyFlag() throws IOException {

        final var resource = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:config/document/definition/noautodeploy/rhino.schema.json");

        documentDefinitionService.deploy(resource.getInputStream());

        final var result = documentDefinitionService.deploy("""
                {
                    "$id": "rhino.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#"
                }
            """);

        final var documentDefinition = (JsonSchemaDocumentDefinition) documentDefinitionService
            .findLatestByName("rhino").orElseThrow();
        assertThat(documentDefinition.isReadOnly()).isTrue();

        assertThat(result.documentDefinition()).isNull();
        assertThat(result.errors().get(0).asString()).isEqualTo(
            "This schema cannot be updated, because its readonly in previous versions");
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    public void shouldDeployNextVersionWhenDifferentSchema() {

        documentDefinitionService.deploy("""
                {
                    "$id": "clown.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#"
                }
            """);

        documentDefinitionService
            .deploy("""
                    {
                        "$id": "clown.schema",
                        "$schema": "http://json-schema.org/draft-07/schema#",
                        "title": "Clown"
                    }
                """);

        final var documentDefinition = (JsonSchemaDocumentDefinition) documentDefinitionService
            .findLatestByName("clown").orElseThrow();
        assertThat(documentDefinition.id().version()).isEqualTo(2);
        assertThat(documentDefinition.isReadOnly()).isFalse();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    public void shouldListLatestVersions() {
        var v1 = documentDefinitionService.deploy("""
                {
                    "$id": "clown.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#"
                }
            """).documentDefinition();

        var v2 = documentDefinitionService
            .deploy("""
                    {
                        "$id": "clown.schema",
                        "$schema": "http://json-schema.org/draft-07/schema#",
                        "title": "Clown"
                    }
                """).documentDefinition();

        final var documentDefinitions = (List<DocumentDefinition>) documentDefinitionService.findAll(Pageable.unpaged()).toList();
        assertThat(documentDefinitions).isNotEqualTo(List.of());
        assertThat(documentDefinitions).doesNotContain(v1);
        assertThat(documentDefinitions).contains(v2);

    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    public void shouldNotDeployNextVersionWhenSchemaAlreadyExists() throws IOException {
        final var resource = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:config/document/definition/house.schema.json");

        documentDefinitionService.deploy(resource.getInputStream());

        final var documentDefinition = documentDefinitionService
            .findLatestByName("house").orElseThrow();
        assertThat(documentDefinition.id().version()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    public void shouldGetForRolesOnly() {
        final var expectedDefinition = runWithoutAuthorization(() -> documentDefinitionService.deploy("""
                {
                    "$id": "roles1.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#"
                }
            """)).documentDefinition();

        //Unused documentDefinition
        final var unexpectedDefinition = runWithoutAuthorization(() -> documentDefinitionService.deploy("""
                {
                    "$id": "roles2.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#"
                }
            """)).documentDefinition();

        permissionRepository.save(new Permission(
            UUID.randomUUID(),
            JsonSchemaDocumentDefinition.class,
            JsonSchemaDocumentDefinitionActionProvider.VIEW_LIST,
            new ConditionContainer(List.of(
                new FieldPermissionCondition<>(
                    "id.name",
                    PermissionConditionOperator.EQUAL_TO,
                    "roles1"
                )
            )),
            roleRepository.findByKey(USER)
        ));

        final var all = (List<DocumentDefinition>) documentDefinitionService.findAll(Pageable.unpaged()).getContent();
        assertThat(all).contains(expectedDefinition);
        assertThat(all).doesNotContain(unexpectedDefinition);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = ADMIN)
    public void shouldGetAllForAdmin() {
        final var documentDefinition1 = runWithoutAuthorization(() -> documentDefinitionService.deploy("""
                {
                    "$id": "roles1.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#"
                }
            """)).documentDefinition();

        final var documentDefinition2 = runWithoutAuthorization(() -> documentDefinitionService.deploy("""
                {
                    "$id": "roles2.schema",
                    "$schema": "http://json-schema.org/draft-07/schema#"
                }
            """)).documentDefinition();

        assertThatThrownBy(() -> documentDefinitionService.findAllForManagement(Pageable.unpaged()))
            .isExactlyInstanceOf(AccessDeniedException.class);

        final var all = (List<DocumentDefinition>) runWithoutAuthorization(() -> documentDefinitionService.findAllForManagement(Pageable.unpaged())).toList();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
        assertThat(all).contains(documentDefinition1, documentDefinition2);
    }
}