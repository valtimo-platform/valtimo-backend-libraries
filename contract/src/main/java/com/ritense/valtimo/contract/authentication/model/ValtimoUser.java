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

import com.ritense.valtimo.contract.authentication.ManageableUser;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static org.springframework.util.ObjectUtils.isEmpty;

public class ValtimoUser implements Serializable, ManageableUser {

    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String name;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNo = "";
    private boolean isEmailVerified;
    private String langKey;
    private boolean blocked;
    private boolean activated;
    private List<String> roles;
    private String password;

    public ValtimoUser() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getFullName() {
        if (isEmpty(firstName) && isEmpty(lastName)) {
            return "";
        } else if (isEmpty(firstName)) {
            return lastName;
        } else  if (isEmpty(lastName)) {
            return firstName;
        } else {
            return firstName + " " + lastName;
        }
    }

    @Override
    public String getLangKey() {
        return langKey;
    }

    @Override
    public String getPhoneNo() {
        return phoneNo;
    }

    @Override
    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    @Override
    public boolean isActivated() {
        return activated;
    }

    @Override
    public void activate() {
        this.activated = true;
    }

    @Override
    public void deactivate() {
        this.activated = false;
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValtimoUser)) {
            return false;
        }
        ValtimoUser that = (ValtimoUser) o;
        return Objects.equals(id, that.id)
            && Objects.equals(username, that.username)
            && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email);
    }
}
