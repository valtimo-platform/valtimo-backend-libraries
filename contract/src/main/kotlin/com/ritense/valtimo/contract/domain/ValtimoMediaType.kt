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

package com.ritense.valtimo.contract.domain

import java.nio.charset.StandardCharsets.UTF_8
import org.springframework.http.MediaType

object ValtimoMediaType {

    val APPLICATION_JSON_UTF8 = MediaType("application", "json", UTF_8)
    const val APPLICATION_JSON_UTF8_VALUE = "application/json;charset=UTF-8"
    const val TEXT_PLAIN_UTF8_VALUE = "text/plain;charset=UTF-8"

}
