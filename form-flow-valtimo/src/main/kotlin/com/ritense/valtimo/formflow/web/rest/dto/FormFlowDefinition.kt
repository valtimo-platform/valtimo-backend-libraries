/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.formflow.web.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty

class FormFlowDefinition(
    private val name: String,
    private val version: Long? = null
) {
    @JsonProperty
    fun getId(): String {
        val versionString = version ?: LATEST_VERSION_KEY
        return "${name}:${versionString}"
    }

    @JsonProperty
    fun getName(): String {
        val versionString = if (version == null) {
            LATEST_VERSION_KEY
        } else {
            "v${version}"
        }
        return "${name} (${versionString})"
    }

    companion object {
        val LATEST_VERSION_KEY = "latest"
    }
}