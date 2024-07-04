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
import com.ritense.processdocument.domain.CaseTask
import com.ritense.processdocument.web.result.TaskListRowDto
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.database.QueryDialectHelper
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
import java.time.LocalDateTime
import java.util.UUID
import java.util.stream.Collectors
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort


class CaseTaskListSearchService(
    private val entityManager: EntityManager,
    private val valueResolverService: ValueResolverService,
    private val taskListColumnRepository: TaskListColumnRepository,
    private val userManagementService: UserManagementService,
    private val authorizationService: AuthorizationService,
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

        return search(caseDefinitionName, assignmentFilter, newPageable)
            .map { task -> toCaseListRowDto(task, taskListColumns) }
    }

    private fun search(caseDefinitionName: String, assignmentFilter: TaskFilter, pageable: Pageable): Page<CaseTask> {
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

        val groupList = query.groupList.toMutableList()
        groupList.addAll(selectCols)
        query.groupBy(groupList)
        query.where(constructWhere(cb, query, taskRoot, documentRoot, caseDefinitionName, assignmentFilter))
        query.orderBy(constructOrderBy(query, cb, taskRoot, documentRoot, pageable.sort))

        val countQuery = cb.createQuery(Long::class.java)
        val countTaskRoot = countQuery.from(CamundaTask::class.java)
        val countDocumentRoot = countQuery.from(JsonSchemaDocument::class.java)
        countQuery.select(cb.count(countTaskRoot))
        entityManager.createQuery(countQuery)
        countQuery.where(constructWhere(cb, countQuery, countTaskRoot, countDocumentRoot, caseDefinitionName, assignmentFilter))

        val count = entityManager.createQuery(countQuery).singleResult

        val pagedQuery = entityManager.createQuery(
            query,
        )
            .setFirstResult(pageable.offset.toInt())
            .setMaxResults(pageable.pageSize)

        return PageImpl(pagedQuery.resultList, pageable, count)
    }

    private fun constructWhere(
        cb: CriteriaBuilder,
        query: CriteriaQuery<*>,
        taskRoot: Root<CamundaTask>,
        documentRoot: Root<JsonSchemaDocument>,
        caseDefinitionName: String,
        assignmentFilter: TaskFilter
    ): Predicate? {
        val authorizationPredicate: Predicate =
            getAuthorizationSpecification(CamundaTaskActionProvider.VIEW_LIST).toPredicate(taskRoot, query, cb)

        val assignmentFilterPredicate: Predicate = constructAssignmentFilter(assignmentFilter, cb, taskRoot)

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
            authorizationPredicate
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
                val currentUserId = userManagementService.currentUserId
                cb.and(cb.equal(taskRoot.get<Any>(CamundaTaskSpecificationHelper.ASSIGNEE), currentUserId))
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
                        val quotedPath = property.substring(DOC_PREFIX.length).split(".").joinToString("."){ "\"${it}\"" }
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
                        ?.let { assigneeId -> userManagementService.findById(assigneeId as String).fullName }
                }

                else -> CaseTaskProperties.getByPropertyName(path)?.getValueFromObject(caseTask)
            }
        }
        return taskPath to value
    }

    private fun <T> stringToPath(parent: Path<*>, path: String): Path<T> {
        val split = path.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var result = parent
        for (s in split) {
            result = result.get<Any>(s)
        }
        return result as Path<T>
    }

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