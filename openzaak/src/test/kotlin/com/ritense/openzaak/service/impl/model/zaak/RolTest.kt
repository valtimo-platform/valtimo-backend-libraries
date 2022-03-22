package com.ritense.openzaak.service.impl.model.zaak

import com.ritense.openzaak.service.impl.Mapper
import com.ritense.openzaak.service.impl.model.zaak.betrokkene.RolNatuurlijkPersoon
import com.ritense.openzaak.service.impl.model.zaak.betrokkene.RolNietNatuurlijkPersoon
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals

internal class RolTest {

    @Test
    fun `should serialize natuurlijk persoon`() {
        val rol = Rol(
            URI("http://zaak.uri"),
            URI("http://betrokkene.uri"),
            BetrokkeneType.NATUURLIJK_PERSOON,
            URI("http://role.type"),
            "role description",
            RolNatuurlijkPersoon(
                inpBsn = "bsn"
            )
        )

        val result = Mapper.get().writeValueAsString(rol)

        val expectation =  "{\"zaak\":\"http://zaak.uri\",\"betrokkene\":\"http://betrokkene.uri\",\"betrokkeneType\":\"natuurlijk_persoon\",\"roltype\":\"http://role.type\",\"roltoelichting\":\"role description\",\"betrokkeneIdentificatie\":{\"inpBsn\":\"bsn\"}}"

        assertEquals(expectation, result)
    }

    @Test
    fun `should serialize niet natuurlijk persoon`() {
        val rol = Rol(
            URI("http://zaak.uri"),
            URI("http://betrokkene.uri"),
            BetrokkeneType.NIET_NATUURLIJK_PERSOON,
            URI("http://role.type"),
            "role description",
            RolNietNatuurlijkPersoon(
                innNnpId = "kvk"
            )
        )

        val result = Mapper.get().writeValueAsString(rol)

        val expectation =  "{\"zaak\":\"http://zaak.uri\",\"betrokkene\":\"http://betrokkene.uri\",\"betrokkeneType\":\"niet_natuurlijk_persoon\",\"roltype\":\"http://role.type\",\"roltoelichting\":\"role description\",\"betrokkeneIdentificatie\":{\"innNnpId\":\"kvk\"}}"

        assertEquals(expectation, result)
    }
}