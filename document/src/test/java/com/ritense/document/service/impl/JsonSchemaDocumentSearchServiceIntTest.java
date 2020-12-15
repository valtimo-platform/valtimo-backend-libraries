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
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;

import javax.transaction.Transactional;
import java.util.List;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Transactional
public class JsonSchemaDocumentSearchServiceIntTest extends BaseIntegrationTest {

    private JsonSchemaDocumentDefinition definition;

    @BeforeEach
    public void beforeEach() {
        definition = definition();
        documentDefinitionService.deploy(definition);

        var content = new JsonDocumentContent("{\"street\": \"Funenpark\"}");

        documentService.createDocument(
            new NewDocumentRequest(
                definition.id().name(),
                content.asJson()
            )
        );

        var content2 = new JsonDocumentContent("{\"street\": \"Kalverstraat\"}");

        documentService.createDocument(
            new NewDocumentRequest(
                definition.id().name(),
                content2.asJson()
            )
        );

        JsonSchemaDocumentDefinition definitionHouseV2 = definitionOf("house", 2, "noautodeploy/house_v2.schema.json");
        documentDefinitionService.deploy(definitionHouseV2);
        documentService.createDocument(
            new NewDocumentRequest(
                definitionHouseV2.id().name(),
                new JsonDocumentContent("{\"street\": \"Kalverstraat\",\"place\": \"Amsterdam\"}").asJson()
            )
        );
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldNotFindSearchMatch() {
        final List<SearchCriteria> searchCriteriaList = List.of(new SearchCriteria("$.street", "random"));
        final Page<? extends Document> page = documentSearchService.search(definition.id().name(), searchCriteriaList, PageRequest.of(0, 2));
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldFindSearchMatch() {
        final List<SearchCriteria> searchCriteriaList = List.of(new SearchCriteria("$.street", "park"));
        final Page<? extends Document> page = documentSearchService.search(definition.id().name(), searchCriteriaList, PageRequest.of(0, 2));
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldFindMultipleSearchMatches() {
        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "park"),
            new SearchCriteria("$.street", "straat")
        );
        final Page<? extends Document> page = documentSearchService.search(definition.id().name(), searchCriteriaList, PageRequest.of(0, 3));
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldFindCaseInsensitive() {
        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "funenpark")
        );
        final Page<? extends Document> page = documentSearchService.search(definition.id().name(), searchCriteriaList, PageRequest.of(0, 2));
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldSearchThroughAllDocumentDefinitionVersions() {
        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.place", "Amsterdam")
        );
        final Page<? extends Document> page = documentSearchService.search(definition.id().name(), searchCriteriaList, PageRequest.of(0, 2));
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

}