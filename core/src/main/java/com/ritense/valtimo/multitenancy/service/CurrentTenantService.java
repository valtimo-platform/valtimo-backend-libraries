package com.ritense.valtimo.multitenancy.service;

public class CurrentTenantService {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<String>();

    public static String getCurrentTenant() {
        return CURRENT.get();
    }

    public static void setCurrentTenant(String tenant) {
        CURRENT.set(tenant);
    }
}
