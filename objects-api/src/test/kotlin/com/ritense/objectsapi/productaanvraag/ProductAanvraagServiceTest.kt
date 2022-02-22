package com.ritense.objectsapi.productaanvraag

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.document.service.result.CreateDocumentResult
import com.ritense.klant.domain.Klant
import com.ritense.klant.service.BurgerService
import com.ritense.objectsapi.domain.ProductAanvraag
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.result.StartProcessForDocumentResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.Optional
import java.util.UUID

internal class ProductAanvraagServiceTest {

    val processDocumentService = mock<ProcessDocumentService>()
    val documentService = mock<DocumentService>()
    val openNotificatieService = mock<OpenNotificatieService>()
    val zaakRolService = mock<ZaakRolService>()
    val zaakInstanceLinkService = mock<ZaakInstanceLinkService>()
    val burgerService = mock<BurgerService>()

    val service = ProductAanvraagService(
        processDocumentService,
        documentService,
        openNotificatieService,
        zaakRolService,
        zaakInstanceLinkService,
        burgerService
    )

    @BeforeEach
    fun setup() {
        val createDocumentResult = mock<CreateDocumentResult>()
        val document = mock<Document>()
        val documentId = mock<Document.Id>()
        val instanceLink = mock<ZaakInstanceLink>()
        val klant = mock<Klant>()
        val processStartResult = mock<StartProcessForDocumentResult>()

        whenever(openNotificatieService.createOpenzaakResources(any())).thenReturn(emptySet())
        whenever(documentService.createDocument(any())).thenReturn(createDocumentResult)
        whenever(createDocumentResult.resultingDocument()).thenReturn(Optional.of(document))
        whenever(document.id()).thenReturn(documentId)
        whenever(documentId.id).thenReturn(UUID.randomUUID())
        whenever(zaakInstanceLinkService.getByDocumentId(any())).thenReturn(instanceLink)
        whenever(burgerService.ensureBurgerExists(any())).thenReturn(klant)
        whenever(klant.url).thenReturn("http://some.url")
        whenever(processDocumentService.startProcessForDocument(any())).thenReturn(processStartResult)
        whenever(processStartResult.resultingDocument()).thenReturn(Optional.of(document))
    }

    @Test
    fun `createDossier should create klant when klantservice was injected`() {

        service.createDossier(
            getProductAanvraag(),
            getProductAanvraagTypeMapping(),
            URI("http://some.rol.url")
        )

        verify(burgerService).ensureBurgerExists("123")
    }

    @Test
    fun `createDossier should not create klant when klantservice was not injected`() {
        val service = ProductAanvraagService(
            processDocumentService,
            documentService,
            openNotificatieService,
            zaakRolService,
            zaakInstanceLinkService,
            null
        )

        service.createDossier(
            getProductAanvraag(),
            getProductAanvraagTypeMapping(),
            URI("http://some.rol.url")
        )

        verify(burgerService, never()).ensureBurgerExists("123")
    }

    fun getProductAanvraag(): ProductAanvraag {
        val mapper = ObjectMapper()
        return ProductAanvraag(
            "type",
            mapper.createObjectNode(),
            emptyList(),
            URI("http://some.attachment.url"),
            "123"
        )
    }

    fun getProductAanvraagTypeMapping(): ProductAanvraagTypeMapping {
        return ProductAanvraagTypeMapping("type", "case", "process")
    }
}