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

package com.ritense.valtimo.contract.io

import java.io.InputStream
import java.util.LinkedList

/** Finds a string and replaces it with a different string. Assumes the input stream uses UTF8. */
class FindReplaceInputStream(
    inputStream: InputStream,
    private val oldValue: String,
    private val newValue: String,
) : BaseInputStream(inputStream) {

    init {
        if (newValue.contains(oldValue)) {
            throw UnsupportedOperationException("Overlapping values are not supported")
        }
    }

    private var futureBuffer: LinkedList<Int> = LinkedList()

    override fun read(): Int {
        val b = if (futureBuffer.isNotEmpty()) {
            futureBuffer.removeFirst()
        } else {
            super.read()
        }

        if (b == -1) {
            return -1
        }

        // assume UTF8
        if (b.toChar() == oldValue[0]) {
            val matchSize = oldValue.length - 1
            val readSize = matchSize - futureBuffer.size
            if (readSize > 0) {
                futureBuffer.addAll(inputStream.readNBytes(readSize).map { it.toInt() })
            }
            if (futureBuffer.size >= matchSize) {
                val match = futureBuffer.subList(0, matchSize).map { it.toByte() }.toByteArray().decodeToString()
                if (match == oldValue.substring(1)) {
                    repeat(matchSize) { futureBuffer.removeFirst() }
                    futureBuffer.addAll(0, newValue.map { it.code })
                    return read()
                }
            }
        }

        return b
    }
}