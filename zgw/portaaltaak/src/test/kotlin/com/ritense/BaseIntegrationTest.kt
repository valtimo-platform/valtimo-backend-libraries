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

package com.ritense

import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.outbox.OutboxService
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.service.PluginService
import com.ritense.resource.service.ResourceService
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.service.OperatonProcessService
import com.ritense.valueresolver.ValueResolverService
import com.ritense.zakenapi.ResourceProvider
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Tag("integration")
abstract class BaseIntegrationTest {

    @MockitoSpyBean
    lateinit var pluginService: PluginService

    @MockitoSpyBean
    lateinit var pluginConfigurationRepository: PluginConfigurationRepository

    @MockitoSpyBean
    lateinit var valueResolverService: ValueResolverService

    @MockitoSpyBean
    lateinit var operatonProcessService: OperatonProcessService

    @MockitoSpyBean
    lateinit var zaakInstanceLinkService: ZaakInstanceLinkService

    @MockitoSpyBean
    lateinit var outboxService: OutboxService

    @MockitoBean
    lateinit var resourceService: ResourceService

    @MockitoBean
    lateinit var userManagementService: UserManagementService

    @MockitoBean
    lateinit var mailSender: MailSender

    @MockitoBean
    lateinit var resourceProvider: ResourceProvider

    @MockitoBean
    lateinit var zaakUrlProvider: ZaakUrlProvider

    @MockitoBean
    lateinit var zaaktypeUrlProvider: ZaaktypeUrlProvider
}
