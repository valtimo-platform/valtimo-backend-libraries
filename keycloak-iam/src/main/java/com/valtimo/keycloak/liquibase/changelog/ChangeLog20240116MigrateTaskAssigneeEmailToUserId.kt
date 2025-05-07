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

package com.valtimo.keycloak.liquibase.changelog

import com.ritense.valtimo.contract.config.ValtimoProperties.IdentifierField
import com.ritense.valtimo.contract.config.ValtimoProperties.IdentifierField.USERID
import com.ritense.valtimo.contract.config.ValtimoProperties.IdentifierField.USERNAME
import io.github.oshai.kotlinlogging.KotlinLogging
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl
import org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.utils.EmailValidationUtil
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment

class ChangeLog20240116MigrateTaskAssigneeEmailToUserId : CustomTaskChange, EnvironmentPostProcessor {

    private val identifierField by lazy { IdentifierField.fromString(environment.getProperty("valtimo.oauth.identifier-field", USERID.toString())) }

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        Companion.environment = environment
    }

    override fun execute(database: Database) {
        logger.info("Starting ${this::class.simpleName}")

        val connection = database.connection as JdbcConnection
        val statement = connection.prepareStatement("SELECT id_,assignee_ FROM act_ru_task")
        val result = statement.executeQuery()

        while (result.next()) {
            val taskId = result.getString("id_")
            val taskAssigneeEmail = result.getString("assignee_")
            if (taskAssigneeEmail != null) {
                if (!EmailValidationUtil.isValidEmail(taskAssigneeEmail)) {
                    logger.error { "Failed to migrate task assignee. Invalid email: '$taskAssigneeEmail' for task '$taskId'" }
                } else {
                    try {
                        val assigneeValue = getKeycloakUserIdByEmail(taskAssigneeEmail)
                        updateTaskInDatabase(connection, taskId, assigneeValue)
                    } catch (_: KeycloakUserNotFoundException) {
                        logger.error { "Could not find user for task '$taskId'. Unknown email: '$taskAssigneeEmail'." +
                            "Unassigning user from task." }
                        updateTaskInDatabase(connection, taskId, null)
                    } catch (ex: Exception) {
                        logger.error(ex) { "Something went wrong when updating assignee for task '$taskId'. Aborting task update."}
                    }
                }
            }
        }
        logger.info("Finished ${this::class.simpleName}")
    }

    override fun getConfirmationMessage(): String {
        return "${this::class.simpleName} executed"
    }

    override fun setUp() {
        // This interface method is not needed
    }

    override fun setFileOpener(resourceAccessor: ResourceAccessor?) {
        // This interface method is not needed
    }

    override fun validate(database: Database?): ValidationErrors {
        return ValidationErrors()
    }

    private fun getKeycloakUserIdByEmail(email: String): String {
        val user = keycloak().use { keycloak ->
            keycloak.realm(getProperty(KEYCLOAK_REALM_PROPERTY)).users()
                .search(null, null, null, email, 0, 1, true, true)
        }.firstOrNull { it.email == email }

        if(user == null) {
            throw KeycloakUserNotFoundException(email)
        }

        return when(identifierField) {
            USERID -> user.id
            USERNAME -> user.username
        }
    }

    private fun keycloak(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(getProperty(KEYCLOAK_AUTH_SERVER_URL_PROPERTY))
            .realm(getProperty(KEYCLOAK_REALM_PROPERTY))
            .grantType(CLIENT_CREDENTIALS)
            .clientId(getProperty(KEYCLOAK_RESOURCE_PROPERTY))
            .clientSecret(getProperty(KEYCLOAK_SECRET_PROPERTY))
            .resteasyClient(
                ResteasyClientBuilderImpl()
                    .connectionPoolSize(10).build()
            )
            .build()
    }

    private fun updateTaskInDatabase(connection: JdbcConnection, taskId: String, taskAssignee: String?) {
        val statement = connection.prepareStatement("UPDATE act_ru_task SET assignee_ = ? WHERE id_ = ?")
        statement.setString(1, taskAssignee)
        statement.setString(2, taskId)
        statement.executeUpdate()
    }

    private fun getProperty(name: String): String {
        return environment.getProperty(name)!!
    }

    private class KeycloakUserNotFoundException(email: String) : RuntimeException("No Keycloak user found with email: '$email'")


    companion object {
        private const val KEYCLOAK_AUTH_SERVER_URL_PROPERTY = "keycloak.auth-server-url"
        private const val KEYCLOAK_REALM_PROPERTY = "keycloak.realm"
        private const val KEYCLOAK_RESOURCE_PROPERTY = "keycloak.resource"
        private const val KEYCLOAK_SECRET_PROPERTY = "keycloak.credentials.secret"

        private val logger = KotlinLogging.logger {}

        private lateinit var environment: Environment
    }
}