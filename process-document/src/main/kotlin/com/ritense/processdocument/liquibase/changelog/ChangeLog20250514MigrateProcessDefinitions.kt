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

import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaProcessService.CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX
import io.github.oshai.kotlinlogging.KotlinLogging
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask
import org.camunda.bpm.model.bpmn.instance.CallActivity
import org.camunda.bpm.model.bpmn.instance.Process
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import java.util.function.Consumer

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
            val caseDefinitionKey = result.getString("document_definition_name")
            val documentDefinitionVersion = result.getInt("document_definition_version")
            val caseDefinitionVersionTag =
                translateDocumentDefVersionToCaseDefVersionTag(documentDefinitionVersion)
            val caseDefinitionVersionTagForDatabase =
                translateDocumentDefVersionToCaseDefVersionTagForDatabase(documentDefinitionVersion)
            val processDefinitionKey = result.getString("camunda_process_definition_key")
            val canInitializeDocument = result.getBoolean("can_initialize_document")
            val startableByUser = result.getBoolean("startable_by_user")

            migrateProcessDefinition(
                connection,
                database,
                caseDefinitionKey,
                caseDefinitionVersionTag,
                caseDefinitionVersionTagForDatabase,
                processDefinitionKey,
                canInitializeDocument,
                startableByUser,
            )
        }
        logger.info { "Finished ${this::class.simpleName}" }
    }

    private fun translateDocumentDefVersionToCaseDefVersionTag(
        documentDefinitionVersion: Int,
    ): String {
        return "0.$documentDefinitionVersion.0-env"
    }

    private fun translateDocumentDefVersionToCaseDefVersionTagForDatabase(
        documentDefinitionVersion: Int,
    ): String {
        val minorVersion = documentDefinitionVersion.toString().padStart(6, '0')
        return "000000.$minorVersion.000000-env"
    }

    private fun migrateProcessDefinition(
        connection: JdbcConnection,
        database: Database,
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
        caseDefinitionVersionTagForDatabase: String,
        processDefinitionKey: String,
        canInitializeDocument: Boolean,
        startableByUser: Boolean,
    ) {
        // since this can be called recursively, first check if the process has not been migrated already
        if (isProcDefMigrated(connection, caseDefinitionKey, caseDefinitionVersionTagForDatabase, processDefinitionKey)) {
            return
        }

        logger.info { "Migrating process $processDefinitionKey for case $caseDefinitionKey:$caseDefinitionVersionTag" }

        // get the original process definition
        val procDef = getLatestProcDef(connection, processDefinitionKey)

        // set version tag to the case definition version tag
        val setCaseVersionTagResult = transformProcessDefinitionData(
            procDef,
            caseDefinitionKey,
            caseDefinitionVersionTag,
        )
        val updatedProcDef = setCaseVersionTagResult.first
        val referencedEntities = setCaseVersionTagResult.second

        migrateProcessLinksWithRelatedResources(
            connection,
            database,
            procDef,
            updatedProcDef,
            caseDefinitionKey,
            caseDefinitionVersionTagForDatabase,
        )

        // save updated process definition
        saveProcDefForCaseDef(
            connection,
            updatedProcDef,
            caseDefinitionKey,
            caseDefinitionVersionTag,
            caseDefinitionVersionTagForDatabase,
            canInitializeDocument,
            startableByUser
        )

        // also migrate subprocesses and decision definitions referenced in the process definition
        referencedEntities.processDefinitionKeys.forEach { subProcessDefinitionKey ->
            migrateProcessDefinition(
                connection,
                database,
                caseDefinitionKey,
                caseDefinitionVersionTag,
                caseDefinitionVersionTagForDatabase,
                subProcessDefinitionKey,
                false,
                false,
            )
        }
        referencedEntities.decisionDefinitionKeys.forEach { decisionDefinitionKey ->
            migrateDecisionDefinition(
                connection,
                caseDefinitionKey,
                caseDefinitionVersionTag,
                decisionDefinitionKey
            )
        }
    }

    private fun migrateProcessLinksWithRelatedResources(
        connection: JdbcConnection,
        database: Database,
        originalProcessDefinition: ProcessDefinition,
        newProcessDefinition: ProcessDefinition,
        caseDefinitionKey: String,
        caseDefinitionVersionTagForDatabase: String,
    ) {
        val processLinks = getProcessLinksForProcess(connection, database, originalProcessDefinition.id)
        processLinks.forEach {
            val updatedLink = it.copy(processDefinitionId = newProcessDefinition.id, id = UUID.randomUUID())
            saveProcessLink(connection, database, updatedLink)
        }
    }

    private fun getProcessLinksForProcess(
        connection: JdbcConnection,
        database: Database,
        processDefinitionId: String,
    ): List<ProcessLink> {
        val query = """
            select
                id,
                process_definition_id,
                activity_id,
                activity_type,
                process_link_type,
                component_key,
                form_definition_id,
                view_model_enabled,
                form_display_type,
                form_size,
                subtitles,
                action_properties,
                plugin_configuration_id,
                plugin_action_definition_key,
                form_flow_definition_id
            from process_link
            where process_definition_id = ?
        """.trimIndent()

        val statement = connection.prepareStatement(query)
        statement.setString(1, processDefinitionId)

        val results = statement.executeQuery()
        val processLinks = mutableListOf<ProcessLink>()
        while (results.next()) {
            processLinks.add(
                ProcessLink(
                    getIdFromResultSet(database, results, "id")!!,
                    results.getString("process_definition_id"),
                    results.getString("activity_id"),
                    results.getString("activity_type"),
                    results.getString("process_link_type"),
                    results.getString("component_key"),
                    getIdFromResultSet(database, results, "form_definition_id"),
                    results.getBoolean("view_model_enabled"),
                    results.getString("form_display_type"),
                    results.getString("form_size"),
                    results.getString("subtitles"),
                    results.getString("action_properties"),
                    getIdFromResultSet(database, results, "plugin_configuration_id"),
                    results.getString("plugin_action_definition_key"),
                    results.getString("form_flow_definition_id")
                )
            )
        }
        return processLinks
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

    private fun saveProcessLink(
        connection: JdbcConnection,
        database: Database,
        processLink: ProcessLink,
    ) {
        val insertProcessLinkPostgresQuery = """
            insert into process_link
            (
                id,
                process_definition_id,
                activity_id,
                activity_type,
                process_link_type,
                component_key,
                form_definition_id,
                view_model_enabled,
                form_display_type,
                form_size,
                subtitles,
                action_properties,
                plugin_configuration_id,
                plugin_action_definition_key,
                form_flow_definition_id,
                migration_form_name
            )
            values
            (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::json, ?::json, ?, ?, ?,
                (select name from form_io_form_definition where id = ?)
            )
        """.trimIndent()

        val insertProcessLinkMysqlQuery = """
            insert into process_link
            (
                id,
                process_definition_id,
                activity_id,
                activity_type,
                process_link_type,
                component_key,
                form_definition_id,
                view_model_enabled,
                form_display_type,
                form_size,
                subtitles,
                action_properties,
                plugin_configuration_id,
                plugin_action_definition_key,
                form_flow_definition_id,
                migration_form_name
            )
            values
            (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                (select name from form_io_form_definition where id = ?)
            )
        """.trimIndent()

        val insertProcessLinkQuery = if (database.databaseProductName == "MySQL") {
            insertProcessLinkMysqlQuery
        } else {
            insertProcessLinkPostgresQuery
        }

        val statement = connection.prepareStatement(insertProcessLinkQuery)
        setUuidParameter(1, processLink.id, statement, database)
        statement.setString(2, processLink.processDefinitionId)
        statement.setString(3, processLink.activityId)
        statement.setString(4, processLink.activityType)
        statement.setString(5, processLink.processLinkType)
        statement.setString(6, processLink.componentKey)
        setUuidParameter(7, processLink.formDefinitionId, statement, database)
        statement.setBoolean(8, processLink.viewModelEnabled)
        statement.setString(9, processLink.formDisplayType)
        statement.setString(10, processLink.formSize)
        statement.setString(11, processLink.subtitles)
        statement.setString(12, processLink.actionProperties)
        setUuidParameter(13, processLink.pluginConfigurationId, statement, database)
        statement.setString(14, processLink.pluginActionDefinitionKey)
        statement.setString(15, processLink.formFlowDefinitionId)
        setUuidParameter(16, processLink.formDefinitionId, statement, database)
        statement.executeUpdate()
    }

    private fun setUuidParameter(
        index: Int,
        value: UUID?,
        statement: PreparedStatement,
        database: Database,
    ) {
        if (database.databaseProductName == "MySQL") {
            if (value == null) {
                statement.setNull(index, java.sql.Types.BINARY)
            } else {
                val bb = ByteBuffer.wrap(ByteArray(16))
                bb.putLong(value.getMostSignificantBits())
                bb.putLong(value.getLeastSignificantBits())
                statement.setBytes(index, bb.array())
            }
        } else {
            statement.setObject(index, value)
        }
    }

    private fun migrateDecisionDefinition(
        connection: JdbcConnection,
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
        decisionDefinitionKey: String,
    ) {
        if (isDecisionDefinition(connection, caseDefinitionKey, caseDefinitionVersionTag, decisionDefinitionKey)) {
            return
        }

        val decisionDefinitionSet: DecisionDefinitionSet? = getLatestDecisionDefinition(connection, decisionDefinitionKey)
        if (decisionDefinitionSet == null) {
            error("No decision definition found for key: $decisionDefinitionKey")
        }
        val updatedDecisionDefinition = changeDecisionDefinitionData(
            caseDefinitionKey,
            caseDefinitionVersionTag,
            decisionDefinitionSet!!
        )
        saveDecisionDefinition(connection, updatedDecisionDefinition)
    }

    private fun changeDecisionDefinitionData(
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
        decisionDefinitionSet: DecisionDefinitionSet,
    ): DecisionDefinitionSet {
        val deploymentId = UUID.randomUUID().toString()

        return decisionDefinitionSet.copy(
            decisionDefinitions = decisionDefinitionSet.decisionDefinitions.map { decisionDefinition ->
                decisionDefinition.copy(
                    id = UUID.randomUUID().toString(),
                    versionTag = CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag),
                    version = decisionDefinition.version + 1,
                    deploymentId = deploymentId,
                )
            },
            deployment = decisionDefinitionSet.deployment.copy(
                id = deploymentId,
                deployTime = Timestamp.from(Instant.now())
            ),
            byteArray = decisionDefinitionSet.byteArray.copy(
                id = UUID.randomUUID().toString(),
                deploymentId = deploymentId,
            ),
            decisionRequirementsDefinition = decisionDefinitionSet.decisionRequirementsDefinition?.copy(
                id = UUID.randomUUID().toString(),
                deploymentId = deploymentId,
                version = decisionDefinitionSet.decisionRequirementsDefinition.version + 1
            )
        )
    }

    private fun saveDecisionDefinition(
        connection: JdbcConnection,
        decisionDefinitionSet: DecisionDefinitionSet,
    ) {
        saveDeployment(connection, decisionDefinitionSet.deployment)
        saveByteArray(connection, decisionDefinitionSet.byteArray)
        decisionDefinitionSet.decisionRequirementsDefinition?.let {
            saveDecisionRequirementsDefinition(connection, it)
        }
        decisionDefinitionSet.decisionDefinitions.forEach {
            saveDecisionDefinition(
                connection,
                it,
                decisionDefinitionSet.decisionRequirementsDefinition
            )
        }
    }

    private fun isDecisionDefinition(
        connection: JdbcConnection,
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
        decisionDefinitionKey: String,
    ): Boolean {
        // since this can be called recursively, first check if the process has not been migrated already
        val existingProcDefQuery = """
            select count(*) as nr_of_decision_definitions
            from act_re_decision_def
            where key_ = ?
            and version_tag_ = ?
        """.trimIndent()

        val statement = connection.prepareStatement(existingProcDefQuery)
        statement.setString(1, decisionDefinitionKey)
        statement.setString(2, CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag))

        val resultSet = statement.executeQuery()
        resultSet.next()
        val numberOfDecisions = resultSet.getInt(1)
        return numberOfDecisions > 0
    }

    private fun getLatestDecisionDefinition(
        connection: JdbcConnection,
        decisionDefinitionKey: String,
    ): DecisionDefinitionSet? {
        val query = """
            select
                ardd.id_ ardd_id_,
                ardd.rev_ ardd_rev_,
                ardd.category_ ardd_category_,
                ardd.name_ ardd_name_,
                ardd.key_ ardd_key_,
                ardd.version_ ardd_version_,
                ardd.deployment_id_ ardd_deployment_id_,
                ardd.resource_name_ ardd_resource_name_,
                ardd.dgrm_resource_name_ ardd_dgrm_resource_name_,
                ardd.dec_req_id_ ardd_dec_req_id_,
                ardd.dec_req_key_ ardd_dec_req_key_,
                ardd.tenant_id_ ardd_tenant_id_,
                ardd.history_ttl_ ardd_history_ttl_,
                ardd.version_tag_ ardd_version_tag_,
                ardrd.id_ ardrd_id_,
                ardrd.rev_ ardrd_rev_,
                ardrd.category_ ardrd_category_,
                ardrd.name_ ardrd_name_,
                ardrd.key_ ardrd_key_,
                ardrd.version_ ardrd_version_,
                ardrd.deployment_id_ ardrd_deployment_id_,
                ardrd.resource_name_ ardrd_resource_name_,
                ardrd.dgrm_resource_name_ ardrd_dgrm_resource_name_,
                ardrd.tenant_id_ ardrd_tenant_id_,
                ard.id_ ard_id_,
                ard.name_ ard_name_,
                ard.deploy_time_ ard_deploy_time_,
                ard.source_ ard_source_,
                ard.tenant_id_ ard_tenant_id_,
                agb.id_ agb_id_,
                agb.rev_ agb_rev_,
                agb.name_ agb_name_,
                agb.bytes_ agb_bytes_,
                agb.generated_ agb_generated_,
                agb.tenant_id_ agb_tenant_id_,
                agb.type_ agb_type_,
                agb.create_time_ agb_create_time_,
                agb.root_proc_inst_id_ agb_root_proc_inst_id_,
                agb.removal_time_ agb_removal_time_
            from act_re_decision_def ardd
            join act_re_deployment ard
                on ardd.deployment_id_ = ard.id_
            join act_ge_bytearray agb
                on agb.deployment_id_ = ardd.deployment_id_
                and agb.name_ = ardd.resource_name_
            left join act_re_decision_req_def ardrd
                on ardrd.deployment_id_ = ardd.deployment_id_
                and ardrd.resource_name_ = ardd.resource_name_
            join (
                select ard2.id_, ardd2.resource_name_
                from act_re_deployment ard2
                join act_re_decision_def ardd2
                    on ardd2.deployment_id_ = ard2.id_
                where ardd2.key_ = ?
                order by version_ desc
                limit 1
            ) as original_definition
                on original_definition.id_ = ardd.deployment_id_
                and original_definition.resource_name_ = ardd.resource_name_
        """.trimIndent()

        val statement = connection.prepareStatement(query)
        statement.setString(1, decisionDefinitionKey)

        val results = statement.executeQuery()
        var decisionDefinition: DecisionDefinitionSet? = null
        val decisionDefinitions = mutableListOf<DecisionDefinition>()
        while (results.next()) {
            if (results.isFirst) {
                val decisionRequirementsDefinitionId = results.getString("ardrd_id_")
                val decisionRequirementsDefinition = if (decisionRequirementsDefinitionId != null) {
                    DecisionRequirementsDefinition(
                        decisionRequirementsDefinitionId,
                        results.getInt("ardrd_rev_"),
                        results.getString("ardrd_category_"),
                        results.getString("ardrd_name_"),
                        results.getString("ardrd_key_"),
                        results.getInt("ardrd_version_"),
                        results.getString("ardrd_deployment_id_"),
                        results.getString("ardrd_resource_name_"),
                        results.getString("ardrd_dgrm_resource_name_"),
                        results.getString("ardrd_tenant_id_"),
                    )
                } else {
                    null
                }
                decisionDefinition = DecisionDefinitionSet(
                    deployment = Deployment(
                        results.getString("ard_id_"),
                        results.getString("ard_name_"),
                        results.getTimestamp("ard_deploy_time_"),
                        results.getString("ard_source_"),
                        results.getString("ard_tenant_id_")
                    ),
                    decisionRequirementsDefinition = decisionRequirementsDefinition,
                    decisionDefinitions = decisionDefinitions,
                    byteArray = CamundaByteArray(
                        results.getString("agb_id_"),
                        results.getInt("agb_rev_"),
                        results.getString("agb_name_"),
                        results.getString("ardd_deployment_id_"),
                        results.getBytes("agb_bytes_"),
                        results.getBoolean("agb_generated_"),
                        results.getString("agb_tenant_id_"),
                        results.getInt("agb_type_"),
                        results.getTimestamp("agb_create_time_"),
                        results.getString("agb_root_proc_inst_id_"),
                        results.getTimestamp("agb_removal_time_")
                    )
                )
            }

            decisionDefinitions.add(
                DecisionDefinition(
                    results.getString("ardd_id_"),
                    results.getInt("ardd_rev_"),
                    results.getString("ardd_category_"),
                    results.getString("ardd_name_"),
                    results.getString("ardd_key_"),
                    results.getInt("ardd_version_"),
                    results.getString("ardd_deployment_id_"),
                    results.getString("ardd_resource_name_"),
                    results.getString("ardd_dgrm_resource_name_"),
                    results.getString("ardd_tenant_id_"),
                    results.getString("ardd_version_tag_"),
                    results.getInt("ardd_history_ttl_"),
                )
            )
        }
        return decisionDefinition
    }

    private fun saveProcDefForCaseDef(
        connection: JdbcConnection,
        processDefinition: ProcessDefinition,
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
        caseDefinitionVersionTagForDatabase: String,
        canInitializeDocument: Boolean,
        startableByUser: Boolean
    ) {
        saveDeployment(connection, processDefinition.deployment)
        saveByteArray(connection, processDefinition.byteArray)
        saveProcessDefinition(connection, processDefinition)
        saveProcessDefinitionCaseDefinition(
            connection,
            processDefinition,
            caseDefinitionKey,
            caseDefinitionVersionTagForDatabase,
            canInitializeDocument,
            startableByUser
        )
    }

    private fun saveProcessDefinition(
        connection: JdbcConnection,
        processDefinition: ProcessDefinition,
    ) {
        val insertProcDefQuery = """
            insert into act_re_procdef (
                id_,
                rev_,
                category_,
                name_,
                key_,
                version_,
                deployment_id_,
                resource_name_,
                dgrm_resource_name_,
                has_start_form_key_,
                suspension_state_,
                tenant_id_,
                version_tag_,
                history_ttl_,
                startable_
            ) values (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?
            )
        """.trimIndent()

        val statement = connection.prepareStatement(insertProcDefQuery)
        statement.setString(1, processDefinition.id)
        statement.setInt(2, processDefinition.rev)
        statement.setString(3, processDefinition.category)
        statement.setString(4, processDefinition.name)
        statement.setString(5, processDefinition.key)
        statement.setInt(6, processDefinition.version)
        statement.setString(7, processDefinition.deploymentId)
        statement.setString(8, processDefinition.resourceName)
        statement.setString(9, processDefinition.dgrmResourceName)
        statement.setBoolean(10, processDefinition.hasStartFormKey)
        statement.setInt(11, processDefinition.suspensionState)
        statement.setString(12, processDefinition.tenantId)
        statement.setString(13, processDefinition.versionTag)
        processDefinition.historyTtl.let {
            if (it == null) {
                statement.setNull(14, java.sql.Types.INTEGER)
            } else {
                statement.setInt(14, it)
            }
        }
        statement.setBoolean(15, processDefinition.startable)

        statement.executeUpdate()
    }

    private fun saveDeployment(
        connection: JdbcConnection,
        deployment: Deployment,
    ) {
        val insertProcDefQuery = """
            insert into act_re_deployment (
                id_,
                name_,
                deploy_time_,
                source_,
                tenant_id_
            ) values (
                ?, ?, ?, ?, ?
            )
        """.trimIndent()

        val statement = connection.prepareStatement(insertProcDefQuery)
        statement.setString(1, deployment.id)
        statement.setString(2, deployment.name)
        statement.setTimestamp(3, deployment.deployTime)
        statement.setString(4, deployment.source)
        statement.setString(5, deployment.tenantId)

        statement.executeUpdate()
    }

    private fun saveByteArray(
        connection: JdbcConnection,
        byteArray: CamundaByteArray
    ) {
        val insertProcDefQuery = """
            insert into act_ge_bytearray (
                id_,
                rev_,
                name_,
                deployment_id_,
                bytes_,
                generated_,
                tenant_id_,
                type_,
                create_time_,
                root_proc_inst_id_,
                removal_time_
            ) values (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?
            )
        """.trimIndent()

        val statement = connection.prepareStatement(insertProcDefQuery)
        statement.setString(1, byteArray.id)
        statement.setInt(2, byteArray.rev)
        statement.setString(3, byteArray.name)
        statement.setString(4, byteArray.deploymentId)
        statement.setBytes(5, byteArray.bytes)
        byteArray.generated.let {
            if (it == null) {
                statement.setNull(6, java.sql.Types.BOOLEAN)
            } else {
                statement.setBoolean(6, it)
            }
        }
        statement.setString(7, byteArray.tenantId)
        statement.setInt(8, byteArray.type)
        statement.setTimestamp(9, byteArray.createTime)
        statement.setString(10, byteArray.rootProcInstId)
        statement.setTimestamp(11, byteArray.removalTime)

        statement.executeUpdate()
    }

    private fun saveProcessDefinitionCaseDefinition(
        connection: JdbcConnection,
        processDefinition: ProcessDefinition,
        caseDefinitionKey: String,
        caseDefinitionVersionTagForDatabase: String,
        canInitializeDocument: Boolean,
        startableByUser: Boolean
    ) {
        val insertProcDefQuery = """
            insert into process_definition_case_definition (
                process_definition_id,
                case_definition_key,
                case_definition_version_tag,
                can_initialize_document,
                startable_by_user
            ) values (
                ?, ?, ?, ?, ?
            )
        """.trimIndent()

        val statement = connection.prepareStatement(insertProcDefQuery)
        statement.setString(1, processDefinition.id)
        statement.setString(2, caseDefinitionKey)
        statement.setString(3, caseDefinitionVersionTagForDatabase)
        statement.setBoolean(4, canInitializeDocument)
        statement.setBoolean(5, startableByUser)

        statement.executeUpdate()
    }

    private fun saveDecisionRequirementsDefinition(
        connection: JdbcConnection,
        drd: DecisionRequirementsDefinition,
    ) {
        val insertProcDefQuery = """
            insert into act_re_decision_req_def (
                id_,
                rev_,
                category_,
                name_,
                key_,
                version_,
                deployment_id_,
                resource_name_,
                dgrm_resource_name_,
                tenant_id_
            ) values (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            )
        """.trimIndent()

        val statement = connection.prepareStatement(insertProcDefQuery)
        statement.setString(1, drd.id)
        statement.setInt(2, drd.rev)
        statement.setString(3, drd.category)
        statement.setString(4, drd.name)
        statement.setString(5, drd.key)
        statement.setInt(6, drd.version)
        statement.setString(7, drd.deploymentId)
        statement.setString(8, drd.resourceName)
        statement.setString(9, drd.dgrmResourceName)
        statement.setString(10, drd.tenantId)

        statement.executeUpdate()
    }

    private fun saveDecisionDefinition(
        connection: JdbcConnection,
        decisionDefinition: DecisionDefinition,
        drd: DecisionRequirementsDefinition? = null,
    ) {
        val insertProcDefQuery = """
            insert into act_re_decision_def (
                id_,
                rev_,
                category_,
                name_,
                key_,
                version_,
                deployment_id_,
                resource_name_,
                dgrm_resource_name_,
                dec_req_id_,
                dec_req_key_,
                tenant_id_,
                history_ttl_,
                version_tag_
            ) values (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            )
        """.trimIndent()

        val statement = connection.prepareStatement(insertProcDefQuery)
        statement.setString(1, decisionDefinition.id)
        statement.setInt(2, decisionDefinition.rev)
        statement.setString(3, decisionDefinition.category)
        statement.setString(4, decisionDefinition.name)
        statement.setString(5, decisionDefinition.key)
        statement.setInt(6, decisionDefinition.version)
        statement.setString(7, decisionDefinition.deploymentId)
        statement.setString(8, decisionDefinition.resourceName)
        statement.setString(9, decisionDefinition.dgrmResourceName)
        statement.setString(10, drd?.id)
        statement.setString(11, drd?.key)
        statement.setString(12, decisionDefinition.tenantId)
        decisionDefinition.historyTtl.let {
            if (it == null) {
                statement.setNull(13, java.sql.Types.INTEGER)
            } else {
                statement.setInt(13, it)
            }
        }
        statement.setString(14, decisionDefinition.versionTag)

        statement.executeUpdate()
    }

    private fun transformProcessDefinitionData(
        processDefinition: ProcessDefinition,
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
    ): Pair<ProcessDefinition, ReferencedEntities> {
        val bpmnModel = Bpmn.readModelFromStream(ByteArrayInputStream(processDefinition.byteArray.bytes))
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, caseDefinitionVersionTag)

        val referencedEntities = updateProcessDefinitionModel(bpmnModel, caseDefinitionId)
        val outputStream = ByteArrayOutputStream()
        Bpmn.writeModelToStream(outputStream, bpmnModel)

        val deploymentId = UUID.randomUUID().toString()
        val versionTag = "CD:$caseDefinitionKey:$caseDefinitionVersionTag"

        return Pair(
            processDefinition.copy(
                id = UUID.randomUUID().toString(),
                version = processDefinition.version+1,
                versionTag = versionTag,
                deploymentId = deploymentId,
                deployment = processDefinition.deployment.copy(
                    id = deploymentId,
                    deployTime = Timestamp.from(Instant.now())
                ),
                byteArray = processDefinition.byteArray.copy(
                    id = UUID.randomUUID().toString(),
                    deploymentId = deploymentId,
                    bytes = outputStream.toByteArray()
                )
            ),
            referencedEntities
        )
    }

    private fun getLatestProcDef(
        connection: JdbcConnection,
        processDefinitionKey: String
    ): ProcessDefinition {
        val processDefinitionQuery = """
            select
                pd.id_,
                pd.rev_,
                pd.category_,
                pd.name_,
                pd.key_,
                pd.version_,
                pd.deployment_id_,
                pd.resource_name_,
                pd.dgrm_resource_name_,
                pd.has_start_form_key_,
                pd.suspension_state_,
                pd.tenant_id_,
                pd.version_tag_,
                pd.history_ttl_,
                pd.startable_,
                agb.rev_ as be_rev_,
                agb.bytes_,
                agb.generated_,
                agb.tenant_id_ as be_tenant_id_,
                agb.type_,
                agb.create_time_,
                agb.root_proc_inst_id_,
                agb.removal_time_
            from act_re_procdef pd
            join (
                select max(version_) as max_version, key_
                from act_re_procdef
                group by key_
            ) as mv
                on pd.key_ = mv.key_
                and pd.version_ = mv.max_version
            join act_ge_bytearray agb
                on agb.deployment_id_ = pd.deployment_id_
                and agb.name_ = pd.resource_name_
            where pd.key_ = ?
        """.trimIndent()

        val statement = connection.prepareStatement(processDefinitionQuery)
        statement.setString(1, processDefinitionKey)

        val results = statement.executeQuery()
        results.next()

        return ProcessDefinition(
            results.getString("id_"),
            results.getInt("rev_"),
            results.getString("category_"),
            results.getString("name_"),
            results.getString("key_"),
            results.getInt("version_"),
            results.getString("deployment_id_"),
            results.getString("resource_name_"),
            results.getString("dgrm_resource_name_"),
            results.getBoolean("has_start_form_key_"),
            results.getInt("suspension_state_"),
            results.getString("tenant_id_"),
            results.getString("version_tag_"),
            results.getInt("history_ttl_"),
            results.getBoolean("startable_"),
            Deployment(
                results.getString("deployment_id_"),
                "gzacApplication",
                null,
                "migration",
                results.getString("tenant_id_")
            ),
            CamundaByteArray(
                results.getString("id_"),
                results.getInt("be_rev_"),
                results.getString("resource_name_"),
                results.getString("deployment_id_"),
                results.getBytes("bytes_"),
                results.getBoolean("generated_"),
                results.getString("be_tenant_id_"),
                results.getInt("type_"),
                results.getTimestamp("create_time_"),
                results.getString("root_proc_inst_id_"),
                results.getTimestamp("removal_time_")
            )
        )
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
            join act_re_procdef pd on pd.id_ = pdcd.process_definition_id
            where pdcd.case_definition_key = ?
            and pdcd.case_definition_version_tag = ?
            and pd.name_ = ?
        """.trimIndent()

        val statement = connection.prepareStatement(existingProcDefQuery)
        statement.setString(1, caseDefinitionKey)
        statement.setString(2, caseDefinitionVersionTag)
        statement.setString(3, processDefinitionKey)

        val resultSet = statement.executeQuery()
        resultSet.next()
        val numberOfProcesses = resultSet.getInt(1)
        return numberOfProcesses > 0
    }

    private fun updateProcessDefinitionModel(bpmnModel: BpmnModelInstance, caseDefinitionId: CaseDefinitionId): ReferencedEntities {
        val referencedProcesses = mutableListOf<String>()
        val referencedDecisions = mutableListOf<String>()

        bpmnModel.getDefinitions().getChildElementsByType<Process?>(Process::class.java).forEach(
            Consumer { process: Process? ->
                process!!.setCamundaVersionTag(CamundaProcessService.CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId.toString())
                process.getChildElementsByType<CallActivity>(CallActivity::class.java).forEach {callActivity ->
                    val elementBinding = callActivity.getCamundaCalledElementBinding()
                    // when the element binding is null, it means it's set to latest
                    if (elementBinding == null) {
                        callActivity.setCamundaCalledElementBinding("versionTag")
                        callActivity.setCamundaCalledElementVersionTag(CamundaProcessService.CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId)
                        referencedProcesses.add(callActivity.calledElement)
                    }
                }
                process.getChildElementsByType(BusinessRuleTask::class.java).forEach { businessRuleTask ->
                    val elementBinding = businessRuleTask.getCamundaDecisionRefBinding()
                    // when the element binding is null, it means it's set to latest
                    if (elementBinding == null) {
                        businessRuleTask.setCamundaDecisionRefBinding("versionTag")
                        businessRuleTask.setCamundaDecisionRefVersionTag(CamundaProcessService.CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId)
                        referencedDecisions.add(businessRuleTask.camundaDecisionRef)
                    }
                }
            }
        )
        return ReferencedEntities(referencedProcesses, referencedDecisions)
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

    override fun validate(database: Database): ValidationErrors {
        return ValidationErrors()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    data class ProcessDefinition(
        val id: String,
        val rev: Int,
        val category: String,
        val name: String,
        val key: String,
        val version: Int,
        val deploymentId: String,
        val resourceName: String,
        val dgrmResourceName: String?,
        val hasStartFormKey: Boolean,
        val suspensionState: Int,
        val tenantId: String?,
        val versionTag: String?,
        val historyTtl: Int?,
        val startable: Boolean,
        val deployment: Deployment,
        val byteArray: CamundaByteArray,
    )

    data class Deployment(
        val id: String,
        val name: String?,
        val deployTime: Timestamp?,
        val source: String?,
        val tenantId: String?,
    )

    data class CamundaByteArray(
        val id: String,
        val rev: Int,
        val name: String,
        val deploymentId: String?,
        val bytes: ByteArray,
        val generated: Boolean?,
        val tenantId: String?,
        val type: Int,
        val createTime: Timestamp,
        val rootProcInstId: String?,
        val removalTime: Timestamp?,
    )

    data class ReferencedEntities(
        val processDefinitionKeys: List<String>,
        val decisionDefinitionKeys: List<String>,
    )

    data class ProcessLink(
        val id: UUID,
        val processDefinitionId: String,
        val activityId: String,
        val activityType: String,
        val processLinkType: String,
        val componentKey: String?,
        val formDefinitionId: UUID?,
        val viewModelEnabled: Boolean,
        val formDisplayType: String?,
        val formSize: String?,
        val subtitles: String?,
        val actionProperties: String?,
        val pluginConfigurationId: UUID?,
        val pluginActionDefinitionKey: String?,
        val formFlowDefinitionId: String?,
        val migrationFormName: String? = null,
    )

    data class DecisionDefinitionSet(
        val decisionDefinitions: List<DecisionDefinition> = mutableListOf(),
        val decisionRequirementsDefinition: DecisionRequirementsDefinition? = null,
        val deployment: Deployment,
        val byteArray: CamundaByteArray
    )

    data class DecisionDefinition(
        val id: String,
        val rev: Int,
        val category: String?,
        val name: String,
        val key: String,
        val version: Int,
        val deploymentId: String,
        val resourceName: String?,
        val dgrmResourceName: String?,
        val tenantId: String?,
        val versionTag: String?,
        val historyTtl: Int? = null,
    )

    data class DecisionRequirementsDefinition(
        val id: String,
        val rev: Int,
        val category: String?,
        val name: String,
        val key: String,
        val version: Int,
        val deploymentId: String,
        val resourceName: String?,
        val dgrmResourceName: String?,
        val tenantId: String?,
    )
}