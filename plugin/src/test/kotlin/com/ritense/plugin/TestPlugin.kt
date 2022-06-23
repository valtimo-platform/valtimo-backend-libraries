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
import com.ritense.plugin.domain.ActivityType.SERVICE_TASK
import com.ritense.plugin.domain.ActivityType.USER_TASK

@Plugin(
    key = "test-plugin",
    title = "Test plugin",
    description = "This is a test plugin used to verify plugin framework functionality"
)
class TestPlugin : TestPluginParent() {

    @PluginAction(
        key = "test-action",
        title = "Test action",
        description = "This is an action used to verify plugin framework functionality",
        activityTypes = [USER_TASK]
    )
    fun testAction(){
        //do nothing
        shouldNotBeDeployed()
    }

    @PluginAction(
        key = "other-test-action",
        title = "Test action 2",
        description = "This is an action used to test method overloading",
        activityTypes = [USER_TASK, SERVICE_TASK]
    )
    fun testAction(someString: String){
        //do nothing
        shouldAlsoNotBeDeployed()
    }

    @PluginAction(
        key = "child-override-test-action",
        title = "Override test action",
        description = "This is an action used to test method inheritance",
        activityTypes = [SERVICE_TASK]
    )
    override fun overrideAction(){
        //do nothing
    }

    private fun shouldNotBeDeployed(){
        //meant to test correct deployment of only methods annotated correctly
    }

    fun shouldAlsoNotBeDeployed(){
        //meant to test correct deployment of only methods annotated correctly
    }
}