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

package com.ritense.dataprovider.service

import com.ritense.dataprovider.domain.DataProvider
import com.ritense.dataprovider.exception.ProviderNotFoundException

class DataProviderService(
    private val dataProviders: Map<String, DataProvider<*>>
) {

    fun getProviderNames(category: String): List<String> {
        return dataProviders.entries.filter { it.value.supportsCategory(category) }.map { it.key }
    }

    fun <T> getAll(category: String, providerName: String?, query: Map<String, Any>): List<T> {
        return getDataProviders<T>(category, providerName)
            .mapNotNull { provider -> provider.get(query) }
    }

    fun <T> getSingle(category: String, providerName: String?, query: Map<String, Any>): T? {
        val dataList = getAll<T>(category, providerName, query)
        return if (dataList.isEmpty()) {
            null
        } else if (dataList.size == 1) {
            dataList[0]
        } else {
            throw IllegalStateException("Multiple DataProviders can provide data for category $category.")
        }
    }

    fun <T> postData(category: String, providerName: String?, query: Map<String, Any>, data: T?) {
        getDataProviders<T>(category, providerName).forEach { provider ->
            if (provider.post(query, data)) {
                return
            }
        }
        throw UnsupportedOperationException("Failed to post value for category: $category")
    }

    fun <T> deleteData(category: String, providerName: String?, query: Map<String, Any>, data: T?) {
        getDataProviders<T>(category, providerName).forEach { provider ->
            if (provider.delete(query, data)) {
                return
            }
        }
        throw UnsupportedOperationException("Failed to delete value for category: $category")
    }

    private fun <T> getDataProviders(category: String, providerName: String? = null): List<DataProvider<T>> {
        return if (providerName != null) {
            val provider = dataProviders[providerName] as DataProvider<T>?
            if (provider == null || !provider.supportsCategory(category)) {
                throw ProviderNotFoundException(providerName, category)
            }
            listOf(provider)
        } else {
            dataProviders.values.filter { it.supportsCategory(category) } as List<DataProvider<T>>
        }
    }

}
