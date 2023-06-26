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

package com.ritense.valtimo.helper;

import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.service.BpmnModelService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DelegateTaskHelper {
    private static final String TASK_ASSIGNMENT_EVENT = "assignment";
    private static final String TASK_CREATION_EVENT = "create";
    static final String PUBLIC_TASK_PROPERTY_NAME = "public";
    private static final String PUBLIC_TASK_PROPERTY_VALUE = "true";

    private final UserManagementService userManagementService;
    private final ActivityHelper activityHelper;
    private final BpmnModelService bpmnModelService;

    public DelegateTaskHelper(UserManagementService userManagementService, ActivityHelper activityHelper, BpmnModelService bpmnModelService) {
        this.userManagementService = userManagementService;
        this.activityHelper = activityHelper;
        this.bpmnModelService = bpmnModelService;
    }

    public Optional<ManageableUser> determineAssignedUserOf(DelegateTask delegateTask) {
        String assignee = delegateTask.getAssignee();
        if (assignee != null) {
            return userManagementService.findByEmail(assignee);
        } else {
            return Optional.empty();
        }
    }

    public boolean isTaskBeingAssigned(DelegateTask delegateTask) {
        return delegateTask.getEventName().equals(TASK_ASSIGNMENT_EVENT);
    }

    public boolean isTaskBeingCreated(DelegateTask delegateTask) {
        return delegateTask.getEventName().equals(TASK_CREATION_EVENT);
    }

    public List<ManageableUser> findCandidateUsers(DelegateTask delegateTask) {
        List<ManageableUser> users = new ArrayList<>();
        Set<IdentityLink> candidates = delegateTask.getCandidates();
        Optional<IdentityLink> candidateGroup = candidates.stream()
            .filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType()))
            .findFirst();
        if (candidateGroup.isPresent()) {
            users = userManagementService.findByRole(candidateGroup.get().getGroupId());
        }
        return users;
    }

    public boolean isTaskPublic(DelegateTask delegateTask) {
        List<CamundaProperty> camundaProperties = activityHelper.getCamundaProperties(delegateTask, PUBLIC_TASK_PROPERTY_NAME);
        return matchPublicPropertiesTrue(camundaProperties);
    }

    public boolean isTaskPublic(CamundaTask task) {
        return isTaskPublic(bpmnModelService.getTask(task));
    }

    public boolean isTaskPublic(HistoricTaskInstanceDto historicTaskInstance) {
        List<CamundaProperty> camundaProperties = activityHelper.getCamundaProperties(historicTaskInstance, PUBLIC_TASK_PROPERTY_NAME);
        return matchPublicPropertiesTrue(camundaProperties);
    }

    public boolean isTaskPublic(HistoricTaskInstance historicTaskInstance) {
        List<CamundaProperty> camundaProperties = activityHelper.getCamundaProperties(historicTaskInstance, PUBLIC_TASK_PROPERTY_NAME);
        return matchPublicPropertiesTrue(camundaProperties);
    }

    public boolean isTaskPublic(Task taskInstance) {
        List<CamundaProperty> camundaProperties = activityHelper.getCamundaProperties(taskInstance, PUBLIC_TASK_PROPERTY_NAME);
        return matchPublicPropertiesTrue(camundaProperties);
    }

    private boolean matchPublicPropertiesTrue(List<CamundaProperty> camundaProperties) {
        return camundaProperties.stream().anyMatch(
            camundaProperty -> camundaProperty.getCamundaValue() != null
                && camundaProperty.getCamundaValue().equalsIgnoreCase(PUBLIC_TASK_PROPERTY_VALUE)
        );
    }
}