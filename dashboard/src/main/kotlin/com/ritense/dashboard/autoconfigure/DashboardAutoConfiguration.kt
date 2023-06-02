/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.dashboard.autoconfigure

import com.ritense.dashboard.liquibase.LiquibaseRunner
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.MySqlDialect
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.orm.hibernate5.LocalSessionFactoryBean
import org.springframework.r2dbc.core.DatabaseClient
import javax.sql.DataSource

@Configuration
@EnableR2dbcRepositories(entityOperationsRef = "valtimoR2dbcEntityTemplate")
class DataProviderAutoConfiguration {


    @Bean(name = ["entityManagerFactory"])
    fun sessionFactory(): LocalSessionFactoryBean {
        return LocalSessionFactoryBean()
    }

    @ConditionalOnMissingBean(name = ["valtimoR2dbcConnectionFactory"])
    @Bean
    @Qualifier(value = "valtimoR2dbcConnectionFactory")
    fun valtimoR2dbcConnectionFactory(): ConnectionFactory {
        return ConnectionFactories.get("r2dbc:postgresql://localhost:5444/gzac-core-db")
    }

    @ConditionalOnMissingBean(name = ["valtimoR2dbcEntityTemplate"])
    @Bean
    fun valtimoR2dbcEntityTemplate(
        @Qualifier("valtimoR2dbcConnectionFactory") connectionFactory: ConnectionFactory
    ): R2dbcEntityOperations {
        val strategy = DefaultReactiveDataAccessStrategy(MySqlDialect.INSTANCE)
        val databaseClient = DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .bindMarkers(MySqlDialect.INSTANCE.bindMarkersFactory)
            .build()
        return R2dbcEntityTemplate(databaseClient, strategy)
    }

    @Order(HIGHEST_PRECEDENCE + 29)
    @Bean
    @ConditionalOnMissingBean(LiquibaseRunner::class)
    fun dashboardLiquibaseRunner(
        liquibaseProperties: LiquibaseProperties,
        datasource: DataSource,
    ): LiquibaseRunner {
        return LiquibaseRunner(
            liquibaseProperties,
            datasource,
        )
    }


}
