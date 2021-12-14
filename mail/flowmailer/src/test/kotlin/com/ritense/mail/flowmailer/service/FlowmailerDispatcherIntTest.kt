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

package com.ritense.mail.flowmailer.service

import com.ritense.mail.flowmailer.BaseIntegrationTest
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.Attachment
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.Sender
import com.ritense.valtimo.contract.mail.model.value.Subject
import com.ritense.valtimo.contract.mail.model.value.attachment.Content
import com.ritense.valtimo.contract.mail.model.value.attachment.Name
import com.ritense.valtimo.contract.mail.model.value.attachment.Type
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import javax.inject.Inject

internal class FlowmailerDispatcherIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var resourceLoader: ResourceLoader

    @Inject
    lateinit var flowmailerMailDispatcher: FlowmailerMailDispatcher

    @Test
    fun `should instantiate FlowmailerDispatcher bean`() {
        assertThat(flowmailerMailDispatcher).isNotNull

        val resource = loadResources()[0]

        val templatedMailMessage = TemplatedMailMessage.with(
            Recipient.to(
                EmailAddress.from("tom.bokma@ritense.com"),
                SimpleName.from("Tom Bokma")
            ),
            MailTemplateIdentifier.from("SUSWRYRS")
        )
            .subject(Subject.from("Subject"))
            .sender(Sender.from(EmailAddress.from("flowmailertest@ritense.com"), SimpleName.from("flowmailertest")))
            .placeholders(mapOf(
                "var" to mapOf(
                    "voornaam" to "Voornaam",
                    "taaknaam" to "taaknaam",
                    "taaklink" to "taaklink"
                )
            ))
            .attachment(
                Attachment.from(
                    Name.from(resource.filename),
                    Type.from(FilenameUtils.getExtension(resource.filename)),
                    //Content.from(getEncoder().encode(resource.inputStream.readAllBytes()))
                    Content.from(resource.inputStream.readAllBytes())
                )
            )
            .build()

        flowmailerMailDispatcher.send(templatedMailMessage)
    }

    fun loadResources(): Array<Resource> {
        val PATH = "classpath*:mail/*.txt"
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PATH)
    }


}