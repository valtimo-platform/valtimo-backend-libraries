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

package com.ritense.valtimo.domain.user;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Type(value = JsonType.class)
    @Column(name = "settings", columnDefinition = "JSON")
    private Map<String, Object> settings;

    public UserSettings(String userId, Map<String, Object> settings) {
        this.userId = userId;
        this.settings = settings;
    }

    protected UserSettings() {

    }

    public String getUserId() {
        return userId;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserSettings that = (UserSettings) o;

        if (getUserId() != null ? !getUserId().equals(that.getUserId()) : that.getUserId() != null) {
            return false;
        }
        return getSettings() != null ? getSettings().equals(that.getSettings()) : that.getSettings() == null;
    }

    @Override
    public int hashCode() {
        int result = getUserId() != null ? getUserId().hashCode() : 0;
        result = 31 * result + (getSettings() != null ? getSettings().hashCode() : 0);
        return result;
    }
}
