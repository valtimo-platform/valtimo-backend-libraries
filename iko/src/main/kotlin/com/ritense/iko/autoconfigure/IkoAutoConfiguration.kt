package com.ritense.iko.autoconfigure

import com.ritense.iko.repository.ViewRepository
import com.ritense.iko.service.ViewService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(basePackages = ["com.ritense.iko.repository"])
@EntityScan("com.ritense.iko.domain")
class IkoAutoConfiguration {

    @Bean
    fun viewService(viewRepository: ViewRepository) = ViewService(viewRepository)

    /*   @Order(300)
       @Bean
       @ConditionalOnMissingBean(CaseHttpSecurityConfigurer::class)
       fun viewHttpSecurityConfigurer(): CaseHttpSecurityConfigurer {
           return CaseHttpSecurityConfigurer()
       }*/

    @Order(Ordered.HIGHEST_PRECEDENCE + 34)
    @ConditionalOnMissingBean(name = ["ikoLiquibaseMasterChangeLogLocation"])
    @Bean
    fun ikoLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/iko-master.xml")
    }

}