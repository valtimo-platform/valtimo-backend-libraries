/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.documentenapi.service

import com.ritense.documentenapi.domain.ZgwDocumentTrefwoord
import com.ritense.documentenapi.repository.ZgwDocumentTrefwoordRepository
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class ZgwDocumentTrefwoordService(
    private val zgwDocumentTrefwoordRepository: ZgwDocumentTrefwoordRepository
) {
    fun getTrefwoorden(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): List<ZgwDocumentTrefwoord> {
        logger.debug { "Get Trefwoorden $caseDefinitionName" }
        return zgwDocumentTrefwoordRepository.findAllByCaseDefinitionName(caseDefinitionName)
    }

    fun getTrefwoorden(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String,
        pageable: Pageable
    ): Page<ZgwDocumentTrefwoord> {
        logger.debug { "Get Trefwoorden $caseDefinitionName $pageable" }
        return zgwDocumentTrefwoordRepository.findAllByCaseDefinitionName(caseDefinitionName, pageable)
    }

    fun getTrefwoorden(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String,
        search: String?,
        pageable: Pageable
    ): Page<ZgwDocumentTrefwoord> {
        return if (!search.isNullOrBlank()) {
            zgwDocumentTrefwoordRepository.findAllByCaseDefinitionNameAndValueContaining(
                caseDefinitionName,
                search,
                pageable
            )
        } else {
            zgwDocumentTrefwoordRepository.findAllByCaseDefinitionName(caseDefinitionName, pageable)
        }
    }

    fun createTrefwoord(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String,
        trefwoord: String
    ) {
        val existingTrefwoord = zgwDocumentTrefwoordRepository.findAllByCaseDefinitionNameAndValue(
            caseDefinitionName,
            trefwoord
        )
        require(existingTrefwoord == null) {
            "Trefwoord $trefwoord already exists for case definition $caseDefinitionName"
        }
        logger.info { "Create Trefwoord $caseDefinitionName $trefwoord" }
        zgwDocumentTrefwoordRepository.save(ZgwDocumentTrefwoord(caseDefinitionName, trefwoord))
    }

    fun deleteTrefwoord(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String,
        trefwoord: String
    ) {
        logger.info { "Delete Trefwoord $caseDefinitionName $trefwoord" }
        return zgwDocumentTrefwoordRepository.deleteByCaseDefinitionNameAndValue(caseDefinitionName, trefwoord)
    }

    fun deleteTrefwoorden(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String,
        trefwoorden: List<String>
    ) {
        logger.info { "Delete Trefwoorden $caseDefinitionName $trefwoorden" }
        return zgwDocumentTrefwoordRepository.deleteByCaseDefinitionNameAndValueIn(caseDefinitionName, trefwoorden)
    }

    companion object {
        val logger = KotlinLogging.logger { }
    }

}
