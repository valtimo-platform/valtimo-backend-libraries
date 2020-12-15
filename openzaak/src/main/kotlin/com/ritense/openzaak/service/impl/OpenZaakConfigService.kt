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

package com.ritense.openzaak.service.impl

import com.ritense.openzaak.domain.configuration.OpenZaakConfig
import com.ritense.openzaak.domain.configuration.OpenZaakConfigId
import com.ritense.openzaak.domain.configuration.Secret
import com.ritense.openzaak.domain.request.CreateOpenZaakConfigRequest
import com.ritense.openzaak.domain.request.ModifyOpenZaakConfigRequest
import com.ritense.openzaak.repository.OpenZaakConfigRepository
import com.ritense.openzaak.service.OpenZaakConfigService
import com.ritense.openzaak.service.impl.result.CreateOpenZaakConfigResultFailed
import com.ritense.openzaak.service.impl.result.CreateOpenZaakConfigResultSucceeded
import com.ritense.openzaak.service.impl.result.ModifyOpenZaakConfigResultFailed
import com.ritense.openzaak.service.impl.result.ModifyOpenZaakConfigResultSucceeded
import com.ritense.openzaak.service.result.CreateOpenZaakConfigResult
import com.ritense.openzaak.service.result.ModifyOpenZaakConfigResult
import com.ritense.valtimo.contract.result.OperationError
import org.springframework.web.client.RestTemplate
import java.util.UUID
import javax.validation.ConstraintViolationException

class OpenZaakConfigService(
    private val openZaakConfigRepository: OpenZaakConfigRepository,
    private val tokenGeneratorService: OpenZaakTokenGeneratorService,
    private val restTemplate: RestTemplate
) : OpenZaakConfigService {

    override fun get(): OpenZaakConfig? {
        return openZaakConfigRepository.findAll().firstOrNull()
    }

    override fun createOpenZaakConfig(request: CreateOpenZaakConfigRequest): CreateOpenZaakConfigResult {
        return try {
            if (get() != null) {
                throw IllegalStateException("Only one OpenZaak config is allowed")
            }
            val openZaakConfig = OpenZaakConfig(
                OpenZaakConfigId.newId(UUID.randomUUID()),
                request.url,
                request.clientId,
                Secret(request.secret),
                request.rsin,
                request.organisation
            )
            testConnection(openZaakConfig)
            openZaakConfigRepository.save(openZaakConfig)
            return CreateOpenZaakConfigResultSucceeded(openZaakConfig)
        } catch (ex: ConstraintViolationException) {
            val errors: List<OperationError> = ex.constraintViolations.map { OperationError.FromString(it.message) }
            CreateOpenZaakConfigResultFailed(errors)
        } catch (ex: IllegalStateException) {
            CreateOpenZaakConfigResultFailed(listOf(OperationError.FromException(ex)))
        }
    }

    override fun modifyOpenZaakConfig(request: ModifyOpenZaakConfigRequest): ModifyOpenZaakConfigResult {
        return try {
            val openZaakConfig = get() ?: throw IllegalStateException("OpenZaak config is not found")
            openZaakConfig.changeConfig(request)
            testConnection(openZaakConfig)
            openZaakConfigRepository.save(openZaakConfig)
            return ModifyOpenZaakConfigResultSucceeded(openZaakConfig)
        } catch (ex: ConstraintViolationException) {
            val errors: List<OperationError> = ex.constraintViolations.map { OperationError.FromString(it.message) }
            ModifyOpenZaakConfigResultFailed(errors)
        } catch (ex: IllegalStateException) {
            ModifyOpenZaakConfigResultFailed(listOf(OperationError.FromException(ex)))
        }
    }

    override fun removeOpenZaakConfig() {
        val openZaakConfig = get() ?: throw IllegalStateException("OpenZaak config is not found")
        openZaakConfigRepository.delete(openZaakConfig)
    }

    private fun testConnection(openZaakConfig: OpenZaakConfig) {
        try {
            OpenZaakRequestBuilder(restTemplate, this, tokenGeneratorService)
                .config(openZaakConfig)
                .path("catalogi/api/v1/zaaktypen")
                .build()
                .execute(String::class.java)
        } catch (ex: Exception) {
            throw IllegalStateException("Testing connection failed")
        }
    }

}