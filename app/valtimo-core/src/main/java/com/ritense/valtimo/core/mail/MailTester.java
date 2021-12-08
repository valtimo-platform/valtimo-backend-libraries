package com.ritense.valtimo.core.mail;

import com.ritense.mail.MailDispatcher;
import com.ritense.mail.service.MailService;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import com.ritense.valtimo.contract.mail.model.value.Attachment;
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier;
import com.ritense.valtimo.contract.mail.model.value.attachment.Content;
import com.ritense.valtimo.contract.mail.model.value.attachment.Name;
import com.ritense.valtimo.contract.mail.model.value.attachment.Type;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class MailTester {
    private final ResourceLoader resourceLoader;
    private final MailService mailService;
    private final MailDispatcher mailDispatcher;

    public void sendEmail(DelegateExecution delegateExecution) throws IOException {
        final var serviceMailSettings = mailService.getMailSettings(delegateExecution);

        final var resource = Arrays.stream(loadResources()).findFirst().orElseThrow();
        final var templatedMailMessage = TemplatedMailMessage.with(
                serviceMailSettings.getRecipient(), MailTemplateIdentifier.from(serviceMailSettings.getMailSendTaskTemplate()))
            .placeholders(serviceMailSettings.getPlaceholders())
            .subject(serviceMailSettings.getSubject())
            .sender(serviceMailSettings.getSender())
            .attachment(
                Attachment.from(
                    Name.from("ExampleAttachment"),
                    Type.from(FilenameUtils.getExtension(resource.getFilename())),
                    Content.from(Base64.getEncoder().encode(resource.getInputStream().readAllBytes()))
                )
            )
            .build();


        mailDispatcher.send(templatedMailMessage);
    }

    private Resource[] loadResources() throws IOException {
        final String PATH = "classpath*:mail/*.txt";
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PATH);
    }

}
