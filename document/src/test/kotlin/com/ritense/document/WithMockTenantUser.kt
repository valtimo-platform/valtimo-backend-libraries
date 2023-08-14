package com.ritense.document

import com.ritense.document.BaseTest.USERNAME
import org.springframework.security.test.context.support.WithSecurityContext

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockTenantUserSecurityContextFactory::class)
annotation class WithMockTenantUser(val username: String = USERNAME, val fullName: String = "John doe")