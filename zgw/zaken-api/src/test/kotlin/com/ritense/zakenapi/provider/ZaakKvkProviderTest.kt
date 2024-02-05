/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.zakenapi.provider

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.domain.rol.BetrokkeneIdentificatie
import com.ritense.zakenapi.domain.rol.BetrokkeneType
import com.ritense.zakenapi.domain.rol.Rol
import com.ritense.zakenapi.domain.rol.RolNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolNietNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolType
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URI
import java.util.UUID

class ZaakKvkProviderTest {

    lateinit var processDocumentService: ProcessDocumentService
    lateinit var zaakInstanceLinkService: ZaakInstanceLinkService
    lateinit var pluginService: PluginService
    lateinit var zaakKvkProvider: ZaakKvkProvider

    @BeforeEach
    fun setUp() {
        processDocumentService = mock()
        zaakInstanceLinkService = mock()
        pluginService = mock()
        zaakKvkProvider = ZaakKvkProvider(
                processDocumentService,
                zaakInstanceLinkService,
                pluginService
        )
    }

    @Test
    fun `should get bsn via zaak rollen`() {
        val task = DelegateTaskFake()
                .withProcessInstanceId(UUID.randomUUID().toString())
        val zaakUrl = URI("example.com")

        prepareMocks(task, zaakUrl)

        val plugin = mock<ZakenApiPlugin>()
        whenever(pluginService.createInstance(eq(ZakenApiPlugin::class.java), any())).thenReturn(plugin)

        whenever(plugin.getZaakRollen(zaakUrl, RolType.INITIATOR)).thenReturn(listOf(
                createRol(BetrokkeneType.NATUURLIJK_PERSOON, RolNatuurlijkPersoon(inpBsn = "12345"), zaakUrl),
                createRol(BetrokkeneType.NIET_NATUURLIJK_PERSOON, RolNietNatuurlijkPersoon(annIdentificatie = "1337"), zaakUrl)
        ))

        val bsn = zaakKvkProvider.getKvkNummer(task)

        assertThat(bsn).isEqualTo("1337")
    }

    @Test
    fun `should throw exception when plugin configuration is not found`() {
        val task = DelegateTaskFake()
                .withProcessInstanceId(UUID.randomUUID().toString())
        val zaakUrl = URI("example.com")

        prepareMocks(task, zaakUrl)

        val exception = assertThrows<IllegalStateException> {
            zaakKvkProvider.getKvkNummer(task)
        }

        assertThat(exception.message).isEqualTo("No plugin configuration was found for zaak with URL $zaakUrl")
    }

    private fun prepareMocks(task: DelegateTaskFake, zaakUrl: URI) {
        val documentId = UUID.randomUUID()
        whenever(processDocumentService.getDocumentId(eq(CamundaProcessInstanceId(task.processInstanceId)), eq(task))).thenReturn(JsonSchemaDocumentId.existingId(documentId))
        val zaakInstanceLink = mock<ZaakInstanceLink>()
        whenever(zaakInstanceLink.zaakInstanceUrl).thenReturn(zaakUrl)
        whenever(zaakInstanceLinkService.getByDocumentId(documentId)).thenReturn(zaakInstanceLink)
    }

    private fun createRol(betrokkeneType: BetrokkeneType, betrokkeneIdentificatie: BetrokkeneIdentificatie, zaakUrl: URI) =
            Rol(betrokkeneType = betrokkeneType, betrokkeneIdentificatie = betrokkeneIdentificatie, roltoelichting = "", roltype = URI(""), zaak = zaakUrl)
}