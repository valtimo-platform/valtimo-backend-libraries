/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static com.ritense.document.service.JsonSchemaDocumentActionProvider.VIEW_LIST;
import static com.ritense.logging.LoggingContextKt.withLoggingContext;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.search.AdvancedSearchRequest;
import com.ritense.document.domain.search.AssigneeFilter;
import com.ritense.document.domain.search.SearchOperator;
import com.ritense.document.domain.search.SearchRequestMapper;
import com.ritense.document.domain.search.SearchRequestValidator;
import com.ritense.document.domain.search.SearchWithConfigRequest;
import com.ritense.document.event.DocumentsListed;
import com.ritense.document.service.DocumentSearchService;
import com.ritense.document.service.SearchFieldService;
import com.ritense.logging.LoggableResource;
import com.ritense.outbox.OutboxService;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.database.QueryDialectHelper;
import com.ritense.valtimo.contract.utils.RequestHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Transactional
public class JsonSchemaDocumentSearchService implements DocumentSearchService {

    private static final String DOCUMENT_DEFINITION_ID = "documentDefinitionId";
    private static final String NAME = "name";
    private static final String CREATED_BY = "createdBy";
    private static final String SEQUENCE = "sequence";
    private static final String CONTENT = "content";
    private static final String ASSIGNEE_ID = "assigneeId";
    private static final String INTERNAL_STATUS = "internalStatus";
    private static final String INTERNAL_STATUS_KEY = "internalStatus.id.key";
    private static final String INTERNAL_STATUS_ORDER = "internalStatus.order";
    private static final String DOC_PREFIX = "doc:";
    private static final String CASE_PREFIX = "case:";

    private static final Map<String, String> DOCUMENT_FIELD_MAP = Map.of(
        "definitionId.name", "documentDefinitionId.name",
        "definitionId.version", "documentDefinitionId.key",
        INTERNAL_STATUS, INTERNAL_STATUS_ORDER
    );

    private final EntityManager entityManager;
    private final QueryDialectHelper queryDialectHelper;
    private final SearchFieldService searchFieldService;
    private final UserManagementService userManagementService;

    private final AuthorizationService authorizationService;
    private final OutboxService outboxService;

    private final ObjectMapper objectMapper;

    public JsonSchemaDocumentSearchService(
        EntityManager entityManager,
        QueryDialectHelper queryDialectHelper,
        SearchFieldService searchFieldService,
        UserManagementService userManagementService,
        AuthorizationService authorizationService, OutboxService outboxService,
        ObjectMapper objectMapper
    ) {
        this.entityManager = entityManager;
        this.queryDialectHelper = queryDialectHelper;
        this.searchFieldService = searchFieldService;
        this.userManagementService = userManagementService;
        this.authorizationService = authorizationService;
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Page<JsonSchemaDocument> search(
        final SearchRequest searchRequest,
        final Pageable pageable
    ) {
        return withLoggingContext("documentDefinitionName", searchRequest.getDocumentDefinitionName(), () ->
            search(
                (cb, query, documentRoot) -> buildQueryWhere(searchRequest, cb, query, documentRoot),
                pageable
            )
        );
    }

    @Override
    public Page<JsonSchemaDocument> search(
        @LoggableResource("documentDefinitionName") String documentDefinitionName,
        SearchWithConfigRequest searchWithConfigRequest,
        Pageable pageable
    ) {
        ZoneOffset zoneOffset = RequestHelper.getZoneOffset();
        var searchFieldMap = searchFieldService.getSearchFields(documentDefinitionName).stream()
            .collect(toMap(SearchField::getKey, searchField -> searchField));

        var searchCriteria = searchWithConfigRequest.getOtherFilters().stream()
            .map(otherFilter -> SearchRequestMapper.toOtherFilter(
                otherFilter,
                searchFieldMap.get(otherFilter.getKey()),
                zoneOffset
            ))
            .toList();

        var advancedSearchRequest = SearchRequestMapper.toAdvancedSearchRequest(searchWithConfigRequest, searchCriteria);

        return search(documentDefinitionName, advancedSearchRequest, pageable);
    }

    @Override
    public Page<JsonSchemaDocument> search(
        @LoggableResource("documentDefinitionName") String documentDefinitionName,
        AdvancedSearchRequest advancedSearchRequest,
        Pageable pageable
    ) {
        SearchRequestValidator.validate(advancedSearchRequest);
        return search(
            (cb, query, documentRoot) -> buildQueryWhere(documentDefinitionName, advancedSearchRequest, cb, query, documentRoot),
            pageable
        );
    }

    @Override
    public Long count(
        @LoggableResource("documentDefinitionName") String documentDefinitionName,
        AdvancedSearchRequest advancedSearchRequest
    ) {
        return count(
            (cb, query, documentRoot) -> buildQueryWhere(documentDefinitionName, advancedSearchRequest, cb, query, documentRoot)
        );
    }

    private Page<JsonSchemaDocument> search(QueryWhereBuilder queryWhereBuilder, Pageable pageable) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JsonSchemaDocument> query = cb.createQuery(JsonSchemaDocument.class);
        final Root<JsonSchemaDocument> selectRoot = query.from(JsonSchemaDocument.class);

        query.select(selectRoot);
        queryWhereBuilder.apply(cb, query, selectRoot);
        query.orderBy(getOrderBy(query, cb, selectRoot, pageable.getSort()));
        final TypedQuery<JsonSchemaDocument> typedQuery = entityManager.createQuery(query);

        if (pageable.isPaged()) {
            typedQuery
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());
        }

        List<JsonSchemaDocument> documents = typedQuery.getResultList();
        outboxService.send(() ->
            new DocumentsListed(
                objectMapper.valueToTree(documents)
            )
        );
        return new PageImpl<>(documents, pageable, count(queryWhereBuilder));
    }

    private Long count(QueryWhereBuilder queryWhereBuilder) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<JsonSchemaDocument> countRoot = countQuery.from(JsonSchemaDocument.class);
        countQuery.select(cb.countDistinct(countRoot));
        queryWhereBuilder.apply(cb, countQuery, countRoot);

        // TODO: Should be turned into a subquery, and then do a count over the results from the subquery.
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private void buildQueryWhere(SearchRequest searchRequest, CriteriaBuilder cb, CriteriaQuery<?> query, Root<JsonSchemaDocument> documentRoot) {
        final List<Predicate> predicates = new ArrayList<>();

        addNonJsonFieldPredicates(cb, documentRoot, searchRequest, predicates);
        addJsonFieldPredicates(cb, documentRoot, searchRequest, predicates);

        predicates.add(
            authorizationService
                .getAuthorizationSpecification(
                    new EntityAuthorizationRequest<>(
                        JsonSchemaDocument.class,
                        VIEW_LIST
                    ),
                    null
                ).toPredicate(documentRoot, query, cb));

        query.where(predicates.toArray(Predicate[]::new));
    }

    private void buildQueryWhere(
        String documentDefinitionName,
        AdvancedSearchRequest searchRequest,
        CriteriaBuilder cb,
        CriteriaQuery<?> query,
        Root<JsonSchemaDocument> documentRoot
    ) {
        final List<Predicate> predicates = new ArrayList<>();

        if (!StringUtils.isEmpty(documentDefinitionName)) {
            predicates.add(cb.equal(documentRoot.get(DOCUMENT_DEFINITION_ID).get(NAME), documentDefinitionName));
        }

        predicates.add(
            authorizationService
                .getAuthorizationSpecification(
                    new EntityAuthorizationRequest<>(
                        JsonSchemaDocument.class,
                        VIEW_LIST
                    ),
                    null
                ).toPredicate(documentRoot, query, cb));

        if (searchRequest.getAssigneeFilter() != null && searchRequest.getAssigneeFilter() != AssigneeFilter.ALL) {
            predicates.add(getAssigneeFilterPredicate(cb, documentRoot, searchRequest.getAssigneeFilter()));
        }

        if (searchRequest.getOtherFilters() != null && !searchRequest.getOtherFilters().isEmpty()) {
            predicates.add(getOtherFilersPredicate(cb, documentRoot, searchRequest));
        }

        if (searchRequest.getStatusFilter() != null && !searchRequest.getStatusFilter().isEmpty()) {
            predicates.add(getStatusFilterPredicate(cb, documentRoot, searchRequest.getStatusFilter()));
        }
        query.where(predicates.toArray(Predicate[]::new));
    }

    private void addNonJsonFieldPredicates(
        CriteriaBuilder cb, Root<JsonSchemaDocument> root,
        SearchRequest searchRequest, List<Predicate> predicates
    ) {

        if (!StringUtils.isEmpty(searchRequest.getDocumentDefinitionName())) {
            predicates.add(cb.equal(
                root.get(DOCUMENT_DEFINITION_ID).get(NAME),
                searchRequest.getDocumentDefinitionName()
            ));
        }

        if (!StringUtils.isEmpty(searchRequest.getCreatedBy())) {
            predicates.add(cb.equal(root.get(CREATED_BY), searchRequest.getCreatedBy()));
        }

        if (searchRequest.getSequence() != null) {
            predicates.add(cb.equal(root.get(SEQUENCE), searchRequest.getSequence()));
        }

        if (!StringUtils.isEmpty(searchRequest.getGlobalSearchFilter())) {
            predicates.add(findJsonValue(cb, root, searchRequest.getGlobalSearchFilter()));
        }
    }

    private void addJsonFieldPredicates(
        CriteriaBuilder cb, Root<JsonSchemaDocument> root,
        SearchRequest searchRequest, List<Predicate> predicates
    ) {

        if (searchRequest.getOtherFilters() != null && !searchRequest.getOtherFilters().isEmpty()) {
            Map<String, List<SearchCriteria>> criteriaPerPath = searchRequest.getOtherFilters()
                .stream()
                .collect(groupingBy(SearchCriteria::getPath));

            List<Predicate> criteriaPredicates = criteriaPerPath.entrySet()
                .stream()
                .flatMap(pathEntry -> {
                    if (pathEntry.getValue().size() == 1) {
                        return Stream.of(
                            findJsonPathValue(
                                cb,
                                root,
                                pathEntry.getKey(),
                                pathEntry.getValue().get(0).getValue()
                            ));
                    } else {
                        return Stream.of(cb.or(
                            pathEntry.getValue().stream()
                                .map(currentCriteria -> findJsonPathValue(
                                    cb,
                                    root,
                                    currentCriteria.getPath(),
                                    currentCriteria.getValue()
                                ))
                                .toList()
                                .toArray(Predicate[]::new)
                        ));
                    }
                })
                .toList();

            predicates.add(cb.and(criteriaPredicates.toArray(Predicate[]::new)));
        }
    }

    private Predicate getAssigneeFilterPredicate(CriteriaBuilder cb, Root<JsonSchemaDocument> documentRoot, AssigneeFilter assigneeFilter) {
        var caseAssigneeIdColumn = documentRoot.get(ASSIGNEE_ID);
        var userId = userManagementService.getCurrentUser().getUserIdentifier();

        return switch (assigneeFilter) {
            case MINE -> cb.equal(caseAssigneeIdColumn, userId);
            case OPEN -> cb.isNull(caseAssigneeIdColumn);
            default -> null;
        };
    }

    private Predicate getOtherFilersPredicate(
        CriteriaBuilder cb,
        Root<JsonSchemaDocument> root,
        AdvancedSearchRequest searchRequest
    ) {
        var jsonPredicates = searchRequest.getOtherFilters().stream()
            .map(currentCriteria -> buildQueryForSearchCriteria(cb, root, currentCriteria))
            .toList()
            .toArray(Predicate[]::new);

        if (searchRequest.getSearchOperator() == SearchOperator.AND) {
            return cb.and(jsonPredicates);
        } else {
            return cb.or(jsonPredicates);
        }
    }

    private Predicate getStatusFilterPredicate(CriteriaBuilder cb, Root<JsonSchemaDocument> documentRoot, Set<String> statusFilter) {
        Path<String> statusField = stringToPath(documentRoot, INTERNAL_STATUS_KEY);
        Predicate[] predicates = statusFilter.stream().map(status -> {
                if (status == null || status.isEmpty()) {
                    return cb.isNull(statusField);
                } else {
                    return cb.equal(statusField, status);
                }
            }
        ).toArray(Predicate[]::new);

        return cb.or(predicates);
    }

    private Predicate findJsonPathValue(CriteriaBuilder cb, Root<JsonSchemaDocument> root, String path, String value) {
        return queryDialectHelper.getJsonValueExistsInPathExpression(cb, root.get(CONTENT).get(CONTENT), path, value);
    }

    private Predicate findJsonValue(CriteriaBuilder cb, Root<JsonSchemaDocument> root, String value) {
        return queryDialectHelper.getJsonValueExistsExpression(cb, root.get(CONTENT).get(CONTENT), value);
    }

    private <T extends Comparable<? super T>> Predicate buildQueryForSearchCriteria(
        CriteriaBuilder cb,
        Root<JsonSchemaDocument> root,
        AdvancedSearchRequest.OtherFilter searchCriteria
    ) {
        Expression<T> value;
        if (searchCriteria.getPath().startsWith(DOC_PREFIX)) {
            value = getValueExpressionForDocPrefix(cb, root, searchCriteria);
        } else if (searchCriteria.getPath().startsWith(CASE_PREFIX)) {
            value = getValueExpressionForCasePrefix(root, searchCriteria);
        } else {
            throw new IllegalArgumentException("Search path doesn't start with known prefix: '" + searchCriteria.getPath() + "'");
        }

        var rangeFrom = searchCriteria.<T>getRangeFrom();
        var rangeTo = searchCriteria.<T>getRangeTo();

        return switch (searchCriteria.getSearchType()) {
            case LIKE -> cb.or(searchLike(cb, value, searchCriteria.getValues()));
            case EQUAL -> cb.or(searchEqual(cb, value, searchCriteria.getValues()));
            case GREATER_THAN_OR_EQUAL_TO -> searchGreaterThanOrEqualTo(cb, value, rangeFrom);
            case LESS_THAN_OR_EQUAL_TO -> searchLessThanOrEqualTo(cb, value, rangeTo);
            case BETWEEN -> searchBetween(cb, value, rangeFrom, rangeTo);
            case IN -> searchIn(cb, value, searchCriteria.getValues());
            default -> throw new NotImplementedException("Searching for search type '" + searchCriteria.getSearchType() + "' hasn't been implemented.");
        };
    }

    private <T extends Comparable<? super T>> Expression<T> getValueExpressionForDocPrefix(
        CriteriaBuilder cb,
        Root<JsonSchemaDocument> documentRoot,
        AdvancedSearchRequest.OtherFilter searchCriteria
    ) {
        var jsonPath = "$." + searchCriteria.getPath().substring(DOC_PREFIX.length());
        return queryDialectHelper.getJsonValueExpression(
            cb,
            documentRoot.get(CONTENT).get(CONTENT),
            jsonPath,
            searchCriteria.getDataType()
        );
    }

    private <T extends Comparable<? super T>> Expression<T> getValueExpressionForCasePrefix(
        Root<JsonSchemaDocument> documentRoot,
        AdvancedSearchRequest.OtherFilter searchCriteria
    ) {
        var documentColumnName = searchCriteria.getPath().substring(CASE_PREFIX.length());
        return documentRoot.get(documentColumnName).as(searchCriteria.getDataType());
    }

    @SuppressWarnings("unchecked")
    private <T> Predicate[] searchEqual(CriteriaBuilder cb, Expression<T> jsonValue, List<T> values) {
        if (values.isEmpty()) {
            return new Predicate[0];
        } else if (values.stream().anyMatch(value -> !(value instanceof String))) {
            return values.stream()
                .map(value -> cb.equal(jsonValue, value))
                .toArray(Predicate[]::new);
        } else {
            var jsonValueLower = cb.lower((Expression<String>) jsonValue);
            return values.stream()
                .map(value -> value.toString().trim().toLowerCase())
                .map(stringValue -> cb.equal(jsonValueLower, stringValue))
                .toArray(Predicate[]::new);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Predicate[] searchLike(CriteriaBuilder cb, Expression<T> jsonValue, List<T> values) {
        if (values.isEmpty()) {
            return new Predicate[0];
        } else if (values.stream().anyMatch(value -> !(value instanceof String))) {
            throw new IllegalArgumentException("Failed to do LIKE search. Reason: values '" + Arrays.toString(values.toArray()) + "' aren't of type 'String'");
        } else {
            var jsonValueLower = cb.lower((Expression<String>) jsonValue);
            return values.stream()
                .map(value -> value.toString().trim().toLowerCase())
                .map(stringValue -> cb.like(jsonValueLower, "%" + stringValue + "%"))
                .toArray(Predicate[]::new);
        }
    }

    private <T> Predicate searchIn(CriteriaBuilder cb, Expression<T> jsonValue, List<T> values) {
        var in = cb.in(jsonValue);
        values.forEach(in::value);
        return in;
    }

    private <T extends Comparable<? super T>> Predicate searchGreaterThanOrEqualTo(CriteriaBuilder cb, Expression<T> documentValue, T rangeFrom) {
        if (rangeFrom instanceof TemporalAccessor) {
            var documentValueTimestamp = documentValue.as(java.util.Date.class);
            return cb.greaterThanOrEqualTo(documentValueTimestamp, toJavaUtilDate(rangeFrom));
        } else {
            return cb.greaterThanOrEqualTo(documentValue, cb.literal(rangeFrom));
        }
    }

    private <T extends Comparable<? super T>> Predicate searchLessThanOrEqualTo(CriteriaBuilder cb, Expression<T> documentValue, T rangeTo) {
        if (rangeTo instanceof TemporalAccessor) {
            var documentValueTimestamp = documentValue.as(java.util.Date.class);
            return cb.lessThanOrEqualTo(documentValueTimestamp, toJavaUtilDate(rangeTo));
        } else {
            return cb.lessThanOrEqualTo(documentValue, cb.literal(rangeTo));
        }
    }

    private <T extends Comparable<? super T>> Predicate searchBetween(CriteriaBuilder cb, Expression<T> documentValue, T rangeFrom, T rangeTo) {
        if (rangeFrom instanceof TemporalAccessor) {
            var documentValueTimestamp = documentValue.as(java.util.Date.class);
            return cb.between(documentValueTimestamp, toJavaUtilDate(rangeFrom), toJavaUtilDate(rangeTo));
        } else {
            return cb.between(documentValue, cb.literal(rangeFrom), cb.literal(rangeTo));
        }
    }

    /**
     * Note: The CriteriaBuilder only supports with java.sql.Timestamp/Date/Time. These types extend java.util.Date
     */
    private static java.util.Date toJavaUtilDate(Object value) {
        if (value instanceof LocalDate) {
            return java.util.Date.from(((LocalDate) value).atStartOfDay().toInstant(ZoneOffset.UTC));
        } else if (value instanceof Instant) {
            return java.sql.Timestamp.from((Instant) value);
        } else if (value instanceof LocalDateTime) {
            return java.sql.Timestamp.from(((LocalDateTime) value).toInstant(ZoneOffset.UTC));
        } else if (value instanceof OffsetDateTime) {
            return java.sql.Timestamp.from(((OffsetDateTime) value).toInstant());
        } else if (value instanceof ZonedDateTime) {
            return java.sql.Timestamp.from(((ZonedDateTime) value).toInstant());
        } else if (value instanceof LocalTime) {
            return java.sql.Time.valueOf((LocalTime) value);
        } else {
            throw new NotImplementedException("Failed to cast '" + value + "' to java.util.Date");
        }
    }

    private List<Order> getOrderBy(
        CriteriaQuery<JsonSchemaDocument> query,
        CriteriaBuilder cb,
        Root<JsonSchemaDocument> root,
        Sort sort
    ) {
        return sort.stream()
            .map(order -> {
                Expression<?> expression;
                String property = order.getProperty();
                if (property.startsWith(DOC_PREFIX)) {
                    var jsonPath = "$." + property.substring(DOC_PREFIX.length());
                    expression = queryDialectHelper.getJsonValueExpression(cb, root.get(CONTENT).get(CONTENT), jsonPath, String.class);
                } else if (property.startsWith("$.")) {
                    expression = cb.lower(queryDialectHelper.getJsonValueExpression(
                        cb,
                        root.get(CONTENT).get(CONTENT),
                        property,
                        String.class
                    ));
                } else {
                    var docProperty = property.startsWith(CASE_PREFIX) ? property.substring(CASE_PREFIX.length()) : property;
                    if (DOCUMENT_FIELD_MAP.containsKey(docProperty)) {
                        docProperty = DOCUMENT_FIELD_MAP.get(docProperty);
                    }

                    Path<?> parent;
                    if( docProperty.equals(INTERNAL_STATUS_ORDER)) {
                        parent = root.join(INTERNAL_STATUS, JoinType.LEFT);
                        docProperty = docProperty.substring(INTERNAL_STATUS.length() + 1);
                    } else {
                        parent = root;
                    }

                    var path = stringToPath(parent, docProperty);
                    // This groupBy workaround is needed because PBAC adds a groupBy on 'id' by default.
                    // Since sorting columns should be added to the groupBy, we do that here
                    if (!query.getGroupList().isEmpty() && !query.getGroupList().contains(path)) {
                        ArrayList<Expression<?>> grouping = new ArrayList<>(query.getGroupList());
                        grouping.add(path);
                        query.groupBy(grouping);
                    }
                    expression = path;
                }

                return order.getDirection().isAscending() ? cb.asc(expression) : cb.desc(expression);
            })
            .collect(Collectors.toList());
    }

    private <T> Path<T> stringToPath(Path<?> parent, String path) {
        String[] split = path.split("\\.");
        Path<?> result = parent;
        for (String s : split) {
            result = result.get(s);
        }
        return (Path<T>) result;
    }

    @FunctionalInterface
    public interface QueryWhereBuilder {
        void apply(
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query,
            Root<JsonSchemaDocument> documentRoot
        );
    }
}
