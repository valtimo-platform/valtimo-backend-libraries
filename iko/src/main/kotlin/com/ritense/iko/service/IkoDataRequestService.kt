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
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.iko.authorization.IkoDataAggregateActionProvider.Companion.VIEW
import com.ritense.iko.domain.IkoDataAggregate
import com.ritense.iko.domain.IkoDataRequest
import com.ritense.iko.domain.IkoDataRequestId
import com.ritense.iko.repository.IkoDataRequestRepository
import com.ritense.iko.repository.IkoDataRequestSpecificationHelper.Companion.byIkoDataAggregateKey
import com.ritense.iko.repository.IkoDataRequestSpecificationHelper.Companion.byKey
import com.ritense.iko.repository.IkoDataRequestSpecificationHelper.Companion.byTitleContains
import com.ritense.iko.repository.IkoDataRequestSpecificationHelper.Companion.query
import com.ritense.valtimo.contract.iko.DataFilter
import com.ritense.valtimo.contract.iko.IkoRepository
import com.ritense.valtimo.contract.iko.PropertyField
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.jpa.domain.Specification
import org.springframework.transaction.annotation.Transactional

@Transactional
class IkoDataRequestService(
    private val ikoDataRequestRepository: IkoDataRequestRepository,
    private val ikoDataAggregateService: IkoDataAggregateService,
    private val authorizationService: AuthorizationService,
    private val ikoRepositories: List<IkoRepository>,
) {

    fun searchData(
        key: String,
        ikoDataAggregateKey: String,
        filters: List<DataFilter>,
        pageable: Pageable
    ): Page<JsonNode> {
        ikoDataAggregateService.requirePermission(ikoDataAggregateKey, VIEW)
        val dataRequest = getByKey(key, ikoDataAggregateKey)
        val ikoRepository = ikoRepositories.first {
            it.getType() == dataRequest.id.ikoDataAggregate.ikoRepositoryConfig.type
        }
        return ikoRepository.findAll(
            dataRequest.id.ikoDataAggregate.ikoRepositoryConfig.properties +
                dataRequest.id.ikoDataAggregate.properties +
                dataRequest.properties,
            filters,
            pageable
        )
    }

    fun findAll(
        key: String? = null,
        ikoDataAggregateKey: String? = null,
        title: String? = null,
    ): List<IkoDataRequest> {
        val spec = getSpecification(
            key = key,
            ikoDataAggregateKey = ikoDataAggregateKey,
            titlePart = title,
        )
        return ikoDataRequestRepository.findAll(spec, Sort.by(ASC, "order"))
    }

    fun getIkoDataRequestPropertyFields(type: Any): List<PropertyField> {
        ikoDataAggregateService.denyAuthorization()
        return ikoRepositories.single { it.getType() == type }.getDataRequestPropertyFields()
    }

    fun getByKey(key: String, ikoDataAggregateKey: String): IkoDataRequest {
        val ikoDataAggregate = ikoDataAggregateService.getByKey(ikoDataAggregateKey)
        val id = IkoDataRequestId(key, ikoDataAggregate)
        val ikoDataRequest = ikoDataRequestRepository.findById(id).orElseThrow()
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                IkoDataAggregate::class.java,
                VIEW,
                ikoDataRequest.id.ikoDataAggregate,
            )
        )
        return ikoDataRequest
    }

    fun create(ikoDataRequest: IkoDataRequest): IkoDataRequest {
        require(!existsByKey(key = ikoDataRequest.id.key, ikoDataAggregateKey = ikoDataRequest.id.ikoDataAggregate.key))
        return ikoDataRequestRepository.save(ikoDataRequest)
    }

    fun update(ikoDataRequest: IkoDataRequest): IkoDataRequest {
        require(existsByKey(key = ikoDataRequest.id.key, ikoDataAggregateKey = ikoDataRequest.id.ikoDataAggregate.key))
        return ikoDataRequestRepository.save(ikoDataRequest)
    }

    fun delete(
        key: String? = null,
        ikoDataAggregateKey: String? = null,
    ) {
        ikoDataAggregateService.denyAuthorization()
        ikoDataRequestRepository.delete(
            getSpecification(
                key = key,
                ikoDataAggregateKey = ikoDataAggregateKey,
            )
        )
    }

    private fun getSpecification(
        key: String? = null,
        ikoDataAggregateKey: String? = null,
        titlePart: String? = null,
    ): Specification<IkoDataRequest> {
        if (ikoDataAggregateKey != null) {
            ikoDataAggregateService.requirePermission(ikoDataAggregateKey, VIEW)
        } else {
            ikoDataAggregateService.denyAuthorization()
        }
        var spec = query()
        if (key != null) {
            spec = spec.and(byKey(key))
        }
        if (ikoDataAggregateKey != null) {
            spec = spec.and(byIkoDataAggregateKey(ikoDataAggregateKey))
        }
        if (titlePart != null) {
            spec = spec.and(byTitleContains(titlePart))
        }
        return spec
    }

    private fun existsByKey(key: String, ikoDataAggregateKey: String): Boolean {
        val ikoDataAggregate = ikoDataAggregateService.getByKey(ikoDataAggregateKey)
        val id = IkoDataRequestId(key, ikoDataAggregate)
        return ikoDataRequestRepository.existsById(id)
    }
}