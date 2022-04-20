package com.ritense.openzaak.liquibase.changelog

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.nio.charset.Charset
import java.util.Date

class ChangeLog20220415ZaakInstanceLinkSetZaakTypeUrl : CustomTaskChange {

    override fun execute(database: Database?) {
        val connection = database!!.connection as JdbcConnection

        val zaakToken = getOpenZaakToken(connection)
        val zaakWebClient = getOpenZaakWebClient(zaakToken)
        var statement = connection.prepareStatement("SELECT zaak_instance_url FROM zaak_instance_link WHERE zaak_type_url IS NULL")
        val result = statement.executeQuery()

        while (result.next()) {
            val zaakInstanceUrl = result.getString(1)
            val zaakTypeUrl = getZaakTypeUrl(zaakWebClient, zaakInstanceUrl)
                statement =
                    connection.prepareStatement("UPDATE zaak_instance_link SET zaak_type_url = ? WHERE zaak_instance_url = ?")
                statement.setString(1, zaakTypeUrl)
                statement.setString(2, zaakInstanceUrl)
                statement.execute()
        }
    }

    private fun getOpenZaakToken(connection: JdbcConnection): String {
        val connectorProperties = getZaakConnectorProperties(connection)
        val clientId = getFieldFromJson("clientId", connectorProperties)
        val secret = getFieldFromJson("secret", connectorProperties)

        val signingKey = Keys.hmacShaKeyFor(secret.toByteArray(Charset.forName("UTF-8")))

        val jwtBuilder = Jwts.builder().setIssuer(clientId).setIssuedAt(Date()).claim("client_id", clientId)
            .claim("user_id", "SYSTEM").claim("user_representation", "SYSTEM").claim("roles", listOf(ADMIN))

        return jwtBuilder.signWith(signingKey, SignatureAlgorithm.HS256).compact()
    }

    private fun getZaakConnectorProperties(connection: JdbcConnection): String {
        var statement =
            connection.prepareStatement("SELECT connector_type_id FROM connector_type WHERE name = 'OpenZaak'")
        var result = statement.executeQuery()
        result.next()
        val connectorTypeId = result.getBytes(1)

        statement =
            connection.prepareStatement("SELECT connector_properties FROM connector_instance WHERE connector_type_id = ?")
        statement.setBytes(1, connectorTypeId)
        result = statement.executeQuery()
        result.next()
        return result.getString(1)
    }

    private fun getOpenZaakWebClient(openZaakToken: String): WebClient {
        return WebClient.builder().defaultHeader("Authorization", "Bearer $openZaakToken")
            .defaultHeader("Accept-Crs", "EPSG:4326").build()
    }

    private fun getZaakTypeUrl(webClient: WebClient, zaakInstanceUrl: String): String {
        val zaakJson = webClient.get().uri(zaakInstanceUrl).retrieve().bodyToMono<String>().block()!!
        return getFieldFromJson("zaaktype", zaakJson)
    }

    private fun getFieldFromJson(field: String, json: String): String {
        return """"$field": ?"(.+?)"""".toRegex().find(json)!!.groupValues[1]
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
}
