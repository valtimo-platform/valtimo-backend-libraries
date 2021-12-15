/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.service

import com.ritense.openzaak.BaseIntegrationTest
import com.ritense.openzaak.domain.mapping.impl.Operation
import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandler
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.service.impl.ZaakTypeLinkService
import com.ritense.openzaak.web.rest.request.ServiceTaskHandlerRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import javax.inject.Inject
import javax.transaction.Transactional

@Transactional
class ZaakTypeLinkServiceIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    val zaakUrl = URI.create("http://example.com")

    @Test
    fun `should create zaakTypeLink`() {
        val result = zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest("test", zaakUrl)
        )
        assertThat(result.zaakTypeLink()).isNotNull
        assertThat(result.zaakTypeLink()!!.documentDefinitionName).isEqualTo("test")
        assertThat(result.zaakTypeLink()!!.zaakTypeUrl).isEqualTo(zaakUrl)
    }

    @Test
    fun `should assign service task handler`() {
        //given
        val zaakTypeLink = zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest("test", zaakUrl)
        )
        val zaakTypeLinkId = zaakTypeLink.zaakTypeLink()!!.zaakTypeLinkId

        val serviceTaskHandlerRequest = ServiceTaskHandlerRequest("processKey", "taskId", Operation.SET_STATUS, URI.create("www.statustype.com"))

        //when
        val createServiceTaskHandlerResult = zaakTypeLinkService.assignServiceTaskHandler(zaakTypeLinkId, serviceTaskHandlerRequest)

        //then
        assertThat(createServiceTaskHandlerResult).isNotNull
        assertThat(createServiceTaskHandlerResult.zaakTypeLink()?.serviceTaskHandlers).contains(
            ServiceTaskHandler(
                serviceTaskHandlerRequest.processDefinitionKey,
                serviceTaskHandlerRequest.serviceTaskId,
                serviceTaskHandlerRequest.operation,
                serviceTaskHandlerRequest.parameter
            )
        )
    }

    @Test
    fun `should update service task handler`() {
        //given
        val zaakTypeLink = zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest("test", zaakUrl)
        )
        val zaakTypeLinkId = zaakTypeLink.zaakTypeLink()!!.zaakTypeLinkId

        val serviceTaskHandlerRequest = ServiceTaskHandlerRequest("processKey", "taskId", Operation.SET_STATUS, URI.create("www.statustype.com"))
        val newServiceTaskHandlerRequest = ServiceTaskHandlerRequest("processKey", "taskId", Operation.SET_RESULTAAT, URI.create("www.resultaattype.com"))

        zaakTypeLinkService.assignServiceTaskHandler(zaakTypeLinkId, serviceTaskHandlerRequest)

        //when
        val modifyServiceTaskHandlerResult = zaakTypeLinkService.modifyServiceTaskHandler(zaakTypeLinkId, newServiceTaskHandlerRequest)

        //then
        assertThat(modifyServiceTaskHandlerResult).isNotNull
        assertThat(modifyServiceTaskHandlerResult.zaakTypeLink()?.serviceTaskHandlers).contains(
            ServiceTaskHandler(
                newServiceTaskHandlerRequest.processDefinitionKey,
                newServiceTaskHandlerRequest.serviceTaskId,
                newServiceTaskHandlerRequest.operation,
                newServiceTaskHandlerRequest.parameter
            )
        )
        assertThat(modifyServiceTaskHandlerResult.zaakTypeLink()?.serviceTaskHandlers).doesNotContain(
            ServiceTaskHandler(
                serviceTaskHandlerRequest.processDefinitionKey,
                serviceTaskHandlerRequest.serviceTaskId,
                serviceTaskHandlerRequest.operation,
                serviceTaskHandlerRequest.parameter
            )
        )

    }

}