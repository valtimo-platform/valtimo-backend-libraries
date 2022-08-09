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

package com.ritense.openzaak.autoconfigure

import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import com.ritense.connector.repository.ConnectorTypeRepository
import com.ritense.document.service.DocumentService
import com.ritense.openzaak.domain.connector.OpenZaakConnector
import com.ritense.openzaak.domain.connector.OpenZaakProperties
import com.ritense.openzaak.form.OpenZaakFormFieldDataResolver
import com.ritense.openzaak.listener.DocumentCreatedListener
import com.ritense.openzaak.listener.EigenschappenSubmittedListener
import com.ritense.openzaak.listener.GlobalZaakEventListener
import com.ritense.openzaak.listener.OpenZaakUndeployDocumentDefinitionEventListener
import com.ritense.openzaak.listener.ServiceTaskListener
import com.ritense.openzaak.plugin.OpenZaakPluginFactory
import com.ritense.openzaak.provider.BsnProvider
import com.ritense.openzaak.provider.KvkProvider
import com.ritense.openzaak.provider.ZaakBsnProvider
import com.ritense.openzaak.provider.ZaakKvkProvider
import com.ritense.openzaak.repository.InformatieObjectTypeLinkRepository
import com.ritense.openzaak.repository.ZaakInstanceLinkRepository
import com.ritense.openzaak.repository.ZaakTypeLinkRepository
import com.ritense.openzaak.service.DocumentenService
import com.ritense.openzaak.service.TokenGeneratorService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.openzaak.service.impl.EigenschapService
import com.ritense.openzaak.service.impl.InformatieObjectTypeLinkService
import com.ritense.openzaak.service.impl.OpenZaakConfigService
import com.ritense.openzaak.service.impl.OpenZaakTokenGeneratorService
import com.ritense.openzaak.service.impl.ZaakInstanceLinkService
import com.ritense.openzaak.service.impl.ZaakProcessService
import com.ritense.openzaak.service.impl.ZaakResultaatService
import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.openzaak.service.impl.ZaakStatusService
import com.ritense.openzaak.service.impl.ZaakTypeLinkService
import com.ritense.openzaak.service.impl.ZaakTypeService
import com.ritense.openzaak.web.rest.impl.InformatieObjectTypeLinkResource
import com.ritense.openzaak.web.rest.impl.InformatieObjectTypeResource
import com.ritense.openzaak.web.rest.impl.OpenZaakConfigResource
import com.ritense.openzaak.web.rest.impl.ResultaatResource
import com.ritense.openzaak.web.rest.impl.StatusResource
import com.ritense.openzaak.web.rest.impl.ZaakTypeLinkResource
import com.ritense.openzaak.web.rest.impl.ZaakTypeResource
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import org.camunda.bpm.engine.RepositoryService
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.client.RestTemplate
import kotlin.contracts.ExperimentalContracts

@Configuration
class OpenZaakPluginAutoConfiguration {

    @Bean
    fun openZaakPluginFactory(
        pluginService: PluginService,
        tokenGeneratorService: TokenGeneratorService
    ): OpenZaakPluginFactory {
        return OpenZaakPluginFactory(pluginService, tokenGeneratorService)
    }
}