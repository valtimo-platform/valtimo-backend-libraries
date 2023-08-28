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

package com.ritense.plugin.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.domain.PluginConfiguration
import mu.KotlinLogging
import javax.persistence.PostLoad
import javax.persistence.PostPersist
import javax.persistence.PostUpdate
import javax.persistence.PrePersist
import javax.persistence.PreUpdate

class PluginConfigurationEntityListener(
    val encryptionService: EncryptionService,
    val objectMapper: ObjectMapper
) {
    @PrePersist
    fun encryptPropertiesOnSave(pluginConfiguration: PluginConfiguration) {
        logger.debug { "Encrypting secrets for PluginConfiguration ${pluginConfiguration.title} on initial save" }
        setBeans(pluginConfiguration)
        pluginConfiguration.encryptProperties()
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    @PreUpdate
    fun setBeans(pluginConfiguration: PluginConfiguration) {
        pluginConfiguration.encryptionService = encryptionService
        pluginConfiguration.objectMapper = objectMapper
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}