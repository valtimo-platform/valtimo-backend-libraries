package com.ritense.formlink.migration

import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType
import com.ritense.formlink.domain.impl.formassociation.StartEventFormAssociation
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementAngularStateUrlLink
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementUrlLink
import com.ritense.valtimo.contract.json.Mapper
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor
import mu.KotlinLogging
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor
import java.sql.Types
import java.util.UUID

/**
 * This class is used in a liquibase changelog see 20220630-storage-performance-improvement-changelog.xml
 */
internal class MigrationV2Table : CustomTaskChange {

    override fun execute(database: Database) {
        logger.info("Starting ${this::class.simpleName}")
        val connection = database.connection as JdbcConnection
        val statement = connection.prepareStatement("SELECT * FROM process_form_association")
        val result = statement.executeQuery()
        while (result.next()) {
            val processDefinitionKey = result.getString("process_definition_key")
            val formAssociationsJson = result.getString("form_associations")
            val formAssociations: List<FormAssociation> = Mapper.INSTANCE.get().readValue(formAssociationsJson)
            logger.info("Processing formAssociations json:\n${formAssociations}")
            formAssociations.forEach { formAssociation ->
                if (formAssociationExists(connection, processDefinitionKey, formAssociation)) {
                    logger.warn("Form association already exists between: $processDefinitionKey and ${formAssociation.formLink.id}")
                } else {
                    insertIntoV2Table(connection, processDefinitionKey, formAssociation)
                }
            }
        }
        logger.info("Finished ${this::class.simpleName}")
    }

    private fun formAssociationExists(
        connection: JdbcConnection,
        processDefinitionKey: String,
        formAssociation: FormAssociation
    ): Boolean {
        val statement = connection.prepareStatement(
            """
            SELECT COUNT(1) 
            FROM process_form_association_v2
            WHERE process_definition_key = ? AND form_association_form_link_element_id = ?
         """
        )
        statement.setString(1, processDefinitionKey)
        statement.setString(2, formAssociation.formLink.id)
        val result = statement.executeQuery()
        result.next()
        return result.getInt(1) >= 1
    }

    private fun insertIntoV2Table(
        connection: JdbcConnection,
        processDefinitionKey: String,
        formAssociation: FormAssociation
    ) {
        val statement = connection.prepareStatement("""
            INSERT INTO process_form_association_v2 (
                id,
                process_definition_key,
                form_association_id,
                form_association_type,
                form_association_form_link_element_id,
                form_association_form_link_form_id,
                form_association_form_link_flow_id,
                form_association_form_link_custom_url,
                form_association_form_link_angular_state_url
            )
            VALUES (
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?
            )
         """)
        statement.setObject(1, UUID.nameUUIDFromBytes(processDefinitionKey.toByteArray()).asBytes(), Types.BINARY)
        statement.setString(2, processDefinitionKey)
        statement.setObject(3, formAssociation.id.asBytes(), Types.BINARY)
        statement.setString(4, formAssociation.asType())
        statement.setString(5, formAssociation.formLink.id)

        if (formAssociation.formLink is BpmnElementFormIdLink) {
            if (formAssociation.formLink.formId != null) {
                statement.setObject(6, formAssociation.formLink.formId.asBytes(), Types.BINARY)
            } else {
                statement.setNull(6, Types.NULL)
            }
        } else {
            statement.setNull(6, Types.NULL)
        }

        if (formAssociation.formLink is BpmnElementFormFlowIdLink) {
            if (formAssociation.formLink.formFlowId != null) {
                statement.setString(7, formAssociation.formLink.formFlowId)
            } else {
                statement.setNull(7, Types.NULL)
            }
        } else {
            statement.setNull(7, Types.NULL)
        }

        if (formAssociation.formLink is BpmnElementUrlLink) {
            if (formAssociation.formLink.url != null) {
                statement.setString(8, formAssociation.formLink.url)
            } else {
                statement.setNull(8, Types.NULL)
            }
        } else {
            statement.setNull(8, Types.NULL)
        }

        if (formAssociation.formLink is BpmnElementAngularStateUrlLink) {
            if (formAssociation.formLink.url != null) {
                statement.setString(9, formAssociation.formLink.url)
            } else {
                statement.setNull(9, Types.NULL)
            }
        } else {
            statement.setNull(9, Types.NULL)
        }
        statement.executeUpdate()
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

    companion object {
        val logger = KotlinLogging.logger {}
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
