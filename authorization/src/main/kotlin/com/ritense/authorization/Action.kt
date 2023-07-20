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

package com.ritense.authorization

import com.fasterxml.jackson.annotation.JsonView
import com.ritense.authorization.permission.PermissionView
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class Action<T>(
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    @Column(name = "action")
    val key: String
) {
    companion object {
        @JvmStatic
        fun <T> deny(): Action<T>{
            return Action(DENY)
        }

        // common action keys are defined here
        const val VIEW_LIST = "view_list"
        const val VIEW = "view"
        const val START = "start"
        const val CREATE = "create"
        const val MODIFY = "modify"
        const val DELETE = "delete"
        const val COMPLETE = "complete"
        const val ASSIGN = "assign"
        const val CLAIM = "claim"
        const val ASSIGNABLE = "assignable" //TODO: re-evaluate if this is the way to go
        const val IGNORE = "ignore"
        const val DENY = "deny"
    }
}