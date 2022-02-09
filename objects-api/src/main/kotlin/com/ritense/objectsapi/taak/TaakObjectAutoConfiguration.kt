package com.ritense.objectsapi.taak

import com.ritense.document.service.DocumentService
import com.ritense.objectsapi.taak.initiator.BsnProvider
import com.ritense.objectsapi.taak.initiator.DocumentInitiatorProvider
import com.ritense.objectsapi.taak.initiator.KvkProvider
import com.ritense.objectsapi.taak.resolve.DocumentValueResolver
import com.ritense.objectsapi.taak.resolve.PlaceHolderValueResolver
import com.ritense.objectsapi.taak.resolve.PlaceHolderValueResolverService
import com.ritense.objectsapi.taak.resolve.ProcessVariableValueResolver
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.valtimo.service.CamundaProcessService
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class TaakObjectAutoConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun taakObjectConnector(
        taakProperties: TaakProperties,
        placeHolderValueResolverService: PlaceHolderValueResolverService,
        bsnProvider: BsnProvider,
        kvkProvider: KvkProvider
    ): TaakObjectConnector {
        return TaakObjectConnector(taakProperties, placeHolderValueResolverService, bsnProvider, kvkProvider)
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun taakProperties(): TaakProperties {
        return TaakProperties()
    }

    @Bean
    @ConditionalOnMissingBean(PlaceHolderValueResolverService::class)
    fun placeHolderValueResolverService(
        placeHolderValueResolvers: List<PlaceHolderValueResolver>
    ): PlaceHolderValueResolverService {
        return PlaceHolderValueResolverService(placeHolderValueResolvers)
    }

    @Bean
    fun documentValueResolver(
        processDocumentService: ProcessDocumentService
    ): PlaceHolderValueResolver {
        return DocumentValueResolver(processDocumentService)
    }

    @Bean
    fun processVariableValueResolver(): PlaceHolderValueResolver {
        return ProcessVariableValueResolver()
    }

    @Bean
    fun bsnProvider(
        processDocumentService: ProcessDocumentService,
        zaakInstanceLinkService: ZaakInstanceLinkService,
        zaakRolService: ZaakRolService
    ): BsnProvider {
        return DocumentInitiatorProvider(processDocumentService, zaakInstanceLinkService, zaakRolService)
    }

    @Bean
    fun kvkProvider(
        processDocumentService: ProcessDocumentService,
        zaakInstanceLinkService: ZaakInstanceLinkService,
        zaakRolService: ZaakRolService
    ): KvkProvider {
        return DocumentInitiatorProvider(processDocumentService, zaakInstanceLinkService, zaakRolService)
    }

    @Bean
    fun taakProcessDocumentService(
        processDocumentAssociationService: ProcessDocumentAssociationService,
        documentService: DocumentService,
        camundaProcessService: CamundaProcessService
    ): ProcessDocumentService {
        return ProcessDocumentService(processDocumentAssociationService, documentService, camundaProcessService)
    }
}