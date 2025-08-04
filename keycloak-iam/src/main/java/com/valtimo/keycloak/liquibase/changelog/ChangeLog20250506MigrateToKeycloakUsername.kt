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

import com.ritense.valtimo.contract.Constants.SYSTEM_ACCOUNT
import jakarta.ws.rs.NotFoundException
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl
import org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.utils.EmailValidationUtil
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import java.sql.ResultSet
import java.util.UUID

class ChangeLog20250506MigrateToKeycloakUsername : CustomTaskChange, EnvironmentPostProcessor {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        Companion.environment = environment
    }

    override fun execute(database: Database) {
        logger.info("Starting ${this::class.simpleName}")

        val connection = database.connection as JdbcConnection
        pingKeycloak()
        migrateJsonSchemaDocument(database, connection)
        migrateActRuTask(connection)
        migrateNote(database, connection)
        migrateActHiTask(connection)
        migrateUserSettings(connection)
        migrateIntermediateSubmission(database, connection)
        pingKeycloak()

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

    private fun migrateJsonSchemaDocument(database: Database, connection: JdbcConnection) {
        if (!checkTableExists(connection, "json_schema_document")) {
            return
        }
        val result = connection.prepareStatement("SELECT json_schema_document_id,assignee_Id FROM json_schema_document")
            .executeQuery()

        while (result.next()) {
            val documentId = getIdFromResultSet(database, result, "json_schema_document_id")
            val assignee = result.getString("assignee_Id")
            if (assignee != null) {
                try {
                    val assigneeUsername = getKeycloakUsername(assignee)
                    if (assignee != assigneeUsername) {
                        executeUpdate(
                            connection,
                            "UPDATE json_schema_document SET assignee_Id = ? WHERE json_schema_document_id = ?",
                            assigneeUsername,
                            documentId
                        )
                    }
                } catch (_: KeycloakUserNotFoundException) {
                    logger.error {
                        "Failed to migrate json_schema_document '$documentId'. Unknown assignee: '$assignee'. Unassigning user from json_schema_document."
                    }
                    executeUpdate(
                        connection,
                        "UPDATE json_schema_document SET assignee_Id = ? WHERE json_schema_document_id = ?",
                        null,
                        documentId
                    )
                } catch (ex: Exception) {
                    logger.error(ex) { "Failed to migrate json_schema_document '$documentId' for assignee: '$assignee'. Skipping json_schema_document update." }
                }
            }
        }
    }

    private fun migrateActRuTask(connection: JdbcConnection) {
        if (!checkTableExists(connection, "act_ru_task")) {
            return
        }
        val result = connection.prepareStatement("SELECT id_,assignee_ FROM act_ru_task").executeQuery()

        while (result.next()) {
            val taskId = result.getString("id_")
            val assignee = result.getString("assignee_")
            if (assignee != null) {
                try {
                    val assigneeUsername = getKeycloakUsername(assignee)
                    if (assignee != assigneeUsername) {
                        executeUpdate(
                            connection, "UPDATE act_ru_task SET assignee_ = ? WHERE id_ = ?",
                            assigneeUsername, taskId
                        )
                    }
                } catch (_: KeycloakUserNotFoundException) {
                    logger.error {
                        "Failed to migrate act_ru_task '$taskId'. Unknown assignee: '$assignee'. Unassigning user from act_ru_task."
                    }
                    executeUpdate(connection, "UPDATE act_ru_task SET assignee_ = ? WHERE id_ = ?", null, taskId)
                } catch (ex: Exception) {
                    logger.error(ex) { "Failed to migrate act_ru_task '$taskId' for assignee: '$assignee'. Skipping act_ru_task update." }
                }
            }
        }
    }

    private fun migrateNote(database: Database, connection: JdbcConnection) {
        if (!checkTableExists(connection, "note")) {
            return
        }
        val result = connection.prepareStatement("SELECT id,created_by_user_id FROM note").executeQuery()

        while (result.next()) {
            val noteId = getIdFromResultSet(database, result, "id")
            val creator = result.getString("created_by_user_id")
            if (creator != null) {
                try {
                    val creatorUsername = getKeycloakUsername(creator)
                    if (creator != creatorUsername) {
                        executeUpdate(
                            connection, "UPDATE note SET created_by_user_id = ? WHERE id = ?",
                            creatorUsername, noteId
                        )
                    }
                } catch (_: KeycloakUserNotFoundException) {
                    logger.error { "Failed to migrate note '$noteId'. Unknown creator: '$creator'. Skipping note update." }
                } catch (ex: Exception) {
                    logger.error(ex) { "Failed to migrate note '$noteId' for assignee: '$creator'. Skipping note update." }
                }
            }
        }
    }

    private fun migrateActHiTask(connection: JdbcConnection) {
        if (!checkTableExists(connection, "act_hi_taskinst")) {
            return
        }
        val result = connection.prepareStatement("SELECT id_,assignee_ FROM act_hi_taskinst").executeQuery()

        while (result.next()) {
            val taskId = result.getString("id_")
            val assignee = result.getString("assignee_")
            if (assignee != null) {
                try {
                    val assigneeUsername = getKeycloakUsername(assignee)
                    if (assignee != assigneeUsername) {
                        executeUpdate(
                            connection, "UPDATE act_hi_taskinst SET assignee_ = ? WHERE id_ = ?",
                            assigneeUsername, taskId
                        )
                    }
                } catch (_: KeycloakUserNotFoundException) {
                    logger.error { "Failed to migrate act_hi_taskinst '$taskId'. Unknown assignee: '$assignee'. Skipping act_hi_taskinst update." }
                } catch (ex: Exception) {
                    logger.error(ex) { "Failed to migrate act_hi_taskinst '$taskId' for assignee: '$assignee'. Skipping act_hi_taskinst update." }
                }
            }
        }
    }

    private fun migrateUserSettings(connection: JdbcConnection) {
        if (!checkTableExists(connection, "user_settings")) {
            return
        }
        val result = connection.prepareStatement("SELECT user_id,settings FROM user_settings").executeQuery()

        while (result.next()) {
            val userId = result.getString("user_id")
            val settings = result.getObject("settings")
            try {
                val username = getKeycloakUsername(userId)
                if (userId != username) {
                    executeUpdate(connection, "INSERT INTO user_settings VALUES (?,?)", username, settings)
                    executeUpdate(connection, "DELETE FROM user_settings WHERE user_id = ?", userId)
                }
            } catch (_: KeycloakUserNotFoundException) {
                logger.error { "Failed to migrate user_settings. Unknown user: '$userId'. Skipping user_settings update." }
            } catch (ex: Exception) {
                logger.error(ex) { "Failed to migrate user_settings for user '$userId'. Skipping user_settings update." }
            }
        }
    }

    private fun migrateIntermediateSubmission(database: Database, connection: JdbcConnection) {
        if (!checkTableExists(connection, "intermediate_submission")) {
            return
        }
        val result = connection.prepareStatement("SELECT id,created_by,edited_by FROM intermediate_submission")
            .executeQuery()

        while (result.next()) {
            val id = getIdFromResultSet(database, result, "id")
            val creator = result.getString("created_by")
            val editor = result.getString("edited_by")
            try {
                val creatorUsername = try {
                    getKeycloakUsername(creator)
                } catch (_: KeycloakUserNotFoundException) {
                    logger.error { "Failed to migrate intermediate_submission '$id'. Unknown creator: '$creator'. Skipping intermediate_submission.created_by update." }
                    creator
                }
                val editorUsername = try {
                    getKeycloakUsername(editor)
                } catch (_: KeycloakUserNotFoundException) {
                    logger.error { "Failed to migrate intermediate_submission '$id'. Unknown editor: '$editor'. Skipping intermediate_submission.edited_by update." }
                    editor
                }
                if (creator != creatorUsername || editor != editorUsername) {
                    executeUpdate(
                        connection, "UPDATE intermediate_submission SET created_by = ?, edited_by = ? WHERE id = ?",
                        creatorUsername, editorUsername, id
                    )
                }
            } catch (ex: Exception) {
                logger.error(ex) { "Failed to migrate intermediate_submission '$id' for creator '$creator' and editor '$editor'. Skipping intermediate_submission update." }
            }
        }
    }

    private fun checkTableExists(connection: JdbcConnection, tableName: String): Boolean {
        val schemaOrDatabaseName = if (connection.databaseProductName == "PostgreSQL") {
            getDatabaseSchema(connection)
        } else {
            connection.catalog
        }

        val result = connection.prepareStatement(
            """
            SELECT EXISTS (
                SELECT 1
                FROM information_schema.tables
                WHERE table_schema = '$schemaOrDatabaseName'
                  AND table_name = '$tableName'
            );
        """.trimIndent()
        ).executeQuery()
        result.next()
        return result.getBoolean(1)
    }

    private fun getDatabaseSchema(connection: JdbcConnection): String {
        val result = connection.prepareStatement("SELECT current_schema()").executeQuery()
        result.next()
        return result.getString(1)
    }

    private fun executeUpdate(connection: JdbcConnection, sql: String, vararg params: Any?): Int {
        val statement = connection.prepareStatement(sql)
        params.forEachIndexed { i, param ->
            val parameterIndex = i + 1
            when (param) {
                is String -> statement.setString(parameterIndex, param)
                null -> statement.setString(parameterIndex, null)
                else -> statement.setObject(parameterIndex, param)
            }
        }
        return statement.executeUpdate()
    }

    private fun getKeycloakUsername(emailOrUsernameOrUserId: String?): String? {
        if (emailOrUsernameOrUserId == null) {
            return null
        } else if (emailOrUsernameOrUserId == SYSTEM_ACCOUNT) {
            return SYSTEM_ACCOUNT
        }

        val properties = keycloakProperties()

        keycloak().use { keycloak ->
            val keycloakRealmUsers = keycloak.realm(properties.realm).users()
            if (EmailValidationUtil.isValidEmail(emailOrUsernameOrUserId)) {
                return keycloakRealmUsers
                    .searchByEmail(emailOrUsernameOrUserId, true)
                    .maxByOrNull { it.isEnabled || it.isEmailVerified }
                    ?.username
                    ?: throw KeycloakUserNotFoundException()
            }

            return keycloakRealmUsers
                .searchByUsername(emailOrUsernameOrUserId, true)
                .maxByOrNull { it.isEnabled || it.isEmailVerified }
                ?.username
                ?: try {
                    keycloakRealmUsers[emailOrUsernameOrUserId].toRepresentation().username
                } catch (_: NotFoundException) {
                    throw KeycloakUserNotFoundException()
                }
        }
    }

    private fun pingKeycloak() {
        keycloak().serverInfo().info
    }

    /** Logic was copied from `KeycloakService.keycloak()` */
    private fun keycloak(): Keycloak {
        val properties = keycloakProperties()
        return KeycloakBuilder.builder()
            .serverUrl(properties.authServerUrl)
            .realm(properties.realm)
            .grantType(CLIENT_CREDENTIALS)
            .clientId(properties.resource)
            .clientSecret(properties.credentials["secret"] as String?)
            .resteasyClient(
                ResteasyClientBuilderImpl()
                    .connectionPoolSize(10).build()
            )
            .build()
    }

    /** Logic was copied from `ValtimoKeycloakPropertyResolver.resolveProperties()` */
    private fun keycloakProperties(): KeycloakSpringBootProperties {
        val issuerUri = environment.getProperty(OAUTH2_ISSUER_URI)
            ?: return KeycloakSpringBootProperties(
                environment.getProperty(KEYCLOAK_REALM_PROPERTY),
                environment.getProperty(KEYCLOAK_AUTH_SERVER_URL_PROPERTY),
                environment.getProperty(KEYCLOAK_RESOURCE_PROPERTY),
                mapOf("secret" to environment.getProperty(KEYCLOAK_SECRET_PROPERTY))
            )

        val clientId = environment.getProperty(OAUTH2_CLIENT_ID)
        val secret = environment.getProperty(OAUTH2_CLIENT_SECRET)
        val (authServerUrl, realm) = issuerUri.split("realms/".toRegex())
        return KeycloakSpringBootProperties(
            realm,
            authServerUrl,
            clientId,
            mapOf("secret" to secret)
        )
    }

    private fun getIdFromResultSet(
        database: Database,
        results: ResultSet,
        columnName: String
    ): UUID? {
        return if (database.databaseProductName == "MySQL") {
            val bytesResult = results.getBytes(columnName)
            bytesResult?.let { UUID.nameUUIDFromBytes(it) }
        } else {
            val stringResult = results.getString(columnName)
            stringResult?.let { UUID.fromString(it) }
        }
    }

    private class KeycloakUserNotFoundException() : RuntimeException("Failed to find Keycloak user")

    companion object {
        private const val KEYCLOAK_AUTH_SERVER_URL_PROPERTY = "keycloak.auth-server-url"
        private const val KEYCLOAK_REALM_PROPERTY = "keycloak.realm"
        private const val KEYCLOAK_RESOURCE_PROPERTY = "keycloak.resource"
        private const val KEYCLOAK_SECRET_PROPERTY = "keycloak.credentials.secret"
        private const val OAUTH2_ISSUER_URI = "spring.security.oauth2.client.provider.keycloakapi.issuer-uri"
        private const val OAUTH2_CLIENT_ID = "spring.security.oauth2.client.registration.keycloakapi.client-id"
        private const val OAUTH2_CLIENT_SECRET = "spring.security.oauth2.client.registration.keycloakapi.client-secret"

        private val logger = KotlinLogging.logger {}

        private lateinit var environment: Environment
    }
}