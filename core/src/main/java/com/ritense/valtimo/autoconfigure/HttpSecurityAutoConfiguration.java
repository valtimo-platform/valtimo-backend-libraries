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

package com.ritense.valtimo.autoconfigure;

import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.contract.security.config.AuthenticationSecurityConfigurer;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import com.ritense.valtimo.security.Http401UnauthorizedEntryPoint;
import com.ritense.valtimo.security.SpringSecurityAuditorAware;
import com.ritense.valtimo.security.adapter.CoreHttpSecurityConfigurerAdapter;
import com.ritense.valtimo.security.config.AccountHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ActuatorHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ApiLoginHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.AuthorityHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.CamundaCockpitHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.CamundaHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.CamundaRestHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ChoiceFieldHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ContextHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.CsrfHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.DenyAllHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.EmailNotificationSettingsSecurityConfigurer;
import com.ritense.valtimo.security.config.ErrorHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.JwtHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.PingHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ProcessHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ProcessInstanceHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.PublicProcessHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ReportingHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.StatelessHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.StaticResourcesHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.SwaggerHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.TaskHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.UserHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ValtimoVersionHttpSecurityConfigurer;
import com.ritense.valtimo.security.interceptor.SecurityWhitelistProperties;
import com.ritense.valtimo.security.interceptor.WhitelistIpRequest;
import com.ritense.valtimo.security.jwt.authentication.TokenAuthenticationService;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(value = SecurityWhitelistProperties.class)
public class HttpSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecurityEvaluationContextExtension.class)
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }

    @Bean
    @ConditionalOnMissingBean(Http401UnauthorizedEntryPoint.class)
    public Http401UnauthorizedEntryPoint http401UnauthorizedEntryPoint() {
        return new Http401UnauthorizedEntryPoint();
    }

    @Bean
    @ConditionalOnMissingBean(SpringSecurityAuditorAware.class)
    public SpringSecurityAuditorAware springSecurityAuditorAware() {
        return new SpringSecurityAuditorAware();
    }

    @Bean
    @ConditionalOnMissingBean(WhitelistIpRequest.class)
    public WhitelistIpRequest whitelistIpRequest(
        SecurityWhitelistProperties properties) {
        return new WhitelistIpRequest(properties.getHosts());
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(Http403ForbiddenEntryPoint.class)
    public Http403ForbiddenEntryPoint http403ForbiddenEntryPoint() {
        return new Http403ForbiddenEntryPoint();
    }

    //CORE ENDPOINT CONFIGURATION

    @Order(260)
    @Bean
    @ConditionalOnMissingBean(PingHttpSecurityConfigurer.class)
    public PingHttpSecurityConfigurer pingHttpSecurityConfigurer() {
        return new PingHttpSecurityConfigurer();
    }

    @Order(270)
    @Bean
    @ConditionalOnMissingBean(ValtimoVersionHttpSecurityConfigurer.class)
    public ValtimoVersionHttpSecurityConfigurer valtimoVersionHttpSecurityConfigurer() {
        return new ValtimoVersionHttpSecurityConfigurer();
    }

    @Order(280)
    @Bean
    @ConditionalOnMissingBean(EmailNotificationSettingsSecurityConfigurer.class)
    public EmailNotificationSettingsSecurityConfigurer emailNotificationSettingsSecurityConfigurer() {
        return new EmailNotificationSettingsSecurityConfigurer();
    }

    @Order(300)
    @Bean
    @ConditionalOnMissingBean(AuthorityHttpSecurityConfigurer.class)
    public AuthorityHttpSecurityConfigurer authorityHttpSecurityConfigurer() {
        return new AuthorityHttpSecurityConfigurer();
    }

    @Order(310)
    @Bean
    @ConditionalOnMissingBean(UserHttpSecurityConfigurer.class)
    public UserHttpSecurityConfigurer userHttpSecurityConfigurer() {
        return new UserHttpSecurityConfigurer();
    }

    @Order(320)
    @Bean
    @ConditionalOnMissingBean(TaskHttpSecurityConfigurer.class)
    public TaskHttpSecurityConfigurer taskHttpSecurityConfigurer() {
        return new TaskHttpSecurityConfigurer();
    }

    @Order(330)
    @Bean
    @ConditionalOnMissingBean(ReportingHttpSecurityConfigurer.class)
    public ReportingHttpSecurityConfigurer reportingHttpSecurityConfigurer() {
        return new ReportingHttpSecurityConfigurer();
    }

    @Order(340)
    @Bean
    @ConditionalOnMissingBean(ProcessHttpSecurityConfigurer.class)
    public ProcessHttpSecurityConfigurer processHttpSecurityConfigurer() {
        return new ProcessHttpSecurityConfigurer();
    }

    @Order(341)
    @Bean
    @ConditionalOnMissingBean(PublicProcessHttpSecurityConfigurer.class)
    public PublicProcessHttpSecurityConfigurer publicProcessHttpSecurityConfigurer() {
        return new PublicProcessHttpSecurityConfigurer();
    }

    @Order(350)
    @Bean
    @ConditionalOnMissingBean(ProcessInstanceHttpSecurityConfigurer.class)
    public ProcessInstanceHttpSecurityConfigurer processInstanceHttpSecurityConfigurer() {
        return new ProcessInstanceHttpSecurityConfigurer();
    }

    @Order(370)
    @Bean
    @ConditionalOnMissingBean(ContextHttpSecurityConfigurer.class)
    public ContextHttpSecurityConfigurer contextHttpSecurityConfigurer() {
        return new ContextHttpSecurityConfigurer();
    }

    @Order(380)
    @Bean
    @ConditionalOnMissingBean(ChoiceFieldHttpSecurityConfigurer.class)
    public ChoiceFieldHttpSecurityConfigurer choiceFieldHttpSecurityConfigurer() {
        return new ChoiceFieldHttpSecurityConfigurer();
    }

    @Order(390)
    @Bean
    @ConditionalOnMissingBean(AccountHttpSecurityConfigurer.class)
    public AccountHttpSecurityConfigurer accountHttpSecurityConfigurer() {
        return new AccountHttpSecurityConfigurer();
    }

    @Order(391)
    @Bean
    @ConditionalOnMissingBean(CamundaHttpSecurityConfigurer.class)
    public CamundaHttpSecurityConfigurer camundaHttpSecurityConfigurer() {
        return new CamundaHttpSecurityConfigurer();
    }


    //DEFAULTS SECURITY CHAIN

    @Order(398)
    @Bean
    @ConditionalOnMissingBean(SwaggerHttpSecurityConfigurer.class)
    public SwaggerHttpSecurityConfigurer swaggerHttpSecurityConfigurer() {
        return new SwaggerHttpSecurityConfigurer();
    }

    @Order(399)
    @Bean
    @ConditionalOnMissingBean(StaticResourcesHttpSecurityConfigurer.class)
    public StaticResourcesHttpSecurityConfigurer staticResourcesHttpSecurityConfigurer() {
        return new StaticResourcesHttpSecurityConfigurer();
    }

    @Order(400)
    @Bean
    @ConditionalOnMissingBean(StatelessHttpSecurityConfigurer.class)
    public StatelessHttpSecurityConfigurer statelessHttpSecurityConfigurer() {
        return new StatelessHttpSecurityConfigurer();
    }

    @Order(410)
    @Bean
    @ConditionalOnMissingBean(CsrfHttpSecurityConfigurer.class)
    public CsrfHttpSecurityConfigurer csrfHttpSecurityConfigurer() {
        return new CsrfHttpSecurityConfigurer();
    }

    @Order(420)
    @Bean
    @ConditionalOnMissingBean(ErrorHttpSecurityConfigurer.class)
    public ErrorHttpSecurityConfigurer errorHttpSecurityConfigurer(Http403ForbiddenEntryPoint http403ForbiddenEntryPoint) {
        return new ErrorHttpSecurityConfigurer(http403ForbiddenEntryPoint);
    }

    @Order(430)
    @Bean
    @ConditionalOnMissingBean(ActuatorHttpSecurityConfigurer.class)
    public ActuatorHttpSecurityConfigurer actuatorHttpSecurityConfigurer(
        @Value("${spring-actuator.username}") String username,
        @Value("${spring-actuator.password}") String password,
        PasswordEncoder passwordEncoder
    ) {
        return new ActuatorHttpSecurityConfigurer(username, password, passwordEncoder);
    }

    @Order(440)
    @Bean
    @ConditionalOnMissingBean(JwtHttpSecurityConfigurer.class)
    public JwtHttpSecurityConfigurer jwtHttpSecurityConfigurer(
        IdentityService identityService,
        TokenAuthenticationService tokenAuthenticationService,
        ValtimoProperties valtimoProperties
    ) {
        return new JwtHttpSecurityConfigurer(identityService, tokenAuthenticationService, valtimoProperties);
    }

    @Order(450)
    @Bean
    @ConditionalOnMissingBean(ApiLoginHttpSecurityConfigurer.class)
    public ApiLoginHttpSecurityConfigurer apiLoginHttpSecurityConfigurer() {
        return new ApiLoginHttpSecurityConfigurer();
    }

    @Order(460)
    @Bean
    @ConditionalOnMissingBean(CamundaRestHttpSecurityConfigurer.class)
    public CamundaRestHttpSecurityConfigurer camundaRestHttpSecurityConfigurer() {
        return new CamundaRestHttpSecurityConfigurer();
    }

    @Order(470)
    @Bean
    @ConditionalOnMissingBean(CamundaCockpitHttpSecurityConfigurer.class)
    public CamundaCockpitHttpSecurityConfigurer camundaCockpitHttpSecurityConfigurer() {
        return new CamundaCockpitHttpSecurityConfigurer();
    }

    @Order(500)
    @Bean
    @ConditionalOnMissingBean(DenyAllHttpSecurityConfigurer.class)
    public DenyAllHttpSecurityConfigurer authenticatedHttpSecurityConfigurer() {
        return new DenyAllHttpSecurityConfigurer();
    }

    @Order(100)
    @Bean
    @ConditionalOnMissingBean(CoreHttpSecurityConfigurerAdapter.class)
    public CoreHttpSecurityConfigurerAdapter coreHttpSecurityConfigurerAdapter(
        List<HttpSecurityConfigurer> httpSecurityConfigurers,
        List<AuthenticationSecurityConfigurer> authenticationSecurityConfigurers
    ) {
        return new CoreHttpSecurityConfigurerAdapter(httpSecurityConfigurers, authenticationSecurityConfigurers);
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManager(CoreHttpSecurityConfigurerAdapter configurerAdapter) throws Exception {
        return configurerAdapter.authenticationManagerBean();
    }

}