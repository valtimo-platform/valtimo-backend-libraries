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

package com.ritense.valtimo.autoconfigure;

import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.camunda.ProcessApplicationStartedEventListener;
import com.ritense.valtimo.camunda.ProcessDefinitionPropertyListener;
import com.ritense.valtimo.camunda.TaskCompletedListener;
import com.ritense.valtimo.camunda.repository.CamundaIdentityLinkRepository;
import com.ritense.valtimo.camunda.repository.CamundaTaskRepository;
import com.ritense.valtimo.camunda.repository.CustomRepositoryServiceImpl;
import com.ritense.valtimo.camunda.service.CamundaHistoryService;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.camunda.service.CamundaRuntimeService;
import com.ritense.valtimo.config.CustomDateTimeProvider;
import com.ritense.valtimo.config.ValtimoApplicationReadyEventListener;
import com.ritense.valtimo.contract.authentication.AuthorizedUserRepository;
import com.ritense.valtimo.contract.authentication.CurrentUserRepository;
import com.ritense.valtimo.contract.authentication.CurrentUserService;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.helper.ActivityHelper;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import com.ritense.valtimo.processdefinition.repository.ProcessDefinitionPropertiesRepository;
import com.ritense.valtimo.repository.CamundaReportingRepository;
import com.ritense.valtimo.repository.CamundaSearchProcessInstanceRepository;
import com.ritense.valtimo.repository.UserSettingsRepository;
import com.ritense.valtimo.security.permission.Permission;
import com.ritense.valtimo.security.permission.TaskAccessPermission;
import com.ritense.valtimo.security.permission.ValtimoPermissionEvaluator;
import com.ritense.valtimo.service.AuthorizedUsersService;
import com.ritense.valtimo.service.BpmnModelService;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import com.ritense.valtimo.service.ContextService;
import com.ritense.valtimo.service.ProcessPropertyService;
import com.ritense.valtimo.service.ProcessShortTimerService;
import com.ritense.valtimo.service.UserSettingsService;
import com.ritense.valtimo.web.rest.AccountResource;
import com.ritense.valtimo.web.rest.PingResource;
import com.ritense.valtimo.web.rest.ProcessInstanceResource;
import com.ritense.valtimo.web.rest.ProcessResource;
import com.ritense.valtimo.web.rest.PublicProcessResource;
import com.ritense.valtimo.web.rest.ReportingResource;
import com.ritense.valtimo.web.rest.TaskResource;
import com.ritense.valtimo.web.rest.UserResource;
import com.ritense.valtimo.web.rest.VersionResource;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.extension.reactor.spring.EnableCamundaEventBus;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

@Configuration
@EnableConfigurationProperties(ValtimoProperties.class)
@EnableJpaAuditing(dateTimeProviderRef = "customDateTimeProvider")
@EnableCamundaEventBus
@EnableJpaRepositories(basePackageClasses = {ProcessDefinitionPropertiesRepository.class, UserSettingsRepository.class})
@EntityScan("com.ritense.valtimo.domain.*")
public class ValtimoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BpmnModelService.class)
    public BpmnModelService bpmnModelService(final CustomRepositoryServiceImpl repositoryService) {
        return new BpmnModelService(repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean(ValtimoPermissionEvaluator.class)
    public ValtimoPermissionEvaluator valtimoPermissionEvaluator(
        final TaskAccessPermission taskAccessPermission
    ) {
        final HashMap<String, Permission> permissionMap = new HashMap<>();
        permissionMap.put("taskAccess", taskAccessPermission);
        return new ValtimoPermissionEvaluator(permissionMap);
    }

    @Bean
    @ConditionalOnMissingBean(TaskAccessPermission.class)
    public TaskAccessPermission taskAccessPermission(final CamundaTaskService taskService) {
        return new TaskAccessPermission(taskService);
    }

    @Bean
    @ConditionalOnMissingBean(ProcessApplicationStartedEventListener.class)
    public ProcessApplicationStartedEventListener processApplicationStartedEventListener(
        final ApplicationEventPublisher applicationEventPublisher,
        final CamundaProcessService camundaProcessService
    ) {
        return new ProcessApplicationStartedEventListener(applicationEventPublisher, camundaProcessService);
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
    public AuthorizedUsersService authorizedUsersService(
        final Collection<AuthorizedUserRepository> authorizedUserRepositories
    ) {
        return new AuthorizedUsersService(authorizedUserRepositories);
    }

    @Bean
    @ConditionalOnMissingBean(com.ritense.valtimo.service.CurrentUserService.class)
    public com.ritense.valtimo.service.CurrentUserService currentUserService(
        final Collection<CurrentUserRepository> currentUserRepositories
    ) {
        return new com.ritense.valtimo.service.CurrentUserService(currentUserRepositories);
    }

    @Bean
    @ConditionalOnMissingBean(CamundaProcessService.class)
    public CamundaProcessService camundaProcessService(
        final RuntimeService runtimeService,
        final CamundaRuntimeService camundaRuntimeService,
        final RepositoryService repositoryService,
        final CamundaRepositoryService camundaRepositoryService,
        final FormService formService,
        final CamundaHistoryService historyService,
        final ProcessPropertyService processPropertyService,
        final ValtimoProperties valtimoProperties
    ) {
        return new CamundaProcessService(runtimeService, camundaRuntimeService, repositoryService, camundaRepositoryService, formService, historyService,processPropertyService,valtimoProperties);
    }

    @Bean
    @ConditionalOnMissingBean(CamundaTaskService.class)
    public CamundaTaskService camundaTaskService(
        final TaskService taskService,
        final FormService formService,
        final ContextService contextService,
        final DelegateTaskHelper delegateTaskHelper,
        final CamundaTaskRepository camundaTaskRepository,
        final CamundaIdentityLinkRepository camundaIdentityLinkRepository,
        final CamundaProcessService camundaProcessService,
        final Optional<ResourceService> resourceServiceOptional,
        final ApplicationEventPublisher applicationEventPublisher,
        final RuntimeService runtimeService,
        final UserManagementService userManagementService,
        final EntityManager entityManager
    ) {
        return new CamundaTaskService(
            taskService,
            formService,
            contextService,
            delegateTaskHelper,
            camundaTaskRepository,
            camundaIdentityLinkRepository,
            camundaProcessService,
            resourceServiceOptional,
            applicationEventPublisher,
            runtimeService,
            userManagementService,
            entityManager
        );
    }

    @Bean
    @ConditionalOnMissingBean(ProcessShortTimerService.class)
    public ProcessShortTimerService processShortTimerService(
        final RepositoryService repositoryService
    ) {
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
    @ConditionalOnMissingBean(CamundaReportingRepository.class)
    public CamundaReportingRepository camundaReportingRepository(
        final SqlSession sqlSession,
        final CamundaRepositoryService repositoryService
    ) {
        return new CamundaReportingRepository(sqlSession, repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean(CamundaSearchProcessInstanceRepository.class)
    public CamundaSearchProcessInstanceRepository camundaSearchProcessInstanceRepository(
        final SqlSession sqlSession
    ) {
        return new CamundaSearchProcessInstanceRepository(sqlSession);
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
        final TaskService taskService,
        final FormService formService,
        final CamundaTaskService camundaTaskService,
        final CamundaProcessService camundaProcessService
    ) {
        return new TaskResource(
            taskService,
            formService,
            camundaTaskService,
            camundaProcessService
        );
    }

    @Bean
    @ConditionalOnMissingBean(ReportingResource.class)
    public ReportingResource reportingResource(
        final SqlSession sqlSession,
        final HistoryService historyService,
        final CamundaHistoryService camundaHistoryService,
        final CamundaReportingRepository camundaReportingRepository
    ) {
        return new ReportingResource(
            sqlSession,
            historyService,
            camundaHistoryService,
            camundaReportingRepository
        );
    }

    @Bean
    @ConditionalOnMissingBean(PublicProcessResource.class)
    public PublicProcessResource publicProcessResource(
        final FormService formService,
        final CamundaRepositoryService repositoryService,
        final CamundaProcessService camundaProcessService
    ) {
        return new PublicProcessResource(formService, repositoryService, camundaProcessService);
    }

    @Bean
    @ConditionalOnMissingBean(ProcessResource.class)
    public ProcessResource processResource(
        final TaskService taskService,
        final HistoryService historyService,
        final CamundaHistoryService camundaHistoryService,
        final RuntimeService runtimeService,
        final RepositoryService repositoryService,
        final CamundaRepositoryService camundaRepositoryService,
        final CamundaTaskService camundaTaskService,
        final CamundaProcessService camundaProcessService,
        final ProcessShortTimerService processShortTimerService,
        final CamundaSearchProcessInstanceRepository camundaSearchProcessInstanceRepository,
        final ProcessPropertyService processPropertyService
    ) {
        return new ProcessResource(
            taskService,
            historyService,
            camundaHistoryService,
            runtimeService,
            repositoryService,
            camundaRepositoryService,
            camundaTaskService,
            camundaProcessService,
            processShortTimerService,
            camundaSearchProcessInstanceRepository,
            processPropertyService
        );
    }

    @Bean
    @ConditionalOnMissingBean(ProcessInstanceResource.class)
    public ProcessInstanceResource processInstanceResource(CamundaRuntimeService runtimeService) {
        return new ProcessInstanceResource(runtimeService);
    }

    @Bean
    @ConditionalOnMissingBean(AccountResource.class)
    public AccountResource accountResource(CurrentUserService currentUserService) {
        return new AccountResource(currentUserService);
    }

    @Bean
    @ConditionalOnMissingBean(UserSettingsService.class)
    public UserSettingsService userSettingsService(UserSettingsRepository userSettingsRepository){
        return new UserSettingsService(userSettingsRepository);
    }

    @Bean
    @ConditionalOnMissingBean(UserResource.class)
    public UserResource userResource(UserManagementService userManagementService, UserSettingsService userSettingsService) {
        return new UserResource(userManagementService, userSettingsService);
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
    public ValtimoApplicationReadyEventListener valtimoApplicationReadyEventListener(
        @Value("${timezone:}") Optional<String> timeZone
    ) {
        return new ValtimoApplicationReadyEventListener(timeZone);
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDefinitionPropertyListener.class)
    public ProcessDefinitionPropertyListener processDefinitionPropertyListener(
        final ProcessDefinitionPropertiesRepository processDefinitionPropertiesRepository,
        final RepositoryService repositoryService,
        final CamundaRepositoryService camundaRepositoryService
    ) {
        return new ProcessDefinitionPropertyListener(processDefinitionPropertiesRepository, repositoryService, camundaRepositoryService);
    }

    @Bean
    @ConditionalOnMissingBean(ProcessPropertyService.class)
    public ProcessPropertyService processPropertyService(
        final ProcessDefinitionPropertiesRepository processDefinitionPropertiesRepository,
        final ValtimoProperties valtimoProperties,
        final CamundaRepositoryService repositoryService
    ) {
        return new ProcessPropertyService(
            processDefinitionPropertiesRepository,
            valtimoProperties,
            repositoryService
        );
    }

}
