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

package com.ritense.openzaak.service.impl

import com.ritense.openzaak.domain.mapping.impl.InformatieObjectTypeLink
import com.ritense.openzaak.domain.mapping.impl.InformatieObjectTypeLinkId
import com.ritense.openzaak.repository.InformatieObjectTypeLinkRepository
import com.ritense.openzaak.service.InformatieObjectTypeLinkService
import com.ritense.openzaak.service.impl.result.CreateInformatieObjectTypeLinkResultFailed
import com.ritense.openzaak.service.impl.result.CreateInformatieObjectTypeLinkResultSucceeded
import com.ritense.openzaak.service.result.CreateInformatieObjectTypeLinkResult
import com.ritense.openzaak.web.rest.request.CreateInformatieObjectTypeLinkRequest
import com.ritense.valtimo.contract.result.OperationError
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import javax.validation.ConstraintViolationException

@Transactional
class InformatieObjectTypeLinkService(
    private val informatieObjectTypeLinkRepository: InformatieObjectTypeLinkRepository
) : InformatieObjectTypeLinkService {

    override fun get(documentDefinitionName: String): InformatieObjectTypeLink? {
        return informatieObjectTypeLinkRepository.findByDocumentDefinitionName(documentDefinitionName)
    }

    override fun create(request: CreateInformatieObjectTypeLinkRequest): CreateInformatieObjectTypeLinkResult {
        return try {
            var informatieObjectTypeLink =
                informatieObjectTypeLinkRepository.findByDocumentDefinitionName(request.documentDefinitionName)
            if (informatieObjectTypeLink == null) {
                informatieObjectTypeLink = InformatieObjectTypeLink(
                    InformatieObjectTypeLinkId.newId(UUID.randomUUID()),
                    request.documentDefinitionName,
                    request.zaakType,
                    request.informatieObjectType
                )
            } else {
                informatieObjectTypeLink.change(request.informatieObjectType)
            }
            informatieObjectTypeLinkRepository.save(informatieObjectTypeLink)
            return CreateInformatieObjectTypeLinkResultSucceeded(informatieObjectTypeLink)
        } catch (ex: ConstraintViolationException) {
            val errors: List<OperationError> = ex.constraintViolations.map { OperationError.FromString(it.message) }
            CreateInformatieObjectTypeLinkResultFailed(errors)
        } catch (ex: RuntimeException) {
            CreateInformatieObjectTypeLinkResultFailed(listOf(OperationError.FromException(ex)))
        }
    }

    fun deleteBy(documentDefinitionName: String) {
        informatieObjectTypeLinkRepository.deleteByDocumentDefinitionName(documentDefinitionName)
    }
}