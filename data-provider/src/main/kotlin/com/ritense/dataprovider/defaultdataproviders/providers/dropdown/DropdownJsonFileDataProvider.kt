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

package com.ritense.dataprovider.defaultdataproviders.providers.dropdown

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.dataprovider.domain.DataProvider
import com.ritense.dataprovider.domain.DataProviderConstants
import com.ritense.dataprovider.domain.DataProviderConstants.Companion.DROPDOWN_CATEGORY
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import java.io.FileNotFoundException

class DropdownJsonFileDataProvider(
    private val resourceLoader: ResourceLoader,
    private val objectMapper: ObjectMapper,
) : DataProvider<Map<String, String>> {

    override fun supportsCategory(category: String) = category == DROPDOWN_CATEGORY

    override fun get(query: Map<String, Any>): Map<String, String>? {
        val key = DataProviderConstants.getQueryKey(query)
        return try {
            val resource = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResource("classpath:config/dropdown/$key.json")

            objectMapper.readValue(resource.inputStream)
        } catch (e: FileNotFoundException) {
            null
        }
    }
}
