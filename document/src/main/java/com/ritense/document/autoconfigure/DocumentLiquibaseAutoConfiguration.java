/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.document.autoconfigure;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@AutoConfiguration
@ConditionalOnClass(DataSource.class)
public class DocumentLiquibaseAutoConfiguration {

    @Order(HIGHEST_PRECEDENCE + 8)
    @Bean
    @ConditionalOnMissingBean(name = "documentLiquibaseMasterChangeLogLocation")
    public LiquibaseMasterChangeLogLocation documentLiquibaseMasterChangeLogLocation() {
        return new LiquibaseMasterChangeLogLocation("config/liquibase/document-master.xml");
    }

}