/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.gzac

import io.github.oshai.kotlinlogging.KotlinLogging
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableScheduling
import java.net.InetAddress

@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@EnableProcessApplication
class Application

fun main(args: Array<String>) {
    val applicationContext = runApplication<Application>(*args)
    val environment: Environment = applicationContext.environment
    val logger = KotlinLogging.logger {}

    logger.info {
        """

        ----------------------------------------------------------
        Application '${environment.getProperty("spring.application.name")}' is running!
        Active profile(s): [${environment.getProperty("spring.profiles.active")}].
        Local URL: [http://127.0.0.1:${environment.getProperty("server.port")}].
        External URL: [http://${InetAddress.getLocalHost().hostAddress}:${environment.getProperty("server.port")}]
        ----------------------------------------------------------
        """.trimIndent()
    }
}