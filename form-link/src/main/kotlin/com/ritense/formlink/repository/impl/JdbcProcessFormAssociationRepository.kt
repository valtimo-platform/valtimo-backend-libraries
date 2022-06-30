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
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociation
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociationId
import com.ritense.formlink.domain.impl.formassociation.FormAssociationFactory
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType
import com.ritense.formlink.domain.impl.formassociation.FormAssociations
import com.ritense.formlink.domain.impl.formassociation.StartEventFormAssociation
import com.ritense.formlink.repository.ProcessFormAssociationRepository
import mu.KotlinLogging
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import java.sql.ResultSet
import java.util.Optional
import java.util.UUID

/* TABLE process_form_association_v2
*   id: BINARY(16)
*   process_definition_key: VARCHAR(64)
*   form_association_id : BINARY(16)
*   form_association_type : VARCHAR(64)
*   form_association_form_link_element_id : VARCHAR(512)
*   form_association_form_link_form_id : BINARY(16)
*   form_association_form_link_flow_id : VARCHAR(512)
*   form_association_form_link_custom_url : VARCHAR(512)
*   form_association_form_link_angular_state_url : VARCHAR(512)
* */
class JdbcProcessFormAssociationRepository(
    private val jdbcTemplate: JdbcTemplate
) : ProcessFormAssociationRepository {

    override fun get(processDefinitionKey: String): CamundaProcessFormAssociation? {
        val formAssociations = FormAssociations()
        var id: UUID? = null
        val sql = "SELECT * FROM $TABLE_NAME WHERE $PROCESS_DEFINITION_KEY_COLUMN = '$processDefinitionKey'"
        jdbcTemplate.query(sql) { rs: ResultSet, _: Int ->
            if (id == null) {
                id = UUIDTypeDescriptor.ToBytesTransformer().parse(rs.getBytes(ID_COLUMN))
            }
            formAssociations.add(
                camundaFormAssociation(rs)
            )
        }
        return CamundaProcessFormAssociation(
            CamundaProcessFormAssociationId.existingId(id),
            processDefinitionKey,
            formAssociations
        )
    }

    override fun add(formAssociationId: UUID, processDefinitionKey: String, camundaFormAssociation: CamundaFormAssociation) {
        SimpleJdbcInsert(jdbcTemplate)
            .withTableName(TABLE_NAME)
            .apply {
                execute(
                    mapOf(
                        ID_COLUMN to formAssociationId.asBytes(),
                        PROCESS_DEFINITION_KEY_COLUMN to processDefinitionKey,
                        FORM_ASSOCIATION_ID to camundaFormAssociation.id.asBytes(),
                        FORM_ASSOCIATION_TYPE to camundaFormAssociation.asType(),
                        FORM_LINK_ELEMENT_ID to camundaFormAssociation.formLink.id,
                        FORM_LINK_FORM_ID to camundaFormAssociation.formLink.formId.asBytes(),
                        FORM_LINK_FLOW_ID to camundaFormAssociation.formLink.formFlowId,
                        FORM_LINK_CUSTOM_URL to camundaFormAssociation.formLink.url,
                        FORM_LINK_ANGULAR_STATE_URL to camundaFormAssociation.formLink.url
                    )
                )
            }
    }

    override fun update(processDefinitionKey: String, camundaFormAssociation: CamundaFormAssociation) {
        TODO("Not yet implemented")
    }

    override fun findAssociationsByProcessDefinitionKey(processDefinitionKey: String): Set<CamundaFormAssociation> {
        val sql = "SELECT * FROM $TABLE_NAME WHERE $PROCESS_DEFINITION_KEY_COLUMN = $processDefinitionKey"
        val associations = mutableSetOf<CamundaFormAssociation>()
        jdbcTemplate.query(sql) { rs: ResultSet, _: Int -> associations.add(camundaFormAssociation(rs)) }
        return associations
    }

    override fun findByProcessDefinitionKeyAndCamundaFormAssociationId(processDefinitionKey: String, camundaFormAssociationId: UUID): Optional<CamundaFormAssociation> {
        TODO("Not yet implemented")
    }

    override fun findByFormLinkId(formLinkId: String): CamundaFormAssociation? {
        val sql = "SELECT * FROM $TABLE_NAME WHERE $FORM_LINK_ELEMENT_ID = ?"
        return jdbcTemplate.queryForObject(sql, { rs, _ -> camundaFormAssociation(rs) }, formLinkId)
    }

    override fun findStartEventAssociation(processDefinitionKey: String): CamundaFormAssociation? {
        val sql = """
            SELECT  *
            FROM    $TABLE_NAME
            WHERE   $PROCESS_DEFINITION_KEY_COLUMN = ?
            AND     $FORM_ASSOCIATION_TYPE = 'start-event'
        """.trimIndent()
        return jdbcTemplate.queryForObject(sql, { rs, _ -> camundaFormAssociation(rs) }, processDefinitionKey)
    }

    override fun removeByProcessDefinitionKeyAndFormAssociationId(processDefinitionKey: String, formAssociationId: UUID) {
        val sql = "DELETE FROM $TABLE_NAME WHERE $PROCESS_DEFINITION_KEY_COLUMN = ? AND $FORM_ASSOCIATION_ID = ?"
        val result = jdbcTemplate.update(sql, arrayOf(processDefinitionKey, formAssociationId.asBytes()))
        require(result == 1)
    }

    override fun findByCamundaFormAssociationId(camundaFormAssociationId: UUID): CamundaFormAssociation? {
        val sql = "SELECT * FROM $TABLE_NAME WHERE $FORM_ASSOCIATION_ID = ?"
        return jdbcTemplate.queryForObject(sql, { rs, _ -> camundaFormAssociation(rs) }, camundaFormAssociationId.asBytes())
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