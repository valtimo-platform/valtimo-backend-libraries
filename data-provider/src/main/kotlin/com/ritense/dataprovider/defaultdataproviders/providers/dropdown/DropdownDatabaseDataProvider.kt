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

import com.ritense.dataprovider.defaultdataproviders.domain.DropdownList
import com.ritense.dataprovider.defaultdataproviders.repository.DropdownListRepository
import com.ritense.dataprovider.domain.DataProvider
import com.ritense.dataprovider.domain.DataProviderConstants
import com.ritense.dataprovider.domain.DataProviderConstants.Companion.DROPDOWN_CATEGORY
import org.springframework.data.repository.findByIdOrNull

class DropdownDatabaseDataProvider(
    private val dropdownListRepository: DropdownListRepository,
) : DataProvider<Map<String, String>> {

    override fun supportsCategory(category: String) = category == DROPDOWN_CATEGORY

    override fun get(query: Map<String, Any>): Map<String, String>? {
        val key = DataProviderConstants.getQueryKey(query)
        val dataEntry = dropdownListRepository.findByIdOrNull(key)
            ?: return null

        return dataEntry.values
    }

    override fun post(query: Map<String, Any>, data: Map<String, String>?): Boolean {
        val key = DataProviderConstants.getQueryKey(query)
        val append = DataProviderConstants.getQueryAppend(query)

        if (data.isNullOrEmpty() && !append) {
            dropdownListRepository.deleteById(key)
        }

        val dropdownList = dropdownListRepository.findByIdOrNull(key)

        if (dropdownList != null && append) {
            data!!.entries.forEach { newDropdownListItem ->
                dropdownList.values[newDropdownListItem.key] = newDropdownListItem.value
            }
            dropdownListRepository.save(dropdownList)
        } else {
            dropdownListRepository.save(DropdownList(key, data!!.toMutableMap()))
        }

        return true
    }

    override fun delete(query: Map<String, Any>, data: Map<String, String>?): Boolean {
        val key = DataProviderConstants.getQueryKey(query)
        val valueToDelete = DataProviderConstants.getQueryValue(query)
        if (valueToDelete == null) {
            dropdownListRepository.deleteById(key)
        } else {
            val dataEntry = dropdownListRepository.findByIdOrNull(key)
            if (dataEntry == null) {
                return false
            } else {
                dataEntry.values.remove(valueToDelete)
                if (dataEntry.values.isEmpty()) {
                    dropdownListRepository.deleteById(key)
                } else {
                    dropdownListRepository.save(dataEntry)
                }
            }

        }
        return true
    }

}
