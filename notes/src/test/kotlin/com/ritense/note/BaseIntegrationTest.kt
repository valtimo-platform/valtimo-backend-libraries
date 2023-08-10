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

import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.role.Role
import com.ritense.authorization.role.RoleRepository
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.condition.FieldPermissionCondition
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.condition.PermissionConditionOperator
import com.ritense.note.domain.Note
import com.ritense.note.service.NoteActionProvider.Companion.CREATE
import com.ritense.note.service.NoteActionProvider.Companion.DELETE
import com.ritense.note.service.NoteActionProvider.Companion.VIEW_LIST
import com.ritense.note.service.NoteActionProvider.Companion.MODIFY
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.servlet.config.annotation.EnableWebMvc
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


    @EnableWebMvc
    class TestConfiguration

    @BeforeEach
    fun beforeEachBase() {
        val role1 = roleRepository.save(Role(key = ADMIN))
        val role2 = roleRepository.save(Role(key = USER))

        val permissions: List<Permission> = listOf(
            Permission(
                UUID.randomUUID(),
                Note::class.java,
                VIEW_LIST,
                ConditionContainer(listOf()),
                role1
            ),
            Permission(
                resourceType = Note::class.java,
                action = CREATE,
                conditionContainer = ConditionContainer(listOf()),
                role = role2
            ),
            Permission(
                resourceType = Note::class.java,
                action = MODIFY,
                conditionContainer = ConditionContainer(
                    listOf(
                        FieldPermissionCondition("createdByUserId", PermissionConditionOperator.EQUAL_TO, "\${currentUserId}")
                    )
                ),
                role = role2
            ),
            Permission(
                resourceType = Note::class.java,
                action = DELETE,
                conditionContainer = ConditionContainer(
                    listOf(
                        FieldPermissionCondition("createdByUserId", PermissionConditionOperator.EQUAL_TO, "\${currentUserId}")
                    )
                ),
                role = role2
            ),
        )
        permissionRepository.saveAllAndFlush(permissions)
    }

    @AfterEach
    fun afterEachBase() {
        permissionRepository.deleteAll()
        roleRepository.deleteAll()
    }

    companion object {
        const val ADMIN = "ADMIN"
        const val USER = "USER"
    }
}
