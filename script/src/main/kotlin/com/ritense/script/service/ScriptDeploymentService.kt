/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.script.service

import com.ritense.script.domain.Script
import mu.KLogger
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import java.io.IOException

open class ScriptDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val scriptService: ScriptService,
) {

    @EventListener(ApplicationReadyEvent::class)
    open fun deployScripts() {
        logger.info { "Deploying all scripts from $PATH" }
        try {
            loadResources().forEach { resource ->
                val fileName = requireNotNull(resource.filename)
                logger.info { "Deploying script from file '${fileName}'" }
                resource.inputStream.bufferedReader().use { reader ->
                    scriptService.saveScript(
                        Script(
                            key = fileName.substringAfterLast('/').substringBeforeLast(".script.js"),
                            content = reader.readText()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error while deploying process-links" }
        }
    }

    @Throws(IOException::class)
    private fun loadResources(): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResources(PATH)
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val PATH = "classpath*:**/*.script.js"
    }
}