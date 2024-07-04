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

package com.ritense.exporter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.core.util.Separators
import java.io.IOException


class ExportPrettyPrinter : DefaultPrettyPrinter {
    constructor() {
        _arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
        _objectIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
    }

    constructor(base: DefaultPrettyPrinter?) : super(base)

    override fun createInstance(): ExportPrettyPrinter {
        check(javaClass == ExportPrettyPrinter::class.java) {
            ("Failed `createInstance()`: " + javaClass.name
                + " does not override method; it has to")
        }
        return ExportPrettyPrinter(this)
    }

    override fun withSeparators(separators: Separators): ExportPrettyPrinter {
        this._separators = separators
        this._objectFieldValueSeparatorWithSpaces = separators.objectFieldValueSeparator.toString() + " "
        return this
    }

    @Throws(IOException::class)
    override fun writeEndArray(generator: JsonGenerator, nrOfValues: Int) {
        if (!_arrayIndenter.isInline) {
            --_nesting
        }
        if (nrOfValues > 0) {
            _arrayIndenter.writeIndentation(generator, _nesting)
        }
        generator.writeRaw(']')
    }

    @Throws(IOException::class)
    override fun writeEndObject(generator: JsonGenerator, nrOfEntries: Int) {
        if (!_objectIndenter.isInline) {
            --_nesting
        }
        if (nrOfEntries > 0) {
            _objectIndenter.writeIndentation(generator, _nesting)
        }
        generator.writeRaw('}')
    }
}