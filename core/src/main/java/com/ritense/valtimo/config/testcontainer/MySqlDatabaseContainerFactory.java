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

package com.ritense.valtimo.config.testcontainer;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.JdbcDatabaseContainerProvider;
import org.testcontainers.jdbc.ConnectionUrl;
import org.testcontainers.shaded.org.apache.commons.lang.NotImplementedException;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class MySqlDatabaseContainerFactory extends JdbcDatabaseContainerProvider {

    @Override
    public boolean supports(String databaseType) {
        return databaseType.equals(BootstrapMySqlContainer.NAME);
    }

    @Override
    public JdbcDatabaseContainer newInstance() {
        throw new NotImplementedException("only newInstance by tag is supported");
    }

    @Override
    public JdbcDatabaseContainer newInstance(String tag) {
        assertArgumentNotNull(tag, "tag is required");
        return new BootstrapMySqlContainer(BootstrapMySqlContainer.IMAGE + ":" + tag);
    }

    @Override
    public JdbcDatabaseContainer newInstance(ConnectionUrl connectionUrl) {
        return newInstanceFromConnectionUrl(connectionUrl, null, null);
    }

}