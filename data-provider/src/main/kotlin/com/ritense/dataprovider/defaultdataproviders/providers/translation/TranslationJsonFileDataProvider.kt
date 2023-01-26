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

package com.ritense.dataprovider.defaultdataproviders.providers.translation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jayway.jsonpath.JsonPath
import com.ritense.dataprovider.domain.DataProvider
import com.ritense.dataprovider.domain.DataProviderConstants
import com.ritense.dataprovider.domain.DataProviderConstants.Companion.TRANSLATION_CATEGORY
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import java.io.FileNotFoundException

class TranslationJsonFileDataProvider(
    private val resourceLoader: ResourceLoader,
    private val objectMapper: ObjectMapper,
) : DataProvider<Map<String, Any>> {

    override fun supportsCategory(category: String) = category == TRANSLATION_CATEGORY

    override fun get(query: Map<String, Any>): Map<String, Any>? {
        val key = DataProviderConstants.getQueryKey(query)
        val properties = DataProviderConstants.getQueryProperties(query)
        return getTranslations(key, properties)
    }

    private fun getTranslations(language: String, properties: List<String>?): Map<String, Any>? {
        return try {
            val resource = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResource("classpath:config/translation/$language.json")

            if (properties == null) {
                objectMapper.readValue(resource.inputStream)
            } else {
                val jsonFile = resource.inputStream.bufferedReader().use { it.readText() }
                properties.associateWith { property -> JsonPath.read(jsonFile, "$.$property") }
            }
        } catch (e: FileNotFoundException) {
            null
        }
    }
}
