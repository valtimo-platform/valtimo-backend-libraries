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

package com.ritense.valtimo.service;

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.ASSIGN;
import static com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.ASSIGNABLE;
import static com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.CLAIM;
import static com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.COMPLETE;
import static com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.VIEW;
import static com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.VIEW_LIST;
import static com.ritense.valtimo.camunda.repository.CamundaIdentityLinkSpecificationHelper.byTaskId;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.KEY;
import static com.ritense.valtimo.camunda.repository.CamundaProcessInstanceSpecificationHelper.BUSINESS_KEY;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.CREATE_TIME;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.DUE_DATE;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.EXECUTION;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.ID;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.PROCESS_DEFINITION;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.PROCESS_INSTANCE;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.all;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byAssignee;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byId;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byProcessInstanceId;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byUnassigned;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toSet;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.DelegateUserEntityAuthorizationRequest;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.authorization.role.Role;
import com.ritense.authorization.specification.AuthorizationSpecification;
import com.ritense.outbox.OutboxService;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.camunda.domain.CamundaIdentityLink;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.dto.CamundaIdentityLinkDto;
import com.ritense.valtimo.camunda.dto.CamundaTaskDto;
import com.ritense.valtimo.camunda.dto.TaskExtended;
import com.ritense.valtimo.camunda.repository.CamundaIdentityLinkRepository;
import com.ritense.valtimo.camunda.repository.CamundaTaskRepository;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.NamedUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import com.ritense.valtimo.contract.event.TaskAssignedEvent;
import com.ritense.valtimo.contract.utils.RequestHelper;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import com.ritense.valtimo.event.TaskAssigned;
import com.ritense.valtimo.event.TaskCompleted;
import com.ritense.valtimo.event.TaskUnassigned;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import com.ritense.valtimo.repository.camunda.dto.TaskInstanceWithIdentityLink;
import com.ritense.valtimo.security.exceptions.TaskNotFoundException;
import com.ritense.valtimo.service.util.FormUtils;
import com.ritense.valtimo.web.rest.dto.TaskCompletionDTO;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.validator.routines.EmailValidator;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidationException;
import org.camunda.bpm.engine.task.Comment;
import org.hibernate.Hibernate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

public class CamundaTaskService {

    private static final String CONTEXT = "context";

    private static final String NO_USER = null;
    private final TaskService taskService;
    private final FormService formService;
    private final DelegateTaskHelper delegateTaskHelper;
    private final CamundaTaskRepository camundaTaskRepository;
    private final CamundaIdentityLinkRepository camundaIdentityLinkRepository;
    private final Optional<ResourceService> optionalResourceService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RuntimeService runtimeService;
    private final UserManagementService userManagementService;
    private final EntityManager entityManager;
    private final AuthorizationService authorizationService;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    public CamundaTaskService(
        TaskService taskService,
        FormService formService,
        DelegateTaskHelper delegateTaskHelper,
        CamundaTaskRepository camundaTaskRepository,
        CamundaIdentityLinkRepository camundaIdentityLinkRepository,
        Optional<ResourceService> optionalResourceService,
        ApplicationEventPublisher applicationEventPublisher,
        RuntimeService runtimeService,
        UserManagementService userManagementService,
        EntityManager entityManager,
        AuthorizationService authorizationService,
        OutboxService outboxService, ObjectMapper objectMapper
    ) {
        this.taskService = taskService;
        this.formService = formService;
        this.delegateTaskHelper = delegateTaskHelper;
        this.camundaTaskRepository = camundaTaskRepository;
        this.camundaIdentityLinkRepository = camundaIdentityLinkRepository;
        this.optionalResourceService = optionalResourceService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.runtimeService = runtimeService;
        this.userManagementService = userManagementService;
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public CamundaTask findTaskById(String taskId) {
        var task = camundaTaskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));
        requirePermission(task, VIEW);
        return task;
    }

    @Transactional
    public void assignByEmail(String taskId, String assigneeEmail) throws IllegalStateException {
        var assignee = userManagementService.findNamedUserByEmail(assigneeEmail)
            .orElseThrow(() -> new IllegalStateException("Error. No registered user found with email: " + assigneeEmail));
        assign(taskId, assignee.getId());
    }

    @Transactional
    public void assign(String taskId, String assignee) throws IllegalStateException {
        if (assignee == null) {
            unassign(taskId);
        } else if (EmailValidator.getInstance().isValid(assignee)) {
            throw new IllegalStateException("Task assignee must be an ID. Not an email: '" + assignee + "'");
        } else {
            String assigneeIdentifier = userManagementService.findById(assignee).getUserIdentifier();
            final CamundaTask task = runWithoutAuthorization(() -> findTaskById(taskId));
            final String currentUser = userManagementService.getCurrentUser().getUserIdentifier();
            if (assignee.equals(currentUser)) {
                try {
                    requirePermission(task, CLAIM);
                } catch (AccessDeniedException e) {
                    requirePermission(task, ASSIGN);
                    requirePermission(task, ASSIGNABLE);
                }
            } else {
                requirePermission(task, ASSIGN);
                authorizationService.requirePermission(
                    new DelegateUserEntityAuthorizationRequest<>(CamundaTask.class, ASSIGNABLE, assigneeIdentifier, task)
                );
            }
            final String currentAssignee = task.getAssignee();
            try {
                taskService.setAssignee(task.getId(), assigneeIdentifier);
                entityManager.refresh(task);
                publishTaskAssignedEvent(task, currentAssignee, assigneeIdentifier);
                outboxService.send(() -> new TaskAssigned(task.getId(), objectMapper.valueToTree(task)));
            } catch (AuthorizationException ex) {
                throw new IllegalStateException("Cannot assign task: the user has no permission.", ex);
            } catch (ProcessEngineException ex) {
                throw new IllegalStateException("An error occurred while assigning the task", ex);
            }
        }
    }

    @Transactional
    public void unassign(String taskId) {
        final CamundaTask task = runWithoutAuthorization(() -> findTaskById(taskId));
        requirePermission(task, ASSIGN);
        try {
            taskService.setAssignee(task.getId(), NO_USER);
            entityManager.refresh(task);
            outboxService.send(() -> new TaskUnassigned(task.getId(), objectMapper.valueToTree(task)));
        } catch (AuthorizationException ex) {
            throw new IllegalStateException("Cannot unassign task: the user has no permission.", ex);
        } catch (ProcessEngineException ex) {
            throw new IllegalStateException("An error occurred while unassigning the task.", ex);
        }
    }

    @Deprecated(since = "10.8.0", forRemoval = true)
    @Transactional(readOnly = true)
    public List<ManageableUser> getCandidateUsers(String taskId) {
        final CamundaTask task = runWithoutAuthorization(() -> findTaskById(taskId));
        requirePermission(task, ASSIGN);

        return authorizationService.getAuthorizedRoles(
                new EntityAuthorizationRequest<>(
                    CamundaTask.class,
                    ASSIGNABLE,
                    task
                )
            ).stream()
            .map(Role::getKey)
            .map(userManagementService::findByRole)
            .flatMap(Collection::stream)
            .distinct()
            .sorted(comparing(ManageableUser::getFirstName, nullsLast(naturalOrder()))
                .thenComparing(ManageableUser::getLastName, nullsLast(naturalOrder())))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<NamedUser> getNamedCandidateUsers(String taskId) {
        final CamundaTask task = runWithoutAuthorization(() -> findTaskById(taskId));
        requirePermission(task, ASSIGN);

        final Set<String> candidateGroups = authorizationService.getAuthorizedRoles(
                new EntityAuthorizationRequest<>(
                    CamundaTask.class,
                    ASSIGNABLE,
                    task
                )
            ).stream()
            .map(Role::getKey)
            .collect(toSet());
        return userManagementService.findNamedUserByRoles(candidateGroups);
    }

    @Transactional
    public void complete(String taskId) {
        final CamundaTask task = runWithoutAuthorization(() -> findTaskById(taskId));
        requirePermission(task, COMPLETE);
        taskService.complete(taskId);
        Hibernate.initialize(task.getVariableInstances());
        Hibernate.initialize(task.getIdentityLinks());
        outboxService.send(() -> new TaskCompleted(taskId, objectMapper.valueToTree(task)));
    }

    @Transactional
    public void completeTaskWithFormData(String taskId, Map<String, Object> variables) {
        try {
            if (variables == null || variables.isEmpty()) {
                complete(taskId);
            } else {
                final CamundaTask task = runWithoutAuthorization(() -> findTaskById(taskId));
                requirePermission(task, COMPLETE);
                formService.submitTaskForm(task.getId(), FormUtils.createTypedVariableMap(variables));
                outboxService.send(() -> new TaskCompleted(taskId, objectMapper.valueToTree(task)));
            }
        } catch (FormFieldValidationException ex) {
            throw ex;
        } catch (ProcessEngineException ex) {
            throw new IllegalStateException("Cannot complete task: when no task exists with the given id.", ex);
        }
    }

    @Transactional
    public void completeTaskAndDeleteFiles(String taskId, TaskCompletionDTO taskCompletionDTO) {
        completeTaskWithFormData(taskId, taskCompletionDTO.getVariables());
        optionalResourceService.ifPresent(
            amazonS3Service -> taskCompletionDTO.getFilesToDelete().forEach(amazonS3Service::removeResource));
    }

    @Transactional(readOnly = true)
    public Page<CamundaTask> findTasks(Specification<CamundaTask> specification, Pageable pageable) {
        var spec = getAuthorizationSpecification(VIEW_LIST);
        return camundaTaskRepository.findAll(spec.and(specification), pageable);
    }

    @Transactional(readOnly = true)
    public List<CamundaTask> findTasks(Specification<CamundaTask> specification, Sort sort) {
        var spec = getAuthorizationSpecification(VIEW_LIST);
        return camundaTaskRepository.findAll(spec.and(specification), sort);
    }

    @Transactional(readOnly = true)
    public List<CamundaTask> findTasks(Specification<CamundaTask> specification) {
        var spec = getAuthorizationSpecification(VIEW_LIST);
        return camundaTaskRepository.findAll(spec.and(specification));
    }

    @Transactional(readOnly = true)
    public CamundaTask findTask(Specification<CamundaTask> specification) {
        var spec = getAuthorizationSpecification(VIEW);
        return camundaTaskRepository.findOne(spec.and(specification)).orElse(null);
    }

    @Transactional(readOnly = true)
    public CamundaTask findTaskOrThrow(Specification<CamundaTask> specification) {
        var task = camundaTaskRepository.findOne(specification)
            .orElseThrow(TaskNotFoundException::new);
        requirePermission(task, VIEW);
        return task;
    }

    @Transactional(readOnly = true)
    public Long countTasks(Specification<CamundaTask> specification) {
        var spec = getAuthorizationSpecification(VIEW_LIST);
        return camundaTaskRepository.count(spec.and(specification));
    }

    @Transactional(readOnly = true)
    public Page<TaskExtended> findTasksFiltered(
        TaskFilter taskFilter, Pageable pageable
    ) {
        var spec = getAuthorizationSpecification(VIEW_LIST);
        var specification = spec.and(buildTaskFilterSpecification(taskFilter));

        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createTupleQuery();
        var taskRoot = query.from(CamundaTask.class);
        var executionIdPath = taskRoot.get(EXECUTION).get(ID);
        var businessKeyPath = taskRoot.get(PROCESS_INSTANCE).get(BUSINESS_KEY);
        var processDefinitionIdPath = taskRoot.get(PROCESS_DEFINITION).get(ID);
        var processDefinitionKeyPath = taskRoot.get(PROCESS_DEFINITION).get(KEY);

        query.multiselect(taskRoot, executionIdPath, businessKeyPath, processDefinitionIdPath, processDefinitionKeyPath);
        query.where(specification.toPredicate(taskRoot, query, cb));
        query.groupBy(taskRoot, executionIdPath, businessKeyPath, processDefinitionIdPath, processDefinitionKeyPath);
        query.orderBy(getOrderBy(cb, taskRoot, pageable.getSort()));

        var typedQuery = entityManager.createQuery(query);
        if (pageable.isPaged()) {
            typedQuery
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());
        }

        var assigneeMap = new java.util.HashMap<String, ValtimoUser>();
        var tasks = typedQuery.getResultList().stream()
            .map(tuple -> {
                var task = tuple.get(0, CamundaTask.class);
                var executionId = tuple.get(1, String.class);
                var businessKey = tuple.get(2, String.class);
                var processDefinitionId = tuple.get(3, String.class);
                var processDefinitionKey = tuple.get(4, String.class);

                ValtimoUser valtimoUser;
                if (task.getAssignee() == null) {
                    valtimoUser = null;
                } else if (assigneeMap.containsKey(task.getAssignee())) {
                    valtimoUser = assigneeMap.get(task.getAssignee());
                } else {
                    valtimoUser = getValtimoUser(task.getAssignee());
                    assigneeMap.put(task.getAssignee(), valtimoUser);
                }

                var context = task.getVariable(CONTEXT);

                return TaskExtended.of(
                    task,
                    executionId,
                    businessKey,
                    processDefinitionId,
                    processDefinitionKey,
                    valtimoUser,
                    context
                );
            })
            .toList();

        return new PageImpl<>(tasks, pageable, countTasksFiltered(specification));
    }

    private long countTasksFiltered(Specification<CamundaTask> specification) {
        var cb = entityManager.getCriteriaBuilder();
        var countQuery = cb.createQuery(Long.class);
        var taskCountRoot = countQuery.from(CamundaTask.class);
        countQuery.select(cb.countDistinct(taskCountRoot));
        countQuery.where(specification.toPredicate(taskCountRoot, countQuery, cb));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    @Transactional(readOnly = true)
    public List<TaskInstanceWithIdentityLink> getProcessInstanceTasks(String processInstanceId, String businessKey) {
        return findTasks(byProcessInstanceId(processInstanceId), Sort.by(DESC, CREATE_TIME))
            .stream()
            .map(task -> AuthorizationContext.runWithoutAuthorization(() -> {
                    final var identityLinks = getIdentityLinks(task.getId());
                    return new TaskInstanceWithIdentityLink(
                        businessKey,
                        CamundaTaskDto.of(task),
                        delegateTaskHelper.isTaskPublic(task),
                        task.getProcessDefinition().getKey(),
                        identityLinks
                    );
                }
            ))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CamundaIdentityLinkDto> getIdentityLinks(String taskId) {
        final List<CamundaIdentityLink> identityLinksForTask = getIdentityLinksForTask(taskId);
        return identityLinksForTask.stream().map(CamundaIdentityLinkDto::of).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getVariables(String taskInstanceId) {
        return findTaskById(taskInstanceId).getVariables();
    }

    @Transactional(readOnly = true)
    public Object getVariable(String taskInstanceId, String variableName) {
        return findTaskById(taskInstanceId).getVariable(variableName);
    }

    @Transactional(readOnly = true)
    public List<CamundaIdentityLink> getIdentityLinksForTask(String taskId) {
        final CamundaTask task = findTaskById(taskId);
        return camundaIdentityLinkRepository.findAll(byTaskId(task.getId()));
    }

    /**
     * Retrieve a list of comments that are associated to the task.
     *
     * @deprecated Task comments will be removed in the future.
     */
    @Deprecated(since = "11.1.0", forRemoval = true)
    @Transactional(readOnly = true)
    public List<Comment> getTaskComments(String taskId) {
        final CamundaTask task = runWithoutAuthorization(() -> findTaskById(taskId));
        requirePermission(task, VIEW);
        return taskService.getTaskComments(task.getId());
    }

    /**
     * Retrieve a list of comments that are associated to a process instance.
     *
     * @deprecated Task comments will be removed in the future.
     */
    @Deprecated(since = "11.1.0", forRemoval = true)
    @Transactional(readOnly = true)
    public List<Comment> getProcessInstanceComments(String processInstanceId) {
        var comments = taskService.getProcessInstanceComments(processInstanceId);
        comments.forEach(comment -> {
            if (comment.getTaskId() != null) {
                final CamundaTask task = runWithoutAuthorization(() -> findTaskById(comment.getTaskId()));
                requirePermission(task, VIEW);
            }
        });
        return comments;
    }

    /**
     * Create a comment and associate that comment to either a task or a process instance.
     *
     * @deprecated Task comments will be removed in the future.
     */
    @Deprecated(since = "11.1.0", forRemoval = true)
    @Transactional
    public void createComment(@Nullable String taskId, @Nullable String processInstanceId, String message) {
        if (taskId != null) {
            final CamundaTask task = runWithoutAuthorization(() -> findTaskById(taskId));
            requirePermission(task, VIEW);
        }
        taskService.createComment(taskId, processInstanceId, message);
    }

    @Transactional(readOnly = true)
    public boolean hasTaskFormData(String taskId) {
        final CamundaTask task = findTaskById(taskId);
        final TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        return taskFormData == null || taskFormData.getFormKey() != null || !taskFormData.getFormFields().isEmpty();
    }

    public enum TaskFilter {
        MINE, OPEN, ALL
    }

    private void publishTaskAssignedEvent(CamundaTask task, String formerAssignee, String newAssignee) {
        final String businessKey = runtimeService
            .createProcessInstanceQuery()
            .processInstanceId(task.getProcessInstanceId())
            .singleResult()
            .getBusinessKey();

        applicationEventPublisher.publishEvent(
            new TaskAssignedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                SecurityUtils.getCurrentUserLogin(),
                formerAssignee,
                newAssignee,
                task.getId(),
                task.getName(),
                task.getCreateTime(),
                task.getProcessDefinitionId(),
                task.getProcessInstanceId(),
                businessKey
            )
        );
    }

    private Specification<CamundaTask> buildTaskFilterSpecification(TaskFilter taskFilter) {
        var filterSpec = all();

        if (taskFilter == TaskFilter.MINE) {
            String currentUserId = userManagementService.getCurrentUser().getUserIdentifier();
            return filterSpec.and(byAssignee(currentUserId));
        } else if (taskFilter == TaskFilter.ALL) {
            return filterSpec;
        } else if (taskFilter == TaskFilter.OPEN) {
            return filterSpec.and(byUnassigned());
        }

        return filterSpec;
    }

    private ValtimoUser getValtimoUser(String assigneeId) {
        return Optional.ofNullable(userManagementService.findByUserIdentifier(assigneeId)).map(user ->
                new ValtimoUserBuilder()
                    .id(user.getUserIdentifier())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build())
            .orElse(null);
    }

    private List<Order> getOrderBy(CriteriaBuilder cb, Root<CamundaTask> root, Sort sort) {
        return sort.stream()
            .map(order -> {
                String sortProperty;
                if (order.getProperty().equals("created")) {
                    sortProperty = CREATE_TIME;
                } else if (order.getProperty().equals("due")) {
                    sortProperty = DUE_DATE;
                } else {
                    sortProperty = order.getProperty();
                }
                Path<Object> expression = root.get(sortProperty);

                return order.isAscending() ? cb.asc(expression) : cb.desc(expression);
            })
            .map(Order.class::cast)
            .toList();
    }

    private AuthorizationSpecification<CamundaTask> getAuthorizationSpecification(Action<CamundaTask> action) {
        return authorizationService.getAuthorizationSpecification(
            new EntityAuthorizationRequest<>(CamundaTask.class, action),
            null
        );
    }

    private void requirePermission(CamundaTask task, Action<CamundaTask> action) {
        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(CamundaTask.class, action, task)
        );
    }

}