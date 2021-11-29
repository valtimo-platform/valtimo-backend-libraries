package com.ritense.openzaak.liquibase.changelog

import com.ritense.document.domain.impl.Mapper
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.sql.Types
import java.util.UUID

class ChangeLog20211126 : CustomTaskChange {

    override fun execute(database: Database?) {
        logger.info("Starting ${this::class.simpleName}")

        val connection = database!!.connection as JdbcConnection
        val statement = connection.prepareStatement("SELECT zaak_instance_links FROM zaak_type_link")
        val result = statement.executeQuery()

        while (result.next()) {
            val zaakInstanceLinks = result.getBytes("zaak_instance_links")
            val arrayNode = Mapper.INSTANCE.get().readTree(zaakInstanceLinks)
            logger.info("Processing zaakInstanceLinks json:\n${arrayNode}")

            for(objectNode in arrayNode) {
                // Each child will contain 4 fields: className, documentId, zaakInstanceId and zaakInstanceUrl
                // Only the last 3 are needed in the new table
                val zaakInstanceId = objectNode.get("zaakInstanceId").asText()
                val zaakInstanceUrl = objectNode.get("zaakInstanceUrl").asText()
                val documentId = objectNode.get("documentId").asText()

                insertIntoZaakInstanceLinkTable(connection, zaakInstanceId, zaakInstanceUrl, documentId)
            }
        }

        logger.info("Finished ${this::class.simpleName}")
    }

    private fun insertIntoZaakInstanceLinkTable(connection:JdbcConnection, zaakInstanceId: String, zaakInstanceUrl: String, documentId:String) {
        val statement = connection.prepareStatement("INSERT INTO zaak_instance_link (zaak_instance_link_id, zaak_instance_url, zaak_instance_id, document_id) values(?, ?, ?, ?)")
        statement.setObject(1, uuidToBytes(UUID.randomUUID()), Types.BINARY)
        statement.setString(2, zaakInstanceUrl)
        statement.setObject(3, uuidToBytes(UUID.fromString(zaakInstanceId)), Types.BINARY)
        statement.setObject(4, uuidToBytes(UUID.fromString(documentId)), Types.BINARY)

        statement.executeUpdate()
    }

    private fun uuidToBytes(uuid: UUID): ByteArray {
        val buffer = ByteBuffer.allocate(16)
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)
        return buffer.array()
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
}