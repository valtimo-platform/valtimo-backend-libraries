/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.opennotificaties

import com.ritense.objectsapi.domain.Abonnement
import com.ritense.objectsapi.domain.Kanaal
import com.ritense.valtimo.contract.utils.SecurityUtils
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.Date
import java.util.UUID
import mu.KotlinLogging

class OpenNotificatieClient(
    private var openNotificatieProperties: OpenNotificatieProperties
) {
    fun setProperties(properties: OpenNotificatieProperties) {
        openNotificatieProperties = properties
    }

    fun createAbonnement(abonnement: Abonnement): Abonnement {
       return requestBuilder()
            .path("/api/v1/abonnement")
            .post()
            .body(abonnement)
            .execute(Abonnement::class.java)
    }

    fun deleteAbonnement(abonnementId: UUID) {
        requestBuilder()
            .path("/api/v1/abonnement/$abonnementId")
            .delete()
            .execute()
    }

    fun getKanalen(): Collection<Kanaal> {
        return requestBuilder()
            .path("/api/v1/kanaal")
            .get()
            .executeForCollection(Kanaal::class.java)
    }

    fun createKanaal(kanaal: Kanaal) {
        requestBuilder()
            .path("/api/v1/kanaal")
            .post()
            .body(kanaal)
            .execute()
    }

    private fun requestBuilder(): RequestBuilder.Builder {
        return RequestBuilder.builder()
            .baseUrl(openNotificatieProperties.baseUrl)
            .token(generateToken())
    }

    private fun generateToken(): String {
        val signingKey = Keys.hmacShaKeyFor(openNotificatieProperties.secret.toByteArray(Charsets.UTF_8))

        val jwtBuilder = Jwts.builder()
        jwtBuilder
            .setIssuer(openNotificatieProperties.clientId)
            .setIssuedAt(Date())
            .claim("client_id", openNotificatieProperties.clientId)

        appendUserInfo(jwtBuilder)

        return jwtBuilder
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()
    }

    private fun appendUserInfo(jwtBuilder: JwtBuilder): JwtBuilder {
        val userLogin = SecurityUtils.getCurrentUserLogin()
        val userId = userLogin ?: "Valtimo"
        return jwtBuilder
            .claim("user_id", userId)
            .claim("user_representation", userId)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}