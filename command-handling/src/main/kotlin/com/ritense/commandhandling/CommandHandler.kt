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

package com.ritense.commandhandling

import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes

@Transactional
interface CommandHandler<C : Command<T>, out T> {

    fun execute(command: C) : T

    @Suppress("UNCHECKED_CAST")
    fun getCommandType() =
        this::class.allSupertypes.first { it.classifier == CommandHandler::class }.arguments.first().type?.let { it.classifier as KClass<C> }
            ?: throw IllegalArgumentException("Could not resolve CommandType for ${this::class}")

}