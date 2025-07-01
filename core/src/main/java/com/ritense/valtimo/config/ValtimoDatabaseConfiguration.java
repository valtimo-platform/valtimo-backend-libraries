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

package com.ritense.valtimo.config;

import javax.sql.DataSource;
import org.operaton.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.operaton.bpm.spring.boot.starter.configuration.OperatonDatasourceConfiguration;
import org.operaton.bpm.spring.boot.starter.configuration.impl.AbstractOperatonConfiguration;
import org.operaton.bpm.spring.boot.starter.property.DatabaseProperty;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

@AutoConfiguration
public class ValtimoDatabaseConfiguration extends AbstractOperatonConfiguration implements OperatonDatasourceConfiguration {

    protected final PlatformTransactionManager transactionManager;
    protected final DataSource dataSource;

    public ValtimoDatabaseConfiguration(PlatformTransactionManager transactionManager, DataSource dataSource) {
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
    }

    @Override
    public void preInit(SpringProcessEngineConfiguration configuration) {
        final DatabaseProperty database = operatonBpmProperties.getDatabase();
        configuration.setTransactionManager(transactionManager);
        configuration.setDataSource(dataSource);
        configuration.setDatabaseType(database.getType());
        configuration.setDatabaseSchemaUpdate(database.getSchemaUpdate());
        if (StringUtils.hasText(database.getTablePrefix())) {
            configuration.setDatabaseTablePrefix(database.getTablePrefix());
        }
    }

}