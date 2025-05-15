/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.liquibase.changelog

import io.github.oshai.kotlinlogging.KotlinLogging
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor

class ChangeLog20250514MigrateProcessDefinitions : CustomTaskChange {
    override fun execute(database: Database) {
        logger.info { "Starting ${this::class.simpleName}" }

        val connection = database.connection as JdbcConnection
        val linkQuery = """
            SELECT
                document_definition_name,
                camunda_process_definition_key,
                can_initialize_document,
                document_definition_version,
                startable_by_user
            FROM
                camunda_process_json_schema_document_definition
        """.trimIndent()
        val statement = connection.prepareStatement(linkQuery)
        val result = statement.executeQuery()

        while (result.next()) {
            val taskId = result.getString("document_definition_name")
            val taskAssigneeEmail = result.getString("camunda_process_definition_key")
        }
        logger.info { "Finished ${this::class.simpleName}" }
    }

    private fun migrateProcessDefinition(
        connection: JdbcConnection,
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
        processDefinitionKey: String,
    ) {
        // since this can be called recursively, first check if the process has not been migrated already
        if (isProcDefMigrated(connection, caseDefinitionKey, caseDefinitionVersionTag, processDefinitionKey)) {
            return
        }

        val statement = connection.prepareStatement("UPDATE act_ru_task SET assignee_ = ? WHERE id_ = ?")
        statement.executeUpdate()
    }

    private fun isProcDefMigrated(
        connection: JdbcConnection,
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
        processDefinitionKey: String,
    ): Boolean {
        // since this can be called recursively, first check if the process has not been migrated already
        val existingProcDefQuery = """
            select count(*) as nr_of_process_definitions
            from process_definition_case_definition pdcd
            join
            where pdcd.
        """.trimIndent()

        val statement = connection.prepareStatement("UPDATE act_ru_task SET assignee_ = ? WHERE id_ = ?")
        //statement.setString(1, taskAssignee)
        return true
    }

    override fun getConfirmationMessage(): String {
        return "${this::class.simpleName} executed"
    }

    override fun setUp() {
        // This interface method is not needed
    }

    override fun setFileOpener(resourceAccessor: ResourceAccessor) {
        // This interface method is not needed
    }

    override fun validate(database: Database?): ValidationErrors {
        return ValidationErrors()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}