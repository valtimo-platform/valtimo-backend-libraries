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

package com.ritense.valtimo.contract.config;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class LiquibaseRunner {

    private static final Logger logger = LoggerFactory.getLogger(LiquibaseRunner.class);
    private final List<LiquibaseMasterChangeLogLocation> liquibaseMasterChangeLogLocations;
    private final Contexts context;
    private final DataSource datasource;

    public LiquibaseRunner(
        final List<LiquibaseMasterChangeLogLocation> liquibaseMasterChangeLogLocations,
        final LiquibaseProperties liquibaseProperties,
        final DataSource datasource
    ) {
        this.liquibaseMasterChangeLogLocations = liquibaseMasterChangeLogLocations;
        this.datasource = datasource;
        this.context = new Contexts(liquibaseProperties.getContexts());
    }

    public void run() throws SQLException, DatabaseException {
        Connection connection = datasource.getConnection();
        JdbcConnection jdbcConnection = new JdbcConnection(connection);
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
        try {
            for (LiquibaseMasterChangeLogLocation changeLogLocation : liquibaseMasterChangeLogLocations) {
                runChangeLog(database, changeLogLocation.getFilePath());
            }
        } catch (LiquibaseException e) {
            throw new DatabaseException(e);
        } finally {
            try {
                connection.rollback();
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing connection ", e);
            }
        }
        logger.info("Finished running liquibase");
    }

    @SuppressWarnings({"squid:S2095", "java:S2095"}) // Liquibase connection is closed elsewhere
    private void runChangeLog(Database database, String filePath) throws LiquibaseException {
        Liquibase liquibase = new Liquibase(filePath, new ClassLoaderResourceAccessor(), database);
        logger.info("Running liquibase master changelog: {}", liquibase.getChangeLogFile());
        liquibase.update(context);
    }
}
