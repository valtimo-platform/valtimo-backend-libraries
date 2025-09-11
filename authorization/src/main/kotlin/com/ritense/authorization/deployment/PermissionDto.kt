/*
 *  Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.authorization.deployment

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView
import com.ritense.authorization.Action
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.PermissionView
import com.ritense.authorization.permission.condition.PermissionCondition
import com.ritense.authorization.role.RoleRepository

data class PermissionDto(
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val resourceType: Class<*>,
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    @field:JsonAlias("action") // accept "action" as an alias for "actions"
    @field:JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val actions: List<String>,
//    @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val conditions: List<PermissionCondition> = emptyList(),
    @field:JsonView(PermissionView.PermissionManagement::class)
    val roleKey: String,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val contextResourceType: Class<*>? = null,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonView(value = [PermissionView.RoleManagement::class, PermissionView.PermissionManagement::class])
    val contextConditions: List<PermissionCondition> = emptyList(),
) {
    fun toPermission(roleRepository: RoleRepository) = Permission(
        resourceType = resourceType,
        actions = actions.map { Action<Any>(it) }.toMutableList(),
        conditionContainer = ConditionContainer(conditions = conditions),
        role = roleRepository.findByKey(roleKey)!!,
        contextResourceType = contextResourceType,
        contextConditionContainer = ConditionContainer(conditions = contextConditions)
    )
}
