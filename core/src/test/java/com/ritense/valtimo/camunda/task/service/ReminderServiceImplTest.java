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

package com.ritense.valtimo.camunda.task.service;

import com.ritense.valtimo.camunda.task.service.impl.ReminderServiceImpl;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.mail.MailSender;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import com.ritense.valtimo.emailnotificationsettings.service.EmailNotificationSettingsService;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.community.mockito.CamundaMockito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;
import static com.ritense.valtimo.camunda.task.service.NotificationTestHelper.user;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReminderServiceImplTest {

    private ReminderService reminderService;
    private TaskService taskService;
    private EmailNotificationSettingsService emailNotificationService;
    private MailSender mailSender;
    private UserManagementService userManagementService;
    private Task task;
    private TaskQuery taskQuery;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
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
        task = mock(Task.class);
        taskQuery = CamundaMockito.mockTaskQuery(taskService).singleResult(task);
    }

    @Test
    void shouldNotifyThreeUsersWithRoleBasedAndAssignedTasks() {
        when(emailNotificationService.findAllUsersWithReminderNotificationsEnabledForToday()).thenReturn(users());

        when(userManagementService.findByEmail(anyString()))
            .thenReturn(Optional.of(user("test1@test.com", List.of("dev"))))
            .thenReturn(Optional.of(user("test2@test.com", List.of("dev", "po"))))
            .thenReturn(Optional.of(user("test3@test.com", List.of("po"))));

        final TaskQuery taskQuery = CamundaMockito.mockTaskQuery(taskService).singleResult(task);
        when(taskService.createTaskQuery().taskCandidateGroup(anyString()).taskUnassigned()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(roleBasedTasks()).thenReturn(null).thenReturn(null);

        when(taskService.createTaskQuery().orderByTaskAssignee().asc().taskAssigned()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(assignedTasks());

        ReflectionTestUtils.setField(reminderService, "reminderTemplate", "template");

        reminderService.notifyUsersWithOpenTasks();

        verify(mailSender, times(3)).send(ArgumentMatchers.any(TemplatedMailMessage.class));
    }

    @Test
    void shouldNotifyZeroUsersNoOpenTasks() {
        when(emailNotificationService.findAllUsersWithReminderNotificationsEnabledForToday()).thenReturn(users());

        when(userManagementService.findByEmail(anyString()))
            .thenReturn(Optional.of(user("test1@test.com", List.of("dev"))))
            .thenReturn(Optional.of(user("test2@test.com", List.of("dev", "po"))))
            .thenReturn(Optional.of(user("test3@test.com", List.of("po"))));

        final TaskQuery taskQuery = CamundaMockito.mockTaskQuery(taskService).singleResult(task);
        when(taskService.createTaskQuery().taskCandidateGroup(anyString()).taskUnassigned()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(null);

        when(taskService.createTaskQuery().orderByTaskAssignee().asc().taskAssigned()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(null);

        ReflectionTestUtils.setField(reminderService, "reminderTemplate", "template");

        reminderService.notifyUsersWithOpenTasks();

        verify(mailSender, times(0)).send(ArgumentMatchers.any(TemplatedMailMessage.class));
    }

    @Test
    void shouldNotifyOneUserWithAssignedTasksOnly() {
        when(emailNotificationService.findAllUsersWithReminderNotificationsEnabledForToday()).thenReturn(users());

        when(userManagementService.findByEmail(anyString()))
            .thenReturn(Optional.of(user("test1@test.com", List.of("dev"))))
            .thenReturn(Optional.of(user("test2@test.com", List.of("dev", "po"))))
            .thenReturn(Optional.of(user("test3@test.com", List.of("po"))));

        when(taskService.createTaskQuery().taskCandidateGroup(anyString()).taskUnassigned()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(null);

        when(taskService.createTaskQuery().orderByTaskAssignee().asc().taskAssigned()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(null)
            .thenReturn(assignedTasks())
            .thenReturn(null);

        ReflectionTestUtils.setField(reminderService, "reminderTemplate", "template");

        reminderService.notifyUsersWithOpenTasks();

        verify(mailSender, times(1)).send(ArgumentMatchers.any(TemplatedMailMessage.class));
    }

    @Test
    void shouldNotifyZeroUsersNoUsers() {
        when(emailNotificationService.findAllUsersWithReminderNotificationsEnabledForToday()).thenReturn(null);

        ReflectionTestUtils.setField(reminderService, "reminderTemplate", "template");

        reminderService.notifyUsersWithOpenTasks();

        verify(mailSender, times(0)).send(ArgumentMatchers.any(TemplatedMailMessage.class));
    }

    private List<String> users() {
        return List.of("test1@test.com", "test2@test.com", "test3@test.com");
    }

    private List<Task> roleBasedTasks() {
        Task taskId0 = roleBasedTaskEntity("id01", "taskDev1");
        Task taskId1 = roleBasedTaskEntity("id2", "task2");
        Task taskId2 = roleBasedTaskEntity("id3", "task3");
        return List.of(taskId0, taskId1, taskId2);
    }

    private List<Task> assignedTasks() {
        Task taskId0 = assignedTaskEntity("id1", "test1@test.com", "task1");
        Task taskId2 = assignedTaskEntity("id3", "test2@test.com", "task3");
        return List.of(taskId0, taskId2);
    }

    private Task assignedTaskEntity(String id, String assignee, String taskName) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(id);
        taskEntity.setAssignee(assignee);
        taskEntity.setCreateTime(new Date());
        taskEntity.setName(taskName);
        return taskEntity;
    }

    private Task roleBasedTaskEntity(String id, String taskName) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(id);
        taskEntity.setCreateTime(new Date());
        taskEntity.setName(taskName);
        return taskEntity;
    }
}
