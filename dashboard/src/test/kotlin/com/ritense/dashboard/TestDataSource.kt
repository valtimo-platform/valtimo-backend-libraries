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

package com.ritense.dashboard

import com.ritense.valtimo.contract.dashboard.WidgetDataSource
import mu.KLogger
import mu.KotlinLogging

class TestDataSource {

    @WidgetDataSource(
        key = NUMBERS_DATA_KEY,
        title = NUMBERS_DATA_TITLE
    )
    fun numbersData(): TestWidgetNumbersResult {
        return TestWidgetNumbersResult(emptyList(), 0)
    }

    @WidgetDataSource(
        key = NUMBER_DATA_KEY,
        title = NUMBER_DATA_TITLE,
    )
    fun numberData(testDataSourceProperties: TestDataSourceProperties): TestWidgetNumberResult {
        logger.info { "numberData($testDataSourceProperties)" }
        return TestWidgetNumberResult(1, 0)
    }

    fun testNonAnnotatedMethod(): String {
        return "test"
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val NUMBER_DATA_KEY = "number-data"
        const val NUMBER_DATA_TITLE = "Number data"
        const val NUMBERS_DATA_KEY = "numbers-data"
        const val NUMBERS_DATA_TITLE = "Numbers data"
    }
}