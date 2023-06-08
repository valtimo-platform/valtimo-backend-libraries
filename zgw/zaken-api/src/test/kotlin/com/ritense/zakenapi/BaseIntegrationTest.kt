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

package com.ritense.zakenapi

import com.ritense.authorization.AuthorizationService
import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.service.PluginService
import com.ritense.resource.service.ResourceService
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.zakenapi.service.ZaakDocumentService
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(classes = [TestApplication::class])
@ExtendWith(SpringExtension::class)
@Tag("integration")
class BaseIntegrationTest {
    @SpyBean
    lateinit var pluginService: PluginService

    @SpyBean
    lateinit var pluginConfigurationRepository: PluginConfigurationRepository

    @MockBean
    lateinit var mailSender: MailSender

    @MockBean
    lateinit var userManagementService: UserManagementService

    @Autowired
    lateinit var zaakUrlProvider: ZaakUrlProvider

    @MockBean
    lateinit var resourceProvider: ResourceProvider

    @Autowired
    lateinit var resourceService: ResourceService

    @SpyBean
    lateinit var zaakDocumentService: ZaakDocumentService

    // TODO: remove authorization service mocking when call to run without permissions is added
    @MockBean
    lateinit var authorizationService: AuthorizationService
}
