package com.ritense.openzaak.service.impl.model.zaak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ritense.openzaak.service.impl.model.zaak.betrokkene.RolNatuurlijkPersoon
import com.ritense.openzaak.service.impl.model.zaak.betrokkene.RolNietNatuurlijkPersoon
import java.net.URI
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RolTest {

    private lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        mapper = ObjectMapper().registerModule(KotlinModule())
    }

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

        val result = mapper.writeValueAsString(rol)

        val expectation =  "{" +
            "\"zaak\":\"http://zaak.uri\"," +
            "\"betrokkene\":\"http://betrokkene.uri\"," +
            "\"betrokkeneType\":\"natuurlijk_persoon\"," +
            "\"roltype\":\"http://role.type\"," +
            "\"roltoelichting\":\"role description\"," +
            "\"betrokkeneIdentificatie\":{\"inpBsn\":\"bsn\"}}"

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
                annIdentificatie = "kvk"
            )
        )

        val result = mapper.writeValueAsString(rol)

        val expectation =  "{" +
            "\"zaak\":\"http://zaak.uri\"," +
            "\"betrokkene\":\"http://betrokkene.uri\"," +
            "\"betrokkeneType\":\"niet_natuurlijk_persoon\"," +
            "\"roltype\":\"http://role.type\"," +
            "\"roltoelichting\":\"role description\"," +
            "\"betrokkeneIdentificatie\":{\"annIdentificatie\":\"kvk\"}}"

        assertEquals(expectation, result)
    }

    @Test
    fun `should deserialize natuurlijk persoon`() {
        val json =   "{" +
            "\"zaak\":\"http://zaak.uri\"," +
            "\"betrokkene\":\"http://betrokkene.uri\"," +
            "\"betrokkeneType\":\"natuurlijk_persoon\"," +
            "\"roltype\":\"http://role.type\"," +
            "\"roltoelichting\":\"role description\"," +
            "\"betrokkeneIdentificatie\":{\"inpBsn\":\"bsn\"}}"


        val result = mapper.readValue(json, Rol::class.java)

        assertTrue(result.betrokkeneIdentificatie is RolNatuurlijkPersoon)
        assertEquals("bsn", (result.betrokkeneIdentificatie as RolNatuurlijkPersoon).inpBsn)
    }

    @Test
    fun `should deserialize niet natuurlijk persoon`() {
        val json =   "{" +
            "\"zaak\":\"http://zaak.uri\"," +
            "\"betrokkene\":\"http://betrokkene.uri\"," +
            "\"betrokkeneType\":\"niet_natuurlijk_persoon\"," +
            "\"roltype\":\"http://role.type\"," +
            "\"roltoelichting\":\"role description\"," +
            "\"betrokkeneIdentificatie\":{\"annIdentificatie\":\"kvk\"}}"


        val result = mapper.readValue(json, Rol::class.java)

        assertTrue(result.betrokkeneIdentificatie is RolNietNatuurlijkPersoon)
        assertEquals("kvk", (result.betrokkeneIdentificatie as RolNietNatuurlijkPersoon).annIdentificatie)
    }
}