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

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.LinkedList
import java.util.Queue

/*
 * An input stream that unescapes unicode characters.
 */
class UnicodeUnescapeInputStream(
    inputStream: InputStream,
    charset: Charset = Charsets.UTF_8,
) : FilterInputStream(inputStream) {

    private var closed: Boolean = false
    private val reader = InputStreamReader(inputStream, charset)
    private val buf: Queue<Int> = LinkedList()

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
            buf.add(char2)
            return char1
        }

        val hexChars = (0..3).map { read0() }
        val unicodeChar = String(hexChars.map { Char(it) }.toCharArray()).toIntOrNull(16)
        if (unicodeChar == null) {
            buf.addAll(hexChars)
            return char1
        }

        return unicodeChar
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (closed) {
            throw IOException("Stream is closed")
        }
        return super.read(b, off, len)
    }

    override fun available(): Int {
        if (closed) {
            throw IOException("Stream is closed")
        }
        return super.available()
    }

    override fun close() {
        if (!closed) {
            closed = true
            reader.close()
        }
    }

    private fun read0(): Int {
        return if (buf.isEmpty()) {
            reader.read()
        } else {
            buf.poll()
        }
    }
}
