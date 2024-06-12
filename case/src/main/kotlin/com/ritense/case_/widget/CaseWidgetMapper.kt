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

package com.ritense.case_.widget

import com.ritense.case_.domain.tab.CaseWidgetTabWidget
import com.ritense.case_.rest.dto.CaseWidgetTabWidgetDto
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes

interface CaseWidgetMapper<ENTITY : CaseWidgetTabWidget, DTO : CaseWidgetTabWidgetDto> {

    fun supportedEntityType(): Class<ENTITY> =
        this::class.allSupertypes.first { it.classifier == CaseWidgetMapper::class }.arguments[0].type?.let { it.classifier as KClass<ENTITY> }?.java
            ?: throw IllegalArgumentException("Could not resolve entity type for ${this::class}")

    fun supportedDtoType(): Class<DTO> =
        this::class.allSupertypes.first { it.classifier == CaseWidgetMapper::class }.arguments[1].type?.let { it.classifier as KClass<DTO> }?.java
            ?: throw IllegalArgumentException("Could not resolve dto type for ${this::class}")


    fun toEntity(dto: DTO, index: Int): ENTITY
    fun toDto(entity: ENTITY): DTO
}