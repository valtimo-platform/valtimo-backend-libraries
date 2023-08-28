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

import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import java.util.List;
import java.util.Map;
import org.camunda.community.mockito.delegate.DelegateExecutionFake;
import org.camunda.community.mockito.delegate.DelegateTaskFake;

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

    public static DelegateTaskFake mockTask(String id) {
        DelegateExecutionFake execution = new DelegateExecutionFake(id)
            .withProcessBusinessKey("businessKey")
            .withVariables(Map.of("executionVariables", "variables"));

        return new DelegateTaskFake(id)
            .withExecution(execution)
            .withVariables(Map.of("variables", "variables"))
            .withName("taskName");
    }

}