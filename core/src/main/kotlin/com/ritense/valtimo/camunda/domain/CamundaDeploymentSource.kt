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

package com.ritense.valtimo.camunda.domain

data class CamundaDeploymentSource @JvmOverloads constructor(
    val skipProcessLinksCopy: Boolean = false,
    val originalVersionTag: String? = null,
    val originalProcessDefinitionId: String? = null
) {
    override fun toString(): String {
        val parts = mutableListOf<String>()
        if (skipProcessLinksCopy) parts.add("skipProcessLinksCopy=true")
        if (!originalVersionTag.isNullOrBlank()) parts.add("originalVersionTag=$originalVersionTag")
        if (!originalProcessDefinitionId.isNullOrBlank()) parts.add("originalProcessDefinitionId=$originalProcessDefinitionId")
        return parts.joinToString("|")
    }

    companion object {
        @JvmStatic
        fun fromString(serialized: String?): CamundaDeploymentSource {
            if (serialized.isNullOrBlank()) {
                return CamundaDeploymentSource()
            }

            val props = serialized.split("|")
                .mapNotNull {
                    val (key, value) = it.split("=", limit = 2).let { pair ->
                        pair.getOrNull(0)?.trim() to pair.getOrNull(1)?.trim()
                    }
                    if (key != null && value != null) key to value else null
                }.toMap()

            return CamundaDeploymentSource(
                skipProcessLinksCopy = props["skipProcessLinksCopy"]?.toBoolean() ?: false,
                originalVersionTag = props["originalVersionTag"],
                originalProcessDefinitionId = props["originalProcessDefinitionId"]
            )
        }
    }
}