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

package com.ritense.valtimo.service.util;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

@Slf4j
public class FormUtils {

    private static final String FORM_KEY_DELIMETER = ":";

    public static String getFormLocation(String formkey, HttpServletRequest request) {
        final String[] formKeyArray = formkey.split(FORM_KEY_DELIMETER);
        final String formType = formKeyArray[0];

        if (formType.equals("app") ||
            formType.equals("external") ||
            formType.equals("formio") ||
            formType.equals("form")
        ) {
            return formKeyArray[1];
        } else if (formType.equals("embedded") && formKeyArray[1].equals("app")) {
            return request.getContextPath() + formKeyArray[2];
        } else {
            logger.warn("Formkey {} not valid ", formkey);
            return null;
        }
    }

    public static VariableMap createTypedVariableMap(Map<String, Object> variables) {
        if (variables == null) {
            return Variables.createVariables();
        }
        VariableMap variableMap = Variables.createVariables();
        variables.forEach((key, value) -> {
            if (value instanceof Boolean) {
                variableMap.putValueTyped(key, Variables.booleanValue((Boolean) value));
            } else if (value instanceof Integer) {
                variableMap.putValueTyped(key, Variables.integerValue((Integer) value));
            } else if (value instanceof Long) {
                variableMap.putValueTyped(key, Variables.longValue((Long) value));
            } else if (value instanceof Date) {
                variableMap.putValueTyped(key, Variables.dateValue((Date) value));
            } else if (value instanceof ObjectValue) {
                variableMap.putValueTyped(key, Variables.objectValue(value).create());
            } else {
                variableMap.putValue(key, value);
            }
        });
        return variableMap;
    }

    public static Map<String, Object> withVariables(final String key, final Object value, final Object... furtherKeyValuePairs) {
        if (key == null) {
            throw new IllegalArgumentException(format("Illegal call of withVariables(key = '%s', value = '%s', ...) - key must not be null!", key, value));
        }
        final Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        if (furtherKeyValuePairs != null) {
            if (furtherKeyValuePairs.length % 2 != 0) {
                String message = format(
                    "Illegal call of withVariables() - must have an even number of arguments, but found length = %s!",
                    furtherKeyValuePairs.length + 2
                );
                throw new IllegalArgumentException(message);
            }
            for (int i = 0; i < furtherKeyValuePairs.length; i += 2) {
                if (!(furtherKeyValuePairs[i] instanceof String)) {
                    throw new IllegalArgumentException(
                        format(
                            "Illegal call of withVariables() - keys must be strings, found object of type '%s'!",
                            furtherKeyValuePairs[i] != null ? furtherKeyValuePairs[i].getClass().getName() : null
                        )
                    );
                }

                map.put((String) furtherKeyValuePairs[i], furtherKeyValuePairs[i + 1]);
            }
        }
        return map;
    }

}