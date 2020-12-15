/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.autoconfigure;

import com.ritense.valtimo.config.LiquibaseRunner;
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Configuration
@ConditionalOnClass(DataSource.class)
@EnableConfigurationProperties(value = LiquibaseProperties.class)
public class LiquibaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LiquibaseRunner.class)
    public LiquibaseRunner liquibaseRunner(
        final List<LiquibaseMasterChangeLogLocation> liquibaseMasterChangeLogLocations,
        final LiquibaseProperties liquibaseProperties,
        final DataSource datasource
    ) {
        return new LiquibaseRunner(liquibaseMasterChangeLogLocations, liquibaseProperties, datasource);
    }

    @Order(HIGHEST_PRECEDENCE + 1)
    @Bean
    public LiquibaseMasterChangeLogLocation coreLiquibaseMasterChangeLogLocation() {
        return new LiquibaseMasterChangeLogLocation("config/liquibase/valtimo-master.xml");
    }

}