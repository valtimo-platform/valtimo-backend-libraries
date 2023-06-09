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

package com.ritense.authorization.testimpl

import com.ritense.authorization.Action
import com.ritense.authorization.ResourceActionProvider

/**
 Used to test extensibility of framework. More than 1 ResourceActionProvider should be usable for the same entity type
 so that implementations can introduce new actions for existing enities.
 */
class CustomTestEntityActionProvider: ResourceActionProvider<TestEntity> {
    override fun getAvailableActions(): List<Action<TestEntity>> {
        return listOf(modify, custom)
    }

    companion object {
        val modify = Action<TestEntity>(Action.MODIFY)
        val custom = Action<TestEntity>("custom")
    }
}