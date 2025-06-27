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
import com.ritense.iko.web.rest.request.IkoDataRequestUpdateRequest
import com.ritense.valtimo.contract.iko.IkoConnector
import com.ritense.valtimo.contract.iko.PropertyField
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.jpa.domain.Specification
import org.springframework.transaction.annotation.Transactional

@Transactional
class IkoDataRequestService(
    private val ikoDataRequestRepository: IkoDataRequestRepository,
    private val ikoDataAggregateService: IkoDataAggregateService,
    private val authorizationService: AuthorizationService,
    private val ikoConnectors: List<IkoConnector>,
) {

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
        return ikoConnectors.single { it.getType() == type }.getDataRequestPropertyFields()
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

    fun createIkoDataRequest(
        key: String,
        ikoDataAggregateKey: String,
        title: String,
        properties: Map<String, Any?>,
    ): IkoDataRequest {
        ikoDataAggregateService.denyAuthorization()
        val ikoDataAggregate = ikoDataAggregateService.getByKey(ikoDataAggregateKey)
        val id = IkoDataRequestId(key, ikoDataAggregate)
        require(!existsByKey(key, ikoDataAggregateKey)) { "IKO data request '$id' already exists" }
        val order = findAll(ikoDataAggregateKey = ikoDataAggregate.key).size
        return ikoDataRequestRepository.save(
            IkoDataRequest(
                id = id,
                title = title,
                order = order,
                properties = properties,
            ),
        )
    }

    fun saveIkoDataRequest(requests: List<IkoDataRequestUpdateRequest>): List<IkoDataRequest> {
        ikoDataAggregateService.denyAuthorization()
        val entities = requests.mapIndexed { i, request ->
            val ikoDataAggregate = ikoDataAggregateService.getByKey(request.ikoDataAggregateKey)
            val id = IkoDataRequestId(request.key, ikoDataAggregate)
            IkoDataRequest(
                id = id,
                title = request.title,
                order = i,
                properties = request.properties,
            )
        }
        return ikoDataRequestRepository.saveAll(entities)
    }

    fun deleteIkoDataRequest(key: String, ikoDataAggregateKey: String) {
        ikoDataAggregateService.denyAuthorization()
        val ikoDataAggregate = ikoDataAggregateService.getByKey(ikoDataAggregateKey)
        val id = IkoDataRequestId(key, ikoDataAggregate)
        ikoDataRequestRepository.deleteById(id)
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