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

package com.ritense.authorization.autoconfigure

import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.databind.module.SimpleModule
import com.ritense.authorization.permission.ContainerPermissionCondition
import com.ritense.authorization.permission.ExpressionPermissionCondition
import com.ritense.authorization.permission.FieldPermissionCondition

class PermissionConditionTypeModule: SimpleModule() {

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        context.registerSubtypes(
            NamedType(ContainerPermissionCondition::class.java),
            NamedType(ExpressionPermissionCondition::class.java),
            NamedType(FieldPermissionCondition::class.java)
        )
    }
}