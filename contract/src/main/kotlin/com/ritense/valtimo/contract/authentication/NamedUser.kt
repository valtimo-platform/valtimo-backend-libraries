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

package com.ritense.valtimo.contract.authentication

data class NamedUser(
    val id: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val userName: String?
) {

    constructor(id: String, firstName: String?, lastName: String?) : this(id, null, firstName, lastName, null)

    constructor(id: String, email: String?, firstName: String?, lastName: String?) : this(id, email, firstName, lastName, null)

    fun getLabel(): String {
        return if (!firstName.isNullOrBlank() && !lastName.isNullOrBlank()) {
            "$firstName $lastName"
        } else if (!firstName.isNullOrBlank()) {
            firstName
        } else if (!lastName.isNullOrBlank()) {
            lastName
        } else if (!email.isNullOrBlank()) {
            email
        } else {
            id
        }
    }

    companion object {
        @JvmStatic
        fun from(user: ManageableUser): NamedUser {
            return NamedUser(
                id = user.userIdentifier,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                userName = user.username
            )
        }
    }
}
