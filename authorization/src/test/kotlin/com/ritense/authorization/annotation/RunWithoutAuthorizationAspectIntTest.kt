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

package com.ritense.authorization.annotation

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.BaseIntegrationTest
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.testimpl.TestBean
import com.ritense.authorization.testimpl.TestEntity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException

class RunWithoutAuthorizationAspectIntTest @Autowired constructor(
    private val authorizationService: AuthorizationService,
    private val testBean: TestBean
) : BaseIntegrationTest() {

    @Test
    fun `should not throw error because of @RunWithoutAuthorization`() {
        testBean.deny()
    }

    @Test
    @RunWithoutAuthorization
    fun `Unfortunately, @RunWithoutAuthorization doesn't work on tests`() {
        assertThrows<AccessDeniedException> {
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    TestEntity::class.java,
                    Action.deny()
                )
            )
        }
    }

}
