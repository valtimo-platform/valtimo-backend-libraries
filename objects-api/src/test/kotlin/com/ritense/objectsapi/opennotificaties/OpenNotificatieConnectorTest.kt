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

package com.ritense.objectsapi.opennotificaties

import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.objectsapi.BaseTest
import com.ritense.objectsapi.domain.Abonnement
import com.ritense.objectsapi.domain.AbonnementLink
import com.ritense.objectsapi.domain.Kanaal
import com.ritense.objectsapi.repository.AbonnementLinkRepository
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class OpenNotificatieConnectorTest : BaseTest() {
    lateinit var openNotificatieProperties: OpenNotificatieProperties
    lateinit var abonnementLinkRepository: AbonnementLinkRepository
    lateinit var openNotificatieClient: OpenNotificatieClient
    lateinit var connector: OpenNotificatieConnector

    @BeforeEach
    fun setup() {
        openNotificatieProperties = OpenNotificatieProperties(
            "http://base.url",
            "clientId",
            "secret",
            "http://callback.base.url"
        )
        abonnementLinkRepository = mock(AbonnementLinkRepository::class.java)
        openNotificatieClient = mock(OpenNotificatieClient::class.java)

        connector = OpenNotificatieConnector(openNotificatieProperties, abonnementLinkRepository, openNotificatieClient)
    }

    @Test
    fun `ensure kanaal exists creates kanaal when kanaal doesnt exist`() {
        `when`(openNotificatieClient.getKanalen()).thenReturn(listOf(Kanaal("otherkanaal")))
        connector.ensureKanaalExists()
        verify(openNotificatieClient, times(1)).createKanaal(Kanaal("objecten"))
    }

    @Test
    fun `ensure kanaal exists doesnt create kanaal when kanaal exists`() {
        `when`(openNotificatieClient.getKanalen()).thenReturn(listOf(Kanaal("objecten")))
        connector.ensureKanaalExists()
        verify(openNotificatieClient, never()).createKanaal(any())
    }

    @Test
    fun `create abonnement should create abonnement`() {
        val connectorId = ConnectorInstanceId(UUID.randomUUID())
        val abonnementId = UUID.randomUUID()
        val abonnement = Abonnement(
            "http://base.url/api/v1/abonnement/$abonnementId",
            "http://callback.base.url/api/v1/notification?connectorId=${connectorId.id}",
            "some-key",
            emptyList()
        )

        `when`(openNotificatieClient.createAbonnement(any())).thenReturn(abonnement)
        connector.createAbonnement(connectorId)
        val argumentCaptor = ArgumentCaptor.forClass(AbonnementLink::class.java)
        verify(abonnementLinkRepository, times(1)).save(argumentCaptor.capture())

        assertEquals(connectorId, argumentCaptor.value.connectorId)
        assertEquals(abonnementId, argumentCaptor.value.abonnementId)
        assertTrue(argumentCaptor.value.key.isNotEmpty())
    }

    @Test
    fun `delete abonnement should delete abonnement if abonnement link exists`() {
        val connectorId = ConnectorInstanceId(UUID.randomUUID())
        val abonnementId = UUID.randomUUID()
        val abonnementLink = AbonnementLink(
            connectorId,
            abonnementId,
            "some-key"
        )
        `when`(abonnementLinkRepository.findById(connectorId)).thenReturn(Optional.of(abonnementLink))

        connector.deleteAbonnement(connectorId)

        verify(openNotificatieClient, times(1)).deleteAbonnement(abonnementId)
        verify(abonnementLinkRepository, times(1)).deleteById(connectorId)
    }

    @Test
    fun `delete abonnement should do nothing if abonnement link doesnt exist`() {
        val connectorId = ConnectorInstanceId(UUID.randomUUID())
        `when`(abonnementLinkRepository.findById(connectorId)).thenReturn(Optional.empty())

        connector.deleteAbonnement(connectorId)

        verify(openNotificatieClient, never()).deleteAbonnement(any())
        verify(abonnementLinkRepository, never()).deleteById(any())
    }
}
