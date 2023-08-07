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

package com.ritense.mail.flowmailer.connector

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.service.DocumentService
import com.ritense.mail.flowmailer.BaseTest
import com.ritense.mail.flowmailer.config.FlowmailerProperties
import com.ritense.mail.flowmailer.service.FlowmailerMailDispatcher
import com.ritense.resource.service.ResourceService
import com.ritense.tenancy.TenantResolver
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.mockito.delegate.DelegateExecutionFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FlowmailerConnectorTest : BaseTest() {
    lateinit var flowmailerConnectorProperties: FlowmailerConnectorProperties
    lateinit var flowmailerMailDispatcher: FlowmailerMailDispatcher
    lateinit var flowmailerConnector: FlowmailerConnector
    lateinit var documentService: DocumentService
    lateinit var resourceService: ResourceService
    lateinit var tenantResolver: TenantResolver

    @BeforeEach
    fun setup() {
        super.baseSetUp()
        val flowmailerProperties = FlowmailerProperties(
            clientId = "clientId",
            clientSecret = "clientSecret",
            accountId = "accountId"
        )
        flowmailerConnectorProperties = FlowmailerConnectorProperties(flowmailerProperties)
        flowmailerMailDispatcher = mock()
        documentService = mock()
        resourceService = mock()
        tenantResolver = mock()
        whenever(tenantResolver.getTenantId()).thenReturn("1")
        flowmailerConnector = FlowmailerConnector(
            flowmailerConnectorProperties = flowmailerConnectorProperties,
            mailDispatcher = flowmailerMailDispatcher,
            documentService = documentService,
            resourceService = resourceService,
            tenantResolver = tenantResolver
        )
    }

    @Test
    fun `should get properties`() {
        //when
        val properties = flowmailerConnector.getProperties()
        //then
        assertThat(properties).isNotNull
    }

    @Test
    fun `should set properties`() {
        //Given
        val flowmailerProperties = FlowmailerProperties(
            clientId = "newClientId",
            clientSecret = "clientSecret",
            accountId = "accountId"
        )
        val flowmailerConnectorProperties = FlowmailerConnectorProperties(flowmailerProperties)
        //when
        flowmailerConnector.setProperties(flowmailerConnectorProperties)
        val properties = flowmailerConnector.getProperties()
        //then
        assertThat(properties).isNotNull
        assertThat(properties).isInstanceOf(FlowmailerConnectorProperties::class.java)
        properties as FlowmailerConnectorProperties
        assertThat(properties.flowmailerProperties.clientId).isEqualTo(flowmailerProperties.clientId)
    }

    @Test
    fun `should build TemplatedMailMessage and call the send method in the FlowmailerDispatcher`() {
        //Given
        val rootNode = ObjectMapper().createObjectNode()
        val recipient = ObjectMapper().createObjectNode().put("email", "recipient@example.com")
        val arrayNode = ObjectMapper().createArrayNode().add(recipient)
        rootNode.replace("members", arrayNode)

        val documentOptional = documentOptional(rootNode.toPrettyString())
        `when`(documentService.findBy(any(), any())).thenReturn(documentOptional)

        flowmailerConnector.sender("a@a.com")
        flowmailerConnector.subject("aSubject")
        flowmailerConnector.templateIdentifier("aIdentifier")
        flowmailerConnector.recipients(
            DelegateExecutionFake().withBusinessKey(documentOptional.get().id.id.toString()),
            "/members",
            "email"
        )
        flowmailerConnector.placeholder("key", "value")

        //When
        flowmailerConnector.sendEmail()

        //Then
        val captor: ArgumentCaptor<TemplatedMailMessage> = ArgumentCaptor.forClass(TemplatedMailMessage::class.java)
        verify(flowmailerMailDispatcher).send(capture(captor))
        assertThat(captor.value.sender.email.get()).isEqualTo("a@a.com")
        assertThat(captor.value.subject.get()).isEqualTo("aSubject")
        assertThat(captor.value.templateIdentifier.get()).isEqualTo("aIdentifier")
        assertThat(captor.value.recipients.get().first().email.get()).isEqualTo("recipient@example.com")
        assertThat(captor.value.placeholders["key"]).isEqualTo("value")
    }

}