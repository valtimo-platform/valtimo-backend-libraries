package com.ritense.outbox
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import java.util.stream.Collectors

open class UserProvider {
    open fun getCurrentUserLogin(): String? {
        val securityContext = SecurityContextHolder.getContext()
        val authentication = securityContext.authentication
        var userName: String? = null
        if (authentication != null) {
            userName = authentication.name
        }
        return userName
    }

    open fun getCurrentUserRoles(): List<String> {
        val roles: List<String> = ArrayList()
        val securityContext = SecurityContextHolder.getContext()
        val authentication = securityContext.authentication
        return if (authentication != null) {
            authentication
                .authorities
                .stream()
                .map { obj: GrantedAuthority -> obj.authority }
                .collect(Collectors.toList())
        } else roles
    }
}