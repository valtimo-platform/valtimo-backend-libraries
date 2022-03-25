package com.ritense.klant.service.impl

import com.ritense.klant.client.OpenKlantClient
import com.ritense.klant.client.OpenKlantClientProperties
import com.ritense.klant.domain.Klant
import com.ritense.klant.domain.KlantCreationRequest
import com.ritense.klant.domain.KlantSearchFilter
import com.ritense.klant.domain.NietNatuurlijkPersoonSubjectIdentificatie
import com.ritense.klant.domain.ResultPage
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

internal class BedrijfServiceTest {

    var openKlantClient = mock(OpenKlantClient::class.java)
    var openKlantClientProperties = mock(OpenKlantClientProperties::class.java)
    var bedrijfService = BedrijfService(openKlantClientProperties, openKlantClient)

    @Test
    fun `getBedrijf bedrijfService calls openklant client with BSN for bedrijf`() {
        bedrijfService.getBedrijf("123")

        verify(openKlantClient).getKlant(kvk = "123")
    }

    @Test
    fun `createBedrijf bedrijfService calls openklant client with creation request`() {
        val captor = argumentCaptor<KlantCreationRequest>()
        `when`(openKlantClientProperties.rsin).thenReturn("654321")
        `when`(openKlantClient.searchKlanten(any()))
            .thenReturn(ResultPage(0, null, null, emptyList()))

        bedrijfService.createBedrijf("123")

        verify(openKlantClient).postKlant(captor.capture())

        val klantCreationRequest = captor.firstValue
        assertIs<NietNatuurlijkPersoonSubjectIdentificatie>(klantCreationRequest.subjectIdentificatie)
        assertEquals("123", (klantCreationRequest.subjectIdentificatie as NietNatuurlijkPersoonSubjectIdentificatie).annIdentificatie)
        assertEquals("654321", klantCreationRequest.bronorganisatie)
    }

    @Test
    fun `ensureBedrijfExists get bedrijf and does not create one if one exists`() {
        `when`(openKlantClient.getKlant(kvk = "123")).thenReturn(mock(Klant::class.java))

        bedrijfService.ensureBedrijfExists("123")

        verify(openKlantClient).getKlant(kvk = "123")
        verify(openKlantClient, never()).postKlant(any())
    }

    @Test
    fun `ensureBedrijfExists creates bedrijf if one does not exist`() {
        val captor = argumentCaptor<KlantCreationRequest>()
        `when`(openKlantClient.getKlant(kvk = "123")).thenReturn(null)
        `when`(openKlantClient.searchKlanten(any()))
            .thenReturn(ResultPage(0, null, null, emptyList()))
        `when`(openKlantClientProperties.rsin).thenReturn("654321")

        bedrijfService.ensureBedrijfExists("123")

        verify(openKlantClient).getKlant(kvk = "123")
        verify(openKlantClient).postKlant(captor.capture())

        val klantCreationRequest = captor.firstValue
        assertIs<NietNatuurlijkPersoonSubjectIdentificatie>(klantCreationRequest.subjectIdentificatie)
        assertEquals("123", (klantCreationRequest.subjectIdentificatie as NietNatuurlijkPersoonSubjectIdentificatie).annIdentificatie)
        assertEquals("654321", klantCreationRequest.bronorganisatie)
    }

    @Test
    fun `createBedrijf regenerates klantnummer if klant with nummer already exists`() {
        val captor = argumentCaptor<KlantSearchFilter>()
        `when`(openKlantClientProperties.rsin).thenReturn("654321")
        `when`(openKlantClient.searchKlanten(any()))
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

        bedrijfService.createBedrijf("123")

        verify(openKlantClient, times(2)).searchKlanten(captor.capture())

        // validate that 2 different klantnummers have been searched
        val firstSearch = captor.firstValue
        val secondSearch = captor.secondValue
        assertNotNull(firstSearch.klantnummer)
        assertNotNull(secondSearch.klantnummer)
        assertNotEquals(firstSearch.klantnummer, secondSearch.klantnummer)
    }
}