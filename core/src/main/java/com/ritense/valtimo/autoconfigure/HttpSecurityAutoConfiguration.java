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

import com.ritense.valtimo.contract.hardening.service.HardeningService;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import com.ritense.valtimo.contract.security.config.oauth2.NoOAuth2ClientsConfiguredCondition;
import com.ritense.valtimo.contract.web.rest.error.ExceptionTranslator;
import com.ritense.valtimo.security.ActuatorSecurityFilterChainFactory;
import com.ritense.valtimo.security.CoreSecurityFactory;
import com.ritense.valtimo.security.Http401UnauthorizedEntryPoint;
import com.ritense.valtimo.security.SpringSecurityAuditorAware;
import com.ritense.valtimo.security.ValtimoCoreSecurityFactory;
import com.ritense.valtimo.security.config.AccountHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ApiLoginHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.OperatonCockpitHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.OperatonRestHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ChoiceFieldHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.CsrfHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.DenyAllHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.EmailNotificationSettingsSecurityConfigurer;
import com.ritense.valtimo.security.config.ErrorHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.JwtHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.OpenApiHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.PingHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ProcessHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ProcessInstanceHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ReportingHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.StatelessHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.StaticResourcesHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.TaskHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.UserHttpSecurityConfigurer;
import com.ritense.valtimo.security.config.ValtimoVersionHttpSecurityConfigurer;
import com.ritense.valtimo.security.jwt.authentication.TokenAuthenticationService;
import com.ritense.valtimo.security.matcher.SecurityWhitelistProperties;
import com.ritense.valtimo.security.matcher.WhitelistIpRequestMatcher;
import java.util.List;
import java.util.Optional;
import org.operaton.bpm.engine.IdentityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

@AutoConfiguration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityWhitelistProperties.class)
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
    @ConditionalOnMissingBean(WhitelistIpRequestMatcher.class)
    public WhitelistIpRequestMatcher whitelistIpRequest(
        SecurityWhitelistProperties properties) {
        return new WhitelistIpRequestMatcher(properties.getHosts());
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

    @Order(350)
    @Bean
    @ConditionalOnMissingBean(ProcessInstanceHttpSecurityConfigurer.class)
    public ProcessInstanceHttpSecurityConfigurer processInstanceHttpSecurityConfigurer() {
        return new ProcessInstanceHttpSecurityConfigurer();
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


    //DEFAULTS SECURITY CHAIN

    @Order(398)
    @Bean
    @ConditionalOnMissingBean(OpenApiHttpSecurityConfigurer.class)
    public OpenApiHttpSecurityConfigurer swaggerHttpSecurityConfigurer() {
        return new OpenApiHttpSecurityConfigurer();
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

    @Order(440)
    @Bean
    @Conditional(NoOAuth2ClientsConfiguredCondition.class)
    @ConditionalOnMissingBean(JwtHttpSecurityConfigurer.class)
    public JwtHttpSecurityConfigurer jwtHttpSecurityConfigurer(
        IdentityService identityService,
        TokenAuthenticationService tokenAuthenticationService
    ) {
        return new JwtHttpSecurityConfigurer(identityService, tokenAuthenticationService);
    }

    @Order(450)
    @Bean
    @ConditionalOnMissingBean(ApiLoginHttpSecurityConfigurer.class)
    public ApiLoginHttpSecurityConfigurer apiLoginHttpSecurityConfigurer() {
        return new ApiLoginHttpSecurityConfigurer();
    }

    @Order(460)
    @Bean
    @ConditionalOnMissingBean(OperatonRestHttpSecurityConfigurer.class)
    public OperatonRestHttpSecurityConfigurer operatonRestHttpSecurityConfigurer() {
        return new OperatonRestHttpSecurityConfigurer();
    }

    @Order(470)
    @Bean
    @ConditionalOnMissingBean(OperatonCockpitHttpSecurityConfigurer.class)
    public OperatonCockpitHttpSecurityConfigurer operatonCockpitHttpSecurityConfigurer(
        SecurityWhitelistProperties whitelistProperties
    ) {
        WhitelistIpRequestMatcher whitelistIpRequestMatcher = new WhitelistIpRequestMatcher(whitelistProperties.getHosts());
        return new OperatonCockpitHttpSecurityConfigurer(whitelistIpRequestMatcher);
    }

    @Order(500)
    @Bean
    @ConditionalOnMissingBean(DenyAllHttpSecurityConfigurer.class)
    public DenyAllHttpSecurityConfigurer authenticatedHttpSecurityConfigurer() {
        return new DenyAllHttpSecurityConfigurer();
    }


    @Bean
    @ConditionalOnMissingBean(CoreSecurityFactory.class)
    public CoreSecurityFactory coreSecurityFactory(
        List<? extends HttpSecurityConfigurer> httpSecurityConfigurers
    ) {
        return new ValtimoCoreSecurityFactory(httpSecurityConfigurers);
    }

    @Order(100)
    @Bean
    public SecurityFilterChain coreSecurityFilterChain(
        CoreSecurityFactory coreSecurityFactory,
        HttpSecurity httpSecurity
    ) {
        return coreSecurityFactory.createSecurityFilterChain(httpSecurity);
    }

    @Order(50)
    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(
        HttpSecurity httpSecurity,
        WebEndpointProperties webEndpointProperties,
        HealthEndpointProperties healthEndpointProperties,
        PasswordEncoder passwordEncoder,
        @Value("${spring-actuator.username}") String username,
        @Value("${spring-actuator.password}") String password
    ) {
        return new ActuatorSecurityFilterChainFactory().createFilterChain(
            httpSecurity,
            webEndpointProperties,
            healthEndpointProperties,
            passwordEncoder,
            username,
            password
        );
    }

    @Order(100)
    @Bean
    public WebSecurityCustomizer coreWebSecurityCustomizer(
        CoreSecurityFactory coreSecurityFactory
    ) {
        return coreSecurityFactory.createWebSecurityCustomizer();
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @Bean
    @ConditionalOnMissingBean(ExceptionTranslator.class)
    public ExceptionTranslator defaultCoreExceptionTranslator(
        Optional<HardeningService> hardeningService
    ) {
        return new ExceptionTranslator(hardeningService);
    }

}
