package com.ritense.valtimo.autoconfiguration
import com.ritense.valtimo.contract.annotation.ProcessBean
import com.ritense.valtimo.job.retries.RetryConfiguration
import com.ritense.valtimo.job.retries.RetryCycleProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@EnableConfigurationProperties(RetryConfiguration::class)
@AutoConfiguration
class RetryCycleProviderAutoConfiguration {

    @Bean
    @ProcessBean
    @ConditionalOnMissingBean(RetryCycleProvider::class)
    fun retryCycleProvider(config: RetryConfiguration): RetryCycleProvider {
        return RetryCycleProvider(config)
    }
}