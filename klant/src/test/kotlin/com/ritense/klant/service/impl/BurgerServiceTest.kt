package com.ritense.klant.service.impl

import com.ritense.klant.client.OpenKlantClient
import com.ritense.klant.client.OpenKlantClientProperties
import com.ritense.klant.domain.Klant
import com.ritense.klant.domain.KlantCreationRequest
import com.ritense.klant.domain.KlantSearchFilter
import com.ritense.klant.domain.ResultPage
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times

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
        `when`(openKlantClient.searchKlanten(any<KlantSearchFilter>()))
            .thenReturn(ResultPage(0, null, null, emptyList()))

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
        `when`(openKlantClient.searchKlanten(any<KlantSearchFilter>()))
            .thenReturn(ResultPage(0, null, null, emptyList()))
        `when`(openKlantClientProperties.rsin).thenReturn("654321")

        burgerService.ensureBurgerExists("123")

        verify(openKlantClient).getKlant("123")
        verify(openKlantClient).postKlant(captor.capture())

        val klantCreationRequest = captor.firstValue
        assertEquals("123", klantCreationRequest.subjectIdentificatie.inpBsn)
        assertEquals("654321", klantCreationRequest.bronorganisatie)
    }

    @Test
    fun `createBurger regenerates klantnummer if klant with nummer already exists`() {
        val captor = argumentCaptor<KlantSearchFilter>()
        `when`(openKlantClientProperties.rsin).thenReturn("654321")
        `when`(openKlantClient.searchKlanten(any<KlantSearchFilter>()))
            .thenReturn(
                // first result finds klant with the klantnummer
                ResultPage(0, null, null, listOf(
                    Klant(
                        "http://some-url",
                        "0123456789",
                        "user@example.org"
                    )
                )),
                // second result does not find klant
                ResultPage(0, null, null, emptyList())
            )

        burgerService.createBurger("123")

        verify(openKlantClient, times(2)).searchKlanten(captor.capture())

        // validate that 2 different klantnummers have been searched
        val firstSearch = captor.firstValue
        val secondSearch = captor.secondValue
        assertNotNull(firstSearch.klantnummer)
        assertNotNull(secondSearch.klantnummer)
        assertNotEquals(firstSearch.klantnummer, secondSearch.klantnummer)
    }
}