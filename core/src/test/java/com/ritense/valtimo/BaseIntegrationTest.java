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

package com.ritense.valtimo;

import com.ritense.outbox.OutboxService;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.mail.MailSender;
import com.ritense.valtimo.repository.CamundaSearchProcessInstanceRepository;
import com.ritense.valtimo.service.ProcessDefinitionCaseDefinitionLinker;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(properties = {"valtimo.outbox.enabled=true"}, classes = {CoreTestConfiguration.class})
@ExtendWith(SpringExtension.class)
@Tag("integration")
public abstract class BaseIntegrationTest {

    @Autowired
    public RuntimeService runtimeService;

    @MockitoBean
    public AuditEventListener auditEventListener;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    public UserManagementService userManagementService;

    @MockitoBean
    public MailSender mailSender;

    @MockitoBean
    public ProcessDefinitionCaseDefinitionLinker processDefinitionCaseDefinitionLinker;

    @MockitoSpyBean
    public OutboxService outboxService;

    @MockitoSpyBean
    public CamundaSearchProcessInstanceRepository camundaSearchProcessInstanceRepository;

    @MockitoSpyBean
    public TaskService camundaTaskService;

    @BeforeAll
    static void beforeAll() {
    }

    @BeforeEach
    public void beforeEach() {
    }

    @AfterEach
    public void afterEach() {
    }

    public interface AuditEventListener {
        @EventListener(classes = AuditEvent.class)
        void handle(AuditEvent auditEvent);
    }

}
