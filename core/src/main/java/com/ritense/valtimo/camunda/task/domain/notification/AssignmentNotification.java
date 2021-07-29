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

package com.ritense.valtimo.camunda.task.domain.notification;

import com.ritense.valtimo.camunda.task.domain.TaskNotification;
import com.ritense.valtimo.contract.authentication.ManageableUser;
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

public class AssignmentNotification extends TaskNotification {

    private final DelegateTask delegateTask;
    private final MailTemplateIdentifier mailTemplate;
    private final ManageableUser user;
    private final String baseUrl;
    private final String languageKey;

    public AssignmentNotification(
        DelegateTask delegateTask,
        MailTemplateIdentifier mailTemplate,
        ManageableUser user,
        String baseUrl,
        String languageKey
    ) {
        assertArgumentNotNull(delegateTask, "delegateTask is required");
        assertArgumentNotNull(mailTemplate, "mailTemplate is required");
        assertArgumentNotNull(user, "user is required");
        assertArgumentNotEmpty(baseUrl, "baseUrl is required");
        assertArgumentNotEmpty(languageKey, "languageKey is required");
        this.delegateTask = delegateTask;
        this.mailTemplate = mailTemplate;
        this.user = user;
        this.baseUrl = baseUrl;
        this.languageKey = languageKey;
    }

    @Override
    public Optional<TemplatedMailMessage> asTemplatedMailMessage() {
        return Optional.of(
            TemplatedMailMessage.with(
                Recipient.to(
                    EmailAddress.from(
                        user.getEmail()
                    ),
                    SimpleName.from(
                        user.getFirstName() + "" + user.getLastName()
                    )
                ),
                mailTemplate.withLanguageKey(languageKey)
            )
                .placeholders(placeholderVariables())
                .attachments(AttachmentCollection.empty())
                .build()
        );
    }

    @Override
    protected Map<String, Object> placeholderVariables() {
        var executionVariables = placeholderExecutionVariables(
            delegateTask.getExecution()
        );
        return Map.of(
            "taskname", delegateTask.getName(),
            "var", delegateTask.getVariables(),
            "baseUrl", baseUrl,
            "link", taskLinkFromIdAndUrl(delegateTask.getId(), baseUrl),
            "firstname", user.getFirstName(),
            "lastname", user.getLastName(),
            "email", user.getEmail(),
            "execution", executionVariables
        );
    }

}