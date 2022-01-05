package com.ritense.klant.service.impl

import com.ritense.klant.client.OpenKlantClient
import com.ritense.klant.client.OpenKlantClientProperties
import com.ritense.klant.domain.Klant
import com.ritense.klant.domain.KlantCreationRequest
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor

internal class BurgerServiceTest {

    var openKlantClient = mock(OpenKlantClient::class.java)
    var openKlantClientProperties = mock(OpenKlantClientProperties::class.java)
    var burgerService = BurgerService(openKlantClientProperties, openKlantClient)

    @Test
    fun `getBurger burgerService calls openklant client with BSN for burger`() {
        burgerService.getBurger("123")

        verify(openKlantClient).getKlant("123")
    }

    @Test
    fun `createBurger burgerService calls openklant client with creation request`() {
        val captor = argumentCaptor<KlantCreationRequest>()
        `when`(openKlantClientProperties.rsin).thenReturn("654321")

        burgerService.createBurger("123")

        verify(openKlantClient).postKlant(captor.capture())

        val klantCreationRequest = captor.firstValue
        assertEquals("123", klantCreationRequest.subjectIdentificatie.inpBsn)
        assertEquals("654321", klantCreationRequest.bronorganisatie)
    }

    @Test
    fun `ensureBurgerExists get burger and does not create one if one exists`() {
        `when`(openKlantClient.getKlant("123")).thenReturn(mock(Klant::class.java))

        burgerService.ensureBurgerExists("123")

        verify(openKlantClient).getKlant("123")
        verify(openKlantClient, never()).postKlant(any<KlantCreationRequest>())
    }

    @Test
    fun `ensureBurgerExists creates burger if one does not exist`() {
        val captor = argumentCaptor<KlantCreationRequest>()
        `when`(openKlantClient.getKlant("123")).thenReturn(null)
        `when`(openKlantClientProperties.rsin).thenReturn("654321")

        burgerService.ensureBurgerExists("123")

        verify(openKlantClient).getKlant("123")
        verify(openKlantClient).postKlant(captor.capture())

        val klantCreationRequest = captor.firstValue
        assertEquals("123", klantCreationRequest.subjectIdentificatie.inpBsn)
        assertEquals("654321", klantCreationRequest.bronorganisatie)
    }
}