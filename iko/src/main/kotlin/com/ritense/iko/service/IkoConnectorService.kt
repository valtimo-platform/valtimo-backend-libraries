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

package com.ritense.iko.service

import com.ritense.authorization.Action.Companion.deny
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.iko.domain.IkoConnectorConfig
import com.ritense.iko.domain.IkoDataAggregate
import com.ritense.iko.repository.IkoConnectorConfigRepository
import com.ritense.iko.repository.IkoConnectorConfigSpecificationHelper.Companion.byKey
import com.ritense.iko.repository.IkoConnectorConfigSpecificationHelper.Companion.byTitleContains
import com.ritense.iko.repository.IkoConnectorConfigSpecificationHelper.Companion.byType
import com.ritense.iko.repository.IkoConnectorConfigSpecificationHelper.Companion.query
import com.ritense.valtimo.contract.iko.IkoConnector
import com.ritense.valtimo.contract.iko.PropertyField
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.transaction.annotation.Transactional

@Transactional
class IkoConnectorService(
    private val ikoConnectorConfigRepository: IkoConnectorConfigRepository,
    private val authorizationService: AuthorizationService,
    private val ikoConnectors: List<IkoConnector>
) {
    fun getIkoConnectorTypes(): List<String> {
        denyAuthorization()
        return ikoConnectors.map { it.getType() }
    }

    fun getIkoConnectorPropertyFields(type: String): List<PropertyField> {
        denyAuthorization()
        return ikoConnectors.single{ it.getType() == type}.getIkoConnectorPropertyFields()
    }

    fun findAll(
        key: String? = null,
        title: String? = null,
        type: String? = null,
        pageable: Pageable = Pageable.unpaged()
    ): Page<IkoConnectorConfig> {
        denyAuthorization()
        val spec = getSpecification(
            key = key,
            titlePart = title,
            type = type,
        )
        return ikoConnectorConfigRepository.findAll(spec, pageable)
    }

    fun getByKey(key: String): IkoConnectorConfig {
        denyAuthorization()
        return ikoConnectorConfigRepository.findById(key).orElseThrow()
    }

    fun createIkoConnectorConfig(
        key: String,
        title: String,
        type: String,
        properties: Map<String, Any?>
    ): IkoConnectorConfig {
        denyAuthorization()
        require(!existsByKey(key)) { "IKO connector '$key' already exists" }
        return ikoConnectorConfigRepository.save(
            IkoConnectorConfig(
                key = key,
                title = title,
                type = type,
                properties = properties,
            )
        )
    }

    fun updateIkoConnectorConfig(
        key: String,
        title: String,
        type: String,
        properties: Map<String, Any?>
    ): IkoConnectorConfig {
        denyAuthorization()
        require(existsByKey(key)) { "IKO conficonnectorguration '$key' does not exist" }
        return ikoConnectorConfigRepository.save(
            IkoConnectorConfig(
                key = key,
                title = title,
                type = type,
                properties = properties,
            )
        )
    }

    fun deleteIkoConnectorConfig(key: String) {
        denyAuthorization()
        ikoConnectorConfigRepository.deleteById(key)
    }

    private fun getSpecification(
        key: String? = null,
        titlePart: String? = null,
        type: String? = null,
    ): Specification<IkoConnectorConfig> {
        var spec = query()
        if (key != null) {
            spec = spec.and(byKey(key))
        }
        if (titlePart != null) {
            spec = spec.and(byTitleContains(titlePart))
        }
        if (type != null) {
            spec = spec.and(byType(type))
        }
        return spec
    }

    private fun existsByKey(key: String): Boolean {
        return ikoConnectorConfigRepository.existsById(key)
    }

    private fun denyAuthorization() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                IkoDataAggregate::class.java,
                deny()
            )
        )
    }
}