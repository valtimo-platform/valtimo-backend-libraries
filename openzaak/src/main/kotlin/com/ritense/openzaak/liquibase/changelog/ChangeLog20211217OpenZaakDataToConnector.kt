package com.ritense.openzaak.liquibase.changelog

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.sql.ResultSet
import java.sql.Types
import java.util.UUID

class ChangeLog20211217OpenZaakDataToConnector : CustomTaskChange {

    override fun execute(database: Database?) {
        logger.info("Starting ${this::class.simpleName}")

        val connection = database!!.connection as JdbcConnection

        if (openZaakConnectorExists(connection)) {
            logger.info("An OpenZaak connector already exists. No migration is performed")
            return
        }

        if (!connectorTypeExists(connection)) {
            addConnectorType(connection)
        }

        val statement = connection.prepareStatement("SELECT open_zaak_config_id, client_id, secret, url, rsin FROM open_zaak_config")
        val result = statement.executeQuery()
        while (result.next()) {
            val jsonString = queryResultToJson(result)
            insertIntoConnectorInstance(connection, jsonString)
            cleanOpenZaakConfigTable(connection, result)
        }
    }

    private fun openZaakConnectorExists(connection: JdbcConnection): Boolean {
        val statement = connection.prepareStatement("SELECT * from connector_instance where name like '%OpenZaak%'")
        val result = statement.executeQuery()

        return result.isBeforeFirst
    }

    private fun connectorTypeExists(connection: JdbcConnection): Boolean {
        val statement = connection.prepareStatement("SELECT * from connector_type where name = 'OpenZaak'")
        val result = statement.executeQuery()

        return result.isBeforeFirst
    }

    private fun addConnectorType(connection: JdbcConnection) {
        val statement = connection.prepareStatement("INSERT INTO connector_type (connector_type_id, class_name, name, allow_multiple) values( ?, ?, ?, ?)")
        statement.setObject(1, uuidToBytes(UUID.randomUUID()), Types.BINARY)
        statement.setString(2, "temporary")
        statement.setString(3, "OpenZaak")
        statement.setBoolean(4, false)

        statement.executeUpdate()
    }

    private fun queryResultToJson(result: ResultSet): String {
        val url = result.getString("url")
        val rsin = result.getString("rsin")
        val secret = result.getString("secret")
        val clientId = result.getString("client_id")

        // Creating the json containing the configuration
        val mapper = ObjectMapper()
        val rootNode = mapper.createObjectNode()
        rootNode.put("className", "com.ritense.openzaak.domain.connector.OpenZaakProperties")
        val configNode = mapper.createObjectNode()
        configNode.put("url", url)
        configNode.put("rsin", rsin)
        configNode.put("secret", secret)
        configNode.put("clientId", clientId)

        rootNode.set<ObjectNode>("openZaakConfig", configNode)

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode)
    }

    private fun insertIntoConnectorInstance(connection: JdbcConnection, jsonString: String) {
        val statement = connection.prepareStatement("INSERT INTO connector_instance (connector_instance_id, name, connector_type_id, connector_properties) values(?, ?, ?, ?)")
        statement.setObject(1, uuidToBytes(UUID.randomUUID()), Types.BINARY)
        statement.setString(2, "OpenZaakConnector")
        statement.setObject(3, getConnectorTypeIdBytes(connection), Types.BINARY)
        statement.setString(4, jsonString)

        statement.executeUpdate()
    }

    private fun cleanOpenZaakConfigTable(connection: JdbcConnection, result: ResultSet) {
        val uuidBytes = result.getBytes("open_zaak_config_id")
        val statement = connection.prepareStatement("DELETE FROM open_zaak_config where open_zaak_config_id = ?")
        statement.setBytes(1, uuidBytes)

        statement.executeUpdate()
    }

    private fun getConnectorTypeIdBytes(connection: JdbcConnection): ByteArray {
        val statement = connection.prepareStatement("SELECT connector_type_id FROM connector_type WHERE name = 'OpenZaak'")
        val result = statement.executeQuery()

        var uuidBytes = ByteArray(16)
        while(result.next()) {
            uuidBytes = result.getBytes("connector_type_id")
        }

        return uuidBytes
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