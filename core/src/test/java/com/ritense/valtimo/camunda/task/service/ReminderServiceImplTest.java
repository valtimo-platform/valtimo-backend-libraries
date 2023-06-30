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

package com.ritense.valtimo.camunda.task.service;

import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.task.service.impl.ReminderServiceImpl;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.mail.MailSender;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import com.ritense.valtimo.emailnotificationsettings.service.EmailNotificationSettingsService;
import com.ritense.valtimo.service.CamundaTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static com.ritense.valtimo.camunda.task.service.NotificationTestHelper.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReminderServiceImplTest {

    private ReminderService reminderService;
    private CamundaTaskService taskService;
    private EmailNotificationSettingsService emailNotificationService;
    private MailSender mailSender;
    private UserManagementService userManagementService;

    @BeforeEach
    void setUp() {
        taskService = mock(CamundaTaskService.class);
        emailNotificationService = mock(EmailNotificationSettingsService.class);
        mailSender = mock(MailSender.class);
        userManagementService = mock(UserManagementService.class);
        reminderService = new ReminderServiceImpl(
            taskService,
            emailNotificationService,
            mailSender,
            userManagementService,
            "reminderTemplate"
        );
    }

    @Test
    void shouldNotifyThreeUsersWithRoleBasedAndAssignedTasks() {
        when(emailNotificationService.findAllUsersWithReminderNotificationsEnabledForToday()).thenReturn(users());

        when(userManagementService.findByEmail(anyString()))
            .thenReturn(Optional.of(user("test1@test.com", List.of("dev"))))
            .thenReturn(Optional.of(user("test2@test.com", List.of("dev", "po"))))
            .thenReturn(Optional.of(user("test3@test.com", List.of("po"))));

        when(taskService.findTasks(any()))
            .thenReturn(roleBasedTasks()).thenReturn(List.of()).thenReturn(List.of()).thenReturn(assignedTasks());

        ReflectionTestUtils.setField(reminderService, "reminderTemplate", "template");

        reminderService.notifyUsersWithOpenTasks();

        verify(mailSender, times(3)).send(any(TemplatedMailMessage.class));
    }

    @Test
    void shouldNotifyZeroUsersNoOpenTasks() {
        when(emailNotificationService.findAllUsersWithReminderNotificationsEnabledForToday()).thenReturn(users());

        when(userManagementService.findByEmail(anyString()))
            .thenReturn(Optional.of(user("test1@test.com", List.of("dev"))))
            .thenReturn(Optional.of(user("test2@test.com", List.of("dev", "po"))))
            .thenReturn(Optional.of(user("test3@test.com", List.of("po"))));

        when(taskService.findTasks(any())).thenReturn(List.of());

        ReflectionTestUtils.setField(reminderService, "reminderTemplate", "template");

        reminderService.notifyUsersWithOpenTasks();

        verify(mailSender, times(0)).send(any(TemplatedMailMessage.class));
    }

    @Test
    void shouldNotifyOneUserWithAssignedTasksOnly() {
        when(emailNotificationService.findAllUsersWithReminderNotificationsEnabledForToday()).thenReturn(users());

        when(userManagementService.findByEmail(anyString()))
            .thenReturn(Optional.of(user("test1@test.com", List.of("dev"))))
            .thenReturn(Optional.of(user("test2@test.com", List.of("dev", "po"))))
            .thenReturn(Optional.of(user("test3@test.com", List.of("po"))));

        when(taskService.findTasks(any()))
            .thenReturn(List.of())
            .thenReturn(assignedTasks())
            .thenReturn(List.of());

        ReflectionTestUtils.setField(reminderService, "reminderTemplate", "template");

        reminderService.notifyUsersWithOpenTasks();

        verify(mailSender, times(1)).send(any(TemplatedMailMessage.class));
    }

    @Test
    void shouldNotifyZeroUsersNoUsers() {
        when(emailNotificationService.findAllUsersWithReminderNotificationsEnabledForToday()).thenReturn(null);

        ReflectionTestUtils.setField(reminderService, "reminderTemplate", "template");

        reminderService.notifyUsersWithOpenTasks();

        verify(mailSender, times(0)).send(any(TemplatedMailMessage.class));
    }

    private List<String> users() {
        return List.of("test1@test.com", "test2@test.com", "test3@test.com");
    }

    private List<CamundaTask> roleBasedTasks() {
        CamundaTask taskId0 = roleBasedTaskEntity("id01", "taskDev1");
        CamundaTask taskId1 = roleBasedTaskEntity("id2", "task2");
        CamundaTask taskId2 = roleBasedTaskEntity("id3", "task3");
        return List.of(taskId0, taskId1, taskId2);
    }

    private List<CamundaTask> assignedTasks() {
        CamundaTask taskId0 = assignedTaskEntity("id1", "test1@test.com", "task1");
        CamundaTask taskId2 = assignedTaskEntity("id3", "test2@test.com", "task3");
        return List.of(taskId0, taskId2);
    }

    private CamundaTask assignedTaskEntity(String id, String assignee, String taskName) {
        return new CamundaTask(
            id,
            0,
            null, null, null,
            List.of(),
            null, null, null,
            taskName,
            null, null, null, null,
            assignee,
            null,
            0,
            LocalDateTime.now(), null, null, null, 0, null,
            Set.of()
        );
    }

    private CamundaTask roleBasedTaskEntity(String id, String taskName) {
        return new CamundaTask(
            id,
            0,
            null, null, null,
            List.of(),
            null, null, null,
            taskName,
            null, null, null, null, null, null,
            0,
            LocalDateTime.now(), null, null, null, 0, null,
            Set.of()
        );
    }
}
