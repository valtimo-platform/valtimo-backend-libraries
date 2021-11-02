/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.web.rest.result

import com.ritense.objectsapi.domain.sync.ObjectSyncConfig
import com.ritense.valtimo.contract.result.OperationError

class CreateObjectSyncConfigResultFailed(
    val errors: List<OperationError>
) : CreateObjectSyncConfigResult {

    override fun objectSyncConfig(): ObjectSyncConfig? {
        return null
    }

    override fun errors(): List<OperationError> {
        return errors
    }
}