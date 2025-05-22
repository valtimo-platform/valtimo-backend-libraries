/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.zaakdetails.documentobjectenapisync

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.client.ObjectsList
import com.ritense.zaakdetails.BaseIntegrationTest
import com.ritense.zakenapi.domain.CreateZaakResponse
import com.ritense.zakenapi.domain.ZaakResponse
import com.ritense.zakenapi.service.ZaakTypeLinkService
import com.ritense.zakenapi.web.rest.request.CreateZaakTypeLinkRequest
import com.ritense.zgw.Rsin
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertTrue

@Transactional
@SpringBootTest(properties = ["valtimo.zgw.zaakdetails.linktozaak.enabled=true"])
class DocumentObjectenApiSyncServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var documentObjectenApiSyncService: DocumentObjectenApiSyncService

    @Autowired
    lateinit var documentObjectenApiSyncManagementService: DocumentObjectenApiSyncManagementService

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    @Test
    fun `should create zaakdetails object and link to the zaak when zaak exists`() {
        val objectWrapper = ObjectWrapper(
            URI.create("http://localhost:56273/objecten/98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
            UUID.randomUUID(),
            URI.create("http://localhost:56273/objecttypen/98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
            mock()
        )

        documentObjectenApiSyncManagementService.saveSyncConfiguration(
            DocumentObjectenApiSync(
                documentDefinitionName = "profile",
                documentDefinitionVersion = 1,
                objectManagementConfigurationId = UUID.fromString("462ef788-f7db-4701-9b87-0400fc79ad7e")
            )
        )

        zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest(
                "profile",
                URI("http://localhost:56273/zaaktype/98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
                UUID.fromString("3079d6fe-42e3-4f8f-a9db-52ce2507b7ee"),
                true,
                "000000000"
            )
        )

        whenever(objectenApiClient.getObjectsByObjecttypeUrlWithSearchParams(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(ObjectsList(0, null, null, listOf()))

        whenever(objectenApiClient.createObject(any(), any(), any()))
            .thenReturn(objectWrapper)

        whenever(zakenApiClient.createZaak(any(), any(), any()))
            .thenReturn(getZaakResponse())

        val result = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    "profile",
                    objectMapper.readTree("""{"lastname":"Doe"}""")
                )
            )
        }

        assertTrue { result.errors().isEmpty() }

        verify(zakenApiClient, times(1)).createZaak(any(), any(), any())
        verify(objectenApiClient, times(1)).getObjectsByObjecttypeUrlWithSearchParams(any(), any(), any(), any(), any(), any(), any())
        verify(objectenApiClient, times(1)).createObject(any(), any(), any())
        verifyNoMoreInteractions(objectenApiClient)
        verify(zakenApiClient, times(1)).createZaakObject(any(), any(), any())
    }

    @Test
    fun `should update zaakdetails object and link to the zaak when zaak exists`() {
        val objectWrapper = ObjectWrapper(
            URI.create("http://localhost:56273/objecten/98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
            UUID.randomUUID(),
            URI.create("http://localhost:56273/objecttypen/98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
            mock()
        )

        documentObjectenApiSyncManagementService.saveSyncConfiguration(
            DocumentObjectenApiSync(
                documentDefinitionName = "profile",
                documentDefinitionVersion = 1,
                objectManagementConfigurationId = UUID.fromString("462ef788-f7db-4701-9b87-0400fc79ad7e")
            )
        )

        zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest(
                "profile",
                URI("http://localhost:56273/zaaktype/98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
                UUID.fromString("3079d6fe-42e3-4f8f-a9db-52ce2507b7ee"),
                true,
                "000000000"
            )
        )

        whenever(objectenApiClient.getObjectsByObjecttypeUrlWithSearchParams(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(ObjectsList(0, null, null, listOf()))

        whenever(objectenApiClient.createObject(any(), any(), any()))
            .thenReturn(objectWrapper)

        whenever(zakenApiClient.createZaak(any(), any(), any()))
            .thenReturn(getZaakResponse())

        val result = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    "profile",
                    objectMapper.readTree("""{"lastname":"Doe"}""")
                )
            )
        }

        assertTrue { result.errors().isEmpty() }

        runWithoutAuthorization {
            documentService.modifyDocument(result.resultingDocument().get(), objectMapper.readTree("""{"lastname":"Doe1"}"""))
        }

        verify(zakenApiClient, times(1)).createZaak(any(), any(), any())
        verify(objectenApiClient, times(1)).getObjectsByObjecttypeUrlWithSearchParams(any(), any(), any(), any(), any(), any(), any())
        verify(objectenApiClient, times(1)).createObject(any(), any(), any())
        verify(objectenApiClient, times(1)).objectUpdate(any(), any(), any())
        verifyNoMoreInteractions(objectenApiClient)
        verify(zakenApiClient, times(1)).createZaakObject(any(), any(), any())
    }

    @Test
    fun `should not create zaakdetails object when no configuration exists`() {
        zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest(
                "profile",
                URI("http://localhost:56273/zaaktype/98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
                UUID.fromString("3079d6fe-42e3-4f8f-a9db-52ce2507b7ee"),
                false,
                "000000000"
            )
        )

        val result = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    "profile",
                    objectMapper.readTree("""{"lastname":"Doe"}""")
                )
            )
        }

        assertTrue { result.errors().isEmpty() }
        verifyNoInteractions(objectenApiClient)
        verifyNoInteractions(zakenApiClient)
    }

    @Test
    fun `should create zaakdetails object and not link to the zaak when zaak does not exist`() {
        val objectWrapper = ObjectWrapper(
            URI.create("http://localhost:56273/objecten/98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
            UUID.randomUUID(),
            URI.create("http://localhost:56273/objecttypen/98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
            mock()
        )

        documentObjectenApiSyncManagementService.saveSyncConfiguration(
            DocumentObjectenApiSync(
                documentDefinitionName = "profile",
                documentDefinitionVersion = 1,
                objectManagementConfigurationId = UUID.fromString("462ef788-f7db-4701-9b87-0400fc79ad7e")
            )
        )

        whenever(objectenApiClient.getObjectsByObjecttypeUrlWithSearchParams(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(ObjectsList(0, null, null, listOf()))

        whenever(objectenApiClient.createObject(any(), any(), any()))
            .thenReturn(objectWrapper)

        val result = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    "profile",
                    objectMapper.readTree("""{"lastname":"Doe"}""")
                )
            )
        }

        verify(objectenApiClient, times(1)).getObjectsByObjecttypeUrlWithSearchParams(any(), any(), any(), any(), any(), any(), any())
        verify(objectenApiClient, times(1)).createObject(any(), any(), any())
        verifyNoInteractions(zakenApiClient)
    }

    private fun getZaakResponse(): ZaakResponse {
        return ZaakResponse(
            URI("http://localhost:56273/zaken/98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
            UUID.fromString("98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
            identificatie = null,
            bronorganisatie = Rsin("000000000"),
            omschrijving = null,
            toelichting = null,
            zaaktype = URI.create("http://localhost:56273/zaaktype/98d703e3-4afa-47fe-9787-e3d1ab0ab42c"),
            registratiedatum = null,
            verantwoordelijkeOrganisatie = Rsin("000000000"),
            startdatum = LocalDate.now(),
            einddatum = null,
            einddatumGepland = null,
            uiterlijkeEinddatumAfdoening = null,
            publicatiedatum = null,
            communicatiekanaal = null,
            productenOfDiensten = null,
            vertrouwelijkheidaanduiding = null,
            betalingsindicatie = null,
            betalingsindicatieWeergave = null,
            laatsteBetaaldatum = null,
            zaakgeometrie = null,
            verlenging = null,
            opschorting = null,
            selectielijstklasse = null,
            hoofdzaak = null,
            deelzaken = null,
            relevanteAndereZaken = null,
            eigenschappen = null,
            rollen = null,
            status = null,
            zaakinformatieobjecten = null,
            zaakobjecten = null,
            kenmerken = null,
            archiefnominatie = null,
            archiefstatus = null,
            archiefactiedatum = null,
            resultaat = null,
            opdrachtgevendeOrganisatie = null,
            processobjectaard = null,
            resultaattoelichting = null,
            startdatumBewaartermijn = null
        )
    }
}