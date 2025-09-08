/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.domain.zaakobjectrequest

import com.fasterxml.jackson.annotation.JsonTypeName
import java.net.URI

@JsonTypeName("zakelijk_recht")
data class ZaakObjectZakelijkRechtRequest(
    override var zaakUrl: URI?,
    override val objectUrl: URI? = null,
    override val zaakobjecttype: String? = null,
    override val objectType: ZaakObjectType = ZaakObjectType.ZAKELIJK_RECHT,
    override val relatieomschrijving: String? = null,
    val objectIdentificatie: ZakelijkRechtIdentificatie? = null,
) : ZaakObjectRequest {
    class ZakelijkRechtIdentificatie(
        val identificatie: String,
        val avgAard: String
    )
}
