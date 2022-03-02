package com.ritense.besluit.autoconfigure

import com.ritense.besluit.service.BesluitApiProperties
import com.ritense.besluit.service.BesluitConnector
import com.ritense.besluit.service.BesluitService
import com.ritense.besluit.service.ServerAuthSpecification
import com.ritense.besluit.web.rest.BesluitResource
import com.ritense.openzaak.besluit.BesluitClient
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
        besluitService: BesluitService,
    ): BesluitResource {
        return com.ritense.besluit.web.rest.impl.BesluitResource(besluitService)
    }

    // Services

    @Bean
    @ConditionalOnMissingBean(BesluitService::class)
    fun besluitService(
        besluitClient: BesluitClient,
    ): BesluitService {
        return BesluitService(besluitClient)
    }

}