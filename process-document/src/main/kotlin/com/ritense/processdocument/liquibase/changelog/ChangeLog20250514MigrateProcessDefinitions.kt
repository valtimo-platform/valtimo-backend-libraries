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
import java.sql.Timestamp
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
            val processDefinitionKey = result.getString("camunda_process_definition_key")
            val canInitializeDocument = result.getBoolean("can_initialize_document")
            val startableByUser = result.getBoolean("startable_by_user")

            migrateProcessDefinition(
                connection,
                caseDefinitionKey,
                caseDefinitionVersionTag,
                processDefinitionKey,
                canInitializeDocument,
                startableByUser
            )
        }
        logger.info { "Finished ${this::class.simpleName}" }
    }

    private fun translateDocumentDefVersionToCaseDefVersionTag(
        documentDefinitionVersion: Int,
    ): String {
        val minorVersion = documentDefinitionVersion.toString().padStart(6, '0')
        return "000000.$minorVersion.000000-env"
    }

    private fun migrateProcessDefinition(
        connection: JdbcConnection,
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
        processDefinitionKey: String,
        canInitializeDocument: Boolean,
        startableByUser: Boolean
    ) {
        // since this can be called recursively, first check if the process has not been migrated already
        if (isProcDefMigrated(connection, caseDefinitionKey, caseDefinitionVersionTag, processDefinitionKey)) {
            return
        }

        logger.info { "Migrating process $processDefinitionKey for case $caseDefinitionKey:$caseDefinitionVersionTag" }

        // get the original process definition
        val procDef = getLatestProcDef(connection, processDefinitionKey)

        // set version tag to the case definition version tag
        val setCaseVersionTagResult = setCaseVersionTag(
            procDef,
            caseDefinitionKey,
            caseDefinitionVersionTag,
        )
        val updatedProcDef = setCaseVersionTagResult.first
        val referencedEntities = setCaseVersionTagResult.second

        // save updated process definition
        saveProcDefForCaseDef(
            connection,
            updatedProcDef,
            caseDefinitionKey,
            caseDefinitionVersionTag,
            canInitializeDocument,
            startableByUser
        )

        referencedEntities.processDefinitionKeys.forEach { subProcessDefinitionKey ->
            migrateProcessDefinition(
                connection,
                caseDefinitionKey,
                caseDefinitionVersionTag,
                subProcessDefinitionKey,
                false,
                false
            )
        }
    }

    private fun migrateDecisionDefinition(
        connection: JdbcConnection,
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
        decisionDefinitionKey: String,
    ) {

    }

    private fun saveProcDefForCaseDef(
        connection: JdbcConnection,
        processDefinition: ProcessDefinition,
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
        canInitializeDocument: Boolean,
        startableByUser: Boolean
    ) {
        saveDeployment(connection, processDefinition)
        saveByteArray(connection, processDefinition)
        saveProcessDefinition(connection, processDefinition)
        saveProcessDefinitionCaseDefinition(
            connection,
            processDefinition,
            caseDefinitionKey,
            caseDefinitionVersionTag,
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
        statement.setInt(14, processDefinition.historyTtl)
        statement.setBoolean(15, processDefinition.startable)

        statement.executeUpdate()
    }

    private fun saveDeployment(
        connection: JdbcConnection,
        processDefinition: ProcessDefinition,
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
        statement.setString(1, processDefinition.deployment.id)
        statement.setString(2, processDefinition.deployment.name)
        statement.setTimestamp(3, processDefinition.deployment.deployTime)
        statement.setString(4, processDefinition.deployment.source)
        statement.setString(5, processDefinition.deployment.tenantId)

        statement.executeUpdate()
    }

    private fun saveByteArray(
        connection: JdbcConnection,
        processDefinition: ProcessDefinition,
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
        statement.setString(1, processDefinition.byteArray.id)
        statement.setInt(2, processDefinition.byteArray.rev)
        statement.setString(3, processDefinition.byteArray.name)
        statement.setString(4, processDefinition.byteArray.deploymentId)
        statement.setBytes(5, processDefinition.byteArray.bytes)
        statement.setBoolean(6, processDefinition.byteArray.generated)
        statement.setString(7, processDefinition.byteArray.tenantId)
        statement.setInt(8, processDefinition.byteArray.type)
        statement.setTimestamp(9, processDefinition.byteArray.createTime)
        statement.setString(10, processDefinition.byteArray.rootProcInstId)
        statement.setTimestamp(11, processDefinition.byteArray.removalTime)

        statement.executeUpdate()
    }

    private fun saveProcessDefinitionCaseDefinition(
        connection: JdbcConnection,
        processDefinition: ProcessDefinition,
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
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
        statement.setString(1, processDefinition.byteArray.id)
        statement.setString(2, caseDefinitionKey)
        statement.setString(3, caseDefinitionVersionTag)
        statement.setBoolean(4, canInitializeDocument)
        statement.setBoolean(5, startableByUser)

        statement.executeUpdate()
    }

    private fun setCaseVersionTag(
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
                version = processDefinition.version,
                versionTag = versionTag,
                deploymentId = deploymentId,
                deployment = processDefinition.deployment.copy(
                    id = deploymentId
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
        statement.setString(0, processDefinitionKey)

        val results = statement.executeQuery()

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
                results.getTimestamp("deploy_time_"),
                "migration",
                results.getString("tenant_id_")
            ),
            CamundaByteArray(
                results.getString("id_"),
                results.getInt("be_rev_"),
                results.getString("name_"),
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

        val numberOfProcesses = statement.executeQuery().getInt(0)
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
                    if (elementBinding == "latest") {
                        callActivity.setCamundaCalledElementBinding("versionTag")
                        callActivity.setCamundaCalledElementVersionTag(CamundaProcessService.CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId)
                        referencedProcesses.add(callActivity.calledElement)
                    }
                }
                process.getChildElementsByType(BusinessRuleTask::class.java).forEach { businessRuleTask ->
                    val elementBinding = businessRuleTask.getCamundaDecisionRefBinding()
                    if (elementBinding == "latest") {
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
        val dgrmResourceName: String,
        val hasStartFormKey: Boolean,
        val suspensionState: Int,
        val tenantId: String,
        val versionTag: String,
        val historyTtl: Int,
        val startable: Boolean,
        val deployment: Deployment,
        val byteArray: CamundaByteArray,
    )

    data class Deployment(
        val id: String,
        val name: String,
        val deployTime: Timestamp,
        val source: String,
        val tenantId: String,
    )

    data class CamundaByteArray(
        val id: String,
        val rev: Int,
        val name: String,
        val deploymentId: String,
        val bytes: ByteArray,
        val generated: Boolean,
        val tenantId: String,
        val type: Int,
        val createTime: Timestamp,
        val rootProcInstId: String,
        val removalTime: Timestamp,
    )

    data class ReferencedEntities(
        val processDefinitionKeys: List<String>,
        val decisionDefinitionKeys: List<String>,
    )
}