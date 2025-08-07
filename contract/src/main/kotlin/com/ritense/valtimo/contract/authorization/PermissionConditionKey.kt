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

enum class PermissionConditionKey(val key: String) {
    CURRENT_USER_ID("\${currentUserId}"),
    CURRENT_USER_EMAIL("\${currentUserEmail}"),
    CURRENT_USER_ROLES("\${currentUserRoles}"),
    CURRENT_USER_NAME("\${currentUsername}"),
    @Deprecated("Use 'CURRENT_USER_NAME' instead")
    CURRENT_USER_IDENTIFIER("\${currentUserIdentifier}");

    companion object {
        private val map = entries.associateBy(PermissionConditionKey::key)

        fun fromKey(key: String): PermissionConditionKey? = map[key]

        fun isValidKey(key: String): Boolean = map.containsKey(key)
    }
}