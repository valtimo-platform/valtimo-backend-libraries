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

package com.ritense.valtimo.service;

import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.DelegateUserEntityAuthorizationRequest;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.authorization.role.Role;
import com.ritense.authorization.specification.AuthorizationSpecification;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.camunda.domain.CamundaIdentityLink;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.dto.CamundaIdentityLinkDto;
import com.ritense.valtimo.camunda.dto.CamundaTaskDto;
import com.ritense.valtimo.camunda.dto.TaskExtended;
import com.ritense.valtimo.camunda.repository.CamundaIdentityLinkRepository;
import com.ritense.valtimo.camunda.repository.CamundaTaskRepository;
import com.ritense.valtimo.camunda.service.CamundaContextService;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import com.ritense.valtimo.contract.event.TaskAssignedEvent;
import com.ritense.valtimo.contract.utils.RequestHelper;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import com.ritense.valtimo.repository.camunda.dto.TaskInstanceWithIdentityLink;
import com.ritense.valtimo.security.exceptions.TaskNotFoundException;
import com.ritense.valtimo.service.util.FormUtils;
import com.ritense.valtimo.web.rest.dto.TaskCompletionDTO;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidationException;
import org.camunda.bpm.engine.task.Comment;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.all;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byAssignee;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byCandidateGroups;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byId;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byProcessInstanceId;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byUnassigned;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static org.springframework.data.domain.Sort.Direction.DESC;

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
    private final CamundaContextService camundaContextService;

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
        CamundaContextService camundaContextService) {
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
        this.camundaContextService = camundaContextService;
    }

    @Transactional(readOnly = true)
    public CamundaTask findTaskById(String taskId) {
        var spec = getAuthorizationSpecification(VIEW);
        return Optional.ofNullable(findTask(spec.and(byId(taskId))))
            .orElseThrow(() -> new TaskNotFoundException(String.format("Cannot find task %s", taskId)));
    }

    @Transactional
    public void assign(String taskId, String assignee) throws IllegalStateException {
        if (assignee == null) {
            unassign(taskId);
        } else {
            final CamundaTask task = runWithoutAuthorization(() -> findTaskById(taskId));
            final String currentUser = SecurityUtils.getCurrentUserLogin();
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
                    new DelegateUserEntityAuthorizationRequest<>(CamundaTask.class, ASSIGNABLE, assignee, task)
                );
            }
            final String currentAssignee = task.getAssignee();
            try {
                taskService.setAssignee(task.getId(), assignee);
                entityManager.refresh(task);
                publishTaskAssignedEvent(task, currentAssignee, assignee);
            } catch (AuthorizationException ex) {
                throw new IllegalStateException("Cannot claim task: the user has no permission.", ex);
            } catch (ProcessEngineException ex) {
                throw new IllegalStateException("Cannot claim task: reason is the task doesn't exist.", ex);
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
        } catch (AuthorizationException ex) {
            throw new IllegalStateException("Cannot claim task: the user has no permission.", ex);
        } catch (ProcessEngineException ex) {
            throw new IllegalStateException("Cannot claim task: reason is the task doesn't exist.", ex);
        }
    }

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

    @Transactional
    public void complete(String taskId) {
        final CamundaTask task = runWithoutAuthorization(() -> findTaskById(taskId));
        requirePermission(task, COMPLETE);
        taskService.complete(taskId);
        entityManager.detach(task);
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
    public Long countTasks(Specification<CamundaTask> specification) {
        var spec = getAuthorizationSpecification(VIEW_LIST);
        return camundaTaskRepository.count(spec.and(specification));
    }

    @Transactional(readOnly = true)
    public Page<TaskExtended> findTasksFiltered(
        TaskFilter taskFilter, Pageable pageable
    ) throws IllegalAccessException {
        var spec = getAuthorizationSpecification(VIEW_LIST);
        var specification = spec.and(buildTaskFilterSpecification(taskFilter));

        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createTupleQuery();
        var taskRoot = query.from(CamundaTask.class);
        var executionIdPath = taskRoot.get(EXECUTION).get(ID);
        var businessKeyPath = taskRoot.get(EXECUTION).get(BUSINESS_KEY);
        var processDefinitionIdPath = taskRoot.get(PROCESS_DEFINITION).get(ID);
        var processDefinitionKeyPath = taskRoot.get(PROCESS_DEFINITION).get(KEY);

        query.multiselect(taskRoot, executionIdPath, businessKeyPath, processDefinitionIdPath, processDefinitionKeyPath);
        query.distinct(true);
        query.where(specification.toPredicate(taskRoot, query, cb));
        query.orderBy(getOrderBy(taskRoot, pageable.getSort()));

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

        var total = camundaTaskRepository.count(spec.and(specification));
        return new PageImpl<>(tasks, pageable, total);
    }

    @Transactional(readOnly = true)
    public List<TaskInstanceWithIdentityLink> getProcessInstanceTasks(String processInstanceId, String businessKey) {
        return findTasks(byProcessInstanceId(processInstanceId), Sort.by(DESC, CREATE_TIME))
            .stream()
            .map(task -> {
                final var identityLinks = getIdentityLinks(task.getId());
                return new TaskInstanceWithIdentityLink(
                    businessKey,
                    CamundaTaskDto.of(task),
                    delegateTaskHelper.isTaskPublic(task),
                    task.getProcessDefinition().getKey(),
                    identityLinks
                );
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CamundaIdentityLinkDto> getIdentityLinks(String taskId) {
        final List<CamundaIdentityLink> identityLinksForTask = getIdentityLinksForTask(taskId);
        return identityLinksForTask.stream().map(CamundaIdentityLinkDto::of).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getVariables(String taskInstanceId) {
        var task = findTaskById(taskInstanceId);
        return camundaContextService.runWithCommandContext(() -> task.getVariables(null));
    }

    @Transactional(readOnly = true)
    public List<CamundaIdentityLink> getIdentityLinksForTask(String taskId) {
        final CamundaTask task = findTaskById(taskId);
        return camundaIdentityLinkRepository.findAll(byTaskId(task.getId()));
    }

    @Transactional(readOnly = true)
    public List<Comment> getTaskComments(String taskId) {
        final CamundaTask task = findTaskById(taskId);
        return taskService.getTaskComments(task.getId());
    }

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

    private Specification<CamundaTask> buildTaskFilterSpecification(TaskFilter taskFilter) throws IllegalAccessException {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin();
        List<String> userRoles = SecurityUtils.getCurrentUserRoles();
        var filterSpec = all();

        if (taskFilter == TaskFilter.MINE) {
            if (currentUserLogin == null) {
                throw new IllegalStateException("Cannot find currentUserLogin");
            }
            return filterSpec.and(byAssignee(currentUserLogin));
        } else if (taskFilter == TaskFilter.ALL) {
            return filterSpec.and(byCandidateGroups(userRoles));
        } else if (taskFilter == TaskFilter.OPEN) {
            return filterSpec
                .and(byCandidateGroups(userRoles))
                .and(byUnassigned());
        }

        return filterSpec;
    }

    private ValtimoUser getValtimoUser(String assigneeEmail) {
        return userManagementService.findByEmail(assigneeEmail).map(user ->
                new ValtimoUserBuilder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build())
            .orElse(null);
    }

    private List<Order> getOrderBy(Root<CamundaTask> root, Sort sort) {
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
                return new OrderImpl(root.get(sortProperty), order.getDirection().isAscending());
            })
            .map(Order.class::cast)
            .toList();
    }

    private AuthorizationSpecification<CamundaTask> getAuthorizationSpecification(Action<CamundaTask> action) {
        return authorizationService.getAuthorizationSpecification(
            new EntityAuthorizationRequest<>(CamundaTask.class, action, null),
            null
        );
    }

    private void requirePermission(CamundaTask task, Action<CamundaTask> action) {
        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(CamundaTask.class, action, task)
        );
    }

}