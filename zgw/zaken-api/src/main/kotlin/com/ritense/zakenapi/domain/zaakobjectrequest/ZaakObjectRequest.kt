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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.net.URI

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "objectType",
    visible = true,
    defaultImpl = SimpleZaakObjectRequest::class
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ZaakObjectZakelijkRechtRequest::class, name = "zakelijk_recht"),
    JsonSubTypes.Type(value = ZaakObjectOverigeRequest::class, name = "overige"),
)
@JsonInclude(JsonInclude.Include.NON_NULL)
interface ZaakObjectRequest {
    // Nullable value so that the zaakUrl can be supplied by the plugin when it is null.
    // The Zaken API does requires a value for this field.
    @get:JsonProperty("zaak")
    var zaakUrl: URI?
    @get:JsonProperty("object")
    val objectUrl: URI?
    val zaakobjecttype: String?
    val objectType: ZaakObjectType
    val relatieomschrijving: String?
}
