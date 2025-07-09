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

package com.ritense.valtimo.operaton.repository

import com.ritense.valtimo.operaton.domain.OperatonBytearray
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

class OperatonByteArraySpecificationHelper {

    companion object {

        const val ID: String = "id"
        const val REVISION: String = "revision"
        const val NAME: String = "name"
        const val DEPLOYMENT_ID: String = "deploymentId"
        const val BYTES: String = "bytes"
        const val GENERATED: String = "generated"
        const val TENANT_ID: String = "tenantId"
        const val TYPE: String = "type"
        const val CREATE_TIME: String = "createTime"
        const val ROOT_PROCESS_INSTANCE_ID: String = "rootProcessInstanceId"
        const val REMOVAL_TIME: String = "removalTime"

        @JvmStatic
        fun byId(id: String) = Specification<OperatonBytearray> { root, _, cb ->
            cb.equal(root.get<Any>(ID), id)
        }

        @JvmStatic
        fun byRevision(revision: Int) = Specification<OperatonBytearray> { root, _, cb ->
            cb.equal(root.get<Any>(REVISION), revision)
        }

        @JvmStatic
        fun byName(name: String) = Specification<OperatonBytearray> { root, _, cb ->
            cb.equal(root.get<Any>(NAME), name)
        }

        @JvmStatic
        fun byDeploymentId(deploymentId: String) = Specification<OperatonBytearray> { root, _, cb ->
            cb.equal(root.get<Any>(DEPLOYMENT_ID), deploymentId)
        }

        @JvmStatic
        fun byBytes(bytes: ByteArray) = Specification<OperatonBytearray> { root, _, cb ->
            cb.equal(root.get<Any>(BYTES), bytes)
        }

        @JvmStatic
        fun byTenantId(tenantId: String) = Specification<OperatonBytearray> { root, _, cb ->
            cb.equal(root.get<Any>(TENANT_ID), tenantId)
        }

        @JvmStatic
        fun byType(type: Int) = Specification<OperatonBytearray> { root, _, cb ->
            cb.equal(root.get<Any>(TYPE), type)
        }

        @JvmStatic
        fun byCreateTime(createTime: LocalDateTime) = Specification<OperatonBytearray> { root, _, cb ->
            cb.equal(root.get<Any>(CREATE_TIME), createTime)
        }

        @JvmStatic
        fun byRootProcessInstanceId(rootProcessInstanceId: String) = Specification<OperatonBytearray> { root, _, cb ->
            cb.equal(root.get<Any>(ROOT_PROCESS_INSTANCE_ID), rootProcessInstanceId)
        }

        @JvmStatic
        fun byRemovalTime(removalTime: LocalDateTime) = Specification<OperatonBytearray> { root, _, cb ->
            cb.equal(root.get<Any>(REMOVAL_TIME), removalTime)
        }
    }
}