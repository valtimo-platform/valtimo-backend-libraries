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

package com.ritense.objectmanagement

import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.domain.ActivityType
import java.net.URI
import org.springframework.http.HttpStatus

@Plugin(
    key = "objectmanagement",
    title = "Object Management",
    description = "Provides object-management plugin actions for process-links"
)
class ObjectManagementPlugin {

    @PluginAction(
        key = "create-object",
        title = "Create object",
        description = "Create an object by Object Management",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun createObject(objectRequest: ObjectRequest): ObjectWrapper {
        TODO("Not implemented!")
    }

    @PluginAction(
        key = "get-object",
        title = "Get object",
        description = "Get an object by Object Management",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun getObject(objectUrl: URI): ObjectWrapper {
        TODO("Not implemented!")
    }

    @PluginAction(
        key = "update-object",
        title = "Update object",
        description = "Update an object by Object Management",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun updateObject(objectUrl: URI, objectRequest: ObjectRequest): ObjectWrapper {
        TODO("Not implemented!")
    }

    @PluginAction(
        key = "delete-object",
        title = "Delete object",
        description = "Delete an object by Object Management",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun deleteObject(@PluginActionProperty objectUrl: URI): HttpStatus {
        TODO("Not implemented!")
    }
}