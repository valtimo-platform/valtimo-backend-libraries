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
 *
 */

package com.ritense.valtimo.domain.user;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_settings")
public class UserSettings {

        @Id
        @Column(name = "user_id", nullable = false)
        private String userId;

        @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
        @Column(name = "user_properties", columnDefinition = "JSON")
        private String userProperties;

        public UserSettings(String userId, String userProperties){
            this.userId = userId;
            this.userProperties = userProperties;
        }

    protected UserSettings() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(String userProperties) {
        this.userProperties = userProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserSettings that = (UserSettings) o;

        if (getUserId() != null ? !getUserId().equals(that.getUserId()) : that.getUserId() != null) return false;
        return getUserProperties() != null ? getUserProperties().equals(that.getUserProperties()) : that.getUserProperties() == null;
    }

    @Override
    public int hashCode() {
        int result = getUserId() != null ? getUserId().hashCode() : 0;
        result = 31 * result + (getUserProperties() != null ? getUserProperties().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
            "userId='" + userId + '\'' +
            ", userProperties='" + userProperties + '\'' +
            '}';
    }
}
