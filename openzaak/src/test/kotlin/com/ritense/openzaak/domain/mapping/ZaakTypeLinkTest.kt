/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.domain.mapping

import com.ritense.openzaak.domain.event.EigenschappenSetEvent
import com.ritense.openzaak.domain.event.ResultaatSetEvent
import com.ritense.openzaak.domain.event.StatusSetEvent
import com.ritense.openzaak.domain.mapping.impl.Operation
import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandler
import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandlers
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLinkId
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.web.rest.request.ServiceTaskHandlerRequest
import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.mockito.delegate.DelegateExecutionFake
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import java.net.URI
import java.util.UUID
import javax.validation.ConstraintViolationException

class ZaakTypeLinkTest {

    private val id = ZaakTypeLinkId.newId(UUID.randomUUID())

    private val documentDefinitionName = "testDocumentDefinitionName"

    private val zaaktypeId = URI.create("http://example.com")

    @BeforeEach
    fun init() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should not create entity`() {
        assertThrows(ConstraintViolationException::class.java) {
            ZaakTypeLink(
                id,
                "definitelywaymorecharactersthanallowedforadocumentdefinitionname",
                zaaktypeId,
                ServiceTaskHandlers()
            )
        }
    }

    @Test
    fun `should create entity`() {
        val documentDefinitionZaaktypeLink = ZaakTypeLink(
            id,
            documentDefinitionName,
            zaaktypeId,
            ServiceTaskHandlers()
        )

        assertThat(documentDefinitionZaaktypeLink.id).isEqualTo(id)
        assertThat(documentDefinitionZaaktypeLink.documentDefinitionName).isEqualTo(documentDefinitionName)
        assertThat(documentDefinitionZaaktypeLink.zaakTypeUrl).isEqualTo(zaaktypeId)
    }

    @Test
    fun `should assign service task handler`() {
        val zaaktypeLink = zaakTypeLink()
        val serviceTaskHandlerRequest = ServiceTaskHandlerRequest("processKey", "taskId", Operation.SET_STATUS, URI.create("http://example.com"))

        zaaktypeLink.assignZaakServiceHandler(serviceTaskHandlerRequest)

        assertThat(zaaktypeLink.serviceTaskHandlers).contains(
            ServiceTaskHandler(
                serviceTaskHandlerRequest.processDefinitionKey,
                serviceTaskHandlerRequest.serviceTaskId,
                serviceTaskHandlerRequest.operation,
                serviceTaskHandlerRequest.parameter
            )
        )
    }

    @Test
    fun `should update service task handler`() {
        val zaaktypeLink = zaakTypeLink()
        val serviceTaskHandlerRequest = ServiceTaskHandlerRequest("processKey", "taskId", Operation.SET_STATUS, URI.create("http://example.com"))
        val newServiceTaskHandlerRequest = ServiceTaskHandlerRequest("processKey", "taskId", Operation.SET_RESULTAAT, URI.create("http://newexample.com"))

        zaaktypeLink.assignZaakServiceHandler(serviceTaskHandlerRequest)
        zaaktypeLink.assignZaakServiceHandler(newServiceTaskHandlerRequest)

        assertThat(zaaktypeLink.serviceTaskHandlers).contains(
            ServiceTaskHandler(
                newServiceTaskHandlerRequest.processDefinitionKey,
                newServiceTaskHandlerRequest.serviceTaskId,
                newServiceTaskHandlerRequest.operation,
                newServiceTaskHandlerRequest.parameter
            )
        )
        assertThat(zaaktypeLink.serviceTaskHandlers).doesNotContain(
            ServiceTaskHandler(
                newServiceTaskHandlerRequest.processDefinitionKey,
                serviceTaskHandlerRequest.serviceTaskId,
                serviceTaskHandlerRequest.operation,
                serviceTaskHandlerRequest.parameter
            )
        )
    }

    @Test
    fun `should register StatuSetEvent`() {
        val documentId = UUID.randomUUID()
        val zaakInstanceLink = zaakInstanceLink(documentId)
        val zaakTypeLink = zaakTypeLink()

        val statusType = URI.create("statustype")

        zaakTypeLink.assignZaakInstanceStatus(zaakInstanceLink.zaakInstanceUrl, statusType)

        assertThat(zaakTypeLink.domainEvents()).contains(StatusSetEvent(zaakInstanceLink.zaakInstanceUrl, statusType))
    }

    @Test
    fun `should register ResultaatSetEvent`() {
        val documentId = UUID.randomUUID()
        val zaakInstanceLink = zaakInstanceLink(documentId)
        val zaakTypeLink = zaakTypeLink()

        val resultaatType = URI.create("resultaatType")

        zaakTypeLink.assignZaakInstanceResultaat(zaakInstanceLink.zaakInstanceUrl, resultaatType)

        assertThat(zaakTypeLink.domainEvents()).contains(ResultaatSetEvent(zaakInstanceLink.zaakInstanceUrl, resultaatType))
    }

    @Test
    fun `should register EigenschappenSetEvent`() {
        val documentId = UUID.randomUUID()
        val zaakInstanceLink = zaakInstanceLink(documentId)
        val zaakTypeLink = zaakTypeLink()

        val eigenschappen = mutableMapOf(URI.create("eigenschapUri") to "value")

        zaakTypeLink.assignZaakInstanceEigenschappen(zaakInstanceLink, eigenschappen)

        assertThat(zaakTypeLink.domainEvents()).contains(
            EigenschappenSetEvent(
                zaakInstanceLink.zaakInstanceUrl,
                zaakInstanceLink.zaakInstanceId,
                eigenschappen
            )
        )
    }

    @Test
    fun `should pick correct serviceTaskHandler when handeling task`() {
        val delegateExecutionFake = DelegateExecutionFake().withCurrentActivityId("taskA")
        val zaakInstanceUrl = URI.create("www.zaakUrl.nl")
        val zaakTypeLink = zaakTypeLink()
        zaakTypeLink.assignZaakServiceHandler(
            ServiceTaskHandlerRequest(
                "process1",
                "taskA",
                Operation.SET_STATUS,
                URI.create("www.statusTypeUrl1.nl")
            )
        )
        zaakTypeLink.assignZaakServiceHandler(
            ServiceTaskHandlerRequest(
                "process2",
                "taskA",
                Operation.SET_STATUS,
                URI.create("www.statusTypeUrl2.nl")
            )
        )

        zaakTypeLink.handleServiceTask(delegateExecutionFake, "process2", zaakInstanceUrl)

        assertThat(zaakTypeLink.domainEvents()).contains(
            StatusSetEvent(
                URI.create("www.zaakUrl.nl"),
                URI.create("www.statusTypeUrl2.nl")
            )
        )
    }

    private fun zaakInstanceLink(documentId: UUID): ZaakInstanceLink {
        return ZaakInstanceLink(
            ZaakInstanceLinkId.newId(UUID.randomUUID()),
            URI.create("www.zaakUrl.nl"),
            UUID.randomUUID(),
            documentId,
            URI.create("www.zaakType.nl")
        )
    }

    private fun zaakTypeLink(): ZaakTypeLink {
        return ZaakTypeLink(
            id,
            documentDefinitionName,
            zaaktypeId,
            ServiceTaskHandlers()
        )
    }

}