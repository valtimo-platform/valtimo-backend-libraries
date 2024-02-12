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

package com.ritense.valtimo.camunda.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ritense.valtimo.contract.config.LiquibaseRunner;
import java.sql.SQLException;
import liquibase.exception.DatabaseException;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValtimoSchemaOperationsCommandTest {

    private LiquibaseRunner liquibaseRunner;
    private CommandContext commandContext;
    private ValtimoSchemaOperationsCommand valtimoSchemaOperationsCommand;

    @BeforeEach
    void setUp() {
        liquibaseRunner = mock(LiquibaseRunner.class);
        commandContext = mock(CommandContext.class);
        valtimoSchemaOperationsCommand = new ValtimoSchemaOperationsCommand(liquibaseRunner);
    }

    @Test
    void shouldNotExecuteWhenCommandContextIsNull() {
        assertThrows(NullPointerException.class, () -> {
            valtimoSchemaOperationsCommand.execute(null);
        });
    }

    @Test
    void shouldExecute() throws DatabaseException, SQLException {
        PersistenceSession persistenceSession = mock(PersistenceSession.class);
        when(commandContext.getSession(any())).thenReturn(persistenceSession);
        valtimoSchemaOperationsCommand.execute(commandContext);
        verify(liquibaseRunner).run();
    }

    @Test
    void shouldNotExecuteWhenPersistenceSessionIsNull() {
        when(commandContext.getSession(any())).thenReturn(null);
        assertThrows(NullPointerException.class, () -> {
            valtimoSchemaOperationsCommand.execute(commandContext);
        });
    }

}