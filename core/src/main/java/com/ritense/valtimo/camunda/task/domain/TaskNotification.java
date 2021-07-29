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

import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.Map;
import java.util.Optional;

public abstract class TaskNotification {

    protected abstract Optional<TemplatedMailMessage> asTemplatedMailMessage();

    protected abstract Map<String, Object> placeholderVariables();

    protected Map<String, Object> placeholderExecutionVariables(DelegateExecution execution) {
        Map<String, Object> executionVariables = execution.getVariables();
        executionVariables.put("business-key", execution.getProcessBusinessKey());
        return executionVariables;
    }

    protected String taskLinkFromIdAndUrl(String taskId, String baseUrl) {
        return String.format("%s#task?id=%s", baseUrl, taskId);
    }

}