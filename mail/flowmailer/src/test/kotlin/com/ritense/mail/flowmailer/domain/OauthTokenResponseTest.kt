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

package com.ritense.mail.flowmailer.domain

import com.ritense.mail.flowmailer.BaseTest
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OauthTokenResponseTest: BaseTest() {

    @Test
    fun `should make instance of OauthTokenResponse`() {
        val oauthTokenResponse = OauthTokenResponse(
            accessToken = "accessToken",
            expiresIn = 1,
            scope = "scope",
            tokenType = "tokenType"
        )
        assertThat(oauthTokenResponse).isNotNull
        assertThat(oauthTokenResponse).isInstanceOf(OauthTokenResponse::class.java)
        assertThat(oauthTokenResponse.accessToken).isEqualTo("accessToken")
        assertThat(oauthTokenResponse.expiresIn).isEqualTo(1)
        assertThat(oauthTokenResponse.scope).isEqualTo("scope")
        assertThat(oauthTokenResponse.tokenType).isEqualTo("tokenType")
    }
}