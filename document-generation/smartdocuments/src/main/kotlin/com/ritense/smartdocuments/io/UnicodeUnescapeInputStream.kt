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

package com.ritense.smartdocuments.io

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

/*
 * An input stream that unescapes unicode characters.
 */
class UnicodeUnescapeInputStream(
    inputStream: InputStream,
    charset: Charset = Charsets.UTF_8,
) : BaseInputStream(inputStream) {

    private val reader = InputStreamReader(inputStream, charset)
    private var spare: Int = -1

    override fun read(): Int {
        if (closed) {
            throw IOException("Stream is closed")
        }

        val char1 = read0()
        if (char1 != '\\'.code) {
            return char1
        }

        val char2 = read0()
        if (char2 != 'u'.code) {
            spare = char2
            return char1
        }

        var char3 = read0()
        while (char3 == 'u'.code) {
            char3 = read0()
        }
        if (char3 == '+'.code) {
            char3 = read0()
        }

        val unicodeHex = String(charArrayOf(Char(char3), Char(read0()), Char(read0()), Char(read0())))
        return try {
            unicodeHex.toInt(16)
        } catch (e: Exception) {
            throw IllegalArgumentException("Unable to parse unicode value: '\\u$unicodeHex'", e)
        }
    }

    private fun read0(): Int {
        return if (spare == -1) reader.read() else spare
    }
}
