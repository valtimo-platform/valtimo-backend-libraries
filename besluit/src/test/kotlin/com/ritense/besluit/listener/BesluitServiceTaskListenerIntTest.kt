package com.ritense.besluit.listener

import com.ritense.besluit.BaseIntegrationTest
import com.ritense.besluit.domain.request.BesluitInformatieobjectRelatieRequest
import com.ritense.besluit.domain.request.CreateBesluitRequest
import com.ritense.connector.domain.Connector
import com.ritense.document.domain.Document
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
import java.time.LocalDate
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
        val besluitInformatieobjectUrl = "http://example/documenten/api/v1/enkelvoudiginformatieobjecten/429cd502-3ddc-43de-aa1b-791404cd2913"
        val zaakTypeLink = createZaakTypeLink()
        assignServiceTaskHandlerToCreateBesluit(zaakTypeLink.id)
        createProcessDocumentDefinition()
        setupOpenZaakConnector()

        val document = startCreateBesluitProcess("{\"voornaam\": \"John\", \"\$besluit\":\"$besluitInformatieobjectUrl\"}")

        assertBesluitCreated()
        assertRelationBetweenBesluitAndInformatieobject()
        assertBesluitFileCreated(document)
    }

    private fun assertBesluitCreated() {
        val requestBody = getRequestBody(HttpMethod.POST, "/api/v1/besluiten", CreateBesluitRequest::class.java)
        Assertions.assertThat(requestBody.verantwoordelijkeOrganisatie).isEqualTo("051845623")
        Assertions.assertThat(requestBody.besluittype).endsWith("/catalogi/api/v1/besluittypen/9305dc14-68bd-4e78-986d-626304196bae")
        Assertions.assertThat(requestBody.zaak).endsWith("/zaken/api/v1/zaken/7413e298-c78b-4ab8-8e8a-a825faed0e7f")
        Assertions.assertThat(requestBody.datum).isEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
        Assertions.assertThat(requestBody.ingangsdatum).isEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    private fun assertRelationBetweenBesluitAndInformatieobject() {
        val requestBody = getRequestBody(HttpMethod.POST, "/api/v1/besluitinformatieobjecten", BesluitInformatieobjectRelatieRequest::class.java)
        Assertions.assertThat(requestBody.informatieobject).isEqualTo(URI("http://example/documenten/api/v1/enkelvoudiginformatieobjecten/429cd502-3ddc-43de-aa1b-791404cd2913"))
        Assertions.assertThat(requestBody.besluit).isEqualTo(URI("http://example/api/v1/besluiten/16d33b53-e283-40ef-8d86-6914282aea25"))
    }

    private fun assertBesluitFileCreated(document: Document) {
        Assertions.assertThat(document.relatedFiles()).hasSize(1)
        val relatedFile = document.relatedFiles().iterator().next()
        Assertions.assertThat(relatedFile.fileId).isNotNull
        Assertions.assertThat(relatedFile.fileName).isEqualTo("passport.jpg")
        Assertions.assertThat(relatedFile.sizeInBytes).isEqualTo(148649)
        Assertions.assertThat(relatedFile.createdOn).isEqualTo(LocalDateTime.parse("2022-02-18T15:19:51.408988"))
        Assertions.assertThat(relatedFile.createdBy).isEqualTo("John Doe")
    }

    fun startCreateBesluitProcess(content: String): Document {
        val newDocumentRequest = NewDocumentRequest(
            "testschema",
            Mapper.get().readTree(content)
        )
        return processDocumentService.newDocumentAndStartProcess(
            NewDocumentAndStartProcessRequest(
                "CreateBesluitProcess",
                newDocumentRequest
            )
        ).resultingDocument().get()
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

    fun assignServiceTaskHandlerToCreateBesluit(zaakTypeLinkId: ZaakTypeLinkId): CreateServiceTaskHandlerResult {
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