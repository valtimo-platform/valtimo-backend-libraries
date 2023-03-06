/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import com.ritense.valtimo.milestones.process.MilestoneProcessActions;
import com.ritense.valtimo.milestones.repository.MilestoneInstanceRepository;
import com.ritense.valtimo.milestones.repository.MilestoneRepository;
import com.ritense.valtimo.milestones.repository.MilestoneSetRepository;
import com.ritense.valtimo.milestones.service.MilestoneInstanceGenerator;
import com.ritense.valtimo.milestones.service.MilestoneInstanceService;
import com.ritense.valtimo.milestones.service.MilestoneService;
import com.ritense.valtimo.milestones.service.MilestoneSetService;
import com.ritense.valtimo.milestones.service.mapper.MilestoneInstanceMapper;
import com.ritense.valtimo.milestones.service.mapper.MilestoneMapper;
import com.ritense.valtimo.milestones.web.rest.MilestoneInstanceResource;
import com.ritense.valtimo.milestones.web.rest.MilestoneResource;
import com.ritense.valtimo.milestones.web.rest.MilestoneSetResource;
import org.camunda.bpm.engine.RepositoryService;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.ritense.valtimo.milestones.repository")
@EntityScan("com.ritense.valtimo.milestones.domain")
public class MilestonesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MilestoneService.class)
    public MilestoneService milestoneService(
        final MilestoneSetRepository milestoneSetRepository,
        final MilestoneRepository milestoneRepository,
        final MilestoneMapper milestoneMapper
    ) {
        return new MilestoneService(milestoneSetRepository, milestoneRepository, milestoneMapper);
    }

    @Bean
    @ConditionalOnMissingBean(MilestoneSetService.class)
    public MilestoneSetService milestoneSetService(
        final MilestoneSetRepository milestoneSetRepository,
        final MilestoneRepository milestoneRepository
    ) {
        return new MilestoneSetService(milestoneSetRepository, milestoneRepository);
    }

    @Bean
    @ConditionalOnMissingBean(MilestoneInstanceService.class)
    public MilestoneInstanceService milestoneInstanceService(
        final MilestoneInstanceRepository milestoneInstanceRepository,
        final MilestoneInstanceMapper milestoneInstanceMapper,
        final MilestoneInstanceGenerator milestoneInstanceGenerator
    ) {
        return new MilestoneInstanceService(
            milestoneInstanceRepository,
            milestoneInstanceMapper,
            milestoneInstanceGenerator
        );
    }

    @Bean
    @ConditionalOnMissingBean(MilestoneInstanceGenerator.class)
    public MilestoneInstanceGenerator milestoneInstanceGenerator(
        final MilestoneRepository milestoneRepository,
        final MilestoneInstanceRepository milestoneInstanceRepository
    ) {
        return new MilestoneInstanceGenerator(milestoneRepository, milestoneInstanceRepository);
    }

    @Bean
    @ConditionalOnMissingBean(MilestoneProcessActions.class)
    public MilestoneProcessActions milestoneProcessActions(
        final MilestoneSetRepository milestoneSetRepository,
        final MilestoneInstanceService milestoneInstanceService
    ) {
        return new MilestoneProcessActions(milestoneSetRepository, milestoneInstanceService);
    }

    @Bean
    @ConditionalOnMissingBean(MilestoneInstanceMapper.class)
    public MilestoneInstanceMapper milestoneInstanceMapper() {
        return Mappers.getMapper(MilestoneInstanceMapper.class);
    }

    @Bean
    @ConditionalOnMissingBean(MilestoneMapper.class)
    public MilestoneMapper milestoneMapper() {
        return Mappers.getMapper(MilestoneMapper.class);
    }

    @Bean
    @ConditionalOnMissingBean(MilestoneInstanceResource.class)
    public MilestoneInstanceResource milestoneInstanceResource(
        final RepositoryService repositoryService,
        final MilestoneInstanceService milestoneInstanceService
    ) {
        return new MilestoneInstanceResource(repositoryService, milestoneInstanceService);
    }

    @Bean
    @ConditionalOnMissingBean(MilestoneResource.class)
    public MilestoneResource milestoneResource(
        final MilestoneService milestoneService,
        final MilestoneRepository milestoneRepository,
        final MilestoneMapper milestoneMapper
    ) {
        return new MilestoneResource(milestoneService, milestoneRepository, milestoneMapper);
    }

    @Bean
    @ConditionalOnMissingBean(MilestoneSetResource.class)
    public MilestoneSetResource milestoneSetResource(
        final MilestoneSetService milestoneSetService,
        final MilestoneSetRepository milestoneSetRepository
    ) {
        return new MilestoneSetResource(milestoneSetService, milestoneSetRepository);
    }

}