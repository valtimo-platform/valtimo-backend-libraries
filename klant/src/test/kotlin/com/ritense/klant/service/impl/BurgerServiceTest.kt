package com.ritense.klant.service.impl

import com.ritense.klant.client.OpenKlantClient
import com.ritense.klant.domain.Klant
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

internal class BurgerServiceTest {

    var openKlantClient = mock(OpenKlantClient::class.java)
    var burgerService = BurgerService(openKlantClient)

    @Test
    fun `burgerService calls openklant client with BSN for burger`() {
        `when`(openKlantClient.getKlant("123")).thenReturn(mock(Klant::class.java))
        burgerService.getBurger("123")
        verify(openKlantClient).getKlant("123")
    }
}