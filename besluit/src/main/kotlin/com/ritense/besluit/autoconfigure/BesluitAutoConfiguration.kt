package com.ritense.besluit.autoconfigure

import com.ritense.besluit.service.BesluitApiProperties
import com.ritense.besluit.service.BesluitConnector
import com.ritense.besluit.service.BesluitenService
import com.ritense.besluit.service.ServerAuthSpecification
import com.ritense.besluit.web.rest.BesluitResource
import com.ritense.openzaak.service.impl.ZaakTypeService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BesluitAutoConfiguration {

    // Connector

    @Bean
    @ConditionalOnMissingBean(BesluitConnector::class)
    fun besluitConnector(
        besluitApiProperties: BesluitApiProperties
    ) : BesluitConnector {
        return BesluitConnector(besluitApiProperties)
    }

    @Bean
    @ConditionalOnMissingBean(BesluitApiProperties::class)
    fun besluitApiConnector(
        besluitApi: ServerAuthSpecification
    ) : BesluitApiProperties {
        return BesluitApiProperties(besluitApi)
    }

    @Bean
    @ConditionalOnMissingBean(ServerAuthSpecification::class)
    fun besluitApi() : ServerAuthSpecification {
        return ServerAuthSpecification()
    }

    @Bean
    @ConditionalOnMissingBean(BesluitResource::class)
    fun besluitResource(
        zaakTypeService: ZaakTypeService
    ): BesluitResource {
        return com.ritense.besluit.web.rest.impl.BesluitResource(zaakTypeService)
    }

    // Services

    @Bean
    @ConditionalOnMissingBean(BesluitenService::class)
    fun besluitenService(
        besluitApiProperties: BesluitApiProperties
    ): BesluitenService {
        return BesluitenService(besluitApiProperties)
    }

}