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

package com.ritense.objectsapi.domain.request

import java.util.UUID

data class HandleNotificationRequest (
    val kanaal: String,
    val resourceUrl: String,
    val actie: String,
    val kenmerken: Map<String, String>
) {
    fun getObjectId(): UUID {
        return UUID.fromString(resourceUrl.substringAfterLast('/'))
    }

    fun isCreateNotification(): Boolean {
        return actie.equals(CREATE_ACTION_TYPE)
    }

    fun isUpdateNotification(): Boolean {
        return actie.equals(UPDATE_ACTION_TYPE)
    }

    fun isTestNotification(): Boolean {
        return kanaal.equals(TEST_KANAAL_NAME)
    }

    fun getObjectTypeUrl(): String? {
        return kenmerken[OBJECTTYPE_URL_KEY]
    }

    companion object {
        const val CREATE_ACTION_TYPE = "create"
        const val UPDATE_ACTION_TYPE = "update"
        const val TEST_KANAAL_NAME = "test"
        const val OBJECTTYPE_URL_KEY = "objectType"
    }
}