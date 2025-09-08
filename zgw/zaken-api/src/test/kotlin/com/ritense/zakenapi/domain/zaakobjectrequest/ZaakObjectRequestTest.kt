package com.ritense.zakenapi.domain.zaakobjectrequest

import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ZaakObjectRequestTest {
    private val objectMapper = MapperSingleton.get()

    @Test
    fun `should deserialize JSON into SimpleZaakObjectRequest`() {
        val zaakObjectRequest: ZaakObjectRequest = objectMapper.readValue("""
            {
                "zaak": "http://example.com",
                "object": "http://example.com",
                "zaakobjecttype": "http://example.com",
                "objectType": "adres",
                "objectTypeOverige": "a",
                "objectTypeOverigeDefinitie": {
                    "url": "http://example.com",
                    "schema": "string",
                    "objectData": "string"
                },
                "relatieomschrijving": "string",
                "_expand": {
                    "zaakobjecttype": {}
                },
                "objectIdentificatie": {
                    "identificatie": "string",
                    "wplWoonplaatsNaam": "string",
                    "gorOpenbareRuimteNaam": "string",
                    "huisnummer": 99999,
                    "huisletter": "s",
                    "huisnummertoevoeging": "stri",
                    "postcode": "string"
                }
            }
        """.trimIndent())

        Assertions.assertThat(zaakObjectRequest).isInstanceOf(SimpleZaakObjectRequest::class.java)
    }

    @Test
    fun `should deserialize JSON into ZaakObjectZakelijkRechtRequest`() {
        val zaakObjectRequest: ZaakObjectRequest = objectMapper.readValue("""
            {
                "zaak": "http://example.com",
                "object": "http://example.com",
                "zaakobjecttype": "http://example.com",
                "objectType": "zakelijk_recht",
                "objectTypeOverige": "a",
                "objectTypeOverigeDefinitie": {
                    "url": "http://example.com",
                    "schema": "string",
                    "objectData": "string"
                },
                "relatieomschrijving": "string",
                "_expand": {
                    "zaakobjecttype": {}
                },
                "objectIdentificatie": {
                    "identificatie": "string",
                    "avgAard": "string",
                    "heeftBetrekkingOp": {},
                    "heeftAlsGerechtigde": {}
                }
            }
        """.trimIndent())

        Assertions.assertThat(zaakObjectRequest).isInstanceOf(ZaakObjectZakelijkRechtRequest::class.java)
    }

    @Test
    fun `should deserialize JSON into ZaakObjectOverigeRequest`() {
        val zaakObjectRequest: ZaakObjectRequest = objectMapper.readValue("""
            {
                "zaak": "http://example.com",
                "object": "http://example.com",
                "zaakobjecttype": "http://example.com",
                "objectType": "overige",
                "objectTypeOverige": "a",
                "objectTypeOverigeDefinitie": {
                    "url": "http://example.com",
                    "schema": "string",
                    "objectData": "string"
                },
                "relatieomschrijving": "string",
                "_expand": {
                    "zaakobjecttype": {}
                },
                "objectIdentificatie": {
                    "overigeData": {}
                }
            }
        """.trimIndent())

        Assertions.assertThat(zaakObjectRequest).isInstanceOf(ZaakObjectOverigeRequest::class.java)
    }

}