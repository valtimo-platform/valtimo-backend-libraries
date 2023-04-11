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

package com.ritense.formlink.domain.impl.formassociation;

import com.ritense.formlink.domain.FormLink;
import com.ritense.formlink.domain.TaskOpenResult;
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink;
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink;
import java.util.UUID;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class FormFormLinkTaskProviderTest {
    FormFormLinkTaskProvider provider = new FormFormLinkTaskProvider();

    @Test
    void supportsReturnsTrueOnFormTypeFormlink() {
        FormLink formLink = new BpmnElementFormIdLink("task-id", UUID.randomUUID());

        boolean supports = provider.supports(formLink);

        assertTrue(supports);
    }

    @Test
    void supportsReturnsFalseOnOtherFormlink() {
        FormLink formLink = new BpmnElementFormFlowIdLink("task-id", "form-flow-id");

        boolean supports = provider.supports(formLink);

        assertFalse(supports);
    }

    @Test
    void getTaskResultShouldReturnTaskKey() {
        FormLink formLink = new BpmnElementFormIdLink("task-id", UUID.randomUUID());

        TaskOpenResult<FormTaskOpenResultProperties> taskResult = provider.getTaskResult(mock(Task.class), formLink);

        assertEquals("form", taskResult.getType());
        assertEquals("task-id", taskResult.getProperties().getFormLinkId());
    }
}