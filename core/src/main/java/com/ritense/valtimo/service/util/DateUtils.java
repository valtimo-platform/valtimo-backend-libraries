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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.TimeZone;

public class DateUtils {

    private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");

    public static SimpleDateFormat getSimpleUtcDateFormat(String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(utcTimeZone);
        return simpleDateFormat;
    }

    public static String getIso8601UtcDateFormat(Object date) {
        DateFormat iso8601UtcFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        iso8601UtcFormatter.setTimeZone(utcTimeZone);
        return iso8601UtcFormatter.format(date);
    }

    public static long toEpochMilliSeconds(LocalDate localDate) {
        return localDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;
    }

}
