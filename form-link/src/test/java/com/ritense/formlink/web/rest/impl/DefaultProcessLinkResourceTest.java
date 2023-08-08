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

package com.ritense.formlink.web.rest.impl;

import com.ritense.formlink.domain.TaskOpenResult;
import com.ritense.formlink.service.ProcessLinkService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultProcessLinkResourceTest {
    private ProcessLinkService service = mock(ProcessLinkService.class);
    private DefaultProcessLinkResource resource = new DefaultProcessLinkResource(service);

    @Test
    void getTaskShouldCallService() {
        TaskOpenResult result = new TaskOpenResult(
            "type",
            "properties"
        );
        when(service.openTask(any(UUID.class))).thenReturn(result);

        ResponseEntity<TaskOpenResult> taskResponse = resource.getTask(UUID.randomUUID());

        assertEquals(result, taskResponse.getBody());
        verify(service).openTask(any(UUID.class));
    }
}