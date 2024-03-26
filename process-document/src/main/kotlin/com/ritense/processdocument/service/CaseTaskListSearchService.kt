package com.ritense.processdocument.service

import com.ritense.case.domain.TaskListColumn
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.processdocument.domain.CaseTask
import com.ritense.processdocument.web.result.TaskListRowDto
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.service.CamundaTaskService
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
    private val queryDialectHelper: QueryDialectHelper
) {
    private val CONTENT = "content"
    private val INTERNAL_STATUS = "internalStatus"
    private val INTERNAL_STATUS_ORDER = "internalStatus.order"
    private val DOC_PREFIX = "doc:"
    private val CASE_PREFIX = "case:"
    private val DOCUMENT_FIELD_MAP: Map<String, String> = mapOf(
        "definitionId.name" to "documentDefinitionId.name",
        "definitionId.version" to "documentDefinitionId.key",
        INTERNAL_STATUS to INTERNAL_STATUS_ORDER
    )

    fun getTasksByCaseDefinition(caseDefinitionName: String, assignmentFilter: CamundaTaskService.TaskFilter, pageable: Pageable): Page<TaskListRowDto> {
        // No authorization on this level, as we have to fully rely on the documentSearchService for filtering results
        val taskListColumns = taskListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
            caseDefinitionName
        )
        val newPageable = mutatePageable(taskListColumns, pageable)

        return search(caseDefinitionName, assignmentFilter, newPageable)
            .map { task -> toCaseListRowDto(task, taskListColumns) }
    }

    private fun search(caseDefinitionName: String, assignmentFilter: TaskFilter, pageable: Pageable): Page<CaseTask> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(CaseTask::class.java)
        val taskRoot = query.from(CamundaTask::class.java)
        val documentRoot = query.from(JsonSchemaDocument::class.java)

        query.select(
            cb.construct(
                CaseTask::class.java,
                taskRoot.get<String>("id"),
                taskRoot.get<CamundaExecution?>("processInstance").get<String>("id"),
                documentRoot.get<JsonSchemaDocumentId>("id").get<UUID>("id").`as`(java.lang.String::class.java)
            )
        )

        query.where(constructWhere(cb, taskRoot, documentRoot, caseDefinitionName, assignmentFilter))
        query.orderBy(constructOrderBy(query, cb, documentRoot, pageable.sort))

        val countQuery = cb.createQuery(Long::class.java)
        val countTaskRoot = countQuery.from(CamundaTask::class.java)
        val countDocumentRoot = countQuery.from(JsonSchemaDocument::class.java)
        countQuery.select(cb.count(countTaskRoot))
        entityManager.createQuery(countQuery)
        countQuery.where(constructWhere(cb, countTaskRoot, countDocumentRoot, caseDefinitionName, assignmentFilter))

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
        taskRoot: Root<CamundaTask>,
        documentRoot: Root<JsonSchemaDocument>,
        caseDefinitionName: String,
        assignmentFilter: TaskFilter
    ): Predicate? {
        val assignmentFilterPredicate: Predicate = constructAssignmentFilter(assignmentFilter, cb, taskRoot)

        val where = cb.and(
            cb.equal(
                documentRoot.get<JsonSchemaDocumentDefinitionId>("documentDefinitionId").get<String>("name"),
                caseDefinitionName
            ),
            cb.equal(
                taskRoot.get<CamundaExecution>("processInstance").get<String>("businessKey"),
                documentRoot.get<JsonSchemaDocumentId>("id").get<UUID>("id").`as`(java.lang.String::class.java)
            ),
            assignmentFilterPredicate
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
        root: Root<JsonSchemaDocument>,
        sort: Sort
    ): List<Order> {
        return sort.stream()
            .map { order: Sort.Order ->
                val expression: Expression<*>
                val property = order.property
                if (property.startsWith(DOC_PREFIX)) {
                    val jsonPath = "$." + property.substring(DOC_PREFIX.length)
                    expression = queryDialectHelper.getJsonValueExpression(
                        cb,
                        root.get<JsonDocumentContent>(CONTENT)
                            .get<String>(CONTENT),
                        jsonPath,
                        String::class.java
                    )
                } else if (property.startsWith("$.")) {
                    expression = cb.lower(
                        queryDialectHelper.getJsonValueExpression(
                            cb,
                            root.get<JsonDocumentContent>(CONTENT)
                                .get<String>(CONTENT),
                            property,
                            String::class.java
                        )
                    )
                } else {
                    var docProperty =
                        if (property.startsWith(CASE_PREFIX)) property.substring(
                            CASE_PREFIX.length
                        ) else property
                    if (DOCUMENT_FIELD_MAP.containsKey(docProperty)) {
                        docProperty = DOCUMENT_FIELD_MAP[docProperty]
                    }

                    val parent: Path<*>
                    if (docProperty == INTERNAL_STATUS_ORDER) {
                        parent = root.join<Any, Any>(
                            INTERNAL_STATUS,
                            JoinType.LEFT
                        )
                        docProperty = docProperty.substring(INTERNAL_STATUS.length + 1)
                    } else {
                        parent = root
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
                if (order.direction.isAscending) cb.asc(expression) else cb.desc(expression)
            }
            .collect(Collectors.toList())
    }

    private fun mutatePageable(taskListColumns: Collection<TaskListColumn>, pageable: Pageable): PageRequest {
        val newSortOrders = pageable.sort.map { sortOrder ->
            val caseListColumn = taskListColumns.find { caseListColumn -> caseListColumn.id.key == sortOrder.property }
            val sortingProperty = caseListColumn?.path ?: sortOrder.property
            Sort.Order(sortOrder.direction, sortingProperty, sortOrder.nullHandling)
        }
        val newSort = Sort.by(newSortOrders.toMutableList())
        return PageRequest.of(pageable.pageNumber, pageable.pageSize, newSort)
    }

    private fun toCaseListRowDto(task: CaseTask, taskListColumns: List<TaskListColumn>): TaskListRowDto {
        val paths = taskListColumns.map { it.path }
        val resolvedValuesMap = valueResolverService.resolveValues(task.documentInstanceId, paths)

        val items = taskListColumns.map { caseListColumn ->
            TaskListRowDto.TaskListItemDto(caseListColumn.id.key, resolvedValuesMap[caseListColumn.path])
        }.toList()

        return TaskListRowDto(task.taskId, items)
    }

    private fun <T> stringToPath(parent: Path<*>, path: String): Path<T> {
        val split = path.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var result = parent
        for (s in split) {
            result = result.get<Any>(s)
        }
        return result as Path<T>
    }
}