/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.openzaak.liquibase.changelog

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.document.domain.impl.Mapper
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CustomChangeException
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor

class ChangeLog20211130 : CustomTaskChange {

    override fun execute(database: Database?) {
        val connection = database!!.connection as JdbcConnection



        val q =
            connection.prepareStatement("INSERT INTO zaak_type_link (zaak_type_link_id, document_definition_name, zaak_type_url, zaak_instance_links, service_task_handlers) VALUES (?, ?, ?, ?, ?)")
        q.setBytes(1, "úõà¥íÿH~".toByteArray())
        q.setString(2, "test")
        q.setString(3, "http://example.com")
        q.setString(
            4,
            "[{\"className\": \"com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink\", \"documentId\": \"5d9c7aa5-1616-4dc2-81db-ef4de0d3a3ef\", \"zaakInstanceId\": \"238790f2-da53-437c-9278-f350c2c3a630\", \"zaakInstanceUrl\": \"http://example.com\"}]"
        )
        q.setString(
            5,
            "[{\"className\": \"com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandler\", \"operation\": \"SET_RESULTAAT\", \"parameter\": \"www.resultaattype.com\", \"serviceTaskId\": \"Task_1edg42x\"}]"
        )
        q.execute()



        val taskToProcessMap = getServiceTaskIdToProcessDefinitionKeyMap(connection)

        var statement = connection.prepareStatement("SELECT zaak_type_link_id, service_task_handlers FROM zaak_type_link")
        val result = statement.executeQuery()

        while (result.next()) {
            val zaakTypeLinkId = result.getBytes(1)
            val handlersString = result.getString(2)
            val handlers = Mapper.INSTANCE.get().readValue(handlersString, ArrayNode::class.java)
            for (handler in handlers) {
                val serviceTaskHandler = handler as ObjectNode
                val serviceTaskId = serviceTaskHandler.get("serviceTaskId").textValue()
                val processDefinitionKey = taskToProcessMap[serviceTaskId]
                    ?: throw CustomChangeException("Failed to find process-definition-key for service-task-id: $serviceTaskId")
                serviceTaskHandler.set<TextNode>("processDefinitionKey", TextNode.valueOf(processDefinitionKey))
            }

            statement = connection.prepareStatement("UPDATE zaak_type_link SET service_task_handlers = ? WHERE zaak_type_link_id = ?")
            statement.setString(1, Mapper.INSTANCE.get().writeValueAsString(handlers))
            statement.setBytes(2, zaakTypeLinkId)
            statement.execute()
        }
    }

    override fun getConfirmationMessage(): String {
        return "${this::class.simpleName} executed."
    }

    override fun setUp() {
    }

    override fun setFileOpener(resourceAccessor: ResourceAccessor?) {
    }

    override fun validate(database: Database?): ValidationErrors {
        return ValidationErrors()
    }

    private fun getServiceTaskIdToProcessDefinitionKeyMap(connection: JdbcConnection): HashMap<String, String> {
        val getTaskIdProcessIdMap = HashMap<String, String>()
        val processDefinitionKeyRegex = Regex("<bpmn:process.*? id=\"([^\"]+)")
        val serviceTaskIdRegex = Regex("<bpmn:serviceTask.*? id=\"([^\"]+)")
        val statement = connection.prepareStatement("SELECT BYTES_ FROM act_ge_bytearray where NAME_ like '%.bpmn'")
        val result = statement.executeQuery()
        while (result.next()) {
            val processDefinitionXml = result.getString(1)
            val processDefinitionKey = processDefinitionKeyRegex.find(processDefinitionXml)?.groupValues?.get(1)
            if (processDefinitionKey != null) {
                val matches = serviceTaskIdRegex.findAll(processDefinitionXml)
                matches.forEach { getTaskIdProcessIdMap[it.groupValues[1]] = processDefinitionKey }
            }
        }
        return getTaskIdProcessIdMap
    }
}