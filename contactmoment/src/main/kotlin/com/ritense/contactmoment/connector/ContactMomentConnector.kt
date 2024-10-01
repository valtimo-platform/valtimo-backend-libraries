/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.contactmoment.connector

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.contactmoment.client.ContactMomentClient
import com.ritense.contactmoment.domain.ContactMoment
import com.ritense.contactmoment.domain.Kanaal
import com.ritense.contactmoment.domain.request.CreateContactMomentRequest
import com.ritense.valtimo.contract.authentication.CurrentUserService
import com.ritense.valtimo.contract.authentication.ManageableUser
import com.ritense.valtimo.contract.authentication.UserManagementService
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Deprecated("Since 12.0.0. No replacement available.")
@ConnectorType(name = "ContactMoment")
class ContactMomentConnector(
    private var contactMomentProperties: ContactMomentProperties,
    private var contactMomentClient: ContactMomentClient,
    private var currentUserService: CurrentUserService,
    private var userManagementService: UserManagementService,
) : Connector {

    /**
     * Get a list of ContactMomenten
     */
    @Deprecated("Since 12.0.0")
    fun getContactMomenten(page: Int): List<ContactMoment> {
        return runBlocking { contactMomentClient.getContactMomenten(1).results }
    }

    /**
     * Create a ContactMoment
     *
     * @param text An explanation that substantively describes the customer interaction of the customer.
     * @param kanaal The communication channel through which the CONTACT MOMENT is conducted.
     */
    @Deprecated("Since 12.0.0")
    fun createContactMoment(kanaal: Kanaal, text: String): ContactMoment {
        val medewerker = userManagementService.currentUser
        val request = CreateContactMomentRequest(
            bronorganisatie = contactMomentProperties.rsin,
            registratiedatum = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            kanaal = kanaal.name,
            tekst = text,
            medewerkerIdentificatie = CreateContactMomentRequest.MedewerkerIdentificatieRequest(
                identificatie = getMedewerkerIdentificatie(medewerker),
                achternaam = medewerker.lastName,
            )
        )
        return runBlocking { contactMomentClient.createContactMoment(request) }
    }

    private fun getMedewerkerIdentificatie(user: ManageableUser): String {
        return if (user.userIdentifier.length > 24) {
            val idHash = user.userIdentifier.hashCode().toString()
            user.userIdentifier.substring(0, 24 - idHash.length) + idHash
        } else {
            user.userIdentifier
        }
    }

    @Deprecated("Since 12.0.0")
    override fun getProperties(): ConnectorProperties {
        return contactMomentProperties
    }

    @Deprecated("Since 12.0.0")
    override fun setProperties(connectorProperties: ConnectorProperties) {
        contactMomentProperties = connectorProperties as ContactMomentProperties
        contactMomentClient.contactMomentProperties = contactMomentProperties
    }
}
