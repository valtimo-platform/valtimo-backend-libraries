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

package com.ritense

import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.specification.impl.NoopAuthorizationSpecificationFactory
import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.service.PluginService
import com.ritense.resource.service.ResourceService
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valueresolver.ValueResolverService
import com.ritense.zakenapi.ResourceProvider
import com.ritense.zakenapi.ZaakUrlProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Tag("integration")
abstract class BaseIntegrationTest {

    @SpyBean
    lateinit var pluginService: PluginService

    @SpyBean
    lateinit var pluginConfigurationRepository: PluginConfigurationRepository

    @SpyBean
    lateinit var valueResolverService: ValueResolverService

    @SpyBean
    lateinit var camundaProcessService: CamundaProcessService

    @MockBean
    lateinit var resourceService: ResourceService

    @MockBean
    lateinit var userManagementService: UserManagementService

    @MockBean
    lateinit var mailSender: MailSender

    @MockBean
    lateinit var resourceProvider: ResourceProvider

    @MockBean
    lateinit var zaakUrlProvider: ZaakUrlProvider

    @MockBean
    lateinit var zaaktypeUrlProvider: ZaaktypeUrlProvider

    // TODO: remove authorization service mocking when call to run without permissions is added
    @MockBean
    lateinit var authorizationService: AuthorizationService

    @Autowired
    lateinit var noopAuthorizationSpecificationFactory: NoopAuthorizationSpecificationFactory<CamundaTask>

    @BeforeEach
    fun beforeEach() {
        val noopAuthSpec: AuthorizationSpecification<CamundaTask> = noopAuthorizationSpecificationFactory.create(
            EntityAuthorizationRequest(CamundaTask::class.java, CamundaTaskActionProvider.VIEW),
            listOf()
        )
        whenever(
            authorizationService.getAuthorizationSpecification(
                any<AuthorizationRequest<CamundaTask>>(),
                eq<List<Permission>?>(null)
            )
        ).thenReturn(noopAuthSpec)
    }
}
