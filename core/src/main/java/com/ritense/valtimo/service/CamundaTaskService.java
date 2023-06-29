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

import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.camunda.domain.CamundaIdentityLink;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.dto.CamundaIdentityLinkDto;
import com.ritense.valtimo.camunda.dto.CamundaTaskDto;
import com.ritense.valtimo.camunda.dto.TaskExtended;
import com.ritense.valtimo.camunda.repository.CamundaIdentityLinkRepository;
import com.ritense.valtimo.camunda.repository.CamundaTaskRepository;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import com.ritense.valtimo.contract.event.TaskAssignedEvent;
import com.ritense.valtimo.contract.utils.RequestHelper;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import com.ritense.valtimo.domain.contexts.Context;
import com.ritense.valtimo.domain.contexts.ContextProcess;
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
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.CREATE_TIME;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.DUE_DATE;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byAssignee;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byCandidateGroups;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byProcessDefinitionKeys;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byProcessInstanceId;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byUnassigned;
import static java.util.stream.Collectors.toSet;
import static org.springframework.data.domain.Sort.Direction.DESC;

public class CamundaTaskService {

    private static final String CONTEXT = "context";

    private static final String NO_USER = null;
    private final TaskService taskService;
    private final FormService formService;
    private final ContextService contextService;
    private final DelegateTaskHelper delegateTaskHelper;
    private final CamundaTaskRepository camundaTaskRepository;
    private final CamundaIdentityLinkRepository camundaIdentityLinkRepository;
    private final CamundaProcessService camundaProcessService;
    private final Optional<ResourceService> optionalResourceService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RuntimeService runtimeService;
    private final UserManagementService userManagementService;
    private final EntityManager entityManager;

    public CamundaTaskService(
        TaskService taskService,
        FormService formService,
        ContextService contextService,
        DelegateTaskHelper delegateTaskHelper,
        CamundaTaskRepository camundaTaskRepository,
        CamundaIdentityLinkRepository camundaIdentityLinkRepository,
        CamundaProcessService camundaProcessService,
        Optional<ResourceService> optionalResourceService,
        ApplicationEventPublisher applicationEventPublisher,
        RuntimeService runtimeService,
        UserManagementService userManagementService,
        EntityManager entityManager
    ) {
        this.taskService = taskService;
        this.formService = formService;
        this.contextService = contextService;
        this.delegateTaskHelper = delegateTaskHelper;
        this.camundaTaskRepository = camundaTaskRepository;
        this.camundaIdentityLinkRepository = camundaIdentityLinkRepository;
        this.camundaProcessService = camundaProcessService;
        this.optionalResourceService = optionalResourceService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.runtimeService = runtimeService;
        this.userManagementService = userManagementService;
        this.entityManager = entityManager;
    }

    public CamundaTask findTaskById(String taskId) {
        return camundaTaskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(String.format("Cannot find task %s", taskId)));
    }

    public void assign(String taskId, String assignee) throws IllegalStateException {
        final CamundaTask task = findTaskById(taskId);
        final String currentAssignee = task.getAssignee();
        try {
            taskService.setAssignee(task.getId(), assignee);
            publishTaskAssignedEvent(task, currentAssignee, assignee);
        } catch (AuthorizationException ex) {
            throw new IllegalStateException("Cannot claim task: the user has no permission.", ex);
        } catch (ProcessEngineException ex) {
            throw new IllegalStateException("Cannot claim task: reason is the task doesn't exist.", ex);
        }
    }

    public void unassign(String taskId) {
        final CamundaTask task = findTaskById(taskId);
        try {
            taskService.setAssignee(task.getId(), NO_USER);
        } catch (AuthorizationException ex) {
            throw new IllegalStateException("Cannot claim task: the user has no permission.", ex);
        } catch (ProcessEngineException ex) {
            throw new IllegalStateException("Cannot claim task: reason is the task doesn't exist.", ex);
        }
    }

    public List<ManageableUser> getCandidateUsers(String taskId) {
        final CamundaTask task = findTaskById(taskId);
        final Optional<CamundaIdentityLink> first = camundaIdentityLinkRepository.findAllByTaskIdAndType(task.getId(), IdentityLinkType.CANDIDATE)
            .stream()
            .findFirst();

        if (first.isPresent()) {
            return userManagementService.findByRole(first.get().getGroupId());
        } else {
            return Collections.emptyList();
        }
    }

    public void completeTaskWithoutFormData(String taskId) {
        taskService.complete(taskId);
    }

    public void completeTask(String taskId, Map<String, Object> variables) {
        final CamundaTask task = findTaskById(taskId);
        try {
            if (variables == null || variables.isEmpty()) {
                completeTaskWithoutFormData(task.getId());
            } else {
                formService.submitTaskForm(task.getId(), FormUtils.createTypedVariableMap(variables));
            }
        } catch (FormFieldValidationException ex) {
            throw ex;
        } catch (ProcessEngineException ex) {
            throw new IllegalStateException("Cannot complete task: when no task exists with the given id.", ex);
        }
    }

    public void completeTaskAndDeleteFiles(String taskId, TaskCompletionDTO taskCompletionDTO) {
        completeTask(taskId, taskCompletionDTO.getVariables());
        optionalResourceService.ifPresent(
            amazonS3Service -> taskCompletionDTO.getFilesToDelete().forEach(amazonS3Service::removeResource));
    }

    public Page<CamundaTask> findTasks(Specification<CamundaTask> specification, Pageable pageable) {
        return camundaTaskRepository.findAll(specification, pageable);
    }

    public List<CamundaTask> findTasks(Specification<CamundaTask> specification, Sort sort) {
        return camundaTaskRepository.findAll(specification, sort);
    }

    public List<CamundaTask> findTasks(Specification<CamundaTask> specification) {
        return camundaTaskRepository.findAll(specification);
    }

    public CamundaTask findTask(Specification<CamundaTask> specification) {
        return camundaTaskRepository.findOne(specification).orElse(null);
    }

    @Transactional
    public Page<TaskExtended> findTasksFiltered(
        TaskFilter taskFilter, Pageable pageable
    ) throws IllegalAccessException {
        var specification = buildTaskFilterSpecification(taskFilter);

        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createTupleQuery();
        var taskRoot = query.from(CamundaTask.class);
        var executionIdPath = taskRoot.get("execution").get("id");
        var businessKeyPath = taskRoot.get("execution").get("businessKey");
        var processDefinitionIdPath = taskRoot.get("processDefinition").get("id");
        var processDefinitionKeyPath = taskRoot.get("processDefinition").get("key");

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

        var total = camundaTaskRepository.count(specification);
        return new PageImpl<>(tasks, pageable, total);
    }

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

    public List<CamundaIdentityLinkDto> getIdentityLinks(String taskId) {
        final List<CamundaIdentityLink> identityLinksForTask = getIdentityLinksForTask(taskId);
        return identityLinksForTask.stream().map(CamundaIdentityLinkDto::of).collect(Collectors.toList());
    }

    public Map<String, Object> getVariables(String taskInstanceId) {
        return findTaskById(taskInstanceId).getVariables(null);
    }

    public List<CamundaIdentityLink> getIdentityLinksForTask(String taskId) {
        return camundaIdentityLinkRepository.findAllByTaskId(taskId);
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
                LocalDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneId.systemDefault()),
                task.getProcessDefinitionId(),
                task.getProcessInstanceId(),
                businessKey
            )
        );
    }

    private Specification<CamundaTask> buildTaskFilterSpecification(TaskFilter taskFilter) throws IllegalAccessException {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin();
        List<String> userRoles = SecurityUtils.getCurrentUserRoles();
        Context context = contextService.getContextOfCurrentUser();
        var processDefinitionKeys = context.getProcesses().stream()
            .map(ContextProcess::getProcessDefinitionKey)
            .collect(toSet());
        var filterSpec = byProcessDefinitionKeys(processDefinitionKeys);

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

    public boolean hasTaskFormData(String taskId) {
        final TaskFormData taskFormData = formService.getTaskFormData(taskId);
        return taskFormData == null || taskFormData.getFormKey() != null || !taskFormData.getFormFields().isEmpty();
    }

}