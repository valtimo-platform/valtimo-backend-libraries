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
import com.ritense.iko.domain.IkoRepositoryConfig
import com.ritense.iko.domain.IkoDataAggregate
import com.ritense.iko.repository.IkoRepositoryConfigRepository
import com.ritense.iko.repository.IkoRepositoryConfigSpecificationHelper.Companion.byKey
import com.ritense.iko.repository.IkoRepositoryConfigSpecificationHelper.Companion.byTitleContains
import com.ritense.iko.repository.IkoRepositoryConfigSpecificationHelper.Companion.byType
import com.ritense.iko.repository.IkoRepositoryConfigSpecificationHelper.Companion.query
import com.ritense.valtimo.contract.iko.IkoRepository
import com.ritense.valtimo.contract.iko.PropertyField
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.transaction.annotation.Transactional

@Transactional
class IkoRepositoryService(
    private val ikoRepositoryConfigRepository: IkoRepositoryConfigRepository,
    private val authorizationService: AuthorizationService,
    private val ikoRepositories: List<IkoRepository>
) {
    fun getIkoRepositoryTypes(): List<String> {
        denyAuthorization()
        return ikoRepositories.map { it.getType() }
    }

    fun getIkoRepositoryConfigPropertyFields(type: String): List<PropertyField> {
        denyAuthorization()
        return ikoRepositories.single{ it.getType() == type}.getIkoRepositoryConfigPropertyFields()
    }

    fun findAll(
        key: String? = null,
        title: String? = null,
        type: String? = null,
        pageable: Pageable = Pageable.unpaged()
    ): Page<IkoRepositoryConfig> {
        denyAuthorization()
        val spec = getSpecification(
            key = key,
            titlePart = title,
            type = type,
        )
        return ikoRepositoryConfigRepository.findAll(spec, pageable)
    }

    fun getByKey(key: String): IkoRepositoryConfig {
        denyAuthorization()
        return ikoRepositoryConfigRepository.findById(key).orElseThrow()
    }

    fun createIkoRepositoryConfig(
        key: String,
        title: String,
        type: String,
        properties: Map<String, Any?>
    ): IkoRepositoryConfig {
        denyAuthorization()
        require(!existsByKey(key)) { "IKO repository '$key' already exists" }
        return ikoRepositoryConfigRepository.save(
            IkoRepositoryConfig(
                key = key,
                title = title,
                type = type,
                properties = properties,
            )
        )
    }

    fun saveIkoRepositoryConfig(
        key: String,
        title: String,
        type: String,
        properties: Map<String, Any?> = emptyMap()
    ): IkoRepositoryConfig {
        denyAuthorization()
        return ikoRepositoryConfigRepository.save(
            IkoRepositoryConfig(
                key = key,
                title = title,
                type = type,
                properties = properties,
            )
        )
    }

    fun deleteIkoRepositoryConfig(key: String) {
        denyAuthorization()
        ikoRepositoryConfigRepository.deleteById(key)
    }

    private fun getSpecification(
        key: String? = null,
        titlePart: String? = null,
        type: String? = null,
    ): Specification<IkoRepositoryConfig> {
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
        return ikoRepositoryConfigRepository.existsById(key)
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