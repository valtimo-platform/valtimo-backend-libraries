/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

import com.ritense.openzaak.domain.mapping.impl.InformatieObjectTypeLink
import com.ritense.openzaak.service.impl.InformatieObjectTypeLinkService
import com.ritense.openzaak.service.result.CreateInformatieObjectTypeLinkResult
import com.ritense.openzaak.web.rest.InformatieObjectTypeLinkResource
import com.ritense.openzaak.web.rest.request.CreateInformatieObjectTypeLinkRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok

class InformatieObjectTypeLinkResource(val informatieObjectTypeLinkService: InformatieObjectTypeLinkService) :
    InformatieObjectTypeLinkResource {

    override fun get(documentDefinitionName: String): ResponseEntity<InformatieObjectTypeLink?> {
        return when (val result = informatieObjectTypeLinkService.get(documentDefinitionName)) {
            null -> noContent().build()
            else -> ok(result)
        }
    }

    override fun create(request: CreateInformatieObjectTypeLinkRequest): ResponseEntity<CreateInformatieObjectTypeLinkResult> {
        val result = informatieObjectTypeLinkService.create(request)
        return when (result.informatieObjectTypeLink()) {
            null -> badRequest().body(result)
            else -> ok(result)
        }
    }

    override fun remove(documentDefinitionName: String): ResponseEntity<InformatieObjectTypeLink?> {
        informatieObjectTypeLinkService.deleteBy(documentDefinitionName)
        return noContent().build()
    }
}