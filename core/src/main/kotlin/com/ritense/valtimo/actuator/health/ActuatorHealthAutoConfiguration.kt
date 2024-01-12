package com.ritense.valtimo.actuator.health

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class ActuatorHealthAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ValtimoHealthAggregator::class)
    fun valtimoHealthAggregator(): ValtimoHealthAggregator {
        return ValtimoHealthAggregator()
    }
}