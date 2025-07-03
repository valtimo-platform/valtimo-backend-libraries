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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.authorization.AuthorizationService;
import com.ritense.outbox.OutboxService;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.operaton.ProcessApplicationStartedEventListener;
import com.ritense.valtimo.operaton.ProcessDefinitionPropertyListener;
import com.ritense.valtimo.operaton.TaskCompletedListener;
import com.ritense.valtimo.operaton.repository.OperatonExecutionRepository;
import com.ritense.valtimo.operaton.repository.OperatonIdentityLinkRepository;
import com.ritense.valtimo.operaton.repository.OperatonTaskRepository;
import com.ritense.valtimo.operaton.repository.CustomRepositoryServiceImpl;
import com.ritense.valtimo.operaton.service.OperatonHistoryService;
import com.ritense.valtimo.operaton.service.OperatonRepositoryService;
import com.ritense.valtimo.operaton.service.OperatonRuntimeService;
import com.ritense.valtimo.config.CustomDateTimeProvider;
import com.ritense.valtimo.config.ValtimoApplicationReadyEventListener;
import com.ritense.valtimo.contract.authentication.AuthorizedUserRepository;
import com.ritense.valtimo.contract.authentication.AuthorizedUsersService;
import com.ritense.valtimo.contract.authentication.CurrentUserRepository;
import com.ritense.valtimo.contract.authentication.CurrentUserService;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.helper.ActivityHelper;
import com.ritense.valtimo.helper.OperatonDeploymentSourceHelper;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import com.ritense.valtimo.processdefinition.repository.ProcessDefinitionPropertiesRepository;
import com.ritense.valtimo.repository.OperatonReportingRepository;
import com.ritense.valtimo.repository.OperatonSearchProcessInstanceRepository;
import com.ritense.valtimo.repository.UserSettingsRepository;
import com.ritense.valtimo.service.AuthorizedUsersServiceImpl;
import com.ritense.valtimo.service.BpmnModelService;
import com.ritense.valtimo.service.OperatonByteArrayService;
import com.ritense.valtimo.service.OperatonProcessService;
import com.ritense.valtimo.service.OperatonTaskService;
import com.ritense.valtimo.service.CurrentUserServiceImpl;
import com.ritense.valtimo.service.ProcessDefinitionCaseDefinitionLinker;
import com.ritense.valtimo.service.ProcessPropertyService;
import com.ritense.valtimo.service.ProcessShortTimerService;
import com.ritense.valtimo.service.UserSettingsService;
import com.ritense.valtimo.web.rest.AccountResource;
import com.ritense.valtimo.web.rest.PingResource;
import com.ritense.valtimo.web.rest.ProcessInstanceResource;
import com.ritense.valtimo.web.rest.ProcessResource;
import com.ritense.valtimo.web.rest.ReportingResource;
import com.ritense.valtimo.web.rest.TaskResource;
import com.ritense.valtimo.web.rest.UserResource;
import com.ritense.valtimo.web.rest.VersionResource;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.Optional;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.operaton.bpm.engine.FormService;
import org.operaton.bpm.engine.HistoryService;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.TaskService;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableConfigurationProperties(ValtimoProperties.class)
@EnableJpaAuditing(dateTimeProviderRef = "customDateTimeProvider")
@EnableJpaRepositories(basePackageClasses = {ProcessDefinitionPropertiesRepository.class, UserSettingsRepository.class})
@EntityScan("com.ritense.valtimo.domain.*")
public class ValtimoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BpmnModelService.class)
    public BpmnModelService bpmnModelService(final CustomRepositoryServiceImpl repositoryService) {
        return new BpmnModelService(repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean(ProcessApplicationStartedEventListener.class)
    public ProcessApplicationStartedEventListener processApplicationStartedEventListener(
        final ApplicationEventPublisher applicationEventPublisher,
        final OperatonProcessService operatonProcessService
    ) {
        return new ProcessApplicationStartedEventListener(applicationEventPublisher, operatonProcessService);
    }

    @Bean
    @ConditionalOnMissingBean(TaskCompletedListener.class)
    public TaskCompletedListener taskCompletedListener(final ApplicationEventPublisher applicationEventPublisher) {
        return new TaskCompletedListener(applicationEventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(CustomDateTimeProvider.class)
    public CustomDateTimeProvider customDateTimeProvider() {
        return new CustomDateTimeProvider();
    }

    @Bean
    @ConditionalOnMissingBean(AuthorizedUsersService.class)
    public AuthorizedUsersService authorizedUsersService(final Collection<AuthorizedUserRepository> authorizedUserRepositories) {
        return new AuthorizedUsersServiceImpl(authorizedUserRepositories);
    }

    @Bean
    @ConditionalOnMissingBean(CurrentUserService.class)
    public CurrentUserService currentUserService(final Collection<CurrentUserRepository> currentUserRepositories) {
        return new CurrentUserServiceImpl(currentUserRepositories);
    }

    @Bean
    @ConditionalOnMissingBean(OperatonProcessService.class)
    public OperatonProcessService operatonProcessService(
        final RuntimeService runtimeService,
        final OperatonRuntimeService operatonRuntimeService,
        final RepositoryService repositoryService,
        final OperatonRepositoryService operatonRepositoryService,
        final FormService formService,
        final OperatonHistoryService historyService,
        final ProcessPropertyService processPropertyService,
        final ValtimoProperties valtimoProperties,
        final AuthorizationService authorizationService,
        final OperatonExecutionRepository operatonExecutionRepository,
        final ProcessDefinitionCaseDefinitionLinker processDefinitionCaseDefinitionLinker,
        final OperatonByteArrayService operatonByteArrayService,
        final ApplicationEventPublisher applicationEventPublisher,
        final OperatonDeploymentSourceHelper operatonDeploymentSourceHelper
    ) {
        return new OperatonProcessService(
            runtimeService,
            operatonRuntimeService,
            repositoryService,
            operatonRepositoryService,
            formService,
            historyService,
            processPropertyService,
            valtimoProperties,
            authorizationService,
            operatonExecutionRepository,
            processDefinitionCaseDefinitionLinker,
            operatonByteArrayService,
            applicationEventPublisher,
            operatonDeploymentSourceHelper
        );
    }

    @Bean
    @ConditionalOnMissingBean(OperatonTaskService.class)
    public OperatonTaskService operatonTaskService(
        final TaskService taskService,
        final FormService formService,
        final DelegateTaskHelper delegateTaskHelper,
        final OperatonTaskRepository operatonTaskRepository,
        final OperatonIdentityLinkRepository operatonIdentityLinkRepository,
        final Optional<ResourceService> resourceServiceOptional,
        final ApplicationEventPublisher applicationEventPublisher,
        final RuntimeService runtimeService,
        final UserManagementService userManagementService,
        final EntityManager entityManager,
        final AuthorizationService authorizationService,
        final OutboxService outboxService,
        final ObjectMapper objectMapper
    ) {
        return new OperatonTaskService(
            taskService,
            formService,
            delegateTaskHelper,
            operatonTaskRepository,
            operatonIdentityLinkRepository,
            resourceServiceOptional,
            applicationEventPublisher,
            runtimeService,
            userManagementService,
            entityManager,
            authorizationService,
            outboxService,
            objectMapper
        );
    }

    @Bean
    @ConditionalOnMissingBean(ProcessShortTimerService.class)
    public ProcessShortTimerService processShortTimerService(final RepositoryService repositoryService) {
        return new ProcessShortTimerService(repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean(DelegateTaskHelper.class)
    public DelegateTaskHelper delegateTaskHelper(
        final UserManagementService userManagementService,
        final ActivityHelper activityHelper,
        final BpmnModelService bpmnModelService
    ) {
        return new DelegateTaskHelper(userManagementService, activityHelper, bpmnModelService);
    }

    @Bean
    @ConditionalOnMissingBean(ActivityHelper.class)
    public ActivityHelper activityHelper(final RepositoryService repositoryService) {
        return new ActivityHelper(repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean(OperatonReportingRepository.class)
    public OperatonReportingRepository operatonReportingRepository(
        final SqlSession sqlSession,
        final OperatonRepositoryService repositoryService
    ) {
        return new OperatonReportingRepository(sqlSession, repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean(OperatonSearchProcessInstanceRepository.class)
    public OperatonSearchProcessInstanceRepository operatonSearchProcessInstanceRepository(final SqlSession sqlSession) {
        return new OperatonSearchProcessInstanceRepository(sqlSession);
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    //API
    @Bean
    @ConditionalOnMissingBean(TaskResource.class)
    public TaskResource taskResource(
        final FormService formService,
        final OperatonTaskService operatonTaskService,
        final OperatonProcessService operatonProcessService
    ) {
        return new TaskResource(formService, operatonTaskService, operatonProcessService);
    }

    @Bean
    @ConditionalOnMissingBean(ReportingResource.class)
    public ReportingResource reportingResource(
        final SqlSession sqlSession,
        final HistoryService historyService,
        final OperatonHistoryService operatonHistoryService,
        final OperatonReportingRepository operatonReportingRepository
    ) {
        return new ReportingResource(sqlSession, historyService, operatonHistoryService, operatonReportingRepository);
    }

    @Bean
    @ConditionalOnMissingBean(ProcessResource.class)
    public ProcessResource processResource(
        final HistoryService historyService,
        final OperatonHistoryService operatonHistoryService,
        final RuntimeService runtimeService,
        final RepositoryService repositoryService,
        final OperatonRepositoryService operatonRepositoryService,
        final OperatonTaskService operatonTaskService,
        final OperatonProcessService operatonProcessService,
        final ProcessShortTimerService processShortTimerService,
        final OperatonSearchProcessInstanceRepository operatonSearchProcessInstanceRepository,
        final ProcessPropertyService processPropertyService
    ) {
        return new ProcessResource(
            historyService,
            operatonHistoryService,
            runtimeService,
            repositoryService,
            operatonRepositoryService,
            operatonTaskService,
            operatonProcessService,
            processShortTimerService,
            operatonSearchProcessInstanceRepository,
            processPropertyService
        );
    }

    @Bean
    @ConditionalOnMissingBean(ProcessInstanceResource.class)
    public ProcessInstanceResource processInstanceResource(OperatonRuntimeService runtimeService) {
        return new ProcessInstanceResource(runtimeService);
    }

    @Bean
    @ConditionalOnMissingBean(AccountResource.class)
    public AccountResource accountResource(CurrentUserService currentUserService) {
        return new AccountResource(currentUserService);
    }

    @Bean
    @ConditionalOnMissingBean(UserSettingsService.class)
    public UserSettingsService userSettingsService(UserSettingsRepository userSettingsRepository) {
        return new UserSettingsService(userSettingsRepository);
    }

    @Bean
    @ConditionalOnMissingBean(UserResource.class)
    public UserResource userResource(
        UserManagementService userManagementService,
        UserSettingsService userSettingsService,
        ObjectMapper objectMapper
    ) {
        return new UserResource(userManagementService, userSettingsService, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(VersionResource.class)
    public VersionResource versionResource() {
        return new VersionResource();
    }

    @Bean
    @ConditionalOnMissingBean(PingResource.class)
    public PingResource pingResource() {
        return new PingResource();
    }

    @Bean
    @ConditionalOnMissingBean(ValtimoApplicationReadyEventListener.class)
    public ValtimoApplicationReadyEventListener valtimoApplicationReadyEventListener(@Value("${timezone:}") Optional<String> timeZone) {
        return new ValtimoApplicationReadyEventListener(timeZone);
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDefinitionPropertyListener.class)
    public ProcessDefinitionPropertyListener processDefinitionPropertyListener(
        final ProcessDefinitionPropertiesRepository processDefinitionPropertiesRepository,
        final RepositoryService repositoryService,
        final OperatonRepositoryService operatonRepositoryService
    ) {
        return new ProcessDefinitionPropertyListener(
            processDefinitionPropertiesRepository,
            repositoryService,
            operatonRepositoryService
        );
    }

    @Bean
    @ConditionalOnMissingBean(ProcessPropertyService.class)
    public ProcessPropertyService processPropertyService(
        final ProcessDefinitionPropertiesRepository processDefinitionPropertiesRepository,
        final ValtimoProperties valtimoProperties,
        final OperatonRepositoryService repositoryService
    ) {
        return new ProcessPropertyService(processDefinitionPropertiesRepository, valtimoProperties, repositoryService);
    }

}
