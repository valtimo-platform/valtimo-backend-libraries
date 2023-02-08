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

package com.ritense.objectenapi.management

import java.util.UUID

interface ObjectManagementInfo {
    val id: UUID
    val title: String
    val objecttypenApiPluginConfigurationId: UUID
    val objecttypeId: String
    val objecttypeVersion: Int
    val objectenApiPluginConfigurationId: UUID
    val showInDataMenu: Boolean
    val formDefinitionView: String?
    val formDefinitionEdit: String?
}