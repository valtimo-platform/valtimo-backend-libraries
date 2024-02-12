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

package com.ritense.smartdocuments.io

import java.io.IOException
import java.io.InputStream
import org.apache.commons.lang3.math.NumberUtils.min

/*
 * A limited input stream. Only reads a section of the given input stream.
 *
 * @param inputStream the stream to change.
 * @param startByteIndex the start index (inclusive).
 * @param endByteIndex the end index (exclusive).
 */
class SubInputStream(
    inputStream: InputStream,
    startByteIndex: Long,
    val endByteIndex: Long
) : BaseInputStream(inputStream) {

    private var index: Long = 0

    init {
        super.skipNBytes(startByteIndex)
        index += startByteIndex
    }

    override fun read(): Int {
        if (closed) {
            throw IOException("Stream is closed")
        }
        if (index >= endByteIndex) {
            return -1
        }
        val b = super.read()
        index++
        return b
    }

    override fun available() = min(super.available(), (endByteIndex - index).toInt())
}