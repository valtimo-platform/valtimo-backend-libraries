package com.ritense.zakenapi.domain.zaakobjectrequest

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ZaakObjectType(@get:JsonValue val value: String) {
    ADRES("adres"),
    BESLUIT("besluit"),
    BUURT("buurt"),
    ENKELVOUDIG_DOCUMENT("enkelvoudig_document"),
    GEMEENTE("gemeente"),
    GEMEENTELIJKE_OPENBARE_RUIMTE("gemeentelijke_openbare_ruimte"),
    HUISHOUDEN("huishouden"),
    INRICHTINGSELEMENT("inrichtingselement"),
    KADASTRALE_ONROERENDE_ZAAK("kadastrale_onroerende_zaak"),
    KUNSTWERKDEEL("kunstwerkdeel"),
    MAATSCHAPPELIJKE_ACTIVITEIT("maatschappelijke_activiteit"),
    MEDEWERKER("medewerker"),
    NATUURLIJK_PERSOON("natuurlijk_persoon"),
    NIET_NATUURLIJK_PERSOON("niet_natuurlijk_persoon"),
    OPENBARE_RUIMTE("openbare_ruimte"),
    ORGANISATORISCHE_EENHEID("organisatorische_eenheid"),
    PAND("pand"),
    SPOORBAANDEEL("spoorbaandeel"),
    STATUS("status"),
    TERREINDEEL("terreindeel"),
    TERREIN_GEBOUWD_OBJECT("terrein_gebouwd_object"),
    VESTIGING("vestiging"),
    WATERDEEL("waterdeel"),
    WEGDEEL("wegdeel"),
    WIJK("wijk"),
    WOONPLAATS("woonplaats"),
    WOZ_DEELOBJECT("woz_deelobject"),
    WOZ_OBJECT("woz_object"),
    WOZ_WAARDE("woz_waarde"),
    ZAKELIJK_RECHT("zakelijk_recht"),
    OVERIGE("overige");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromValue(value: String): ZaakObjectType =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Value $value is not a ZaakObjectType")
    }
}