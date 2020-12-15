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

package com.ritense.valtimo.contract.authentication.model;

import java.util.List;

public class ValtimoUserBuilder {

    private String id;
    private String userName;
    private String name;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNo;
    private boolean isEmailVerified;
    private String langKey;
    private boolean blocked;
    private boolean activated;
    private List<String> roles;

    public ValtimoUserBuilder id(String id) {
        this.id = id;
        return this;
    }

    public ValtimoUserBuilder username(String userName) {
        this.userName = userName;
        return this;
    }

    public ValtimoUserBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ValtimoUserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public ValtimoUserBuilder firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public ValtimoUserBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public ValtimoUserBuilder phoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
        return this;
    }

    public ValtimoUserBuilder isEmailVerified(boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
        return this;
    }

    public ValtimoUserBuilder langKey(String langKey) {
        this.langKey = langKey;
        return this;
    }

    public ValtimoUserBuilder blocked(boolean blocked) {
        this.blocked = blocked;
        return this;
    }

    public ValtimoUserBuilder activated(boolean activated) {
        this.activated = activated;
        return this;
    }

    public ValtimoUserBuilder roles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public ValtimoUser createValtimoUser() {
        return new ValtimoUser(id, userName, name, email, firstName, lastName, phoneNo, isEmailVerified, langKey, blocked, activated, roles);
    }

    public ValtimoUser build() {
        return new ValtimoUser(id, userName, name, email, firstName, lastName, phoneNo, isEmailVerified, langKey, blocked, activated, roles);
    }
}