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

package com.ritense.valtimo.camunda.task.domain.notification;

import com.ritense.valtimo.camunda.task.domain.TaskNotification;
import com.ritense.valtimo.contract.basictype.EmailAddress;
import com.ritense.valtimo.contract.basictype.SimpleName;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import com.ritense.valtimo.contract.mail.model.value.AttachmentCollection;
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier;
import com.ritense.valtimo.contract.mail.model.value.Recipient;
import org.camunda.bpm.engine.delegate.DelegateTask;
import java.util.Map;
import java.util.Optional;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class PublicAssignmentNotification extends TaskNotification {

    private final DelegateTask delegateTask;
    private final MailTemplateIdentifier mailTemplate;
    private final String firstName;
    private final String lastName;
    private final String emailAddress;
    private final String baseUrl;
    private final String languageKey;

    public PublicAssignmentNotification(
        DelegateTask delegateTask,
        MailTemplateIdentifier mailTemplate,
        String firstName,
        String lastName,
        String emailAddress,
        String baseUrl,
        String languageKey
    ) {
        assertArgumentNotNull(delegateTask, "delegateTask is required");
        assertArgumentNotNull(mailTemplate, "mailTemplate is required");
        assertArgumentNotEmpty(firstName, "firstName is required");
        assertArgumentNotEmpty(lastName, "lastName is required");
        assertArgumentNotEmpty(emailAddress, "emailAddress is required");
        assertArgumentNotEmpty(baseUrl, "baseUrl is required");
        assertArgumentNotEmpty(languageKey, "languageKey is required");
        this.delegateTask = delegateTask;
        this.mailTemplate = mailTemplate;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.baseUrl = baseUrl;
        this.languageKey = languageKey;
    }

    @Override
    public Optional<TemplatedMailMessage> asTemplatedMailMessage() {
        return Optional.of(TemplatedMailMessage.with(
            Recipient.to(EmailAddress.from(emailAddress), SimpleName.from(firstName + "" + lastName)),
            mailTemplate.withLanguageKey(languageKey))
            .placeholders(placeholderVariables())
            .attachments(AttachmentCollection.empty())
            .build());
    }

    @Override
    public Map<String, Object> placeholderVariables() {
        Map<String, Object> executionVariables = placeholderExecutionVariables(delegateTask.getExecution());

        return Map.of(
            "taskname", delegateTask.getName(),
            "var", delegateTask.getVariables(),
            "baseUrl", baseUrl,
            "link", taskLinkFromIdAndUrl(delegateTask.getId(), baseUrl),
            "firstname", firstName,
            "lastname", lastName,
            "email", emailAddress,
            "execution", executionVariables);
    }

}