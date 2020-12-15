/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.web.rest.impl

import com.ritense.openzaak.domain.configuration.OpenZaakConfig
import com.ritense.openzaak.domain.request.CreateOpenZaakConfigRequest
import com.ritense.openzaak.domain.request.ModifyOpenZaakConfigRequest
import com.ritense.openzaak.service.impl.OpenZaakConfigService
import com.ritense.openzaak.service.result.CreateOpenZaakConfigResult
import com.ritense.openzaak.service.result.ModifyOpenZaakConfigResult
import com.ritense.openzaak.web.rest.OpenZaakConfigResource
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok

class OpenZaakConfigResource(
    private val openZaakConfigService: OpenZaakConfigService
) : OpenZaakConfigResource {

    override fun getConfig(): ResponseEntity<OpenZaakConfig> {
        val openZaakConfig = openZaakConfigService.get()
        return when (openZaakConfig) {
            null -> noContent().build()
            else -> ok(openZaakConfig)
        }
    }

    override fun createConfig(request: CreateOpenZaakConfigRequest): ResponseEntity<CreateOpenZaakConfigResult> {
        val result = openZaakConfigService.createOpenZaakConfig(request)
        return when (result.openZaakConfig()) {
            null -> badRequest().body(result)
            else -> ok(result)
        }
    }

    override fun modifyConfig(request: ModifyOpenZaakConfigRequest): ResponseEntity<ModifyOpenZaakConfigResult> {
        val result = openZaakConfigService.modifyOpenZaakConfig(request)
        return when (result.openZaakConfig()) {
            null -> badRequest().body(result)
            else -> ok(result)
        }
    }

    override fun deleteConfig(): ResponseEntity<Void> {
        openZaakConfigService.removeOpenZaakConfig()
        return noContent().build()
    }
}
