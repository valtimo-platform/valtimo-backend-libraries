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

package com.ritense.processdocument.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.domain.DisplayType
import com.ritense.case.domain.EmptyDisplayTypeParameter
import com.ritense.case.domain.TaskListColumn
import com.ritense.case.domain.TaskListColumnId
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.search.DatabaseSearchType
import com.ritense.document.domain.search.SearchOperator
import com.ritense.document.service.JsonSchemaDocumentActionProvider
import com.ritense.processdocument.domain.CaseTask
import com.ritense.processdocument.tasksearch.AdvancedSearchRequest
import com.ritense.processdocument.tasksearch.SearchRequestMapper
import com.ritense.processdocument.tasksearch.SearchWithConfigRequest
import com.ritense.processdocument.web.result.TaskListRowDto
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.contract.utils.RequestHelper
import com.ritense.valtimo.service.CamundaTaskService.TaskFilter
import com.ritense.valueresolver.ValueResolverService
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Order
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.apache.commons.lang3.NotImplementedException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.sql.Time
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.TemporalAccessor
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.function.Consumer
import java.util.stream.Collectors


const val SEARCH_FIELD_OWNER_TYPE = "TaskListSearchColumns"

@Service
@SkipComponentScan
class CaseTaskListSearchService(
    private val entityManager: EntityManager,
    private val valueResolverService: ValueResolverService,
    private val taskListColumnRepository: TaskListColumnRepository,
    private val userManagementService: UserManagementService,
    private val authorizationService: AuthorizationService,
    private val searchFieldV2Service: SearchFieldV2Service,
    private val queryDialectHelper: QueryDialectHelper
) {
    private val CONTENT = "content"
    private val INTERNAL_STATUS = "internalStatus"
    private val INTERNAL_STATUS_ORDER = "internalStatus.order"
    private val DOC_PREFIX = "doc:"
    private val CASE_PREFIX = "case:"
    private val TASK_PREFIX = "task:"
    private val DOCUMENT_FIELD_MAP: Map<String, String> = mapOf(
        "definitionId.name" to "documentDefinitionId.name",
        "definitionId.version" to "documentDefinitionId.key",
        INTERNAL_STATUS to INTERNAL_STATUS_ORDER
    )

    fun getTasksByCaseDefinition(caseDefinitionName: String, assignmentFilter: TaskFilter, pageable: Pageable): Page<TaskListRowDto> {
        val taskListColumns = taskListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
            caseDefinitionName
        ).ifEmpty { defaultColumns }
        val newPageable = mutatePageable(taskListColumns, pageable)

        return search(caseDefinitionName, AdvancedSearchRequest().assigneeFilter(assignmentFilter), newPageable)
            .map { task -> toCaseListRowDto(task, taskListColumns) }
    }

    fun searchTaskListRows(
        caseDefinitionName: String,
        searchWithConfigRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): Page<TaskListRowDto> {
        val taskListColumns = taskListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
            caseDefinitionName
        ).ifEmpty { defaultColumns }
        val newPageable = mutatePageable(taskListColumns, pageable)

        return search(caseDefinitionName, searchWithConfigRequest, newPageable)
            .map { task -> toCaseListRowDto(task, taskListColumns) }
    }

    fun search(
        caseDefinitionName: String,
        searchWithConfigRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): Page<CaseTask> {
        val zoneOffset = RequestHelper.getZoneOffset()
        val searchFieldMap: Map<String, SearchFieldV2> =
            searchFieldV2Service.findAllByOwnerTypeAndOwnerId(SEARCH_FIELD_OWNER_TYPE, caseDefinitionName)
                .associateBy { it.key }

        val searchCriteria = searchWithConfigRequest.otherFilters.stream()
            .map { otherFilter: SearchWithConfigRequest.SearchWithConfigFilter ->
                SearchRequestMapper.toOtherFilter(
                    otherFilter,
                    searchFieldMap[otherFilter.key],
                    zoneOffset
                )
            }
            .toList()

        val advancedSearchRequest = SearchRequestMapper.toAdvancedSearchRequest(searchWithConfigRequest, searchCriteria)

        return search(caseDefinitionName, advancedSearchRequest, pageable)
    }

    fun search(caseDefinitionName: String, advancedSearchRequest: AdvancedSearchRequest, pageable: Pageable): Page<CaseTask> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(CaseTask::class.java)
        val taskRoot = query.from(CamundaTask::class.java)
        val documentRoot = query.from(JsonSchemaDocument::class.java)

        val selectCols = arrayOf(
            taskRoot.get<String>("id"),
            taskRoot.get<LocalDateTime?>(CaseTaskProperties.CREATE_TIME.propertyName),
            taskRoot.get<String?>(CaseTaskProperties.NAME.propertyName),
            taskRoot.get<String?>(CaseTaskProperties.ASSIGNEE.propertyName),
            taskRoot.get<LocalDateTime?>(CaseTaskProperties.DUE_DATE.propertyName),
            taskRoot.get<CamundaExecution?>("processInstance").get<String>("id"),
            documentRoot.get<JsonSchemaDocumentId>("id").get<UUID>("id")
        )

        query.select(
            cb.construct(
                CaseTask::class.java,
                *selectCols
            )
        )

        // Due to the JsonSchemaDocumentSpecification#toPredicate adding a groupBy (which is called when applying PBAC)
        // ...we are forced to add all the columns we want to select, to the group by.
        // This can be removed if we have a solution for this group by (TP story #106335)
        val groupList = query.groupList.toMutableList()
        groupList.addAll(selectCols)
        query.groupBy(groupList)

        // TODO: look into ability to re-use where predicate in list and count query. improves performance
        query.where(constructWhere(cb, query, taskRoot, documentRoot, caseDefinitionName, advancedSearchRequest))

        query.orderBy(constructOrderBy(query, cb, taskRoot, documentRoot, pageable.sort))

        val pagedQuery = entityManager.createQuery(query)
            .setFirstResult(pageable.offset.toInt())
            .setMaxResults(pageable.pageSize)

        return PageImpl(pagedQuery.resultList, pageable, count(caseDefinitionName, advancedSearchRequest))
    }

    private fun count(caseDefinitionName: String, advancedSearchRequest: AdvancedSearchRequest): Long {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(Long::class.java)
        val taskRoot = query.from(CamundaTask::class.java)
        val documentRoot = query.from(JsonSchemaDocument::class.java)
        query.select(cb.countDistinct(taskRoot))
        query.where(constructWhere(cb, query, taskRoot, documentRoot, caseDefinitionName, advancedSearchRequest))
        return entityManager.createQuery(query).singleResult
    }

    private fun constructWhere(
        cb: CriteriaBuilder,
        query: CriteriaQuery<*>,
        taskRoot: Root<CamundaTask>,
        documentRoot: Root<JsonSchemaDocument>,
        caseDefinitionName: String,
        advancedSearchRequest: AdvancedSearchRequest
    ): Predicate? {
        val authorizationPredicate: Predicate =
            getAuthorizationSpecification(CamundaTaskActionProvider.VIEW_LIST).toPredicate(taskRoot, query, cb)

        val assignmentFilterPredicate: Predicate = constructAssignmentFilter(advancedSearchRequest.assigneeFilter, cb, taskRoot)

        // TODO: look into options to improve performance as with complex rules this takes quite some time to finish
        val searchRequestPredicate: Array<Predicate> = constructSearchCriteriaFilter(advancedSearchRequest, cb, query, taskRoot, documentRoot)

        val where = cb.and(
            cb.equal(
                documentRoot.get<JsonSchemaDocumentDefinitionId>("documentDefinitionId").get<String>("name"),
                caseDefinitionName
            ),
            cb.equal(
                taskRoot.get<CamundaExecution>("processInstance").get<String>("businessKey"),
                queryDialectHelper.uuidToString(cb, documentRoot.get<JsonSchemaDocumentId>("id").get("id"))
            ),
            assignmentFilterPredicate,
            authorizationPredicate,
            *searchRequestPredicate
        )
        return where
    }

    private fun constructAssignmentFilter(
        assignmentFilter: TaskFilter,
        cb: CriteriaBuilder,
        taskRoot: Root<CamundaTask>
    ): Predicate {
        val assignmentFilterPredicate: Predicate = when (assignmentFilter) {
            TaskFilter.MINE -> {
                val userIdentifier = userManagementService.currentUser.userIdentifier
                cb.and(cb.equal(taskRoot.get<Any>(CamundaTaskSpecificationHelper.ASSIGNEE), userIdentifier))
            }

            TaskFilter.ALL -> {
                cb.equal(cb.literal(1), 1)
            }

            TaskFilter.OPEN -> {
                cb.and(taskRoot.get<Any>(CamundaTaskSpecificationHelper.ASSIGNEE).isNull)
            }
        }
        return assignmentFilterPredicate
    }

    private fun constructSearchCriteriaFilter(
        searchRequest: AdvancedSearchRequest,
        cb: CriteriaBuilder,
        query: CriteriaQuery<*>,
        taskRoot: Root<CamundaTask>,
        documentRoot: Root<JsonSchemaDocument>
    ): Array<Predicate> {
        val predicates = mutableListOf<Predicate>()

        predicates.add(
            authorizationService
                .getAuthorizationSpecification(
                    EntityAuthorizationRequest(
                        JsonSchemaDocument::class.java,
                        JsonSchemaDocumentActionProvider.VIEW_LIST
                    ),
                    null
                ).toPredicate(documentRoot, query, cb)
        )

        if (searchRequest.otherFilters != null && searchRequest.otherFilters.isNotEmpty()) {
            predicates.add(getOtherFiltersPredicate(cb, taskRoot, documentRoot, searchRequest))
        }

        if (searchRequest.statusFilter != null && searchRequest.statusFilter.isNotEmpty()) {
            predicates.add(getStatusFilterPredicate(cb, documentRoot, searchRequest.statusFilter))
        }

        return predicates.toTypedArray()
    }

    private fun getOtherFiltersPredicate(
        cb: CriteriaBuilder,
        taskRoot: Root<CamundaTask>,
        documentRoot: Root<JsonSchemaDocument>,
        searchRequest: AdvancedSearchRequest
    ): Predicate {
        val jsonPredicates: Array<Predicate> = searchRequest.otherFilters.map { currentCriteria: AdvancedSearchRequest.OtherFilter ->
            buildQueryForSearchCriteria(
                cb,
                taskRoot,
                documentRoot,
                currentCriteria
            )
        }.toTypedArray()

        return if (searchRequest.searchOperator == SearchOperator.AND) {
            cb.and(*jsonPredicates)
        } else {
            cb.or(*jsonPredicates)
        }
    }

    private fun getStatusFilterPredicate(
        cb: CriteriaBuilder,
        documentRoot: Root<JsonSchemaDocument>,
        statusFilter: Set<String>
    ): Predicate {
        val statusField = stringToPath<String>(documentRoot, "internalStatus.id.key")
        val predicates: Array<Predicate> = statusFilter.map { status: String? ->
            return if (status.isNullOrEmpty()) {
                cb.isNull(statusField)
            } else {
                cb.equal(statusField, status)
            }
        }.toTypedArray()

        return cb.or(*predicates)
    }

    private fun buildQueryForSearchCriteria(
        cb: CriteriaBuilder,
        taskRoot: Root<CamundaTask>,
        documentRoot: Root<JsonSchemaDocument>,
        searchCriteria: AdvancedSearchRequest.OtherFilter
    ): Predicate {
        val value: Expression<Comparable<Any>> =
            if (searchCriteria.path.startsWith(DOC_PREFIX)) {
                getValueExpressionForDocPrefix(cb, documentRoot, searchCriteria)
            } else if (searchCriteria.path.startsWith(CASE_PREFIX)) {
                getValueExpressionForCasePrefix(documentRoot, searchCriteria)
            } else if (searchCriteria.path.startsWith(TASK_PREFIX)) {
                getValueExpressionForTaskPrefix(taskRoot, searchCriteria)
            } else {
                throw IllegalArgumentException("Search path doesn't start with known prefix: '" + searchCriteria.path + "'")
            }

        val rangeFrom = searchCriteria.getRangeFrom<Comparable<Any>>()
        val rangeTo = searchCriteria.getRangeTo<Comparable<Any>>()

        return when (searchCriteria.searchType) {
            DatabaseSearchType.LIKE -> cb.or(*searchLike(cb, value, searchCriteria.getValues()))
            DatabaseSearchType.EQUAL -> cb.or(*searchEqual(cb, value, searchCriteria.getValues()))
            DatabaseSearchType.GREATER_THAN_OR_EQUAL_TO -> searchGreaterThanOrEqualTo(cb, value, rangeFrom)
            DatabaseSearchType.LESS_THAN_OR_EQUAL_TO -> searchLessThanOrEqualTo(cb, value, rangeTo)
            DatabaseSearchType.BETWEEN -> searchBetween(cb, value, rangeFrom, rangeTo)
            DatabaseSearchType.IN -> searchIn(cb, value, searchCriteria.getValues())
            else -> throw NotImplementedException("Searching for search type '" + searchCriteria.searchType + "' hasn't been implemented.")
        }
    }

    private fun getValueExpressionForDocPrefix(
        cb: CriteriaBuilder,
        documentRoot: Root<JsonSchemaDocument>,
        searchCriteria: AdvancedSearchRequest.OtherFilter
    ): Expression<Comparable<Any>> {
        val jsonPath = "$." + quoteJsonPath(searchCriteria.path.substring(DOC_PREFIX.length))
        return queryDialectHelper.getJsonValueExpression(
            cb,
            documentRoot.get<Any>("content")
                .get<Any>("content"),
            jsonPath,
            searchCriteria.getDataType()
        )
    }

    private fun getValueExpressionForTaskPrefix(
        taskRoot: Root<CamundaTask>,
        searchCriteria: AdvancedSearchRequest.OtherFilter
    ): Expression<Comparable<Any>> {
        val taskColumnName = searchCriteria.path.substring(TASK_PREFIX.length)
        return taskRoot.get<Any>(taskColumnName).`as`(searchCriteria.getDataType())
    }

    private fun getValueExpressionForCasePrefix(
        documentRoot: Root<JsonSchemaDocument>,
        searchCriteria: AdvancedSearchRequest.OtherFilter
    ): Expression<Comparable<Any>> {
        val documentColumnName = searchCriteria.path.substring(CASE_PREFIX.length)
        return documentRoot.get<Any>(documentColumnName).`as`(searchCriteria.getDataType())
    }

    @Suppress("UNCHECKED_CAST")
    private fun searchLike(cb: CriteriaBuilder, jsonValue: Expression<Comparable<Any>>, values: List<Any>): Array<Predicate?> {
        if (values.isEmpty()) {
            return arrayOfNulls(0)
        } else require(
            !values.stream().anyMatch { value -> value !is String }) {
            "Failed to do LIKE search. Reason: values '" + values.toTypedArray()
                .contentToString() + "' aren't of type 'String'"
        }
        val jsonValueLower = cb.lower(jsonValue as Expression<String>)
        return values.map { value ->
            value.toString().trim { it <= ' ' }
                .lowercase(Locale.getDefault())
        }
            .map { stringValue: String ->
                cb.like(
                    jsonValueLower,
                    "%$stringValue%"
                )
            }
            .toTypedArray()
    }

    @Suppress("UNCHECKED_CAST")
    private fun searchEqual(cb: CriteriaBuilder, jsonValue: Expression<Comparable<Any>>, values: List<Any>): Array<Predicate?> {
        if (values.isEmpty()) {
            return arrayOfNulls(0)
        } else if (values.stream().anyMatch { value -> value !is String }) {
            return values
                .map { value ->
                    cb.equal(
                        jsonValue,
                        value
                    )
                }
                .toTypedArray()
        } else {
            val jsonValueLower = cb.lower(jsonValue as Expression<String?>)
            return values.map { value ->
                value.toString().trim { it <= ' ' }
                    .lowercase(Locale.getDefault())
            }
                .map { stringValue: String? ->
                    cb.equal(
                        jsonValueLower,
                        stringValue
                    )
                }
                .toTypedArray()
        }
    }

    private fun searchGreaterThanOrEqualTo(
        cb: CriteriaBuilder,
        documentValue: Expression<Comparable<Any>>,
        rangeFrom: Comparable<Any>
    ): Predicate {
        return if (rangeFrom is TemporalAccessor) {
            val documentValueTimestamp = documentValue.`as`(
                Date::class.java
            )
            cb.greaterThanOrEqualTo(
                documentValueTimestamp,
                toJavaUtilDate(rangeFrom)
            )
        } else {
            cb.greaterThanOrEqualTo(documentValue, cb.literal(rangeFrom))
        }
    }

    private fun searchLessThanOrEqualTo(
        cb: CriteriaBuilder,
        documentValue: Expression<Comparable<Any>>,
        rangeTo: Comparable<Any>
    ): Predicate {
        return if (rangeTo is TemporalAccessor) {
            val documentValueTimestamp = documentValue.`as`(
                Date::class.java
            )
            cb.lessThanOrEqualTo(documentValueTimestamp, toJavaUtilDate(rangeTo))
        } else {
            cb.lessThanOrEqualTo(documentValue, cb.literal(rangeTo))
        }
    }

    private fun searchBetween(
        cb: CriteriaBuilder,
        documentValue: Expression<Comparable<Any>>,
        rangeFrom: Comparable<Any>,
        rangeTo: Comparable<Any>
    ): Predicate {
        return if (rangeFrom is TemporalAccessor) {
            val documentValueTimestamp = documentValue.`as`(
                Date::class.java
            )
            cb.between(
                documentValueTimestamp,
                toJavaUtilDate(rangeFrom),
                toJavaUtilDate(rangeTo)
            )
        } else {
            cb.between(documentValue, cb.literal(rangeFrom), cb.literal(rangeTo))
        }
    }

    private fun searchIn(cb: CriteriaBuilder, jsonValue: Expression<Comparable<Any>>, values: List<Comparable<Any>>): Predicate {
        val inCriteria = cb.`in`(jsonValue)
        values.forEach(Consumer { t: Comparable<Any> -> inCriteria.value(t) })
        return inCriteria
    }

    /**
     * Note: The CriteriaBuilder only supports with java.sql.Timestamp/Date/Time. These types extend java.util.Date
     */
    private fun toJavaUtilDate(value: Any): Date {
        return when (value) {
            is LocalDate -> Date.from((value).atStartOfDay().toInstant(ZoneOffset.UTC))
            is Instant -> Timestamp.from(value)
            is LocalDateTime -> Timestamp.from((value).toInstant(ZoneOffset.UTC))
            is OffsetDateTime -> Timestamp.from((value).toInstant())
            is ZonedDateTime -> Timestamp.from((value).toInstant())
            is LocalTime -> Time.valueOf(value)
            else -> throw NotImplementedException("Failed to cast '$value' to java.util.Date")
        }
    }

    private fun constructOrderBy(
        query: CriteriaQuery<*>,
        cb: CriteriaBuilder,
        taskRoot: Root<CamundaTask>,
        documentRoot: Root<JsonSchemaDocument>,
        sort: Sort
    ): List<Order> {
        return sort.stream()
            .map { order: Sort.Order ->
                val expression: Expression<*>
                val property = order.property
                when {
                    property.startsWith(DOC_PREFIX) -> {
                        val quotedPath = quoteJsonPath(property.substring(DOC_PREFIX.length))
                        val jsonPath = "$.${quotedPath}"
                        expression = queryDialectHelper.getJsonValueExpression(
                            cb,
                            documentRoot.get<JsonDocumentContent>(CONTENT)
                                .get<String>(CONTENT),
                            jsonPath,
                            String::class.java
                        )
                    }

                    property.startsWith("$.") -> {
                        expression = cb.lower(
                            queryDialectHelper.getJsonValueExpression(
                                cb,
                                documentRoot.get<JsonDocumentContent>(CONTENT)
                                    .get<String>(CONTENT),
                                property,
                                String::class.java
                            )
                        )
                    }

                    property.startsWith(TASK_PREFIX) -> {
                        expression = taskRoot.get<Any>(property.substring(TASK_PREFIX.length))
                    }

                    else -> {
                        var docProperty =
                            if (property.startsWith(CASE_PREFIX)) property.substring(
                                CASE_PREFIX.length
                            ) else property
                        if (DOCUMENT_FIELD_MAP.containsKey(docProperty)) {
                            docProperty = DOCUMENT_FIELD_MAP[docProperty]
                        }

                        val parent: Path<*>
                        if (docProperty == INTERNAL_STATUS_ORDER) {
                            parent = documentRoot.join<Any, Any>(
                                INTERNAL_STATUS,
                                JoinType.LEFT
                            )
                            docProperty = docProperty.substring(INTERNAL_STATUS.length + 1)
                        } else {
                            parent = documentRoot
                        }

                        val path: Path<Any> = stringToPath(parent, docProperty)
                        // This groupBy workaround is needed because PBAC adds a groupBy on 'id' by default.
                        // Since sorting columns should be added to the groupBy, we do that here
                        if (query.groupList.isNotEmpty() && !query.groupList.contains(path)) {
                            val grouping =
                                ArrayList(query.groupList)
                            grouping.add(path)
                            query.groupBy(grouping)
                        }
                        expression = path
                    }
                }
                if (order.direction.isAscending) cb.asc(expression) else cb.desc(expression)
            }
            .collect(Collectors.toList())
    }

    private fun getAuthorizationSpecification(action: Action<CamundaTask>): AuthorizationSpecification<CamundaTask> {
        return authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(CamundaTask::class.java, action),
            null
        )
    }

    private fun mutatePageable(taskListColumns: Collection<TaskListColumn>, pageable: Pageable): PageRequest {
        val newSortOrders = if (pageable.sort.isUnsorted) {
            // Default is the defaultSort or when absent, the first sortable column
            val defaultSortColumn = taskListColumns.find { it.defaultSort != null } ?: taskListColumns.find { it.sortable }
            val defaultSortDirection = if (defaultSortColumn?.defaultSort == ColumnDefaultSort.DESC) Sort.Direction.DESC else Sort.Direction.ASC

            if (defaultSortColumn != null) {
                Sort.by(defaultSortDirection, defaultSortColumn.path)
            } else {
                Sort.unsorted()
            }
        } else {
            pageable.sort.map { sortOrder ->
                val caseListColumn = taskListColumns.find { caseListColumn -> caseListColumn.id.key == sortOrder.property }
                val sortingProperty = caseListColumn?.path ?: sortOrder.property
                Sort.Order(sortOrder.direction, sortingProperty, sortOrder.nullHandling)
            }
        }
        val newSort = Sort.by(newSortOrders.toMutableList())
        return PageRequest.of(pageable.pageNumber, pageable.pageSize, newSort)
    }

    private fun toCaseListRowDto(caseTask: CaseTask, taskListColumns: List<TaskListColumn>): TaskListRowDto {
        val paths = taskListColumns.map { it.path }

        val (taskPaths, otherPaths) = paths.partition { it.startsWith(TASK_PREFIX) }

        val resolvedValuesMap = valueResolverService.resolveValues(caseTask.documentInstanceId.toString(), otherPaths).toMutableMap()
        resolvedValuesMap.putAll(taskPaths.map { taskPath -> resolveTaskValue(caseTask, taskPath) })

        val items = taskListColumns.map { caseListColumn ->
            TaskListRowDto.TaskListItemDto(caseListColumn.id.key, resolvedValuesMap[caseListColumn.path])
        }.toList()

        return TaskListRowDto(caseTask.taskId, caseTask.documentInstanceId.toString(), caseTask.processInstanceId, caseTask.name, caseTask.createTime, items)
    }

    private fun resolveTaskValue(caseTask: CaseTask, taskPath: String): Pair<String, Any?> {
        val value = runWithoutAuthorization {
            when (val path = taskPath.substringAfter(TASK_PREFIX).substringBefore(".")) {
                "assignee" -> {
                    CaseTaskProperties.getByPropertyName("assignee")
                        ?.getValueFromObject(caseTask)
                        ?.let { assigneeId -> userManagementService.findByUserIdentifier(assigneeId as String)?.fullName }
                }

                else -> CaseTaskProperties.getByPropertyName(path)?.getValueFromObject(caseTask)
            }
        }
        return taskPath to value
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> stringToPath(parent: Path<*>, path: String): Path<T> {
        val split = path.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var result = parent
        for (s in split) {
            result = result.get<Any>(s)
        }
        return result as Path<T>
    }

    private fun quoteJsonPath(property: String): String =
        property.split(".").joinToString(".") { "\"${it}\"" }

    companion object {
        val defaultColumns: List<TaskListColumn> = listOf(
            TaskListColumn(
                id = TaskListColumnId("Default", "createTime"),
                title = "createTime",
                path = "task:${CaseTaskProperties.CREATE_TIME.propertyName}",
                displayType = DisplayType("string", EmptyDisplayTypeParameter()),
                sortable = true,
                defaultSort = null,
                order = 1
            ),
            TaskListColumn(
                id = TaskListColumnId("Default", "name"),
                title = "name",
                path = "task:${CaseTaskProperties.NAME.propertyName}",
                displayType = DisplayType("string", EmptyDisplayTypeParameter()),
                sortable = true,
                defaultSort = null,
                order = 2
            ),
            TaskListColumn(
                id = TaskListColumnId("Default", "assignee"),
                title = "assignee",
                path = "task:${CaseTaskProperties.ASSIGNEE.propertyName}",
                displayType = DisplayType("string", EmptyDisplayTypeParameter()),
                sortable = true,
                defaultSort = null,
                order = 3
            ),
            TaskListColumn(
                id = TaskListColumnId("Default", "dueDate"),
                title = "dueDate",
                path = "task:${CaseTaskProperties.DUE_DATE.propertyName}",
                displayType = DisplayType("string", EmptyDisplayTypeParameter()),
                sortable = true,
                defaultSort = null,
                order = 4
            )
        )
    }
}

enum class CaseTaskProperties(val propertyName: String) {
    CREATE_TIME("createTime") {
        override fun getValueFromObject(caseTask: CaseTask) = caseTask.createTime
    },
    NAME("name") {
        override fun getValueFromObject(caseTask: CaseTask) = caseTask.name
    },
    ASSIGNEE("assignee") {
        override fun getValueFromObject(caseTask: CaseTask) = caseTask.assignee
    },
    DUE_DATE("dueDate") {
        override fun getValueFromObject(caseTask: CaseTask) = caseTask.dueDate
    };

    abstract fun getValueFromObject(caseTask: CaseTask): Any?

    override fun toString(): String {
        return propertyName
    }

    companion object {
        fun getNames() = entries.map { it.propertyName }
        fun getByPropertyName(propertyName: String) = entries.find { it.propertyName == propertyName }
    }
}