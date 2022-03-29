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

package com.ritense.testutilscommon.junit.extension;

import com.ritense.valtimo.contract.config.LiquibaseRunner;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public class LiquibaseRunnerExtension
    implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static final Logger logger = LoggerFactory.getLogger(LiquibaseRunnerExtension.class);
    private static boolean started = false;
    // Gate keeper to prevent multiple Threads within the same routine
    static final Lock lock = new ReentrantLock();

    @Override
    public void beforeAll(final ExtensionContext extensionContext) throws Exception {
        // lock the access so only one Thread has access to it
        lock.lock();
        if (!started) {
            started = true;

            ApplicationContext springContext = SpringExtension.getApplicationContext(extensionContext);
            LiquibaseRunner liquibaseRunner = springContext.getBean(LiquibaseRunner.class);

            // Your "before all tests" startup logic goes here
            // The following line registers a callback hook when the root test context is
            // shut down
            extensionContext.getRoot().getStore(GLOBAL).put("Liquibase - setup trigger", this);

            // do your work - which might take some time -
            // or just uses more time than the simple check of a boolean
            logger.info("BeforeAll : LiquibaseRunner : Running master changelogs");
            liquibaseRunner.run();
        }
        // free the access
        lock.unlock();
    }

    @Override
    public void close() {
        // Your "after all tests" logic goes here
    }
}