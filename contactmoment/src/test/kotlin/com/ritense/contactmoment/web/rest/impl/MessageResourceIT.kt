/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.contactmoment.web.rest.impl

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.contactmoment.BaseIntegrationTest
import com.ritense.klant.domain.Klant
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.Recipient
import java.util.UUID
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasEntry
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

internal class MessageResourceIT: BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    internal fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun getKlantcontactService() {
        val documentId = UUID.randomUUID()
        val postBody = """
            {
                "subject": "some-subject",
                "bodyText": "some-body"
            }
        """.trimIndent()

        val captor = argumentCaptor<TemplatedMailMessage>()
        whenever(klantService.getKlantForDocument(documentId)).thenReturn(
            Klant(
                "http://example.org",
                "0612345678",
                "user@example.org"
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/document/${documentId}/message")
                .content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()

        verify(mailSender).send(captor.capture())

        val sentMessage = captor.firstValue
        assertTrue(sentMessage.recipients.isPresent)
        assertThat(sentMessage.recipients.get().size, equalTo(1))

        val recipient = sentMessage.recipients.get().first()
        assertThat(recipient.email.get(), equalTo("user@example.org"))
        assertThat(recipient.name.get(), equalTo("user@example.org"))
        assertThat(recipient.type, equalTo(Recipient.Type.To))

        assertThat(sentMessage.templateIdentifier.get(), equalTo("test-template"))
        assertThat(sentMessage.subject.get(), equalTo("some-subject"))
        assertThat(sentMessage.placeholders, hasEntry("bodyText", "some-body"))
    }

}