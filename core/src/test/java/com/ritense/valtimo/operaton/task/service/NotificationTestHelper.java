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

package com.ritense.valtimo.operaton.task.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.DelegateTask;

public class NotificationTestHelper {

    public static ManageableUser user(String email, List<String> role) {
        return new ValtimoUserBuilder()
            .id("id")
            .username("username")
            .name("full name")
            .email(email)
            .firstName("firstName")
            .lastName("lastName")
            .phoneNo("04545656")
            .isEmailVerified(true)
            .langKey("nl")
            .blocked(false)
            .activated(true)
            .roles(role).build();
    }

    public static DelegateTask mockTask(String id) {
        DelegateExecution delegateExecution = mock(DelegateExecution.class);
        when(delegateExecution.getId()).thenReturn(id);
        when(delegateExecution.getProcessBusinessKey()).thenReturn("businessKey");
        Map<String, Object> executionVariables = new HashMap<>();
        executionVariables.put("executionVariables", "variables");
        when(delegateExecution.getVariables()).thenReturn(executionVariables);

        DelegateTask delegateTask = mock(DelegateTask.class);
        when(delegateTask.getId()).thenReturn(id);
        when(delegateTask.getExecution()).thenReturn(delegateExecution);
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("variables", "variables");
        when(delegateTask.getVariables()).thenReturn(taskVariables);
        when(delegateTask.getName()).thenReturn("taskName");
        when(delegateTask.getAssignee()).thenReturn("AAAA-1111");

        return delegateTask;
    }

}