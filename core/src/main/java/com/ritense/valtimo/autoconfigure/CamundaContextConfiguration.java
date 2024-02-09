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

package com.ritense.valtimo.autoconfigure;

import com.ritense.valtimo.CamundaBeansPlugin;
import com.ritense.valtimo.contract.annotation.ProcessBean;
import java.util.Map;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

@AutoConfiguration
@ConditionalOnProperty(prefix = "valtimo.camunda", name = "bean-whitelisting", havingValue = "true", matchIfMissing = true)
public class CamundaContextConfiguration {

    @Bean
    @Order(Ordering.DEFAULT_ORDER - 1)
    public CamundaBeansPlugin camundaBeansPlugin(
        @Lazy @ProcessBean Map<String, Object> processBeans,
        ApplicationContext applicationContext
    ) {
        return new CamundaBeansPlugin(processBeans, applicationContext);
    }

}
