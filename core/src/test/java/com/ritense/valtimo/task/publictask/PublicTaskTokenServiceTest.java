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

package com.ritense.valtimo.task.publictask;

import com.ritense.valtimo.contract.config.ValtimoProperties;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicTaskTokenServiceTest {

    private PublicTaskTokenService publicTaskTokenService;

    @BeforeEach
    public void setUp() {
        ValtimoProperties valtimoProperties = mock(ValtimoProperties.class);
        ValtimoProperties.JWT jwt = mock(ValtimoProperties.JWT.class);
        ValtimoProperties.PublicTask publicTask = mock(ValtimoProperties.PublicTask.class);

        String key = "01234567890123456789012345678901234567890123456789012345678901234567890123456789";

        when(publicTask.getTokenSecret()).thenReturn(key.getBytes(StandardCharsets.UTF_8));

        when(jwt.getTokenValidityInSeconds()).thenReturn(3600L);
        when(valtimoProperties.getJwt()).thenReturn(jwt);
        when(valtimoProperties.getPublicTask()).thenReturn(publicTask);
        publicTaskTokenService = new PublicTaskTokenService(valtimoProperties);
    }

    @Test
    public void getTokenForTask() throws PublicTaskTokenParseException {
        final String processDefinitionKey = "Test123";
        final String formLinkId = "Test456";
        final String username = "ivar.koreman@ritense.com";
        final String documentId = "Test789";
        final String taskInstanceId = "Test101112";

        Task task = mock(Task.class);
        when(task.getId()).thenReturn(processDefinitionKey);

        PublicTaskRequest.PublicTaskRequestBuilder builder = new PublicTaskRequest.PublicTaskRequestBuilder()
            .setUsername(username)
            .setProcessDefinitionKey(processDefinitionKey)
            .setFormLinkId(formLinkId)
            .setDocumentId(documentId)
            .setTaskInstanceId(taskInstanceId);

        String tokenForTask = publicTaskTokenService.getTokenForTask(builder.createPublicTaskTokenClaims(), username);
        assertThat(tokenForTask).isNotBlank();

        PublicTaskRequest claims = publicTaskTokenService.getTaskClaims(tokenForTask);

        assertThat(claims.getProcessDefinitionKey()).isEqualTo(processDefinitionKey);
        assertThat(claims.getFormLinkId()).isEqualTo(formLinkId);
        assertThat(claims.getDocumentId()).isEqualTo(documentId);
        assertThat(claims.getTaskInstanceId()).isEqualTo(taskInstanceId);
        assertThat(claims.getUsername()).isEqualTo(username);
    }

    @Test
    void testGetTokenForTask() {
    }
}