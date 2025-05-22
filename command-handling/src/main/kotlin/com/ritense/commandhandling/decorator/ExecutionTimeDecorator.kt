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

package com.ritense.commandhandling.decorator

import com.ritense.commandhandling.Command
import com.ritense.commandhandling.CommandHandler
import mu.KotlinLogging

class ExecutionTimeDecorator<C : Command<T>, T>(
    commandHandler: CommandHandler<C, T>
) : CommandHandlerDecorator<C, T>(commandHandler) {

    override fun execute(command: C): T {
        val startTime = System.currentTimeMillis()
        val result = super.execute(command)
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        logger.trace { "Timed '${command.javaClass.simpleName}' execution time = '$totalTime' in milliseconds" }
        return result
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}