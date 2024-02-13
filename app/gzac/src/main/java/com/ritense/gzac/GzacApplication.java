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

package com.ritense.gzac;

import com.ritense.connector.service.ConnectorService;
import com.ritense.gzac.listener.ApplicationReadyEventListener;
import com.ritense.objectsapi.service.ObjectSyncService;
import com.ritense.openzaak.service.InformatieObjectTypeLinkService;
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService;
import com.ritense.valtimo.config.DefaultProfileUtil;
import java.net.InetAddress;
import java.net.UnknownHostException;
import com.ritense.zakenapi.service.ZaakTypeLinkService;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@EnableProcessApplication
public class GzacApplication {
    private static final Logger logger = LoggerFactory.getLogger(GzacApplication.class);

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(GzacApplication.class);
        DefaultProfileUtil.addDefaultProfile(app);
        Environment environment = app.run(args).getEnvironment();
        logger.info(
            """

                ----------------------------------------------------------
                \tApplication '{}' is running! Access URLs:
                \tLocal: \t\thttp://127.0.0.1:{}
                \tExternal: \thttp://{}:{}
                ----------------------------------------------------------""",
            environment.getProperty("spring.application.name"),
            environment.getProperty("server.port"),
            InetAddress.getLocalHost().getHostAddress(),
            environment.getProperty("server.port")
        );
    }

    @Bean
    public ApplicationReadyEventListener setupListener(
        ConnectorService connectorService,
        ObjectSyncService objectSyncService,
        ZaakTypeLinkService zaakTypeLinkService,
        InformatieObjectTypeLinkService informatieObjectTypeLinkService,
        DocumentDefinitionProcessLinkService documentDefinitionProcessLinkService
    ) {
        return new ApplicationReadyEventListener(
            connectorService,
            objectSyncService,
            zaakTypeLinkService,
            informatieObjectTypeLinkService,
            documentDefinitionProcessLinkService
        );
    }
}
