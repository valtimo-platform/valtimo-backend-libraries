package com.ritense.zakenapi.domain.rol

import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

internal class RolTest {

    private val mapper = Mapper.INSTANCE.get()

    @Test
    fun `should serialize natuurlijk persoon`() {
        val rol = Rol(
            URI("http://rol.uri"),
            UUID.fromString("3dd4ea1f-3419-43ae-aca2-def868083689"),
            URI("http://zaak.uri"),
            URI("http://betrokkene.uri"),
            BetrokkeneType.NATUURLIJK_PERSOON,
            URI("http://role.type"),
            "omschrijving",
            ZaakRolOmschrijving.INITIATOR,
            "role-description",
            LocalDateTime.of(2023, 2, 15, 10, 23, 43),
            IndicatieMachtiging.GEMACHTIGDE.key,
            RolNatuurlijkPersoon(
                inpBsn = "bsn"
            )
        )

        val result = mapper.writeValueAsString(rol)

        val expectation =  """
            {
                "url": "http://rol.uri",
                "uuid": "3dd4ea1f-3419-43ae-aca2-def868083689",
                "zaak": "http://zaak.uri",
                "betrokkene": "http://betrokkene.uri",
                "betrokkeneType": "natuurlijk_persoon",
                "roltype": "http://role.type",
                "omschrijving": "omschrijving",
                "omschrijvingGeneriek": "initiator",
                "roltoelichting": "role-description",
                "registratiedatum": "2023-02-15T10:23:43",
                "indicatieMachtiging": "gemachtigde",
                "betrokkeneIdentificatie": {
                    "inpBsn": "bsn"
                }
            }
            """.replace("[ \n]".toRegex(), "")

        assertEquals(expectation, result)
    }

    @Test
    fun `should serialize niet natuurlijk persoon`() {
        val rol = Rol(
            URI("http://rol.uri"),
            UUID.fromString("3dd4ea1f-3419-43ae-aca2-def868083689"),
            URI("http://zaak.uri"),
            URI("http://betrokkene.uri"),
            BetrokkeneType.NATUURLIJK_PERSOON,
            URI("http://role.type"),
            "omschrijving",
            ZaakRolOmschrijving.INITIATOR,
            "roltoelichting",
            LocalDateTime.of(2023, 2, 15, 10, 23, 43),
            IndicatieMachtiging.GEMACHTIGDE.key,
            RolNietNatuurlijkPersoon(
                annIdentificatie = "kvk"
            )
        )

        val result = mapper.writeValueAsString(rol)

        val expectation =  """
            {
                "url": "http://rol.uri",
                "uuid": "3dd4ea1f-3419-43ae-aca2-def868083689",
                "zaak": "http://zaak.uri",
                "betrokkene": "http://betrokkene.uri",
                "betrokkeneType": "natuurlijk_persoon",
                "roltype": "http://role.type",
                "omschrijving": "omschrijving",
                "omschrijvingGeneriek": "initiator",
                "roltoelichting": "roltoelichting",
                "registratiedatum": "2023-02-15T10:23:43",
                "indicatieMachtiging": "gemachtigde",
                "betrokkeneIdentificatie": {
                    "annIdentificatie": "kvk"
                }
            }
            """.replace("[ \n]".toRegex(), "")
        assertEquals(expectation, result)
    }

    @Test
    fun `should deserialize natuurlijk persoon`() {
        val json =  """
            {
                "url": "http://rol.uri",
                "uuid": "3dd4ea1f-3419-43ae-aca2-def868083689",
                "zaak": "http://zaak.uri",
                "betrokkene": "http://betrokkene.uri",
                "betrokkeneType": "natuurlijk_persoon",
                "roltype": "http://role.type",
                "omschrijving": "omschrijving",
                "omschrijvingGeneriek": "initiator",
                "roltoelichting": "role-description",
                "registratiedatum": "2023-02-15T10:23:43Z",
                "indicatieMachtiging": "gemachtigde",
                "betrokkeneIdentificatie": {
                    "inpBsn": "bsn"
                }
            }
            """.replace("[ \n]".toRegex(), "")

        val result = mapper.readValue(json, Rol::class.java)

        assertTrue(result.betrokkeneIdentificatie is RolNatuurlijkPersoon)
        assertEquals("bsn", (result.betrokkeneIdentificatie as RolNatuurlijkPersoon).inpBsn)
    }

    @Test
    fun `should deserialize niet natuurlijk persoon`() {
        val json =  """
            {
                "url": "http://rol.uri",
                "uuid": "3dd4ea1f-3419-43ae-aca2-def868083689",
                "zaak": "http://zaak.uri",
                "betrokkene": "http://betrokkene.uri",
                "betrokkeneType": "niet_natuurlijk_persoon",
                "roltype": "http://role.type",
                "omschrijving": "omschrijving",
                "omschrijvingGeneriek": "initiator",
                "roltoelichting": "role-description",
                "registratiedatum": "2023-02-15T10:23:43Z",
                "indicatieMachtiging": "gemachtigde",
                "betrokkeneIdentificatie": {
                    "annIdentificatie": "kvk"
                }
            }
            """.replace("[ \n]".toRegex(), "")

        val result = mapper.readValue(json, Rol::class.java)

        assertTrue(result.betrokkeneIdentificatie is RolNietNatuurlijkPersoon)
        assertEquals("kvk", (result.betrokkeneIdentificatie as RolNietNatuurlijkPersoon).annIdentificatie)
    }
}
