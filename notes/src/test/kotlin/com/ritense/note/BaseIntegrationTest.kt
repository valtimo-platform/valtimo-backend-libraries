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

import com.ritense.authorization.PermissionRepository
import com.ritense.authorization.Role
import com.ritense.authorization.RoleRepository
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.FieldPermissionCondition
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.PermissionConditionOperator
import com.ritense.note.domain.Note
import com.ritense.note.service.NoteActionProvider.Companion.CREATE
import com.ritense.note.service.NoteActionProvider.Companion.DELETE
import com.ritense.note.service.NoteActionProvider.Companion.LIST_VIEW
import com.ritense.note.service.NoteActionProvider.Companion.MODIFY
import com.ritense.note.service.NoteActionProvider.Companion.VIEW
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
import java.util.UUID
import javax.inject.Inject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.servlet.config.annotation.EnableWebMvc

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
        roleRepository.save(Role(ADMIN))
        roleRepository.save(Role(USER))

        val permissions: List<Permission> = listOf(
            Permission(
                UUID.randomUUID(),
                Note::class.java,
                LIST_VIEW,
                ConditionContainer(listOf()),
                ADMIN
            ),
            Permission(
                resourceType = Note::class.java,
                action = VIEW,
                conditionContainer = ConditionContainer(
                    listOf(
                        FieldPermissionCondition("createdByUserId", PermissionConditionOperator.EQUAL_TO, "\${currentUserId}")
                    )
                ),
                roleKey = USER
            ),
            Permission(
                resourceType = Note::class.java,
                action = CREATE,
                conditionContainer = ConditionContainer(listOf()),
                roleKey = USER
            ),
            Permission(
                resourceType = Note::class.java,
                action = MODIFY,
                conditionContainer = ConditionContainer(
                    listOf(
                        FieldPermissionCondition("createdByUserId", PermissionConditionOperator.EQUAL_TO, "\${currentUserId}")
                    )
                ),
                roleKey = USER
            ),
            Permission(
                resourceType = Note::class.java,
                action = DELETE,
                conditionContainer = ConditionContainer(
                    listOf(
                        FieldPermissionCondition("createdByUserId", PermissionConditionOperator.EQUAL_TO, "\${currentUserId}")
                    )
                ),
                roleKey = USER
            ),
        )
        permissionRepository.saveAllAndFlush(permissions)
    }

    companion object {
        const val ADMIN = "ADMIN"
        const val USER = "USER"
    }
}
