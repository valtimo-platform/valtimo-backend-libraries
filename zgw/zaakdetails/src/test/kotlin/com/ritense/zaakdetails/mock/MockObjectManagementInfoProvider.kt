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

package com.ritense.zaakdetails.mock

import com.ritense.objectenapi.management.ObjectManagementInfo
import com.ritense.objectenapi.management.ObjectManagementInfoProvider
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MockObjectManagementInfoProvider : ObjectManagementInfoProvider {

    override fun getObjectManagementInfo(objectManagementId: UUID): ObjectManagementInfo {
        return MockObjectManagement(
            id = UUID.fromString("462ef788-f7db-4701-9b87-0400fc79ad7e"),
            title = "Profile object",
            objectenApiPluginConfigurationId = UUID.fromString("b6d83348-97e7-4660-bd35-2e5fcc9629b4"),
            objecttypeId = "f46de37a-3a17-4c89-8cb7-dd596ea1dcc2",
            objecttypenApiPluginConfigurationId = UUID.fromString("4021bb75-18c8-4ca5-8658-b9f9c728bba0"),
        )
    }
}