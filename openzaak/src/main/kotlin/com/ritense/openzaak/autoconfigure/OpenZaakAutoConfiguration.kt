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

import com.ritense.document.service.DocumentService
import com.ritense.openzaak.form.OpenZaakFormFieldDataResolver
import com.ritense.openzaak.listener.EigenschappenSubmittedListener
import com.ritense.openzaak.listener.GlobalZaakEventListener
import com.ritense.openzaak.listener.OpenZaakUndeployDocumentDefinitionEventListener
import com.ritense.openzaak.listener.ServiceTaskListener
import com.ritense.openzaak.repository.InformatieObjectTypeLinkRepository
import com.ritense.openzaak.repository.OpenZaakConfigRepository
import com.ritense.openzaak.repository.ZaakTypeLinkRepository
import com.ritense.openzaak.repository.converter.Encryptor
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.openzaak.service.DocumentenService
import com.ritense.openzaak.service.impl.EigenschapService
import com.ritense.openzaak.service.impl.InformatieObjectTypeLinkService
import com.ritense.openzaak.service.impl.OpenZaakConfigService
import com.ritense.openzaak.service.impl.OpenZaakTokenGeneratorService
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
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.client.RestTemplate

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.openzaak.repository"])
@EntityScan("com.ritense.openzaak.domain")
class OpenZaakAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Encryptor::class)
    fun encryptor(@Value("\${openzaak.superSecret}") superSecret: String): Encryptor {
        return Encryptor(superSecret)
    }

    @Bean
    @ConditionalOnMissingBean(RestTemplate::class)
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder().build()
    }

    // FormFieldDataResolver
    @Bean
    @ConditionalOnMissingBean(OpenZaakFormFieldDataResolver::class)
    fun openZaakFormFieldDataResolver(
        zaakService: ZaakService,
        zaakTypeLinkService: ZaakTypeLinkService
    ): OpenZaakFormFieldDataResolver {
        return OpenZaakFormFieldDataResolver(zaakService, zaakTypeLinkService)
    }

    //Services

    @Bean
    @ConditionalOnMissingBean(OpenZaakTokenGeneratorService::class)
    fun openzaakTokenGeneratorService(): OpenZaakTokenGeneratorService {
        return OpenZaakTokenGeneratorService()
    }

    @Bean
    @ConditionalOnMissingBean(OpenZaakConfigService::class)
    fun openZaakConfigService(
        openZaakConfigRepository: OpenZaakConfigRepository,
        tokenGeneratorService: OpenZaakTokenGeneratorService,
        restTemplate: RestTemplate
    ): OpenZaakConfigService {
        return OpenZaakConfigService(openZaakConfigRepository, tokenGeneratorService, restTemplate)
    }

    @Bean
    @ConditionalOnMissingBean(ZaakService::class)
    fun zaakService(
        restTemplate: RestTemplate,
        openZaakConfigService: OpenZaakConfigService,
        tokenGeneratorService: OpenZaakTokenGeneratorService,
        zaakTypeLinkService: ZaakTypeLinkService,
        documentService: DocumentService
    ): ZaakService {
        return ZaakService(
            restTemplate,
            openZaakConfigService,
            tokenGeneratorService,
            zaakTypeLinkService,
            documentService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ZaakTypeService::class)
    fun zaakTypeService(
        restTemplate: RestTemplate,
        openZaakConfigService: OpenZaakConfigService,
        tokenGeneratorService: OpenZaakTokenGeneratorService
    ): ZaakTypeService {
        return ZaakTypeService(restTemplate, openZaakConfigService, tokenGeneratorService)
    }

    @Bean
    @ConditionalOnMissingBean(ZaakStatusService::class)
    fun zaakStatusService(
        restTemplate: RestTemplate,
        openZaakConfigService: OpenZaakConfigService,
        tokenGeneratorService: OpenZaakTokenGeneratorService
    ): ZaakStatusService {
        return ZaakStatusService(restTemplate, openZaakConfigService, tokenGeneratorService)
    }

    @Bean
    @ConditionalOnMissingBean(ZaakTypeLinkService::class)
    fun zaakTypeLinkService(
        zaakTypeLinkRepository: ZaakTypeLinkRepository,
        processDocumentAssociationService: ProcessDocumentAssociationService
    ): ZaakTypeLinkService {
        return ZaakTypeLinkService(zaakTypeLinkRepository, processDocumentAssociationService)
    }

    @Bean
    @ConditionalOnMissingBean(ZaakResultaatService::class)
    fun zaakResultaatService(
        restTemplate: RestTemplate,
        openZaakConfigService: OpenZaakConfigService,
        tokenGeneratorService: OpenZaakTokenGeneratorService
    ): ZaakResultaatService {
        return ZaakResultaatService(restTemplate, openZaakConfigService, tokenGeneratorService)
    }

    @Bean
    @ConditionalOnMissingBean(EigenschapService::class)
    fun eigenschapService(
        restTemplate: RestTemplate,
        openZaakConfigService: OpenZaakConfigService,
        tokenGeneratorService: OpenZaakTokenGeneratorService
    ): EigenschapService {
        return EigenschapService(restTemplate, openZaakConfigService, tokenGeneratorService)
    }

    @Bean
    @ConditionalOnMissingBean(InformatieObjectTypeLinkService::class)
    fun informatieObjectTypeLinkService(
        informatieObjectTypeLinkRepository: InformatieObjectTypeLinkRepository
    ): InformatieObjectTypeLinkService {
        return InformatieObjectTypeLinkService(informatieObjectTypeLinkRepository)
    }

    @Bean
    @ConditionalOnMissingBean(ZaakRolService::class)
    fun zaakRolService(
        restTemplate: RestTemplate,
        openZaakConfigService: OpenZaakConfigService,
        tokenGeneratorService: OpenZaakTokenGeneratorService
    ): ZaakRolService {
        return com.ritense.openzaak.service.impl.ZaakRolService(restTemplate, openZaakConfigService, tokenGeneratorService)
    }

    @Bean
    @ConditionalOnMissingBean(ServiceTaskListener::class)
    fun serviceTaskListener(
        zaakTypeLinkService: ZaakTypeLinkService,
        documentService: DocumentService
    ): ServiceTaskListener {
        return ServiceTaskListener(zaakTypeLinkService, documentService)
    }

    @Bean
    @ConditionalOnMissingBean(GlobalZaakEventListener::class)
    fun globalZaakEventListener(zaakService: ZaakService): GlobalZaakEventListener {
        return GlobalZaakEventListener(zaakService)
    }

    @Bean
    @ConditionalOnMissingBean(EigenschappenSubmittedListener::class)
    fun eigenschappenSubmittedListener(
        zaakTypeLinkService: ZaakTypeLinkService,
        eigenschapService: EigenschapService,
        zaakService: ZaakService
    ): EigenschappenSubmittedListener {
        return EigenschappenSubmittedListener(zaakTypeLinkService, eigenschapService, zaakService)
    }

    @Bean
    @ConditionalOnMissingBean(OpenZaakUndeployDocumentDefinitionEventListener::class)
    fun openZaakUndeployDocumentDefinitionEventListener(
        zaakTypeLinkService: ZaakTypeLinkService
    ): OpenZaakUndeployDocumentDefinitionEventListener {
        return OpenZaakUndeployDocumentDefinitionEventListener(zaakTypeLinkService)
    }

    @Bean
    @ConditionalOnMissingBean(DocumentenService::class)
    fun documentenService(
        restTemplate: RestTemplate,
        openZaakConfigService: OpenZaakConfigService,
        openZaakTokenGeneratorService: OpenZaakTokenGeneratorService,
        informatieObjectTypeLinkService: InformatieObjectTypeLinkService,
        zaakTypeLinkService: ZaakTypeLinkService
    ): DocumentenService {
        return com.ritense.openzaak.service.impl.DocumentenService(
            restTemplate,
            openZaakConfigService,
            openZaakTokenGeneratorService,
            informatieObjectTypeLinkService,
            zaakTypeLinkService
        )
    }

    //Resources
    @Bean
    @ConditionalOnMissingBean(OpenZaakConfigResource::class)
    fun openZaakConfigResource(
        openZaakConfigService: OpenZaakConfigService
    ): OpenZaakConfigResource {
        return OpenZaakConfigResource(openZaakConfigService)
    }

    @Bean
    @ConditionalOnMissingBean(ZaakTypeResource::class)
    fun zaakTypeResource(
        zaakTypeService: ZaakTypeService
    ): ZaakTypeResource {
        return ZaakTypeResource(zaakTypeService)
    }

    @Bean
    @ConditionalOnMissingBean(ZaakTypeLinkResource::class)
    fun zaakTypeLinkResource(
        zaakTypeLinkService: ZaakTypeLinkService
    ): ZaakTypeLinkResource {
        return ZaakTypeLinkResource(zaakTypeLinkService)
    }

    @Bean
    @ConditionalOnMissingBean(ResultaatResource::class)
    fun resultaatResource(
        zaakResultaatService: ZaakResultaatService
    ): ResultaatResource {
        return ResultaatResource(zaakResultaatService)
    }

    @Bean
    @ConditionalOnMissingBean(StatusResource::class)
    fun statusResource(
        zaakStatusService: ZaakStatusService
    ): StatusResource {
        return StatusResource(zaakStatusService)
    }

    @Bean
    @ConditionalOnMissingBean(InformatieObjectTypeLinkResource::class)
    fun informatieObjectTypeLinkResource(
        informatieObjectTypeLinkService: InformatieObjectTypeLinkService
    ): InformatieObjectTypeLinkResource {
        return InformatieObjectTypeLinkResource(informatieObjectTypeLinkService)
    }

    @Bean
    @ConditionalOnMissingBean(InformatieObjectTypeResource::class)
    fun informatieObjectTypeResource(
        zaakService: ZaakService
    ): InformatieObjectTypeResource {
        return InformatieObjectTypeResource(zaakService)
    }

}