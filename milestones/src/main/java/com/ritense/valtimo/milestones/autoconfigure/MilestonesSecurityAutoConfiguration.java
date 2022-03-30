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

package com.ritense.valtimo.milestones.autoconfigure;

import com.ritense.valtimo.milestones.security.config.MilestoneHttpSecurityConfigurer;
import com.ritense.valtimo.milestones.security.config.MilestoneInstanceHttpSecurityConfigurer;
import com.ritense.valtimo.milestones.security.config.MilestoneSetHttpSecurityConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass(DataSource.class)
public class MilestonesSecurityAutoConfiguration {

    @Order(270)
    @Bean
    @ConditionalOnMissingBean(MilestoneHttpSecurityConfigurer.class)
    public MilestoneHttpSecurityConfigurer milestoneHttpSecurityConfigurer() {
        return new MilestoneHttpSecurityConfigurer();
    }

    @Order(271)
    @Bean
    @ConditionalOnMissingBean(MilestoneSetHttpSecurityConfigurer.class)
    public MilestoneSetHttpSecurityConfigurer milestoneSetHttpSecurityConfigurer() {
        return new MilestoneSetHttpSecurityConfigurer();
    }

    @Order(272)
    @Bean
    @ConditionalOnMissingBean(MilestoneInstanceHttpSecurityConfigurer.class)
    public MilestoneInstanceHttpSecurityConfigurer milestoneInstanceHttpSecurityConfigurer() {
        return new MilestoneInstanceHttpSecurityConfigurer();
    }

}