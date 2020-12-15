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

import com.ritense.valtimo.config.LiquibaseRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.SchemaOperationsCommand;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

@Slf4j
@RequiredArgsConstructor
public class ValtimoSchemaOperationsCommand implements SchemaOperationsCommand {

    private final LiquibaseRunner liquibaseRunner;

    @Override
    public Void execute(CommandContext commandContext) {
        PersistenceSession persistenceSession = commandContext.getSession(PersistenceSession.class);
        persistenceSession.dbSchemaUpdate();
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