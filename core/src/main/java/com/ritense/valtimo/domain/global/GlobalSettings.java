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

package com.ritense.valtimo.domain.global;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "global_settings")
public class GlobalSettings {

    @Id
    @Column(name = "settings_id", nullable = false)
    private UUID settingsId;

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "settings", columnDefinition = "JSON")
    private Map<String, Object> settings;

    public GlobalSettings(UUID settingsId, Map<String, Object> settings) {
        this.settingsId = settingsId;
        this.settings = settings;
    }

    protected GlobalSettings() {

    }

    public UUID getSettingsId() {
        return settingsId;
    }

    public void setSettings(Map<String, Object> newSettings) {
        settings = newSettings;
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

        GlobalSettings that = (GlobalSettings) o;

        if (getSettingsId() != null ? !getSettingsId().equals(that.getSettingsId()) : that.getSettingsId() != null) {
            return false;
        }
        return getSettings() != null ? getSettings().equals(that.getSettings()) : that.getSettings() == null;
    }

    @Override
    public int hashCode() {
        int result = getSettingsId() != null ? getSettingsId().hashCode() : 0;
        result = 31 * result + (getSettings() != null ? getSettings().hashCode() : 0);
        return result;
    }
}
