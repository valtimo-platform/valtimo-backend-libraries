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

package com.ritense.dashboard.liquibase

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.LiquibaseException
import liquibase.resource.ClassLoaderResourceAccessor
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import java.sql.SQLException
import javax.sql.DataSource

class LiquibaseRunner(
    liquibaseProperties: LiquibaseProperties,
    datasource: DataSource
) {

    init {
        val context = Contexts(liquibaseProperties.contexts)
        val connection = datasource.connection
        val jdbcConnection = JdbcConnection(connection)
        val database: Database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection)
        try {
            val liquibase = Liquibase("config/liquibase/dashboard-master.xml", ClassLoaderResourceAccessor(), database)
            logger.info("Running liquibase master changelog: {}", liquibase.changeLogFile)
            liquibase.update(context)
        } catch (e: LiquibaseException) {
            throw liquibase.exception.DatabaseException(e)
        } finally {
            try {
                connection.rollback()
                connection.close()
            } catch (e: SQLException) {
                logger.error("Error closing connection ", e)
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
