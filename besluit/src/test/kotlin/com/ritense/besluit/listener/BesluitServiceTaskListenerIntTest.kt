package com.ritense.besluit.listener

import com.ritense.besluit.BaseIntegrationTest
import com.ritense.besluit.domain.request.CreateBesluitRequest
import com.ritense.connector.domain.Connector
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.openzaak.domain.configuration.Rsin
import com.ritense.openzaak.domain.connector.OpenZaakConfig
import com.ritense.openzaak.domain.connector.OpenZaakProperties
import com.ritense.openzaak.domain.mapping.impl.Operation
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.openzaak.service.impl.Mapper
import com.ritense.openzaak.service.result.CreateServiceTaskHandlerResult
import com.ritense.openzaak.web.rest.request.ServiceTaskHandlerRequest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BesluitServiceTaskListenerIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    @Qualifier("openZaakConnector")
    lateinit var openZaakConnector: Connector

    @Test
    fun `Should create besluit by process connection`() {
        val zaakTypeLink = createZaakTypeLink()
        assignServiceTaskHandler(zaakTypeLink.id)
        createProcessDocumentDefinition()
        setupOpenZaakConnector()

        startProcess()

        val requestBody = getRequestBody(HttpMethod.POST, "/api/v1/besluiten", CreateBesluitRequest::class.java)
        Assertions.assertThat(requestBody.verantwoordelijkeOrganisatie).isEqualTo("051845623")
        Assertions.assertThat(requestBody.besluittype)
            .isEqualTo("${server.url("/")}catalogi/api/v1/besluittypen/9305dc14-68bd-4e78-986d-626304196bae")
        Assertions.assertThat(requestBody.zaak)
            .isEqualTo("http://some-url/zaken/api/v1/zaken/7413e298-c78b-4ab8-8e8a-a825faed0e7f")
        Assertions.assertThat(requestBody.datum).isEqualTo(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
        Assertions.assertThat(requestBody.ingangsdatum)
            .isEqualTo(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    fun startProcess() {
        val newDocumentRequest = NewDocumentRequest(
            "testschema",
            Mapper.get().readTree("{\"voornaam\": \"John\"}")
        )
        processDocumentService.newDocumentAndStartProcess(
            NewDocumentAndStartProcessRequest(
                "CreateBesluitProcess",
                newDocumentRequest
            )
        )
    }

    fun createProcessDocumentDefinition() {
        processDocumentAssociationService.createProcessDocumentDefinition(
            ProcessDocumentDefinitionRequest(
                "CreateBesluitProcess",
                "testschema",
                true,
                true
            )
        )
    }

    fun assignServiceTaskHandler(zaakTypeLinkId: ZaakTypeLinkId): CreateServiceTaskHandlerResult {
        return zaakTypeLinkService.assignServiceTaskHandler(
            zaakTypeLinkId,
            ServiceTaskHandlerRequest(
                "CreateBesluitProcess",
                "CreateBesluitTask",
                Operation.CREATE_BESLUIT,
                URI("${server.url("/")}catalogi/api/v1/besluittypen/9305dc14-68bd-4e78-986d-626304196bae")
            )
        )
    }

    fun createZaakTypeLink(): ZaakTypeLink {
        return zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest(
                "testschema",
                URI("${server.url("/")}catalogi/api/v1/zaaktypen/4e9c2359-83ac-4e3b-96b6-3f278f1fc773"),
                true
            )
        ).zaakTypeLink()!!
    }


    private fun setupOpenZaakConnector() {
        val properties = OpenZaakProperties(
            OpenZaakConfig(
                server.url("/").toString(),
                "test-client",
                "711de9a3-1af6-4196-b4dd-e8a2e2ade17c",
                Rsin("051845623")
            )
        )
        connectorDeploymentService.deployAll(listOf(openZaakConnector))
        val connectorType = connectorService.getConnectorTypes().first { it.name == "OpenZaak" }

        connectorService.createConnectorInstance(
            connectorType.id.id,
            "openZaakInstance",
            properties
        )

        openZaakConnector = connectorService.loadByClassName(openZaakConnector::class.java)
    }

}