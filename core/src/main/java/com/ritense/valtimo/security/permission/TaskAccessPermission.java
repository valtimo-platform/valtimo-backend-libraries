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

package com.ritense.valtimo.security.permission;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.IdentityLink;
import org.springframework.security.core.Authentication;

import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.utils.SecurityUtils.getCurrentUserLogin;
import static com.ritense.valtimo.contract.utils.SecurityUtils.isCurrentUserInRole;
import static org.camunda.bpm.engine.task.IdentityLinkType.ASSIGNEE;
import static org.camunda.bpm.engine.task.IdentityLinkType.CANDIDATE;

public class TaskAccessPermission implements Permission<String> {

    private final TaskService taskService;

    public TaskAccessPermission(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public boolean isAllowed(Authentication authentication, String taskInstanceId) {
        if (taskInstanceId == null || taskInstanceId.isEmpty()) {
            return false;
        }
        return checkForCandidateGroupOrAssigneeIdentityLink(taskInstanceId);
    }

    @Override
    public String name() {
        return "taskAccess";
    }

    private boolean checkForCandidateGroupOrAssigneeIdentityLink(String taskId) {
        return taskService.getIdentityLinksForTask(taskId)
            .stream()
            .filter(TaskAccessPermission::hasCandidateOrAssigneeMatch)
            .collect(Collectors.toSet())
            .size() > 0;
    }

    private static boolean hasCandidateOrAssigneeMatch(IdentityLink identityLink) {
        return identityLink.getType().equals(CANDIDATE) && isCurrentUserInRole(identityLink.getGroupId())
            ||
            identityLink.getType().equals(ASSIGNEE) && getCurrentUserLogin().equals(identityLink.getUserId());
    }

}