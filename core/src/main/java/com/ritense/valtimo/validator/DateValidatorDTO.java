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

package com.ritense.valtimo.validator;

import io.jsonwebtoken.lang.Assert;
import java.util.Date;

public class DateValidatorDTO {

    private Date submittedValue;
    private Date configValue;

    public DateValidatorDTO(Date submittedValue, Date configValue) {
        Assert.notNull(submittedValue, "SubmittedValue can not be null");
        Assert.notNull(configValue, "ConfigValue can not be null");
        this.submittedValue = submittedValue;
        this.configValue = configValue;
    }

    public Date getSubmittedValue() {
        return submittedValue;
    }

    public Date getConfigValue() {
        return configValue;
    }
}
