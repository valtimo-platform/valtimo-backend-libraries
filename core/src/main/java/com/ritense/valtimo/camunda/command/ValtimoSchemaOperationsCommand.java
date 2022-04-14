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

package com.ritense.valtimo.camunda.command;

import com.ritense.valtimo.contract.config.LiquibaseRunner;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.SchemaOperationsCommand;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSession;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.slf4j.Logger;

public class ValtimoSchemaOperationsCommand implements SchemaOperationsCommand {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ValtimoSchemaOperationsCommand.class);
    private final LiquibaseRunner liquibaseRunner;

    public ValtimoSchemaOperationsCommand(LiquibaseRunner liquibaseRunner) {
        this.liquibaseRunner = liquibaseRunner;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        PersistenceSession persistenceSession = commandContext.getSession(PersistenceSession.class);
        persistenceSession.dbSchemaUpdate();

        // TODO: not this
        if (persistenceSession instanceof DbSqlSession) {
            try {
                ((DbSqlSession) persistenceSession).getSqlSession().getConnection().commit();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        persistenceSession.close();

        try {
            liquibaseRunner.run();
        } catch (Exception e) {
            throw new RuntimeException("Error running liquibaseRunner", e);
        }
        logger.debug("Camunda schema updated");
        return null;
    }

}