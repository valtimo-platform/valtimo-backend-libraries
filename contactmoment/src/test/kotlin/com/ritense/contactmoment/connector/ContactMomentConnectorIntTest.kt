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

package com.ritense.contactmoment.connector

import com.ritense.contactmoment.BaseContactMomentIntegrationTest
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.`when`
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient

@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ContactMomentConnectorIntTest : BaseContactMomentIntegrationTest() {

    @Test
    fun `should get list of contactmomenten`() {

        val contactMomenten = (contactMomentConnector as ContactMomentConnector).getContactMomenten(1)

        Assertions.assertThat(contactMomenten).hasSize(1)
        Assertions.assertThat(contactMomenten[0].tekst).isEqualTo("content-1")
    }

    @Test
    fun `should create contactmoment`() {
        val medewerker = ValtimoUser()
        medewerker.id = "test-id"
        `when`(currentUserService.currentUser).thenReturn(medewerker)

        val contactMoment = (contactMomentConnector as ContactMomentConnector).createContactMoment("mail", "content-2")

        Assertions.assertThat(contactMoment.tekst).isEqualTo("content-2")
    }

}