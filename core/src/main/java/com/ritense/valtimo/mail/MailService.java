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

package com.ritense.valtimo.mail;

import com.ritense.resource.service.ResourceService;
import com.ritense.resource.web.ObjectContentDTO;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.basictype.EmailAddress;
import com.ritense.valtimo.contract.basictype.SimpleName;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.contract.mail.MailSender;
import com.ritense.valtimo.contract.mail.model.MailMessageStatus;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import com.ritense.valtimo.contract.mail.model.value.Attachment;
import com.ritense.valtimo.contract.mail.model.value.AttachmentCollection;
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier;
import com.ritense.valtimo.contract.mail.model.value.Recipient;
import com.ritense.valtimo.contract.mail.model.value.Sender;
import com.ritense.valtimo.contract.mail.model.value.Subject;
import com.ritense.valtimo.contract.mail.model.value.attachment.Content;
import com.ritense.valtimo.contract.mail.model.value.attachment.Name;
import com.ritense.valtimo.contract.mail.model.value.attachment.Type;
import com.ritense.valtimo.exception.ExpectedElementTemplatePropertyNotFoundException;
import com.ritense.valtimo.exception.IllegalElementTemplatePropertyValueException;
import com.ritense.valtimo.helper.ActivityHelper;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import com.ritense.valtimo.helper.SendElementTemplateTaskMailHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.VariableScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/*
Please do not ever use this class anymore.
Instead use package com.ritense.valtimo.camunda.task.service to implement task notifications.
Example NotificationService.class*/
@Deprecated(since = "4.0.5-RELEASE", forRemoval = true)
@Slf4j
@RequiredArgsConstructor
public class MailService {

    public static final String BUSINESS_KEY = "business-key";
    public static final String PLACEHOLDERS_KEY = "var";
    private final MailSender mailSender;
    private final DelegateTaskHelper delegateTaskHelper;
    private final ValtimoProperties valtimoProperties;
    private final ActivityHelper activityHelper;
    private final Optional<ResourceService> optionalResourceService;

    @Deprecated(since = "16-09-2019")
    public Optional<List<MailMessageStatus>> send(
        DelegateExecution delegateExecution,
        Subject subject,
        EmailAddress emailAddress,
        SimpleName name,
        MailTemplateIdentifier templateIdentifier,
        Map<String, Object> variables
    ) {
        return send(
            delegateExecution, subject, emailAddress, name, templateIdentifier, variables, AttachmentCollection.empty()
        );
    }

    @Deprecated(since = "16-09-2019")
    public Optional<List<MailMessageStatus>> send(
        DelegateExecution delegateExecution,
        Subject subject,
        EmailAddress emailAddress,
        SimpleName name,
        MailTemplateIdentifier templateIdentifier,
        Map<String, Object> variables,
        AttachmentCollection attachments
    ) {
        logger.info("Send mail to {} using template {}", emailAddress, templateIdentifier);

        appendProcessVariables(variables, delegateExecution);

        TemplatedMailMessage templatedMailMessage = TemplatedMailMessage.with(Recipient.to(emailAddress, name), templateIdentifier)
            .placeholders(variables)
            .subject(subject)
            .attachments(attachments)
            .build();

        return mailSender.send(templatedMailMessage);
    }

    @Deprecated(since = "16-09-2019")
    public Optional<List<MailMessageStatus>> send(
        DelegateExecution delegateExecution,
        Subject subject,
        EmailAddress emailAddress,
        SimpleName name,
        Sender sender,
        MailTemplateIdentifier templateIdentifier,
        Map<String, Object> variables
    ) {
        return send(
            delegateExecution, subject, emailAddress, name, sender, templateIdentifier, variables, AttachmentCollection.empty()
        );
    }

    @Deprecated(since = "16-09-2019")
    public Optional<List<MailMessageStatus>> send(
        DelegateExecution delegateExecution,
        Subject subject,
        EmailAddress emailAddress,
        SimpleName name,
        Sender sender,
        MailTemplateIdentifier templateIdentifier,
        Map<String, Object> variables,
        AttachmentCollection attachments
    ) {
        logger.info("Send mail to {} from {} using template {}", emailAddress, sender, templateIdentifier);

        appendProcessVariables(variables, delegateExecution);

        TemplatedMailMessage templatedMailMessage = TemplatedMailMessage.with(Recipient.to(emailAddress, name), templateIdentifier)
            .placeholders(variables)
            .subject(subject)
            .sender(sender)
            .attachments(attachments)
            .build();

        return mailSender.send(templatedMailMessage);
    }

    @Deprecated(since = "16-09-2019")
    public Optional<List<MailMessageStatus>> send(
        DelegateExecution delegateExecution,
        Subject subject,
        EmailAddress emailAddress,
        SimpleName name,
        MailTemplateIdentifier templateIdentifier
    ) {
        return send(delegateExecution, subject, emailAddress, name, templateIdentifier, Collections.emptyMap());
    }

    @Deprecated(since = "16-09-2019")
    public Optional<List<MailMessageStatus>> send(
        DelegateExecution delegateExecution,
        EmailAddress emailAddress,
        SimpleName name,
        MailTemplateIdentifier templateIdentifier,
        Map<String, Object> variables
    ) {
        return send(delegateExecution, Subject.none(), emailAddress, name, templateIdentifier, variables, AttachmentCollection.empty());
    }

    @Deprecated(since = "16-09-2019")
    public Optional<List<MailMessageStatus>> send(
        DelegateExecution delegateExecution,
        EmailAddress emailAddress,
        SimpleName name,
        MailTemplateIdentifier templateIdentifier,
        Map<String, Object> variables,
        AttachmentCollection attachments
    ) {
        return send(delegateExecution, Subject.none(), emailAddress, name, templateIdentifier, variables, attachments);
    }

    @Deprecated(since = "16-09-2019")
    public Optional<List<MailMessageStatus>> send(
        DelegateExecution delegateExecution,
        EmailAddress emailAddress,
        SimpleName name,
        MailTemplateIdentifier templateIdentifier
    ) {
        return send(delegateExecution, Subject.none(), emailAddress, name, templateIdentifier, Collections.emptyMap());
    }

    @Deprecated
    public Optional<List<MailMessageStatus>> send(
        DelegateExecution delegateExecution, String email, String name, String template, Map<String, Object> variables
    ) {
        logger.info("Send mail to {} using template {}", email, template);

        variables.put("var", delegateExecution.getVariables());

        return send(
            delegateExecution,
            EmailAddress.from(email),
            SimpleName.from(name),
            MailTemplateIdentifier.from(template),
            variables,
            AttachmentCollection.empty()
        );
    }

    @Deprecated
    public Optional<List<MailMessageStatus>> send(DelegateExecution delegateExecution, String email, String name, String template) {
        logger.info("Send mail to {} using template {}", email, template);

        Map<String, Object> variables = new HashMap<>();
        variables.put("var", delegateExecution.getVariables());

        return send(
            delegateExecution,
            EmailAddress.from(email),
            SimpleName.from(name),
            MailTemplateIdentifier.from(template),
            variables,
            AttachmentCollection.empty()
        );
    }

    @Deprecated
    public Optional<List<MailMessageStatus>> sendElementTemplateTaskMail(
        DelegateExecution delegateExecution
    ) throws ExpectedElementTemplatePropertyNotFoundException, IllegalElementTemplatePropertyValueException {
        // get camunda element template properties
        Map<String, Object> camundaProperties = activityHelper.getCamundaProperties(delegateExecution.getBpmnModelElementInstance().getExtensionElements());

        // validate expected properties to be available
        SendElementTemplateTaskMailHelper.validateExpectedKeys(camundaProperties);

        // get process variables
        Map<String, Object> processVariables = delegateExecution.getVariables();

        return send(
            delegateExecution,
            Subject.from(SendElementTemplateTaskMailHelper.getSubjectKeyValue(camundaProperties, processVariables)),
            EmailAddress.from(SendElementTemplateTaskMailHelper.getReceiverKeyValue(camundaProperties, processVariables)),
            SimpleName.none(),
            Sender.from(EmailAddress.from(SendElementTemplateTaskMailHelper.getSenderKeyValue(camundaProperties, processVariables))),
            MailTemplateIdentifier.from(SendElementTemplateTaskMailHelper.getTemplateKeyValue(camundaProperties, processVariables)),
            camundaProperties,
            extractAttachmentsFromVariables(delegateExecution, SendElementTemplateTaskMailHelper.getAttachmentsKeyValue(camundaProperties, processVariables))
        );
    }

    private void appendProcessVariables(Map<String, Object> map, DelegateExecution delegateExecution) {
        Map<String, Object> variables = delegateExecution.getVariables();
        variables.put(BUSINESS_KEY, delegateExecution.getProcessBusinessKey());
        map.put(PLACEHOLDERS_KEY, variables);
    }

    public void sendNotification(DelegateTask delegateTask) {
        logger.info("Task Notification for task '{}'", delegateTask.getName());
        sendTaskNotificationMail(delegateTask, defaultNotificationTemplate(), AttachmentCollection.empty());
    }

    public void sendNotification(DelegateTask delegateTask, String template) {
        logger.info("Task Notification with custom template '{}'", template);
        sendTaskNotificationMail(delegateTask, MailTemplateIdentifier.from(template), AttachmentCollection.empty());
    }

    public void sendNotification(DelegateTask delegateTask, String template, String... attachmentVariableKeys) {
        logger.info("Task Notification with custom template '{}' with attachments '{}'", template, attachmentVariableKeys);
        AttachmentCollection attachmentCollection = extractAttachmentsFromVariables(delegateTask, Arrays.asList(attachmentVariableKeys));
        sendTaskNotificationMail(delegateTask, MailTemplateIdentifier.from(template), attachmentCollection);
    }

    private Optional<List<MailMessageStatus>> sendTaskNotificationMail(
        DelegateTask delegateTask,
        MailTemplateIdentifier mailTemplateIdentifier,
        AttachmentCollection attachments
    ) {
        logger.info("sendTaskNotificationMail for task '{}' and assignee '{}'", delegateTask.getName(), delegateTask.getAssignee());
        if (delegateTaskHelper.isTaskBeingAssigned(delegateTask)) {
            Optional<ManageableUser> assignee = delegateTaskHelper.determineAssignedUserOf(delegateTask);
            if (assignee.isPresent()) {
                logger.info("Sending notification to {}", assignee.get().getEmail());
                return sendTaskAssignmentNotificationMail(assignee.get(), delegateTask, mailTemplateIdentifier, attachments);
            } else {
                logger.error("Completion link could not be send user {} does not exist", delegateTask.getAssignee());
            }
        } else if (delegateTaskHelper.isTaskBeingCreated(delegateTask)) {
            List<MailMessageStatus> mailMessageStatusList = new ArrayList<>();
            for (ManageableUser candidate : delegateTaskHelper.findCandidateUsers(delegateTask)) {
                Optional<List<MailMessageStatus>> mailMessageStatusListOfOneSend = sendTaskAssignmentNotificationMail(
                    candidate, delegateTask, mailTemplateIdentifier, attachments
                );
                mailMessageStatusListOfOneSend.ifPresent(mailMessageStatusList::addAll);
            }
            return Optional.of(mailMessageStatusList);
        }
        return Optional.empty();
    }

    private Optional<List<MailMessageStatus>> sendTaskAssignmentNotificationMail(
        ManageableUser user,
        DelegateTask delegateTask,
        MailTemplateIdentifier template,
        AttachmentCollection attachments
    ) {
        logger.debug("Sending task assignment notification e-mail to '{}'", user.getEmail());

        String taskDetailUrl = createTaskDetailUrl(delegateTask.getId());

        Map<String, Object> variables = extractRelevantUserVariables(user);
        variables.put("taskname", delegateTask.getName());
        variables.put("var", delegateTask.getVariables());
        variables.put("baseUrl", valtimoProperties.getApp().getBaselUrl());
        variables.put("link", taskDetailUrl);

        // default voor nu alleen NL mails
        MailTemplateIdentifier mailTemplateIdentifierWithLangkey = template.withLanguageKey("nl");
        return send(
            delegateTask.getExecution(),
            EmailAddress.from(user.getEmail()),
            SimpleName.from(user.getFullName()),
            mailTemplateIdentifierWithLangkey,
            variables,
            attachments
        );
    }

    /* SILLY HELPERS */
    private MailTemplateIdentifier defaultCompletionTemplate() {
        return MailTemplateIdentifier.from(valtimoProperties.getMandrill().getCompletionTemplate());
    }

    private MailTemplateIdentifier defaultNotificationTemplate() {
        return MailTemplateIdentifier.from(valtimoProperties.getMandrill().getNotificationTemplate());
    }

    private Map<String, Object> extractRelevantUserVariables(ManageableUser user) {
        return extractRelevantUserVariables(user.getFirstName(), user.getLastName(), user.getLastName());
    }

    private Map<String, Object> extractRelevantUserVariables(String firstName, String lastName, String email) {
        Map<String, Object> userVariables = new HashMap<>();
        userVariables.put("firstname", firstName);
        userVariables.put("lastname", lastName);
        userVariables.put("email", email);

        return userVariables;
    }

    private String createTaskDetailUrl(String taskId) {
        String baseUrl = valtimoProperties.getApp().getBaselUrl();

        return String.format("%s#?taskId=%s", baseUrl, taskId);
    }

    private String createExternalTaskDetailUrl(String taskId) {
        String baseUrl = valtimoProperties.getApp().getBaselUrl();

        return String.format("%s#/external/complete-task/%s", baseUrl, taskId);
    }

    private AttachmentCollection extractAttachmentsFromVariables(VariableScope variableScope, Collection<String> attachmentVariableKeys) {
        Collection<Attachment> attachmentCollection = new ArrayList<>();
        for (String attachmentVariableKey : attachmentVariableKeys) {
            attachmentCollection.addAll(
                extractAttachmentsFromVariables((List) variableScope.getVariable(attachmentVariableKey))
            );
        }

        int attachmentsSize = getAttachmentsSize(attachmentCollection);
        if (mailSender.getMaximumSizeAttachments() > attachmentsSize) {
            return AttachmentCollection.from(attachmentCollection);
        } else {
            logger.warn("FileUploads with ids {} too large!", attachmentVariableKeys);
            return AttachmentCollection.empty();
        }
    }

    private Collection<Attachment> extractAttachmentsFromVariables(List<UUID> attachmentIds) {
        Collection<Attachment> attachmentCollection = new ArrayList<>();
        if (attachmentIds != null) {
            for (UUID serializedResourceId : attachmentIds) {
                optionalResourceService.ifPresent(resourceService -> {
                    ObjectContentDTO objectContent = resourceService.getResourceContent(serializedResourceId);
                    attachmentCollection.add(
                        Attachment.from(
                            Name.from(objectContent.getResource().getName()),
                            Type.from(objectContent.getResource().getExtension()),
                            Content.from(objectContent.getContent())
                        )
                    );
                });
            }
        }
        return attachmentCollection;
    }

    private int getAttachmentsSize(Collection<Attachment> attachments) {
        int attachmentsSize = 0;
        for (Attachment attachment : attachments) {
            attachmentsSize = attachmentsSize + attachment.content.get().length;
        }
        return attachmentsSize;
    }

}