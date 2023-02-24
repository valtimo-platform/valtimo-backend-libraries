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

package com.ritense.formlink.repository.impl

import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.impl.formassociation.CamundaFormAssociation
import com.ritense.formlink.domain.impl.formassociation.FormAssociationFactory
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType
import com.ritense.formlink.domain.impl.formassociation.StartEventFormAssociation
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementAngularStateUrlLink
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementUrlLink
import com.ritense.formlink.repository.ProcessFormAssociationRepository
import mu.KotlinLogging
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.SqlParameterValue
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.sql.Types
import java.util.UUID

@Transactional
class JdbcProcessFormAssociationRepository(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) : ProcessFormAssociationRepository {

    override fun add(processDefinitionKey: String, camundaFormAssociation: CamundaFormAssociation) {

        if (findByCamundaFormAssociationId(camundaFormAssociation.id) != null) {
            throw DuplicateKeyException("Form process_form_association with form_association_id '${camundaFormAssociation.id}' already exists.")
        }

        val sql = """
            INSERT  INTO $TABLE_NAME (
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
                PROCESS_DEFINITION_KEY_COLUMN to SqlParameterValue(Types.VARCHAR, processDefinitionKey),
                FORM_ASSOCIATION_ID to SqlParameterValue(Types.BINARY, camundaFormAssociation.id.asBytes()),
                FORM_ASSOCIATION_TYPE to SqlParameterValue(Types.VARCHAR, camundaFormAssociation.asType()),
                FORM_LINK_ELEMENT_ID to SqlParameterValue(Types.VARCHAR, camundaFormAssociation.formLink.id),
                FORM_LINK_FORM_ID to if (camundaFormAssociation.formLink is BpmnElementFormIdLink) SqlParameterValue(Types.BINARY, camundaFormAssociation.formLink.formId.asBytes()) else SqlParameterValue(Types.NULL, null),
                FORM_LINK_FLOW_ID to if (camundaFormAssociation.formLink is BpmnElementFormFlowIdLink) SqlParameterValue(Types.VARCHAR, camundaFormAssociation.formLink.formFlowId) else SqlParameterValue(Types.NULL, null),
                FORM_LINK_CUSTOM_URL to if (camundaFormAssociation.formLink is BpmnElementUrlLink) SqlParameterValue(Types.VARCHAR, camundaFormAssociation.formLink.url) else SqlParameterValue(Types.NULL, null),
                FORM_LINK_ANGULAR_STATE_URL to if (camundaFormAssociation.formLink is BpmnElementAngularStateUrlLink) SqlParameterValue(Types.VARCHAR, camundaFormAssociation.formLink.url) else SqlParameterValue(Types.NULL, null)
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
                PROCESS_DEFINITION_KEY_COLUMN to SqlParameterValue(Types.VARCHAR, processDefinitionKey),
                FORM_ASSOCIATION_ID to SqlParameterValue(Types.BINARY, camundaFormAssociation.id.asBytes()),
                FORM_ASSOCIATION_TYPE to SqlParameterValue(Types.VARCHAR, camundaFormAssociation.asType()),
                FORM_LINK_ELEMENT_ID to SqlParameterValue(Types.VARCHAR, camundaFormAssociation.formLink.id),
                FORM_LINK_FORM_ID to if (camundaFormAssociation.formLink is BpmnElementFormIdLink) SqlParameterValue(Types.BINARY, camundaFormAssociation.formLink.formId.asBytes()) else SqlParameterValue(Types.NULL, null),
                FORM_LINK_FLOW_ID to if (camundaFormAssociation.formLink is BpmnElementFormFlowIdLink) SqlParameterValue(Types.VARCHAR, camundaFormAssociation.formLink.formFlowId) else SqlParameterValue(Types.NULL, null),
                FORM_LINK_CUSTOM_URL to if (camundaFormAssociation.formLink is BpmnElementUrlLink) SqlParameterValue(Types.VARCHAR, camundaFormAssociation.formLink.url) else SqlParameterValue(Types.NULL, null),
                FORM_LINK_ANGULAR_STATE_URL to if (camundaFormAssociation.formLink is BpmnElementAngularStateUrlLink) SqlParameterValue(Types.VARCHAR, camundaFormAssociation.formLink.url) else SqlParameterValue(Types.VARCHAR, null)
            )
        )
        require(result == 1)
    }

    override fun findAssociationsByProcessDefinitionKey(processDefinitionKey: String): Set<CamundaFormAssociation>? {
        val sql = "SELECT * FROM $TABLE_NAME WHERE $PROCESS_DEFINITION_KEY_COLUMN = :$PROCESS_DEFINITION_KEY_COLUMN"
        val associations = mutableSetOf<CamundaFormAssociation>()
        namedParameterJdbcTemplate.query(
            sql,
            mapOf(PROCESS_DEFINITION_KEY_COLUMN to SqlParameterValue(Types.VARCHAR, processDefinitionKey))
        ) { rs: ResultSet, _: Int -> associations.add(camundaFormAssociation(rs)) }
        if (associations.isEmpty()) {
            return null
        }
        return associations
    }

    override fun findByFormLinkId(processDefinitionKey: String, formLinkId: String): CamundaFormAssociation? {
        return try {
            val sql = """
            SELECT  *
            FROM    $TABLE_NAME
            WHERE   $PROCESS_DEFINITION_KEY_COLUMN = :$PROCESS_DEFINITION_KEY_COLUMN
            AND     $FORM_LINK_ELEMENT_ID = :$FORM_LINK_ELEMENT_ID
        """.trimIndent()
            return namedParameterJdbcTemplate.queryForObject(
                sql,
                mapOf(
                    PROCESS_DEFINITION_KEY_COLUMN to SqlParameterValue(Types.VARCHAR, processDefinitionKey),
                    FORM_LINK_ELEMENT_ID to SqlParameterValue(Types.VARCHAR, formLinkId)
                )
            )
            { rs, _ -> camundaFormAssociation(rs) }
        } catch (ex: EmptyResultDataAccessException) {
            null
        }
    }

    override fun findStartEventAssociation(processDefinitionKey: String): CamundaFormAssociation? {
        return try {
            val sql = """
            SELECT  *
            FROM    $TABLE_NAME
            WHERE   $PROCESS_DEFINITION_KEY_COLUMN = :$PROCESS_DEFINITION_KEY_COLUMN
            AND     $FORM_ASSOCIATION_TYPE = 'start-event'
        """.trimIndent()
            return namedParameterJdbcTemplate.queryForObject(
                sql,
                mapOf(PROCESS_DEFINITION_KEY_COLUMN to SqlParameterValue(Types.VARCHAR, processDefinitionKey))
            )
            { rs, _ -> camundaFormAssociation(rs) }
        } catch (ex: EmptyResultDataAccessException) {
            null
        }
    }

    override fun removeByProcessDefinitionKeyAndFormAssociationId(processDefinitionKey: String, formAssociationId: UUID) {
        try {
            val sql = """
                DELETE FROM $TABLE_NAME
                WHERE   $PROCESS_DEFINITION_KEY_COLUMN = :$PROCESS_DEFINITION_KEY_COLUMN
                AND     $FORM_ASSOCIATION_ID = :$FORM_ASSOCIATION_ID
            """.trimIndent()
            val result = namedParameterJdbcTemplate.update(
                sql,
                mapOf(
                    PROCESS_DEFINITION_KEY_COLUMN to SqlParameterValue(Types.VARCHAR, processDefinitionKey),
                    FORM_ASSOCIATION_ID to SqlParameterValue(Types.BINARY, formAssociationId.asBytes())
                )
            )
            require(result == 1)
        } catch (ex: Exception) {
            logger.error { ex }
        }
    }

    override fun findByCamundaFormAssociationId(camundaFormAssociationId: UUID): CamundaFormAssociation? {
        return try {
            val sql = "SELECT * FROM $TABLE_NAME WHERE $FORM_ASSOCIATION_ID = :$FORM_ASSOCIATION_ID"
            namedParameterJdbcTemplate.queryForObject(
                sql,
                mapOf(
                    FORM_ASSOCIATION_ID to SqlParameterValue(Types.BINARY, camundaFormAssociationId.asBytes())
                )
            )
            { rs, _ -> camundaFormAssociation(rs) }
        } catch (ex: EmptyResultDataAccessException) {
            null
        }
    }

    private fun camundaFormAssociation(rs: ResultSet) = FormAssociationFactory.getFormAssociation(
        if (rs.getBytes(FORM_ASSOCIATION_ID) != null) UUIDTypeDescriptor.ToBytesTransformer().parse(rs.getBytes(FORM_ASSOCIATION_ID)) else null,
        FormAssociationType.fromString(rs.getString(FORM_ASSOCIATION_TYPE)),
        rs.getString(FORM_LINK_ELEMENT_ID),
        if (rs.getBytes(FORM_LINK_FORM_ID) != null) UUIDTypeDescriptor.ToBytesTransformer().parse(rs.getBytes(FORM_LINK_FORM_ID)) else null,
        rs.getString(FORM_LINK_FLOW_ID),
        rs.getString(FORM_LINK_CUSTOM_URL),
        rs.getString(FORM_LINK_ANGULAR_STATE_URL)
    )

    companion object {
        val logger = KotlinLogging.logger {}
        private const val TABLE_NAME = "process_form_association_v2"
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
