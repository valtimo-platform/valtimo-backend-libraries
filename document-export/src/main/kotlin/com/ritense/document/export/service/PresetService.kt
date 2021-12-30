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

package com.ritense.document.export.service

import com.ritense.document.export.domain.Preset
import com.ritense.document.export.domain.PresetId
import com.ritense.document.export.domain.Status
import com.ritense.document.export.domain.Tree
import com.ritense.document.export.domain.UserId
import com.ritense.document.export.repository.PresetRepository
import com.ritense.document.service.DocumentDefinitionService
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
class PresetService(
    private val presetRepository: PresetRepository,
    private val documentDefinitionService: DocumentDefinitionService
) {

    fun getPreset(id: UUID): Preset {
        return presetRepository.getById(PresetId.existingId(id))
    }

    fun create(documentDefinitionName: String): Preset {
        val definition = documentDefinitionService.findLatestByName(documentDefinitionName).get()!!
        val preset = Preset(
            presetId = PresetId.newId(UUID.randomUUID()),
            status = Status.open(),
            tree = Tree.init(definition.schema()),
            userId = UserId.fromAuthentication()
        )
        return presetRepository.save(preset)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}