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

package com.ritense.valtimo.listener;

import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.task.IdentityLink;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Deprecated
@Slf4j
@RequiredArgsConstructor
public class TaskCreationListener implements TaskListener {

    private final UserManagementService userManagementService;

    @Override
    public void notify(DelegateTask delegateTask) {
        if (isTaskCreated(delegateTask)) {
            tryAutoAssignUserToTask(delegateTask);
        }
    }

    protected boolean isTaskCreated(DelegateTask delegateTask) {
        return delegateTask.getEventName().equalsIgnoreCase(TaskListener.EVENTNAME_CREATE);
    }

    protected void tryAutoAssignUserToTask(DelegateTask delegateTask) {
        Set<IdentityLink> candidates = delegateTask.getCandidates();
        if (candidates.size() == 1) {
            IdentityLink candidate = candidates.iterator().next();

            Optional<ManageableUser> candidateForAutoAssignment = getUserFromIdentityLink(candidate);

            if (candidateForAutoAssignment.isPresent()) {
                String userId = candidateForAutoAssignment.get().getEmail();
                delegateTask.setAssignee(userId);
                logger.info("Auto assigned user '{}' to task '{}'", userId, delegateTask.getName());
            }
        }
    }

    protected Optional<ManageableUser> getUserFromIdentityLink(IdentityLink identityLink) {
        Optional<ManageableUser> getUserResult = Optional.empty();
        if (identityLink.getUserId() != null) {
            getUserResult = Optional.ofNullable(userManagementService.findById(identityLink.getUserId()));

            if (!getUserResult.isPresent()) {
                logger.warn("Cannot auto-assign: No user found with id {}", identityLink.getUserId());
            }
        } else if (identityLink.getGroupId() != null) {
            List<ManageableUser> usersOfGroup = userManagementService.findByRole(identityLink.getGroupId());

            if (usersOfGroup.size() == 1) {
                getUserResult = Optional.of(usersOfGroup.get(0));
            } else {
                logger.debug("Cannot auto-assign. Candidate was a user-group and group did not consist of exactly one user, was: {}.", usersOfGroup.size());
            }
        } else {
            logger.debug("Cannot auto-assign. Candidate is neither a user nor a group.");
        }
        return getUserResult;
    }

}
