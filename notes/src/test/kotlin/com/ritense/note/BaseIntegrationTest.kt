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

package com.ritense.note

import com.ritense.authorization.Action
import com.ritense.authorization.PermissionRepository
import com.ritense.authorization.Role
import com.ritense.authorization.RoleRepository
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.note.domain.Note
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID
import javax.inject.Inject

@SpringBootTest
@ExtendWith(SpringExtension::class, LiquibaseRunnerExtension::class)
@Tag("integration")
abstract class BaseIntegrationTest {

    @MockBean
    lateinit var userManagementService: UserManagementService

    @MockBean
    lateinit var mailSender: MailSender

    @Inject
    lateinit var roleRepository: RoleRepository

    @Inject
    lateinit var permissionRepository: PermissionRepository

    @BeforeEach
    fun beforeEachBase() {
        roleRepository.save(Role(FULL_ACCESS_ROLE))

        val permissions: List<Permission> = listOf(
            Permission(
                UUID.randomUUID(),
                Note::class.java,
                Action.VIEW,
                ConditionContainer(listOf()),
                FULL_ACCESS_ROLE
            )
        )
        permissionRepository.saveAllAndFlush(permissions)
    }

    companion object {
        const val FULL_ACCESS_ROLE = "FULL_ACCESS_ROLE"
    }
}
