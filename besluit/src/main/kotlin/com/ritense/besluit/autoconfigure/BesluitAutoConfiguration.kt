package com.ritense.besluit.autoconfigure

import com.ritense.besluit.service.BesluitApiProperties
import com.ritense.besluit.service.BesluitConnector
import com.ritense.besluit.service.BesluitenService
import com.ritense.besluit.service.ServerAuthSpecification
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.besluit.repository"])
@EntityScan("com.ritense.besluit.domain")
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

    // Services

    @Bean
    @ConditionalOnMissingBean(BesluitenService::class)
    fun besluitenService(
        besluitApiProperties: BesluitApiProperties
    ): BesluitenService {
        return BesluitenService(besluitApiProperties)
    }

}