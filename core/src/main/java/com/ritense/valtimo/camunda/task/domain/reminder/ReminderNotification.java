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

package com.ritense.valtimo.camunda.task.domain.reminder;

import com.ritense.valtimo.camunda.task.domain.TaskNotification;
import com.ritense.valtimo.contract.basictype.EmailAddress;
import com.ritense.valtimo.contract.basictype.SimpleName;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier;
import com.ritense.valtimo.contract.mail.model.value.Recipient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;

public class ReminderNotification extends TaskNotification {

    private final String userEmail;
    private final MailTemplateIdentifier mailTemplate;
    private final String userName;
    private List<AssignedTask> assignedTasks;
    private List<RoleBasedTask> roleBasedTasks;

    public ReminderNotification(String userEmail, String mailTemplate, String userName) {
        assertArgumentNotEmpty(userEmail, "userEmail is required");
        assertArgumentNotEmpty(mailTemplate, "mailTemplate is required");
        assertArgumentNotEmpty(userName, "userName is required");
        this.userEmail = userEmail;
        this.mailTemplate = MailTemplateIdentifier.from(mailTemplate);
        this.userName = userName;
        this.assignedTasks = new ArrayList<>();
        this.roleBasedTasks = new ArrayList<>();
    }

    public void assignAssignedTasks(List<AssignedTask> tasks) {
        if (tasks != null) {
            assignedTasks.addAll(tasks);
        }
    }

    public void assignRoleBasedTasks(List<RoleBasedTask> tasks) {
        if (tasks != null) {
            roleBasedTasks.addAll(tasks);
        }
    }

    public List<AssignedTask> getAssignedTasks() {
        return assignedTasks;
    }

    public List<RoleBasedTask> getRoleBasedTasks() {
        return roleBasedTasks.stream().sorted(Comparator.comparing(RoleBasedTask::getRole)).collect(Collectors.toList());
    }

    private String getUserName() {
        return userName;
    }

    private boolean containsTasks() {
        return !(assignedTasks.isEmpty() && roleBasedTasks.isEmpty());
    }

    @Override
    public Optional<TemplatedMailMessage> asTemplatedMailMessage() {
        if (this.containsTasks()) {
            return Optional.of(TemplatedMailMessage.with(
                Recipient.to(EmailAddress.from(userEmail), SimpleName.from(userName)),
                mailTemplate.withLanguageKey("nl"))
                .placeholders(placeholderVariables())
                .build());
        }
        return Optional.empty();
    }

    @Override
    public Map<String, Object> placeholderVariables() {
        return Map.of(
            "userName", getUserName(),
            "assignedTasks", getAssignedTasks(),
            "roleTasks", getRoleBasedTasks());
    }

}
