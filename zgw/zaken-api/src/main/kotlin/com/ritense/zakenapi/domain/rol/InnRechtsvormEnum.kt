/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.domain.rol

enum class InnRechtsvormEnum(val value: String) {
    BESLOTEN_VENNOOTSCHAP("besloten_vennootschap"),
    COOPERATIE_EUROPEES_ECONOMISCHE_SAMENWERKING("cooperatie_europees_economische_samenwerking"),
    EUROPESE_COOPERATIEVE_VENOOTSCHAP("europese_cooperatieve_venootschap"),
    EUROPESE_NAAMLOZE_VENNOOTSCHAP("europese_naamloze_vennootschap"),
    KERKELIJKE_ORGANISATIE("kerkelijke_organisatie"),
    NAAMLOZE_VENNOOTSCHAP("naamloze_vennootschap"),
    ONDERLINGE_WAARBORG_MAATSCHAPPIJ("onderlinge_waarborg_maatschappij"),
    OVERIG_PRIVATERECHTELIJKE_RECHTSPERSOON("overig_privaatrechtelijke_rechtspersoon"),
    STICHTING("stichting"),
    VERENIGING("vereniging"),
    VERENIGING_VAN_EIGENAREN("vereniging_van_eigenaars"),
    PUBLIEKRECHTELIJKE_RECHTSPERSOON("publiekrechtelijke_rechtspersoon"),
    VENNOOTSCHAP_ONDER_FIRMA("vennootschap_onder_firma"),
    MAATSCHAP("maatschap"),
    REDERIJ("rederij"),
    COMMANDITAIRE_VENNOOTSCHAP("commanditaire_vennootschap"),
    KAPITAALVENNOOTSCHAP_BINNEN_EER("kapitaalvennootschap_binnen_eer"),
    OVERIGE_BUITENLANDSE_RECHTSPERSOON_VENNOOTSCHAP("overige_buitenlandse_rechtspersoon_vennootschap"),
    KAPITAALVENNOOTSCHAP_BUITEN_EER("kapitaalvennootschap_buiten_eer");

    override fun toString(): String = value
}