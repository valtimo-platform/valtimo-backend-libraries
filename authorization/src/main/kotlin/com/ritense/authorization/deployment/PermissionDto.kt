/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
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

import com.ritense.authorization.Action
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.PermissionCondition

data class PermissionDto(
    val resourceType: Class<*>,
    val action: String,
    val conditionContainer: ConditionContainerDto?,
    val roleKey: String
) {
    fun toPermission() = Permission(
        resourceType = resourceType,
        action = Action<Any>(action),
        conditionContainer = (conditionContainer ?: ConditionContainerDto()).toConditionContainer(),
        roleKey = roleKey
    )
}

data class ConditionContainerDto(
    val conditions: List<PermissionCondition> = emptyList()
) {
    fun toConditionContainer() = ConditionContainer(conditions = conditions)
}