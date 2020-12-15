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

package com.ritense.valtimo.camunda.task.domain;

import com.ritense.valtimo.camunda.task.domain.reminder.AssignedTask;
import com.ritense.valtimo.camunda.task.domain.reminder.ReminderNotification;
import com.ritense.valtimo.camunda.task.domain.reminder.RoleBasedTask;
import com.ritense.valtimo.camunda.task.domain.reminder.Task;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.internal.util.JavaEightUtil.emptyOptional;

public class ReminderNotificationTest {

    private ReminderNotification reminderNotification;
    private final String email = "test@test.com";
    private final String name = "name";
    private List<AssignedTask> assignedTasks;
    private List<RoleBasedTask> roleBasedTasks;

    @BeforeEach
    public void setUp() {
        reminderNotification = new ReminderNotification(email, "template", name);
        assignedTasks = List.of(getAssignedTask());
        roleBasedTasks = List.of(getRoleBasedTask());
    }

    @Test
    public void shouldAssignAssignedTasks() {
        reminderNotification.assignAssignedTasks(assignedTasks);
        assertThat(reminderNotification.getAssignedTasks(), is(assignedTasks));
    }

    @Test
    public void assignRoleBasedTasks() {
        reminderNotification.assignRoleBasedTasks(roleBasedTasks);
        assertThat(reminderNotification.getRoleBasedTasks(), is(roleBasedTasks));
    }

    @Test
    public void shouldReturnAsEmailWithTasks() {
        reminderNotification.assignRoleBasedTasks(roleBasedTasks);
        Optional<TemplatedMailMessage> mailMessage = reminderNotification.asTemplatedMailMessage();

        assertThat(mailMessage, is(not(emptyOptional())));
    }

    @Test
    public void shouldReturnEmptyOptionalWithoutTasks() {
        Optional<TemplatedMailMessage> mailMessage = reminderNotification.asTemplatedMailMessage();

        assertThat(mailMessage, is(emptyOptional()));
    }

    private RoleBasedTask getRoleBasedTask() {
        return new RoleBasedTask("dev", new Task("id1", "devTask", LocalDate.now()));
    }

    private AssignedTask getAssignedTask() {
        return new AssignedTask(new Task("id", "name", LocalDate.now()));
    }

}