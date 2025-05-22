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

package com.ritense.outbox

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.exception.LiquibaseException
import liquibase.resource.ClassLoaderResourceAccessor
import mu.KotlinLogging
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import java.sql.SQLException
import javax.sql.DataSource

class OutboxLiquibaseRunner(
    liquibaseProperties: LiquibaseProperties,
    private val datasource: DataSource,
) : InitializingBean {
    private val context: Contexts = Contexts(liquibaseProperties.contexts)

    @Throws(SQLException::class, DatabaseException::class)
    override fun afterPropertiesSet() {
        val connection = datasource.connection
        val jdbcConnection = JdbcConnection(connection)
        val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection)
        try {
            val liquibase = Liquibase(LIQUIBASE_CHANGE_LOG_LOCATION, ClassLoaderResourceAccessor(), database)
            logger.info("Running liquibase master changelog: {}", liquibase.changeLogFile)
            liquibase.update(context)
        } catch (liquibaseException: LiquibaseException) {
            throw DatabaseException(liquibaseException)
        } finally {
            try {
                connection.rollback()
                connection.close()
            } catch (sqlException: SQLException) {
                logger.error("Error closing connection ", sqlException)
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}

        private const val LIQUIBASE_CHANGE_LOG_LOCATION = "config/liquibase/outbox-master.xml"
    }
}
