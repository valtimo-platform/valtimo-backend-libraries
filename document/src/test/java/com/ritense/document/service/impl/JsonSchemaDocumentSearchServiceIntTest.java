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

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.Mapper;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.search.AdvancedSearchRequest;
import com.ritense.document.domain.search.AssigneeFilter;
import com.ritense.document.domain.search.SearchOperator;
import com.ritense.document.event.DocumentsListed;
import com.ritense.document.service.result.CreateDocumentResult;
import com.ritense.outbox.domain.BaseEvent;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.test.context.support.WithMockUser;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static com.ritense.document.domain.search.DatabaseSearchType.BETWEEN;
import static com.ritense.document.domain.search.DatabaseSearchType.EQUAL;
import static com.ritense.document.domain.search.DatabaseSearchType.GREATER_THAN_OR_EQUAL_TO;
import static com.ritense.document.domain.search.DatabaseSearchType.IN;
import static com.ritense.document.domain.search.DatabaseSearchType.LESS_THAN_OR_EQUAL_TO;
import static com.ritense.document.domain.search.DatabaseSearchType.LIKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("integration")
@Transactional
class JsonSchemaDocumentSearchServiceIntTest extends BaseIntegrationTest {

    private static final String USER_ID = "a28994a3-31f9-4327-92a4-210c479d3055";
    private static final String USERNAME = "john@ritense.com";
    private JsonSchemaDocumentDefinition definition;
    private CreateDocumentResult originalDocument;

    @BeforeEach
    public void beforeEach() {

        definition = definition();
        var content = new JsonDocumentContent("{\"street\": \"Funenpark\"}");

        originalDocument = AuthorizationContext.runWithoutAuthorization(() -> documentService.createDocument(
            new NewDocumentRequest(
                definition.id().name(),
                content.asJson()
            )
        ));

        var content2 = new JsonDocumentContent("{\"street\": \"Kalverstraat\"}");

        AuthorizationContext.runWithoutAuthorization(() -> documentService.createDocument(
            new NewDocumentRequest(
                definition.id().name(),
                content2.asJson()
            )
        ));

        JsonSchemaDocumentDefinition definitionHouseV2 = definitionOf("house", 2, "noautodeploy/house_v2.schema.json");
        documentDefinitionService.store(definitionHouseV2);
        documentService.createDocument(
            new NewDocumentRequest(
                definitionHouseV2.id().name(),
                new JsonDocumentContent("{\"street\": \"Kalverstraat\",\"place\": \"Amsterdam\"}").asJson()
            )
        );

        var user = new ValtimoUserBuilder().username(USERNAME).email(USERNAME).id(USER_ID).build();
        when(userManagementService.findById(USER_ID)).thenReturn(user);
        when(userManagementService.getCurrentUser()).thenReturn(user);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
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
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void searchShouldOrderAllDocumentsBasedOnAssigneeFullName() {
        documentRepository.deleteAllInBatch();
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
        assertThat(page.getTotalElements()).isEqualTo(3);
        var content = page.getContent();
        assertThat(content.get(0).assigneeFullName()).isEqualTo("Beth Zabala");
        assertThat(content.get(1).assigneeFullName()).isEqualTo("Beth Xander");
        assertThat(content.get(2).assigneeFullName()).isEqualTo("Anna Yablon");
    }


    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestAndLikeText() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"street\": \"Alexanderkade\"}").resultingDocument().get();
        createDocument("{\"street\": \"Alexanderkade\"}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue("kade")
                .searchType(LIKE)
                .path("doc:street"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.DESC, "doc:street")));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestAndLikeTextInListOfMultipleValues() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"street\": \"Funenpark\"}").resultingDocument().get();
        createDocument("{\"street\": \"Alexanderkade\"}").resultingDocument().get();
        createDocument("{\"street\": \"Czaar Peterstraat\"}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue("kade")
                .addValue("park")
                .searchType(LIKE)
                .path("doc:street"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.DESC, "doc:street")));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestAndTextInListOfMultipleValues() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"street\": \"Funenpark\"}").resultingDocument().get();
        createDocument("{\"street\": \"Alexanderkade\"}").resultingDocument().get();
        createDocument("{\"street\": \"Czaar Peterstraat\"}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue("Funenpark")
                .addValue("Alexanderkade")
                .addValue("straat") // should not find this one since it's EQUAL not LIKE
                .searchType(EQUAL)
                .path("doc:street"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.DESC, "doc:street")));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
    }


    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestAndBetweenRangedValues() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"housenumber\": 1}").resultingDocument().get();
        createDocument("{\"housenumber\": 2}").resultingDocument().get();
        createDocument("{\"housenumber\": 3}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .rangeFrom(1)
                .rangeTo(2)
                .searchType(BETWEEN)
                .path("doc:housenumber"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "doc:housenumber")));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);

        var content = result.getContent();
        assertTrue(content.get(0).content().getValueBy(JsonPointer.valueOf("/housenumber")).isPresent());
        assertTrue(content.get(1).content().getValueBy(JsonPointer.valueOf("/housenumber")).isPresent());
        assertEquals(1, content.get(0).content().getValueBy(JsonPointer.valueOf("/housenumber")).get().asInt());
        assertEquals(2, content.get(1).content().getValueBy(JsonPointer.valueOf("/housenumber")).get().asInt());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestAndFromRangedValue() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"housenumber\": 1}").resultingDocument().get();
        createDocument("{\"housenumber\": 2}").resultingDocument().get();
        createDocument("{\"housenumber\": 3}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .rangeFrom(2)
                .searchType(GREATER_THAN_OR_EQUAL_TO)
                .path("doc:housenumber"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "doc:housenumber")));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);

        var content = result.getContent();
        assertTrue(content.get(0).content().getValueBy(JsonPointer.valueOf("/housenumber")).isPresent());
        assertTrue(content.get(1).content().getValueBy(JsonPointer.valueOf("/housenumber")).isPresent());
        assertEquals(2, content.get(0).content().getValueBy(JsonPointer.valueOf("/housenumber")).get().asInt());
        assertEquals(3, content.get(1).content().getValueBy(JsonPointer.valueOf("/housenumber")).get().asInt());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestAndToRangedValue() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"housenumber\": 1}").resultingDocument().get();
        createDocument("{\"housenumber\": 2}").resultingDocument().get();
        createDocument("{\"housenumber\": 3}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .rangeTo(1)
                .searchType(LESS_THAN_OR_EQUAL_TO)
                .path("doc:housenumber"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "doc:housenumber")));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);

        var content = result.getContent();
        assertTrue(content.get(0).content().getValueBy(JsonPointer.valueOf("/housenumber")).isPresent());
        assertEquals(1, content.get(0).content().getValueBy(JsonPointer.valueOf("/housenumber")).get().asInt());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldFailOnFromSearchWithoutRangeFrom() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"housenumber\": 1}").resultingDocument().get();
        createDocument("{\"housenumber\": 2}").resultingDocument().get();
        createDocument("{\"housenumber\": 3}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .rangeTo(2)
                .searchType(GREATER_THAN_OR_EQUAL_TO)
                .path("doc:housenumber"));

        assertThrows(ValidationException.class, () -> documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10)
        ));
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestAndRangedDateValues() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"movedAtDate\": \"2022-01-01\"}").resultingDocument().get();
        createDocument("{\"movedAtDate\": \"2022-02-01\"}").resultingDocument().get();
        createDocument("{\"movedAtDate\": \"2022-03-01\"}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .rangeFrom(LocalDate.parse("2022-01-01"))
                .rangeTo(LocalDate.parse("2022-02-01"))
                .searchType(BETWEEN)
                .path("doc:movedAtDate"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "doc:movedAtDate")));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);

        var content = result.getContent();
        assertTrue(content.get(0).content().getValueBy(JsonPointer.valueOf("/movedAtDate")).isPresent());
        assertTrue(content.get(1).content().getValueBy(JsonPointer.valueOf("/movedAtDate")).isPresent());
        assertEquals("2022-01-01", content.get(0).content().getValueBy(JsonPointer.valueOf("/movedAtDate")).get().asText());
        assertEquals("2022-02-01", content.get(1).content().getValueBy(JsonPointer.valueOf("/movedAtDate")).get().asText());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestAndFromDateTimeValues() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"movedAtDateTime\": \"2022-01-01T12:00:00\"}").resultingDocument().get();
        createDocument("{\"movedAtDateTime\": \"2022-01-01T12:10:00\"}").resultingDocument().get();
        createDocument("{\"movedAtDateTime\": \"2022-01-01T12:20:00\"}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .rangeFrom(LocalDateTime.parse("2022-01-01T12:10:00"))
                .searchType(GREATER_THAN_OR_EQUAL_TO)
                .path("doc:movedAtDateTime"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "doc:movedAtDateTime")));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);

        var content = result.getContent();
        assertTrue(content.get(0).content().getValueBy(JsonPointer.valueOf("/movedAtDateTime")).isPresent());
        assertTrue(content.get(1).content().getValueBy(JsonPointer.valueOf("/movedAtDateTime")).isPresent());
        assertEquals("2022-01-01T12:10:00",
            content.get(0).content().getValueBy(JsonPointer.valueOf("/movedAtDateTime")).get().asText());
        assertEquals("2022-01-01T12:20:00",
            content.get(1).content().getValueBy(JsonPointer.valueOf("/movedAtDateTime")).get().asText());
    }

    @Test
    @WithMockUser(username = "example@ritense.com", authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestAndCreatedBy() {
        documentRepository.deleteAllInBatch();

        createDocument("{}");

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue("example@ritense.com")
                .searchType(EQUAL)
                .path("case:createdBy"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);

        var content = result.getContent();
        assertEquals("example@ritense.com", content.get(0).createdBy());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestAndOrderBySequence() {
        documentRepository.deleteAllInBatch();

        var document1 = createDocument("{}").resultingDocument().get();
        var document2 = createDocument("{}").resultingDocument().get();
        var document3 = createDocument("{}").resultingDocument().get();

        var result = documentSearchService.search(
            definition.id().name(),
            new AdvancedSearchRequest(),
            PageRequest.of(0, 10, Sort.by(Direction.DESC, "case:sequence")));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3);

        var content = result.getContent();
        assertEquals(document3.sequence(), content.get(0).sequence());
        assertEquals(document2.sequence(), content.get(1).sequence());
        assertEquals(document1.sequence(), content.get(2).sequence());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchForCreatedOnCasePropertyWithLocalDateClass() {
        documentRepository.deleteAllInBatch();

        var document = createDocument("{}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue(document.createdOn().toLocalDate())
                .searchType(EQUAL)
                .path("case:createdOn"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            Pageable.unpaged()
        );

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchForBooleanTrueProperty() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"loan-approved\": true}").resultingDocument().get();
        createDocument("{\"loan-approved\": false}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue(true)
                .searchType(EQUAL)
                .path("doc:\"loan-approved\""));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            Pageable.unpaged()
        );

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.toList().get(0).content().getValueBy(JsonPointer.valueOf("/loan-approved")).get().booleanValue())
            .isTrue();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchForBooleanFalseProperty() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"loan-approved\": true}").resultingDocument().get();
        createDocument("{\"loan-approved\": false}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue(false)
                .searchType(EQUAL)
                .path("doc:\"loan-approved\""));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            Pageable.unpaged()
        );

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.toList().get(0).content().getValueBy(JsonPointer.valueOf("/loan-approved")).get().booleanValue())
            .isFalse();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchForIntegerProperty() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"size\": 5}").resultingDocument().get();
        createDocument("{\"size\": 6}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue(5)
                .searchType(EQUAL)
                .path("doc:size"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            Pageable.unpaged()
        );

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestWithMultipleFieldsUsingAnd() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"street\": \"Funenpark\", \"registrationDate\": \"1999-10-23\"}").resultingDocument().get();
        createDocument("{\"street\": \"Funenpark\", \"registrationDate\": \"2017-06-01\"}").resultingDocument().get();
        createDocument("{\"street\": \"Czaar Peterstraat\", \"registrationDate\": \"2017-06-01\"}").resultingDocument().get();

        // relying on default SearchOperator being AND
        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue("Funenpark")
                .searchType(EQUAL)
                .path("doc:street"))
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue(LocalDate.of(2017, 6, 1))
                .searchType(EQUAL)
                .path("doc:registrationDate"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.DESC, "doc:street")));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestWithMultipleFieldsUsingOr() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"street\": \"Funenpark\", \"registrationDate\": \"1999-10-23\"}").resultingDocument().get();
        createDocument("{\"street\": \"Funenpark\", \"registrationDate\": \"2017-06-01\"}").resultingDocument().get();
        createDocument("{\"street\": \"Czaar Peterstraat\", \"registrationDate\": \"2017-06-01\"}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .searchOperator(SearchOperator.OR)
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue("Funenpark")
                .searchType(EQUAL)
                .path("doc:street"))
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue(LocalDate.of(2017, 6, 1))
                .searchType(EQUAL)
                .path("doc:registrationDate"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.DESC, "doc:street")));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchWithSearchRequestWithIn() {
        documentRepository.deleteAllInBatch();

        createDocument("{\"street\": \"Funenpark\"}").resultingDocument().get();
        createDocument("{\"street\": \"Wallstreet\"}").resultingDocument().get();
        createDocument("{\"street\": \"Czaar Peterstraat\"}").resultingDocument().get();

        var searchRequest = new AdvancedSearchRequest()
            .addOtherFilters(new AdvancedSearchRequest.OtherFilter()
                .addValue("Funenpark")
                .addValue("Czaar Peterstraat")
                .searchType(IN)
                .path("doc:street"));

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchForOpenCases() {
        documentRepository.deleteAllInBatch();

        var document1 = createDocument("{\"street\": \"Alpaccalaan\"}").resultingDocument().get();
        var document2 = createDocument("{\"street\": \"Baarnseweg\"}").resultingDocument().get();
        var document3 = createDocument("{\"street\": \"Comeniuslaan\"}").resultingDocument().get();

        AuthorizationContext.runWithoutAuthorization(() -> {
                documentService.assignUserToDocument(document2.id().getId(), USER_ID);
                return null;
            }
        );

        var searchRequest = new AdvancedSearchRequest()
            .assigneeFilter(AssigneeFilter.OPEN);

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "doc:street")));

        assertThat(result.toList()).hasSize(2);
        assertThat(result.toList().get(0).id().getId()).isEqualTo(document1.id().getId());
        assertThat(result.toList().get(1).id().getId()).isEqualTo(document3.id().getId());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchForMyCases() {
        documentRepository.deleteAllInBatch();

        var document1 = createDocument("{\"street\": \"Alpaccalaan\"}").resultingDocument().get();
        var document2 = createDocument("{\"street\": \"Baarnseweg\"}").resultingDocument().get();
        var document3 = createDocument("{\"street\": \"Comeniuslaan\"}").resultingDocument().get();

        AuthorizationContext.runWithoutAuthorization(() -> {
                documentService.assignUserToDocument(document2.id().getId(), USER_ID);
                return null;
            }
        );

        var searchRequest = new AdvancedSearchRequest()
            .assigneeFilter(AssigneeFilter.MINE);

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "doc:street")));

        assertThat(result.toList()).hasSize(1);
        assertThat(result.toList().get(0).id().getId()).isEqualTo(document2.id().getId());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldSearchForAll() {
        documentRepository.deleteAllInBatch();

        var document1 = createDocument("{\"street\": \"Alpaccalaan\"}").resultingDocument().get();
        var document2 = createDocument("{\"street\": \"Baarnseweg\"}").resultingDocument().get();
        var document3 = createDocument("{\"street\": \"Comeniuslaan\"}").resultingDocument().get();

        AuthorizationContext
            .runWithoutAuthorization(() -> {
                    documentService.assignUserToDocument(
                        document2.id().getId(),
                        USER_ID
                    );
                    return null;
                }
            );

        var searchRequest = new AdvancedSearchRequest()
            .assigneeFilter(AssigneeFilter.ALL);

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "doc:street")));

        assertThat(result.toList()).hasSize(3);
        assertThat(result.toList().get(0).id().getId()).isEqualTo(document1.id().getId());
        assertThat(result.toList().get(1).id().getId()).isEqualTo(document2.id().getId());
        assertThat(result.toList().get(2).id().getId()).isEqualTo(document3.id().getId());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldEmitOutboxEventWhenListing() throws JsonProcessingException {
        documentRepository.deleteAllInBatch();

        var documents = Stream.of("Alpaccalaan", "Baarnseweg", "Comeniuslaan").map(street ->
            createDocument("{\"street\": \"" + street + "\"}").resultingDocument().get()
        ).toList();

        var searchRequest = new AdvancedSearchRequest()
            .assigneeFilter(AssigneeFilter.ALL);

        reset(outboxService);

        var result = documentSearchService.search(
            definition.id().name(),
            searchRequest,
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "doc:street"))
        );

        assertThat(result.toList()).hasSize(3);

        final ArgumentCaptor<Supplier<BaseEvent>> eventCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(outboxService, atLeastOnce()).send(eventCaptor.capture());
        List<BaseEvent> documentsListedEvents = eventCaptor.getAllValues().stream()
            .map(Supplier::get)
            .filter(baseEvent -> baseEvent instanceof DocumentsListed)
            .toList();

        assertThat(documentsListedEvents).hasSize(1);
        DocumentsListed documentsListed = (DocumentsListed) documentsListedEvents.stream().findFirst().orElseThrow();
        String resultJson = Mapper.INSTANCE.get().writeValueAsString(documentsListed.getResult());
        documents.forEach(document -> {
            assertThat(resultJson).contains("\"" + document.id() + "\"");
        });
    }


    private CreateDocumentResult createDocument(String content) {
        var documentContent = new JsonDocumentContent(content);

        return AuthorizationContext.runWithoutAuthorization(
            () -> documentService.createDocument(
                new NewDocumentRequest(
                    definition.id().name(),
                    documentContent.asJson()
                )
            )
        );
    }
}
