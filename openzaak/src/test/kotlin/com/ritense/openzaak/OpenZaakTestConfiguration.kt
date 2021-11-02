/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak

import com.ritense.resource.service.ResourceService
import com.ritense.valtimo.contract.mail.MailSender
import org.mockito.Mockito
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class OpenZaakTestConfiguration {

    @Bean
    fun resourceService(): ResourceService {
        return Mockito.mock(ResourceService::class.java)
    }

    @Bean
    fun mailSender(): MailSender {
        return Mockito.mock(MailSender::class.java)
    }

    fun main(args: Array<String>) {
        SpringApplication.run(OpenZaakTestConfiguration::class.java, *args)
    }

}