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
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class SendElementTemplateTaskMailHelperTest {

    private static final String RECEIVER_VALUE_STATIC = "receiver@test.com";
    private static final String RECEIVER_VALUE_EXPRESSION = "receiver@process-variable.com";

    private static final String SENDER_VALUE_STATIC = "sender@test.com";
    private static final String SENDER_VALUE_EXPRESSION = "sender@process-variable.com";

    private static final String SUBJECT_VALUE_STATIC = "subject from camunda property";
    private static final String SUBJECT_VALUE_EXPRESSION = "subject from process-variable";

    private static final String TEMPLATE_ID = "template_id";

    @Test
    void getReceiverKeyValueStaticTest() {
        String receiver = SendElementTemplateTaskMailHelper.getReceiverKeyValue(getCamundaPropertiesWithStatics(), getProcessVariables());

        assertEquals(RECEIVER_VALUE_STATIC, receiver);
    }

    @Test
    void getReceiverKeyValueExpressionTest() {
        String receiver = SendElementTemplateTaskMailHelper.getReceiverKeyValue(getCamundaPropertiesWithExpressions(), getProcessVariables());

        assertEquals(RECEIVER_VALUE_EXPRESSION, receiver);
    }

    @Test
    void getSenderKeyValueStaticTest() {
        String sender = SendElementTemplateTaskMailHelper.getSenderKeyValue(getCamundaPropertiesWithStatics(), getProcessVariables());

        assertEquals(SENDER_VALUE_STATIC, sender);
    }

    @Test
    void getSenderKeyValueExpressionTest() {
        String sender = SendElementTemplateTaskMailHelper.getSenderKeyValue(getCamundaPropertiesWithExpressions(), getProcessVariables());

        assertEquals(SENDER_VALUE_EXPRESSION, sender);
    }

    @Test
    void getSubjectKeyValueStaticTest() {
        String subject = SendElementTemplateTaskMailHelper.getSubjectKeyValue(getCamundaPropertiesWithStatics(), getProcessVariables());

        assertEquals(SUBJECT_VALUE_STATIC, subject);
    }

    @Test
    void getSubjectKeyValueExpressionTest() {
        String subject = SendElementTemplateTaskMailHelper.getSubjectKeyValue(getCamundaPropertiesWithExpressions(), getProcessVariables());

        assertEquals(SUBJECT_VALUE_EXPRESSION, subject);
    }

    @Test
    void getTemplateKeyValueStaticTest() {
        String templateId = SendElementTemplateTaskMailHelper.getTemplateKeyValue(getCamundaPropertiesWithStatics(), getProcessVariables());

        assertEquals(TEMPLATE_ID, templateId);
    }

    @Test
    void getTemplateKeyValueExpressionTest() {
        String templateId = SendElementTemplateTaskMailHelper.getTemplateKeyValue(getCamundaPropertiesWithExpressions(), getProcessVariables());

        assertEquals(TEMPLATE_ID, templateId);
    }

    @Test
    void shouldThrowExpectedElementTemplatePropertyNotFoundExceptionTest() throws IllegalElementTemplatePropertyValueException {
        Map<String, Object> camundaProperties = new HashMap<>();

        try {
            SendElementTemplateTaskMailHelper.validateExpectedKeys(camundaProperties);
            fail();
        } catch (ExpectedElementTemplatePropertyNotFoundException e) {
            System.out.print(e.getMessage());
        }
    }

    @Test
    void shouldThrowIllegalElementTemplatePropertyValueExceptionTest() throws ExpectedElementTemplatePropertyNotFoundException {
        try {
            SendElementTemplateTaskMailHelper.validateExpectedKeys(getCamundaPropertiesWithEmptyStatics());
            fail();
        } catch (IllegalElementTemplatePropertyValueException e) {
            System.out.print(e.getMessage());
        }
    }

    private Map<String, Object> getCamundaPropertiesWithStatics() {
        Map<String, Object> map = new HashMap<>();
        map.put(SendElementTemplateTaskMailHelper.RECEIVER_KEY, RECEIVER_VALUE_STATIC);
        map.put(SendElementTemplateTaskMailHelper.SENDER_KEY, SENDER_VALUE_STATIC);
        map.put(SendElementTemplateTaskMailHelper.SUBJECT_KEY, SUBJECT_VALUE_STATIC);
        map.put(SendElementTemplateTaskMailHelper.TEMPLATE_KEY, TEMPLATE_ID);

        return map;
    }

    private Map<String, Object> getCamundaPropertiesWithEmptyStatics() {
        Map<String, Object> map = new HashMap<>();
        map.put(SendElementTemplateTaskMailHelper.RECEIVER_KEY, "");
        map.put(SendElementTemplateTaskMailHelper.SENDER_KEY, "");
        map.put(SendElementTemplateTaskMailHelper.SUBJECT_KEY, "");
        map.put(SendElementTemplateTaskMailHelper.TEMPLATE_KEY, "");

        return map;
    }

    private Map<String, Object> getCamundaPropertiesWithExpressions() {
        Map<String, Object> map = new HashMap<>();
        map.put(SendElementTemplateTaskMailHelper.RECEIVER_KEY, "${email-receiver}");
        map.put(SendElementTemplateTaskMailHelper.SENDER_KEY, "${email-sender}");
        map.put(SendElementTemplateTaskMailHelper.SUBJECT_KEY, "${email-subject}");
        map.put(SendElementTemplateTaskMailHelper.TEMPLATE_KEY, TEMPLATE_ID);

        return map;
    }

    private Map<String, Object> getProcessVariables() {
        Map<String, Object> map = new HashMap<>();
        map.put("email-receiver", RECEIVER_VALUE_EXPRESSION);
        map.put("email-sender", SENDER_VALUE_EXPRESSION);
        map.put("email-subject", SUBJECT_VALUE_EXPRESSION);

        return map;
    }

    private Map<String, Object> getEmptyProcessVariables() {
        Map<String, Object> map = new HashMap<>();
        map.put("email-receiver", "");
        map.put("email-sender", "");
        map.put("email-subject", "");

        return map;
    }

}
