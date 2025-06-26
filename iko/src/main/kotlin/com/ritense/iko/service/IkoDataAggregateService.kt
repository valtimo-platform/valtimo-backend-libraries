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

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.authorization.Action
import com.ritense.authorization.Action.Companion.deny
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.iko.authorization.IkoDataAggregateActionProvider
import com.ritense.iko.domain.IkoDataAggregate
import com.ritense.iko.repository.IkoDataAggregateRepository
import com.ritense.iko.repository.IkoDataAggregateSpecificationHelper.Companion.byIkoConnectorConfigKey
import com.ritense.iko.repository.IkoDataAggregateSpecificationHelper.Companion.byKey
import com.ritense.iko.repository.IkoDataAggregateSpecificationHelper.Companion.byTitleContains
import com.ritense.valtimo.contract.iko.IkoConnector
import com.ritense.valtimo.contract.iko.PropertyField
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.transaction.annotation.Transactional

@Transactional
class IkoDataAggregateService(
    private val ikoDataAggregateRepository: IkoDataAggregateRepository,
    private val ikoConnectorService: IkoConnectorService,
    private val authorizationService: AuthorizationService,
    private val ikoConnectors: List<IkoConnector>,
) {

    fun findData(key: String): JsonNode {
        val dataAggregate = getByKey(key)
        val dataRepository = ikoConnectors.first { it.getType() == dataAggregate.ikoConnectorConfig.type }
        return dataRepository.findAll(
            dataAggregate.ikoConnectorConfig.properties + dataAggregate.properties,
            emptyList()
        )
    }

    fun getIkoDataAggregatePropertyFields(type: Any): List<PropertyField> {
        denyAuthorization()
        return ikoConnectors.single { it.getType() == type }.getDataAggregatePropertyFields()
    }

    fun findAll(
        key: String? = null,
        title: String? = null,
        pageable: Pageable = Pageable.unpaged()
    ): Page<IkoDataAggregate> {
        val spec = getSpecification(
            key = key,

            titlePart = title,
        )
        return ikoDataAggregateRepository.findAll(spec, pageable)
    }

    fun getByKey(key: String): IkoDataAggregate {
        val ikoDataAggregate = ikoDataAggregateRepository.findById(key).orElseThrow()
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                IkoDataAggregate::class.java,
                IkoDataAggregateActionProvider.VIEW,
                ikoDataAggregate,
            )
        )
        return ikoDataAggregate
    }

    fun createIkoDataAggregate(
        key: String,
        ikoConnectorConfigKey: String,
        title: String,
        properties: Map<String, Any?>,
    ): IkoDataAggregate {
        denyAuthorization()
        require(!existsByKey(key)) { "IKO data aggregate '$key' already exists" }
        return ikoDataAggregateRepository.save(
            IkoDataAggregate(
                key = key,
                title = title,
                properties = properties,
                ikoConnectorConfig = ikoConnectorService.getByKey(ikoConnectorConfigKey),
            )
        )
    }

    fun updateIkoDataAggregate(
        key: String,
        ikoConnectorConfigKey: String,
        title: String,
        properties: Map<String, Any?>,
    ): IkoDataAggregate {
        denyAuthorization()
        require(existsByKey(key)) { "IKO data aggregate '$key' does not exist" }
        return ikoDataAggregateRepository.save(
            IkoDataAggregate(
                key = key,
                title = title,
                properties = properties,
                ikoConnectorConfig = ikoConnectorService.getByKey(ikoConnectorConfigKey),
            )
        )
    }

    fun deleteIkoDataAggregate(key: String) {
        denyAuthorization()
        ikoDataAggregateRepository.deleteById(key)
    }

    fun requirePermission(key: String, action: Action<IkoDataAggregate>) {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                IkoDataAggregate::class.java,
                action,
                ikoDataAggregateRepository.findById(key).orElseThrow(),
            )
        )
    }

    fun denyAuthorization() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                IkoDataAggregate::class.java,
                deny()
            )
        )
    }

    private fun getSpecification(
        key: String? = null,
        ikoConnectorConfigKey: String? = null,
        titlePart: String? = null,
    ): Specification<IkoDataAggregate> {
        var spec: Specification<IkoDataAggregate> = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                IkoDataAggregate::class.java,
                IkoDataAggregateActionProvider.VIEW_LIST
            )
        )
        if (key != null) {
            spec = spec.and(byKey(key))
        }
        if (ikoConnectorConfigKey != null) {
            spec = spec.and(byIkoConnectorConfigKey(ikoConnectorConfigKey))
        }
        if (titlePart != null) {
            spec = spec.and(byTitleContains(titlePart))
        }
        return spec
    }

    private fun existsByKey(key: String): Boolean {
        return ikoDataAggregateRepository.existsById(key)
    }

}