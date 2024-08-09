package com.ritense.processlink.url.configuration

import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher

class URLProcessLinkSecurityConfigurer : HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        http.authorizeHttpRequests { requests ->
            requests.requestMatchers(antMatcher(POST, "/api/v1/process-link/url/{processLinkId}")).authenticated()
        }
    }

}