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

package com.ritense.objectmanagement.service

import com.ritense.objectenapi.web.rest.ObjectManagementProvider
import java.util.UUID

class ObjectManagementProviderImpl(
    private val objectManagementService: ObjectManagementService
) : ObjectManagementProvider {

    override fun getObjectManagementInfo(objectManagementId: UUID): HashMap<String, Any> {
        val objectManagement = objectManagementService.getById(objectManagementId)
            ?: throw Exception("No uuid defined!")

        return hashMapOf(
            "objectenApiPluginConfigurationId" to objectManagement.objectenApiPluginConfigurationId,
            "objecttypenApiPluginConfigurationId" to objectManagement.objecttypenApiPluginConfigurationId,
            "objecttypeId" to objectManagement.objecttypeId,
            "version" to objectManagement.version
        )
    }

}
