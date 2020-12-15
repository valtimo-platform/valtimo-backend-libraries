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

import com.ritense.valtimo.security.jwt.token.TokenClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class PublicTaskRequest implements TokenClaims {

    private final String processDefinitionKey;
    private final String formLinkId;
    private final String documentId;
    private final String taskInstanceId;
    private final String username;

    public PublicTaskRequest(String processDefinitionKey, String formLinkId, String documentId, String taskInstanceId) {
        this(processDefinitionKey, formLinkId, documentId, taskInstanceId, null);
    }

    public PublicTaskRequest(String processDefinitionKey, String formLinkId, String documentId, String taskInstanceId, String username) {
        assertArgumentNotNull(processDefinitionKey, "processDefinitionKey is required");
        assertArgumentNotNull(formLinkId, "formLinkId is required");
        assertArgumentNotNull(documentId, "documentId is required");
        assertArgumentNotNull(taskInstanceId, "taskInstanceId is required");
        this.processDefinitionKey = processDefinitionKey;
        this.formLinkId = formLinkId;
        this.documentId = documentId;
        this.taskInstanceId = taskInstanceId;
        this.username = username;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getFormLinkId() {
        return formLinkId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTaskInstanceId() {
        return taskInstanceId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public Claims getClaims() {
        final Claims claims = new DefaultClaims();
        claims.put(PublicTaskTokenService.PROCESS_DEFINITION_KEY, this.processDefinitionKey);
        claims.put(PublicTaskTokenService.FORM_LINK_ID, this.formLinkId);
        claims.put(PublicTaskTokenService.DOCUMENT_ID, this.documentId);
        claims.put(PublicTaskTokenService.TASK_INSTANCE_ID, this.taskInstanceId);

        if (this.username != null) {
            claims.put("username", this.username);
        }

        return claims;
    }

    @Override
    public String toString() {
        return "PublicTaskTokenClaims{" +
            "processDefinitionKey='" + processDefinitionKey + '\'' +
            ", formLinkId='" + formLinkId + '\'' +
            ", documentId='" + documentId + '\'' +
            ", taskInstanceId='" + taskInstanceId + '\'' +
            ", username='" + username + '\'' +
            '}';
    }

    public static class PublicTaskRequestBuilder {
        private String processDefinitionKey;
        private String formLinkId;
        private String documentId;
        private String taskInstanceId;
        private String username = null;

        public PublicTaskRequestBuilder setProcessDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public PublicTaskRequestBuilder setFormLinkId(String formLinkId) {
            this.formLinkId = formLinkId;
            return this;
        }

        public PublicTaskRequestBuilder setDocumentId(String documentId) {
            this.documentId = documentId;
            return this;
        }

        public PublicTaskRequestBuilder setTaskInstanceId(String taskInstanceId) {
            this.taskInstanceId = taskInstanceId;
            return this;
        }

        public PublicTaskRequestBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public PublicTaskRequest createPublicTaskTokenClaims() {
            return new PublicTaskRequest(processDefinitionKey, formLinkId, documentId, taskInstanceId, username);
        }
    }
}
