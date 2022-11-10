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

import com.fasterxml.jackson.core.JsonPointer;
import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.search.DatabaseSearchType;
import com.ritense.document.domain.search.SearchRequest2;
import com.ritense.document.service.result.CreateDocumentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.test.context.support.WithMockUser;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.DEVELOPER;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration")
@Transactional
class JsonSchemaDocumentSearchServiceIntTest extends BaseIntegrationTest {

    private static final String USERNAME = "john@ritense.com";
    private JsonSchemaDocumentDefinition definition;
    private CreateDocumentResult originalDocument;

    @BeforeEach
    public void beforeEach() {
        definition = definition();
        documentDefinitionService.putDocumentDefinitionRoles(definition.id().name(), Set.of(USER, DEVELOPER));
        var content = new JsonDocumentContent("{\"street\": \"Funenpark\"}");

        originalDocument = documentService.createDocument(
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
        documentDefinitionService.store(definitionHouseV2);
        documentDefinitionService.putDocumentDefinitionRoles(definitionHouseV2.id().name(), Set.of(USER, DEVELOPER));
        documentService.createDocument(
            new NewDocumentRequest(
                definitionHouseV2.id().name(),
                new JsonDocumentContent("{\"street\": \"Kalverstraat\",\"place\": \"Amsterdam\"}").asJson()
            )
        );
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldNotFindSearchMatch() {
        final List<SearchCriteria> searchCriteriaList = List.of(new SearchCriteria("$.street", "random"));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10)
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getTotalPages()).isZero();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldFindSearchMatch() {
        final List<SearchCriteria> searchCriteriaList = List.of(new SearchCriteria("$.street", "park"));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10)
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    void searchWithoutAuthorizationShouldFindSearchMatch() {
        final List<SearchCriteria> searchCriteriaList = List.of(new SearchCriteria("$.street", "park"));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.searchWithoutAuthorization(
            searchRequest,
            PageRequest.of(0, 10)
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldFindMultipleSearchMatches() {
        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "park"),
            new SearchCriteria("$.street", "straat")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10)
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldFindCaseInsensitive() {
        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "funenpark")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10)
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldSearchThroughAllDocumentDefinitionVersions() {
        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.place", "Amsterdam")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10)
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldFindDocumentByMultipleCriteria() {
        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "Kalver"),
            new SearchCriteria("$.place", "Amster")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10)
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldNotFindDocumentIfNotAllCriteriaMatch() {
        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "Kalver"),
            new SearchCriteria("$.place", "random")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10)
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getTotalPages()).isZero();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldFindDocumentByGlobalSearchFilter() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setGlobalSearchFilter("park");

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10)
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldFindDocumentBySequence() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setSequence(originalDocument.resultingDocument().get().sequence());

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10)
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldFindDocumentByCreatedBy() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setCreatedBy(USERNAME);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10)
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldReturnPageableData() {

        createDocument("{\"street\": \"Czaar Peterstraat\", \"number\": 7}");
        createDocument("{\"street\": \"Czaar Peterstraat\", \"number\": 8}");

        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "Czaar Peterstraat")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 1)
        );
        assertThat(page).isNotNull();
        assertThat(page.getSize()).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldReturnFullPages() {

        createDocument("{\"street\": \"Czaar Peterstraat 1\"}");
        createDocument("{\"street\": \"Czaar Peterstraat 2\"}");
        createDocument("{\"street\": \"Czaar Peterstraat 3\"}");
        createDocument("{\"street\": \"Czaar Peterstraat 4\"}");

        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "Czaar Peterstraat")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(1, 2, Sort.by(Direction.ASC, "$.street"))
        );
        assertThat(page).isNotNull();
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent().get(0).content().asJson().findPath("street").asText()).isEqualTo("Czaar Peterstraat 3");
        assertThat(page.getContent().get(1).content().asJson().findPath("street").asText()).isEqualTo("Czaar Peterstraat 4");
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldOrderAllDocumentsByContentProperty() {

        createDocument("{\"street\": \"Alexanderkade\"}");

        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "park"),
            new SearchCriteria("$.street", "straat"),
            new SearchCriteria("$.street", "kade")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10, Sort.by("$.street"))
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent().get(0).content().asJson().findPath("street").asText()).isEqualTo("Alexanderkade");
        assertThat(page.getContent().get(1).content().asJson().findPath("street").asText()).isEqualTo("Funenpark");
        assertThat(page.getContent().get(2).content().asJson().findPath("street").asText()).isEqualTo("Kalverstraat");
        assertThat(page.getContent().get(3).content().asJson().findPath("street").asText()).isEqualTo("Kalverstraat");
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldOrderAllDocumentsDescendingByContentProperty() {

        createDocument("{\"street\": \"Alexanderkade\"}");

        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "park"),
            new SearchCriteria("$.street", "straat"),
            new SearchCriteria("$.street", "kade")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.DESC, "$.street"))
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent().get(0).content().asJson().findPath("street").asText()).isEqualTo("Kalverstraat");
        assertThat(page.getContent().get(1).content().asJson().findPath("street").asText()).isEqualTo("Kalverstraat");
        assertThat(page.getContent().get(2).content().asJson().findPath("street").asText()).isEqualTo("Funenpark");
        assertThat(page.getContent().get(3).content().asJson().findPath("street").asText()).isEqualTo("Alexanderkade");
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldOrderAllDocumentsByMultipleProperties() {

        createDocument("{\"street\": \"Alexanderkade\", \"number\": 7}");
        createDocument("{\"street\": \"Westerkade\", \"number\": 1}");
        createDocument("{\"street\": \"Alexanderkade\", \"number\": 3}");
        createDocument("{\"street\": \"Westerkade\", \"number\": 4}");

        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "kade")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10, Sort.by("$.street", "$.number"))
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent().get(0).content().asJson().findPath("street").asText()).isEqualTo("Alexanderkade");
        assertThat(page.getContent().get(0).content().asJson().findPath("number").asInt()).isEqualTo(3);
        assertThat(page.getContent().get(1).content().asJson().findPath("street").asText()).isEqualTo("Alexanderkade");
        assertThat(page.getContent().get(1).content().asJson().findPath("number").asInt()).isEqualTo(7);
        assertThat(page.getContent().get(2).content().asJson().findPath("street").asText()).isEqualTo("Westerkade");
        assertThat(page.getContent().get(2).content().asJson().findPath("number").asInt()).isEqualTo(1);
        assertThat(page.getContent().get(3).content().asJson().findPath("street").asText()).isEqualTo("Westerkade");
        assertThat(page.getContent().get(3).content().asJson().findPath("number").asInt()).isEqualTo(4);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldOrderAllDocumentsByOtherField() {

        CreateDocumentResult documentOne = createDocument("{\"street\": \"Alexanderkade\"}");
        CreateDocumentResult documentTwo = createDocument("{\"street\": \"Alexanderkade\"}");

        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.street", "kade")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.DESC, "sequence"))
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).sequence()).isEqualTo(documentTwo.resultingDocument().get().sequence());
        assertThat(page.getContent().get(1).sequence()).isEqualTo(documentOne.resultingDocument().get().sequence());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldOrderAllDocumentsWithoutCapitals() {

        CreateDocumentResult documentOne = createDocument("{\"street\": \"abc\",\"place\": \"test\"}");
        CreateDocumentResult documentTwo = createDocument("{\"street\": \"Ade\",\"place\": \"test\"}");

        final List<SearchCriteria> searchCriteriaList = List.of(
            new SearchCriteria("$.place", "test")
        );

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDocumentDefinitionName(definition.id().name());
        searchRequest.setOtherFilters(searchCriteriaList);

        final Page<? extends Document> page = documentSearchService.search(
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "$.street"))
        );
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).id()).isEqualTo(documentOne.resultingDocument().get().id());
        assertThat(page.getContent().get(1).id()).isEqualTo(documentTwo.resultingDocument().get().id());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void searchShouldOrderAllDocumentsBasedOnAssigneeFullName() {
        documentRepository.deleteAll();
        var documentOne = (JsonSchemaDocument) createDocument("{}").resultingDocument().get();
        var documentTwo = (JsonSchemaDocument) createDocument("{}").resultingDocument().get();
        var documentThree = (JsonSchemaDocument) createDocument("{}").resultingDocument().get();
        documentOne.setAssignee("1111", "Beth Xander");
        documentTwo.setAssignee("2222", "Anna Yablon");
        documentThree.setAssignee("33", "Beth Zabala");
        documentRepository.saveAll(List.of(documentOne, documentTwo, documentThree));

        final Page<? extends Document> page = documentSearchService.search(
            new SearchRequest(),
            PageRequest.of(0, 10, Sort.by(Direction.DESC, "assigneeFullName"))
        );

        assertThat(page).isNotNull();
        var content = page.getContent();
        assertThat(content.get(0).assigneeFullName()).isEqualTo("Beth Zabala");
        assertThat(content.get(1).assigneeFullName()).isEqualTo("Beth Xander");
        assertThat(content.get(2).assigneeFullName()).isEqualTo("Anna Yablon");
    }


    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void shouldSearchWithSearchRequestAndValues() {
        documentRepository.deleteAll();

        var documentOne = (JsonSchemaDocument) createDocument("{\"street\": \"Alexanderkade\"}").resultingDocument().get();
        var documentTwo = (JsonSchemaDocument) createDocument("{\"street\": \"Alexanderkade\"}").resultingDocument().get();

        documentRepository.saveAll(List.of(documentOne, documentTwo));

        var searchRequest = new SearchRequest2();
        var searchCriteria = new SearchRequest2.SearchCriteria2();
        searchCriteria.setSearchType(DatabaseSearchType.LIKE);
        searchCriteria.setPath("$.street");
        searchCriteria.setValues(List.of("kade"));
        searchRequest.setOtherFilters(List.of(searchCriteria));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.DESC, "$.street")));

        assertThat(result).isNotNull();
        assertEquals(2, result.getContent().size());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void shouldSearchWithSearchRequestAndBetweenRangedValues() throws InterruptedException {
        documentRepository.deleteAllInBatch();

        createDocument("{\"housenumber\": 1}").resultingDocument().get();
        createDocument("{\"housenumber\": 2}").resultingDocument().get();
        createDocument("{\"housenumber\": 3}").resultingDocument().get();

        var searchRequest = new SearchRequest2();
        var searchCriteria = new SearchRequest2.SearchCriteria2();
        searchCriteria.setRangeFrom(1);
        searchCriteria.setRangeTo(2);
        searchCriteria.setSearchType(DatabaseSearchType.BETWEEN);
        searchCriteria.setPath("$.housenumber");
        searchRequest.setOtherFilters(List.of(searchCriteria));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "$.housenumber")));

        assertThat(result).isNotNull();
        var content = result.getContent();
        assertEquals(2, content.size());

        assertEquals(1, content.get(0).content().getValueBy(JsonPointer.valueOf("/housenumber")).get().asInt());
        assertEquals(2, content.get(1).content().getValueBy(JsonPointer.valueOf("/housenumber")).get().asInt());
    }

    @Test
    void shouldSearchWithSearchRequestAndFromRangedValue() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"housenumber\": 1}").resultingDocument().get();
        createDocument("{\"housenumber\": 2}").resultingDocument().get();
        createDocument("{\"housenumber\": 3}").resultingDocument().get();

        var searchRequest = new SearchRequest2();
        var searchCriteria = new SearchRequest2.SearchCriteria2();
        searchCriteria.setRangeFrom(2);
        searchCriteria.setSearchType(DatabaseSearchType.GREATER_THAN_OR_EQUAL_TO);
        searchCriteria.setPath("$.housenumber");
        searchRequest.setOtherFilters(List.of(searchCriteria));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "$.housenumber")));

        assertThat(result).isNotNull();
        var content = result.getContent();
        assertEquals(2, content.size());

        assertEquals(2, content.get(0).content().getValueBy(JsonPointer.valueOf("/housenumber")).get().asInt());
        assertEquals(3, content.get(1).content().getValueBy(JsonPointer.valueOf("/housenumber")).get().asInt());
    }

    @Test
    void shouldFailOnFromSearchWithoutRangeFrom() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"housenumber\": 1}").resultingDocument().get();
        createDocument("{\"housenumber\": 2}").resultingDocument().get();
        createDocument("{\"housenumber\": 3}").resultingDocument().get();

        var searchRequest = new SearchRequest2();
        var searchCriteria = new SearchRequest2.SearchCriteria2();
        searchCriteria.setRangeTo(2);
        searchCriteria.setSearchType(DatabaseSearchType.GREATER_THAN_OR_EQUAL_TO);
        searchCriteria.setPath("$.housenumber");
        searchRequest.setOtherFilters(List.of(searchCriteria));

        assertThrows(Exception.class, () -> {
         var result =   documentSearchService.search(
                definition.id().name(),
                searchRequest,
                PageRequest.of(0, 10)
            );
        });
    }

    @Test
    void shouldSearchWithSearchRequestAndRangedDateValues() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"movedAt\": \"2022-01-01\"}").resultingDocument().get();
        createDocument("{\"movedAt\": \"2022-02-01\"}").resultingDocument().get();
        createDocument("{\"movedAt\": \"2022-03-01\"}").resultingDocument().get();

        var searchRequest = new SearchRequest2();
        var searchCriteria = new SearchRequest2.SearchCriteria2();
        searchCriteria.setRangeFrom(LocalDate.of(2022, 01, 01));
        searchCriteria.setRangeTo(LocalDate.of(2022, 02, 01));
        searchCriteria.setSearchType(DatabaseSearchType.BETWEEN);
        searchCriteria.setPath("$.movedAt");
        searchRequest.setOtherFilters(List.of(searchCriteria));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "$.movedAt")));

        assertThat(result).isNotNull();
        var content = result.getContent();
        assertEquals(2, content.size());

        assertEquals("2022-01-01", content.get(0).content().getValueBy(JsonPointer.valueOf("/movedAt")).get().asText());
        assertEquals("2022-02-01", content.get(1).content().getValueBy(JsonPointer.valueOf("/movedAt")).get().asText());
    }

    private CreateDocumentResult createDocument(String content) {
        var documentContent = new JsonDocumentContent(content);

        return documentService.createDocument(
            new NewDocumentRequest(
                definition.id().name(),
                documentContent.asJson()
            )
        );
    }

}
