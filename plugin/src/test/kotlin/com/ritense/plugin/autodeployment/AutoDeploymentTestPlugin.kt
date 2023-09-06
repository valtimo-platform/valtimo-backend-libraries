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

package com.ritense.plugin.autodeployment

import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.valtimo.contract.validation.Url
import java.net.URI

@Plugin(
    key = "auto-deployment-test-plugin",
    title = "Auto deployment Test plugin",
    description = "This is a test plugin used to verify plugin auto deployment functionality"
)
class AutoDeploymentTestPlugin {
    @Url
    @PluginProperty(key = "property1", secret = false)
    lateinit var property1: URI

    @PluginProperty(key = "property2", required = false, secret = false)
    var property2: Boolean? = null

    @PluginProperty(key = "property3", secret = false)
    lateinit var property3: Number

    @PluginAction(
        key = "test-action-task",
        title = "Test action task",
        description = "This is an action used to verify plugin framework functionality",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun testActionTask() {
        //do nothing
    }

}
