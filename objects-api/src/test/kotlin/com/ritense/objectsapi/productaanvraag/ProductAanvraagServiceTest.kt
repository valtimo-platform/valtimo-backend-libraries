package com.ritense.objectsapi.productaanvraag

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.document.service.result.CreateDocumentResult
import com.ritense.klant.domain.Klant
import com.ritense.klant.service.BedrijfService
import com.ritense.klant.service.BurgerService
import com.ritense.objectsapi.domain.ProductAanvraag
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.result.StartProcessForDocumentResult
import com.ritense.tenancy.TenantResolver
import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
    val bedrijfService = mock<BedrijfService>()
    val tenantResolver = mock<TenantResolver>()

    val service = ProductAanvraagService(
        processDocumentService,
        documentService,
        openNotificatieService,
        zaakRolService,
        zaakInstanceLinkService,
        burgerService,
        bedrijfService,
        tenantResolver
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
        whenever(instanceLink.zaakInstanceUrl).thenReturn(URI("http://some-zaak-url"))
        whenever(burgerService.ensureBurgerExists(any())).thenReturn(klant)
        whenever(bedrijfService.ensureBedrijfExists(any())).thenReturn(klant)
        whenever(klant.url).thenReturn("http://some.url")
        whenever(processDocumentService.startProcessForDocument(any())).thenReturn(processStartResult)
        whenever(processStartResult.resultingDocument()).thenReturn(Optional.of(document))
    }

    @Test
    fun `createDossier should create klant when klantservice was injected`() {

        service.createDossier(
            getProductAanvraag(bsn = "123"),
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
            null,
            null,
            tenantResolver
        )

        service.createDossier(
            getProductAanvraag(bsn = "!23"),
            getProductAanvraagTypeMapping(),
            URI("http://some.rol.url")
        )

        verify(burgerService, never()).ensureBurgerExists("123")
    }

    @Test
    fun `createDossier should create natuurlijk persoon when productaanvraag has bsn`() {

        service.createDossier(
            getProductAanvraag(bsn = "123"),
            getProductAanvraagTypeMapping(),
            URI("http://some.rol.url")
        )

        verify(burgerService).ensureBurgerExists("123")
        verify(zaakRolService).addNatuurlijkPersoon(any(), any(), any(), eq("123"), any())
        verify(bedrijfService, never()).ensureBedrijfExists("123")
        verify(zaakRolService, never()).addNietNatuurlijkPersoon(any(), any(), any(), any(), any())
    }

    @Test
    fun `createDossier should create niet natuurlijk persoon when productaanvraag has kvk`() {

        service.createDossier(
            getProductAanvraag(kvk = "123"),
            getProductAanvraagTypeMapping(),
            URI("http://some.rol.url")
        )

        verify(bedrijfService).ensureBedrijfExists("123")
        verify(zaakRolService).addNietNatuurlijkPersoon(any(), any(), any(), eq("123"), any())
        verify(burgerService, never()).ensureBurgerExists("123")
        verify(zaakRolService, never()).addNatuurlijkPersoon(any(), any(), any(), any(), any())
    }

    fun getProductAanvraag(bsn: String? = null, kvk: String? = null): ProductAanvraag {
        val mapper = ObjectMapper()
        return ProductAanvraag(
            "type",
            mapper.createObjectNode(),
            emptyList(),
            URI("http://some.attachment.url"),
            bsn,
            kvk
        )
    }

    fun getProductAanvraagTypeMapping(): ProductAanvraagTypeMapping {
        return ProductAanvraagTypeMapping("type", "case", "process")
    }
}
