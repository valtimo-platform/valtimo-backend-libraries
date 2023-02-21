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

package com.ritense.valtimo.contract.utils;

public class AssertionConcern {

    public void assertArgumentEquals(Object firstObject, Object secondObject, String message) {
        if (!firstObject.equals(secondObject)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgumentFalse(boolean value, String message) {
        if (value) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgumentLength(String value, int maximum, String message) {
        int length = value.trim().length();
        if (length > maximum) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgumentLength(String value, int minimum, int maximum, String message) {
        int length = value.trim().length();
        if (length < minimum || length > maximum) {
            throw new IllegalArgumentException(message + " - value '" + value + "'");
        }
    }

    public static void assertArgumentNotEmpty(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgumentNotEquals(Object firstObject, Object secondObject, String message) {
        if (firstObject.equals(secondObject)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgumentNotNull(Object anObject, String message) {
        if (anObject == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgumentNull(Object anObject, String message) {
        if (anObject != null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgumentRange(double value, double minimum, double maximum, String message) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgumentRange(float value, float minimum, float maximum, String message) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgumentRange(int value, int minimum, int maximum, String message) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgumentRange(long value, long minimum, long maximum, String message) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgumentTrue(boolean value, String message) {
        if (!value) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertStateFalse(boolean value, String message) {
        if (value) {
            throw new IllegalStateException(message);
        }
    }

    public static void assertStateTrue(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }

}