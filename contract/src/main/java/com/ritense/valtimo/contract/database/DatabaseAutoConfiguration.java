/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.contract.database;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

public class DatabaseAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = "database", havingValue = "postgres")
    public QueryDialectHelper postgresQueryDialectHelper() {
        return new PostgresQueryDialectHelper();
    }

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = "database", havingValue = "mysql", matchIfMissing = true)
    public QueryDialectHelper mysqlQueryDialectHelper() {
        return new MysqlQueryDialectHelper();
    }

}
