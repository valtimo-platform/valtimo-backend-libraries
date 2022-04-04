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

package com.ritense.valtimo.helper;

import com.ritense.valtimo.exception.ExpectedElementTemplatePropertyNotFoundException;
import com.ritense.valtimo.exception.IllegalElementTemplatePropertyValueException;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendElementTemplateTaskMailHelper {

    public static final String RECEIVER_KEY = "mailSendTaskTo";
    public static final String SENDER_KEY = "mailSendTaskFrom";
    public static final String SUBJECT_KEY = "mailSendTaskSubject";
    public static final String TEMPLATE_KEY = "mailSendTaskTemplate";
    public static final String ATTACHMENTS_KEY = "mailSendTaskAttachments";

    private static final Pattern camundaExpressionPattern = Pattern.compile("^\\$\\{([a-zA-Z0-9_\\-\\.]+)\\}$");

    public static void validateExpectedKeys(Map<String, Object> camundaProperties)
            throws ExpectedElementTemplatePropertyNotFoundException, IllegalElementTemplatePropertyValueException {
        validateExpectedKey(RECEIVER_KEY, camundaProperties);
        validateExpectedKey(SENDER_KEY, camundaProperties);
        validateExpectedKey(SUBJECT_KEY, camundaProperties);
        validateExpectedKey(TEMPLATE_KEY, camundaProperties);
    }

    public static String getReceiverKeyValue(Map<String, Object> camundaProperties, Map<String, Object> processVariables) {
        return getKeyValue(RECEIVER_KEY, camundaProperties, processVariables);
    }

    public static String getSenderKeyValue(Map<String, Object> camundaProperties, Map<String, Object> processVariables) {
        return getKeyValue(SENDER_KEY, camundaProperties, processVariables);
    }

    public static String getSubjectKeyValue(Map<String, Object> camundaProperties, Map<String, Object> processVariables) {
        return getKeyValue(SUBJECT_KEY, camundaProperties, processVariables);
    }

    public static Collection<String> getAttachmentsKeyValue(Map<String, Object> camundaProperties, Map<String, Object> processVariables) {
        String keyValue = getKeyValue(ATTACHMENTS_KEY, camundaProperties, processVariables);
        return keyValue == null || keyValue.length() == 0
            ? Collections.singletonList(keyValue)
            : Arrays.asList(keyValue.split("\\s*,\\s*"));
    }

    public static String getTemplateKeyValue(Map<String, Object> camundaProperties, Map<String, Object> processVariables) {
        return getKeyValue(TEMPLATE_KEY, camundaProperties, processVariables);
    }

    private static void validateExpectedKey(String keyName, Map<String, Object> camundaProperties)
            throws ExpectedElementTemplatePropertyNotFoundException, IllegalElementTemplatePropertyValueException {
        if (!camundaProperties.containsKey(keyName)) {
            throw new ExpectedElementTemplatePropertyNotFoundException("Expected property key '" + keyName + "' not found!");
        }
        if (StringUtils.isBlank((String) camundaProperties.get(keyName))) {
            throw new IllegalElementTemplatePropertyValueException("Property value for '" + keyName + "' is blank!");
        }
    }

    private static String getKeyValue(String keyName, Map<String, Object> camundaProperties, Map<String, Object> processVariables) {
        // get key value from camunda properties
        String keyValue = (String) camundaProperties.get(keyName);

        if (keyValue == null) {
            return null;
        }

        // check if the key value is a camunda expression
        Matcher camundaExpressionMatcher = camundaExpressionPattern.matcher(keyValue);
        if (camundaExpressionMatcher.find()) {
            String keyNameFromExpression = camundaExpressionMatcher.group(1);

            // return key value from process variables
            return (String) processVariables.get(keyNameFromExpression);
        } else {
            // return key value from camunda properties
            return keyValue;
        }
    }
}
