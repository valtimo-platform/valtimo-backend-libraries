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

package com.ritense.valtimo.security.permission;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import java.io.Serializable;
import java.util.Map;
import static java.util.Objects.requireNonNull;

public class ValtimoPermissionEvaluator implements PermissionEvaluator {

    private final Map<String, Permission> permissionNameToPermissionMap;

    public ValtimoPermissionEvaluator(Map<String, Permission> permissionNameToPermissionMap) {
        requireNonNull(permissionNameToPermissionMap, "permissionNameToPermissionMap is required");
        this.permissionNameToPermissionMap = permissionNameToPermissionMap;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        boolean hasPermission = false;
        if (canHandle(authentication, targetDomainObject, permission)) {
            hasPermission = checkPermission(authentication, targetDomainObject, (String) permission);
        }
        return hasPermission;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        throw new IllegalStateException("Id and Class permissions are not supported by " + this.getClass().toString());
    }

    private boolean canHandle(Authentication authentication, Object targetDomainObject, Object permission) {
        return targetDomainObject != null && authentication != null && permission instanceof String;
    }

    private boolean checkPermission(Authentication authentication, Object targetDomainObject, String permissionKey) {
        verifyPermissionIsDefined(permissionKey);
        Permission permission = permissionNameToPermissionMap.get(permissionKey);
        return permission.isAllowed(authentication, targetDomainObject);
    }

    private void verifyPermissionIsDefined(String permissionKey) {
        if (!permissionNameToPermissionMap.containsKey(permissionKey)) {
            throw new IllegalArgumentException("No permission with key " + permissionKey + " is defined in " + this.getClass()
                .toString());
        }
    }

}
