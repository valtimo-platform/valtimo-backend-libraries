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

import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.service.result.DeployDocumentDefinitionResult;
import com.ritense.valtimo.contract.authentication.CurrentUserService;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Tag("integration")
@Transactional
public class JsonSchemaDocumentDefinitionServiceIntTest extends BaseIntegrationTest {

    @Inject
    private ResourceLoader resourceLoader;

    @MockBean
    private CurrentUserService currentUserService;

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldDeployFromString() {
        DeployDocumentDefinitionResult result = documentDefinitionService.deploy("" +
            "{\n" +
            "    \"$id\": \"testing.schema\",\n" +
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n");

        assertThat(result.documentDefinition()).isNotNull();
        assertThat(result.errors()).isEmpty();

        final var documentDefinition = (JsonSchemaDocumentDefinition) documentDefinitionService
            .findLatestByName("testing").orElseThrow();
        assertThat(documentDefinition.isReadOnly()).isFalse();
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldDeployResourceAsReadOnly() throws IOException {

        var resource = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:config/document/definition/noautodeploy/giraffe.schema.json");

        documentDefinitionService.deploy(resource.getInputStream());

        final var documentDefinition = (JsonSchemaDocumentDefinition) documentDefinitionService
            .findLatestByName("giraffe").orElseThrow();
        assertThat(documentDefinition.isReadOnly()).isTrue();
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldNotDeployASchemaThatChangedReadOnlyFlag() throws IOException {

        var resource = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:config/document/definition/noautodeploy/rhino.schema.json");

        documentDefinitionService.deploy(resource.getInputStream());

        DeployDocumentDefinitionResult result = documentDefinitionService.deploy("" +
            "{\n" +
            "    \"$id\": \"rhino.schema\",\n" +
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n");

        final var documentDefinition = (JsonSchemaDocumentDefinition) documentDefinitionService
            .findLatestByName("rhino").orElseThrow();
        assertThat(documentDefinition.isReadOnly()).isTrue();

        assertThat(result.documentDefinition()).isNull();
        assertThat(result.errors().get(0).asString()).isEqualTo("This schema cannot be updated, because its readonly in previous versions");
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldDeployNextVersionWhenDifferentSchema() {

        documentDefinitionService.deploy("" +
            "{\n" +
            "    \"$id\": \"clown.schema\",\n" +
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n");

        documentDefinitionService.deploy("" +
            "{\n" +
            "    \"$id\": \"clown.schema\",\n" +
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n"  +
            "    \"title\": \"Clown\"" +
            "}\n");

        final var documentDefinition = (JsonSchemaDocumentDefinition) documentDefinitionService
            .findLatestByName("clown").orElseThrow();
        assertThat(documentDefinition.id().version()).isEqualTo(2);
        assertThat(documentDefinition.isReadOnly()).isFalse();
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldNotDeployNextVersionWhenSchemaAlreadyExists() throws IOException {
        var resource = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:config/document/definition/house.schema.json");

        documentDefinitionService.deploy(resource.getInputStream());

        final var documentDefinition = documentDefinitionService
            .findLatestByName("house").orElseThrow();
        assertThat(documentDefinition.id().version()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldGetForRolesOnly() throws IllegalAccessException {
        DocumentDefinition expectedDefinition = documentDefinitionService.deploy("" +
            "{\n" +
            "    \"$id\": \"roles1.schema\",\n" +
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n").documentDefinition();

        //Unused documentDefinition
        documentDefinitionService.deploy("" +
            "{\n" +
            "    \"$id\": \"roles2.schema\",\n" +
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n");

        documentDefinitionService.putDocumentDefinitionRoles(expectedDefinition.id().name(), Set.of(USER));

        ValtimoUser valtimoUser = new ValtimoUser(null, "john@ritense.com", null, null, null, null, null, true, null, false, false, List.of(USER));
        when(currentUserService.getCurrentUser()).thenReturn(valtimoUser);

        boolean filteredForRole = false; //The user we're testing with is not ADMIN, so the result should be the same
        List<DocumentDefinition> all = (List<DocumentDefinition>)documentDefinitionService.findForUser(filteredForRole, Pageable.unpaged()).getContent();
        assertThat(all).hasSize(1);
        assertThat(all).containsOnly(expectedDefinition);
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldGetAllForAdmin() throws IllegalAccessException {
        DocumentDefinition documentDefinition1 = documentDefinitionService.deploy("" +
            "{\n" +
            "    \"$id\": \"roles1.schema\",\n" +
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n").documentDefinition();

        DocumentDefinition documentDefinition2 = documentDefinitionService.deploy("" +
            "{\n" +
            "    \"$id\": \"roles2.schema\",\n" +
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n").documentDefinition();

        ValtimoUser valtimoUser = new ValtimoUser(null, "john@ritense.com", null, null, null, null, null, true, null, false, false, List.of(ADMIN));
        when(currentUserService.getCurrentUser()).thenReturn(valtimoUser);

        boolean filteredForRole = false;
        List<DocumentDefinition> all = (List<DocumentDefinition>)documentDefinitionService.findForUser(filteredForRole, Pageable.unpaged()).getContent();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
        assertThat(all).contains(documentDefinition1, documentDefinition2);
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldNotGetAllForAdmin() throws IllegalAccessException {
        DocumentDefinition documentDefinition1 = documentDefinitionService.deploy("" +
            "{\n" +
            "    \"$id\": \"roles1.schema\",\n" +
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n").documentDefinition();

        documentDefinitionService.deploy("" +
            "{\n" +
            "    \"$id\": \"roles2.schema\",\n" +
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n").documentDefinition();

        documentDefinitionService.putDocumentDefinitionRoles(documentDefinition1.id().name(), Set.of(ADMIN));

        ValtimoUser valtimoUser = new ValtimoUser(null, "john@ritense.com", null, null, null, null, null, true, null, false, false, List.of(ADMIN));
        when(currentUserService.getCurrentUser()).thenReturn(valtimoUser);

        boolean filteredForRole = true;
        List<DocumentDefinition> all = (List<DocumentDefinition>)documentDefinitionService.findForUser(filteredForRole, Pageable.unpaged()).getContent();
        assertThat(all).hasSize(1);
        assertThat(all).containsOnly(documentDefinition1);
    }

}