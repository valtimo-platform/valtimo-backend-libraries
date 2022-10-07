package com.ritense.valtimo.multitenancy.interceptor;

import com.ritense.valtimo.multitenancy.domain.TenantDomain;
import com.ritense.valtimo.multitenancy.service.CurrentTenantService;
import com.ritense.valtimo.multitenancy.service.TenantDomainService;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class MultitenancyFilter extends GenericFilterBean {
    private final TenantDomainService tenantDomainService;

    public MultitenancyFilter(TenantDomainService tenantDomainService) {
        this.tenantDomainService = tenantDomainService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        TenantDomain tenantDomain = tenantDomainService.findByDomain(httpServletRequest.getHeader("host"));
        String tenantId;
        if(tenantDomain != null) {
            tenantId = tenantDomain.getTenantId();
        } else {
            tenantId = "non-existing-tenant";
        }

        CurrentTenantService.setCurrentTenant(tenantId);

        chain.doFilter(request, response);
        CurrentTenantService.setCurrentTenant(null);
    }
}