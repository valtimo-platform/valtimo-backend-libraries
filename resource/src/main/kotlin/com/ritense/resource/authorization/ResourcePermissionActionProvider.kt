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

package com.ritense.resource.authorization

import com.ritense.authorization.Action
import com.ritense.authorization.ResourceActionProvider

class ResourcePermissionActionProvider : ResourceActionProvider<ResourcePermission> {
    override fun getAvailableActions(): List<Action<ResourcePermission>> {
        return listOf(VIEW, VIEW_LIST, CREATE, MODIFY, DELETE)
    }

    companion object {
        var VIEW = Action<ResourcePermission>(Action.VIEW)
        var VIEW_LIST = Action<ResourcePermission>(Action.VIEW_LIST)
        var CREATE = Action<ResourcePermission>(Action.CREATE)
        var MODIFY = Action<ResourcePermission>(Action.MODIFY)
        var DELETE = Action<ResourcePermission>(Action.DELETE)
    }
}
