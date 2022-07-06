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

package com.ritense.formlink.repository.impl

import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.impl.formassociation.CamundaFormAssociation
import com.ritense.formlink.domain.impl.formassociation.FormAssociationFactory
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType
import com.ritense.formlink.domain.impl.formassociation.StartEventFormAssociation
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementAngularStateUrlLink
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementUrlLink
import com.ritense.formlink.repository.ProcessFormAssociationRepository
import mu.KotlinLogging
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.UUID

class JdbcProcessFormAssociationRepository(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) : ProcessFormAssociationRepository {

    override fun add(processDefinitionKey: String, camundaFormAssociation: CamundaFormAssociation) {
        val sql = """
            INSERT  INTO $TABLE_NAME (
                $ID_COLUMN,
                $PROCESS_DEFINITION_KEY_COLUMN,
                $FORM_ASSOCIATION_ID,
                $FORM_ASSOCIATION_TYPE,
                $FORM_LINK_ELEMENT_ID,
                $FORM_LINK_FORM_ID,
                $FORM_LINK_FLOW_ID,
                $FORM_LINK_CUSTOM_URL,
                $FORM_LINK_ANGULAR_STATE_URL
            )
            VALUES (
                :$ID_COLUMN,
                :$PROCESS_DEFINITION_KEY_COLUMN,
                :$FORM_ASSOCIATION_ID,
                :$FORM_ASSOCIATION_TYPE,
                :$FORM_LINK_ELEMENT_ID,
                :$FORM_LINK_FORM_ID,
                :$FORM_LINK_FLOW_ID,
                :$FORM_LINK_CUSTOM_URL,
                :$FORM_LINK_ANGULAR_STATE_URL
            )
        """.trimIndent()
        val result = namedParameterJdbcTemplate.update(
            sql,
            mapOf(
                ID_COLUMN to UUID.nameUUIDFromBytes(processDefinitionKey.toByteArray()).asBytes(),
                PROCESS_DEFINITION_KEY_COLUMN to processDefinitionKey,
                FORM_ASSOCIATION_ID to camundaFormAssociation.id.asBytes(),
                FORM_ASSOCIATION_TYPE to camundaFormAssociation.asType(),
                FORM_LINK_ELEMENT_ID to camundaFormAssociation.formLink.id,
                FORM_LINK_FORM_ID to camundaFormAssociation.formLink.formId.asBytes(),
                FORM_LINK_FLOW_ID to camundaFormAssociation.formLink.formFlowId,
                FORM_LINK_CUSTOM_URL to if (camundaFormAssociation.formLink is BpmnElementUrlLink) camundaFormAssociation.formLink.url else null,
                FORM_LINK_ANGULAR_STATE_URL to if (camundaFormAssociation.formLink is BpmnElementAngularStateUrlLink) camundaFormAssociation.formLink.url else null
            )
        )
        require(result == 1)
    }

    override fun update(processDefinitionKey: String, camundaFormAssociation: CamundaFormAssociation) {
        val sql = """
            UPDATE  $TABLE_NAME
            SET     $FORM_ASSOCIATION_TYPE = :$FORM_ASSOCIATION_TYPE
            ,       $FORM_LINK_ELEMENT_ID = :$FORM_LINK_ELEMENT_ID
            ,       $FORM_LINK_FORM_ID = :$FORM_LINK_FORM_ID
            ,       $FORM_LINK_FLOW_ID = :$FORM_LINK_FLOW_ID
            ,       $FORM_LINK_CUSTOM_URL = :$FORM_LINK_CUSTOM_URL
            ,       $FORM_LINK_ANGULAR_STATE_URL = :$FORM_LINK_ANGULAR_STATE_URL
            WHERE   $PROCESS_DEFINITION_KEY_COLUMN = :$PROCESS_DEFINITION_KEY_COLUMN
            AND     $FORM_ASSOCIATION_ID = :$FORM_ASSOCIATION_ID
        """.trimIndent()
        val result = namedParameterJdbcTemplate.update(
            sql,
            mapOf(
                PROCESS_DEFINITION_KEY_COLUMN to processDefinitionKey,
                FORM_ASSOCIATION_ID to camundaFormAssociation.id.asBytes(),
                FORM_ASSOCIATION_TYPE to camundaFormAssociation.asType(),
                FORM_LINK_ELEMENT_ID to camundaFormAssociation.formLink.id,
                FORM_LINK_FORM_ID to camundaFormAssociation.formLink.formId.asBytes(),
                FORM_LINK_FLOW_ID to camundaFormAssociation.formLink.formFlowId,
                FORM_LINK_CUSTOM_URL to if (camundaFormAssociation.formLink is BpmnElementUrlLink) camundaFormAssociation.formLink.url else null,
                FORM_LINK_ANGULAR_STATE_URL to if (camundaFormAssociation.formLink is BpmnElementAngularStateUrlLink) camundaFormAssociation.formLink.url else null
            )
        )
        require(result == 1)
    }

    override fun findAssociationsByProcessDefinitionKey(processDefinitionKey: String): Set<CamundaFormAssociation> {
        val sql = "SELECT * FROM $TABLE_NAME WHERE $PROCESS_DEFINITION_KEY_COLUMN = :$PROCESS_DEFINITION_KEY_COLUMN"
        val associations = mutableSetOf<CamundaFormAssociation>()
        namedParameterJdbcTemplate.query(sql, mapOf(PROCESS_DEFINITION_KEY_COLUMN to processDefinitionKey)) { rs: ResultSet, _: Int -> associations.add(camundaFormAssociation(rs)) }
        return associations
    }

    override fun findByFormLinkId(processDefinitionKey: String, formLinkId: String): CamundaFormAssociation? {
        val sql = """
            SELECT  *
            FROM    $TABLE_NAME
            WHERE   $PROCESS_DEFINITION_KEY_COLUMN = :$PROCESS_DEFINITION_KEY_COLUMN
            AND     $FORM_LINK_ELEMENT_ID = :$FORM_LINK_ELEMENT_ID
        """.trimIndent()
        return namedParameterJdbcTemplate.queryForObject(
            sql,
            mapOf(
                PROCESS_DEFINITION_KEY_COLUMN to processDefinitionKey,
                FORM_LINK_ELEMENT_ID to formLinkId
            )
        )
        { rs, _ -> camundaFormAssociation(rs) }
    }

    override fun findStartEventAssociation(processDefinitionKey: String): CamundaFormAssociation? {
        val sql = """
            SELECT  *
            FROM    $TABLE_NAME
            WHERE   $PROCESS_DEFINITION_KEY_COLUMN = :$PROCESS_DEFINITION_KEY_COLUMN
            AND     $FORM_ASSOCIATION_TYPE = 'start-event'
        """.trimIndent()
        return namedParameterJdbcTemplate.queryForObject(
            sql,
            mapOf(PROCESS_DEFINITION_KEY_COLUMN to processDefinitionKey)
        )
        { rs, _ -> camundaFormAssociation(rs) }
    }

    override fun removeByProcessDefinitionKeyAndFormAssociationId(processDefinitionKey: String, formAssociationId: UUID) {
        val sql = """
            DELETE FROM $TABLE_NAME
            WHERE   $PROCESS_DEFINITION_KEY_COLUMN = :$PROCESS_DEFINITION_KEY_COLUMN
            AND     $FORM_ASSOCIATION_ID = :$FORM_ASSOCIATION_ID
        """.trimIndent()
        val result = namedParameterJdbcTemplate.update(
            sql,
            mapOf(
                PROCESS_DEFINITION_KEY_COLUMN to processDefinitionKey,
                FORM_ASSOCIATION_ID to formAssociationId.asBytes()
            )
        )
        require(result == 1)
    }

    override fun findByCamundaFormAssociationId(camundaFormAssociationId: UUID): CamundaFormAssociation? {
        val sql = "SELECT * FROM $TABLE_NAME WHERE $FORM_ASSOCIATION_ID = :$FORM_ASSOCIATION_ID"
        return namedParameterJdbcTemplate.queryForObject(
            sql,
            mapOf(
                FORM_ASSOCIATION_ID to camundaFormAssociationId.asBytes()
            )
        )
        { rs, _ -> camundaFormAssociation(rs) }
    }

    private fun camundaFormAssociation(rs: ResultSet) = FormAssociationFactory.getFormAssociation(
        UUIDTypeDescriptor.ToBytesTransformer().parse(rs.getBytes(FORM_ASSOCIATION_ID)),
        FormAssociationType.fromString(rs.getString(FORM_ASSOCIATION_TYPE)),
        rs.getString(FORM_LINK_ELEMENT_ID),
        UUIDTypeDescriptor.ToBytesTransformer().parse(rs.getBytes(FORM_LINK_FORM_ID)),
        rs.getString(FORM_LINK_FLOW_ID),
        rs.getString(FORM_LINK_CUSTOM_URL),
        rs.getString(FORM_LINK_ANGULAR_STATE_URL)
    )

    companion object {
        val logger = KotlinLogging.logger {}
        private const val TABLE_NAME = "process_form_association_v2"
        private const val ID_COLUMN = "id"
        private const val PROCESS_DEFINITION_KEY_COLUMN = "process_definition_key"
        private const val FORM_ASSOCIATION_ID = "form_association_id"
        private const val FORM_ASSOCIATION_TYPE = "form_association_type"
        private const val FORM_LINK_ELEMENT_ID = "form_association_form_link_element_id"
        private const val FORM_LINK_FORM_ID = "form_association_form_link_form_id"
        private const val FORM_LINK_FLOW_ID = "form_association_form_link_flow_id"
        private const val FORM_LINK_CUSTOM_URL = "form_association_form_link_custom_url"
        private const val FORM_LINK_ANGULAR_STATE_URL = "form_association_form_link_angular_state_url"
    }

    private fun UUID.asBytes(): ByteArray {
        return UUIDTypeDescriptor.ToBytesTransformer().transform(this)
    }

    private fun FormAssociation.asType(): String {
        return when (this) {
            is StartEventFormAssociation -> FormAssociationType.START_EVENT.toString()
            else -> FormAssociationType.USER_TASK.toString()
        }
    }

}