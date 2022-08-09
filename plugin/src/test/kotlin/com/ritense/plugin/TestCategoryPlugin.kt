/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.plugin

import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType.SERVICE_TASK

@Plugin(
    key = "test-category-plugin",
    title = "Test category plugin",
    description = "This is a test plugin used to verify category plugin framework functionality"
)
class TestCategoryPlugin(): TestPluginCategory {
    @PluginAction(
        key = "test-category action",
        title = "Test category action",
        description = "This is an action used to verify category plugin framework functionality",
        activityTypes = [SERVICE_TASK]
    )
    fun testAction() {
        //do nothing
    }
}
