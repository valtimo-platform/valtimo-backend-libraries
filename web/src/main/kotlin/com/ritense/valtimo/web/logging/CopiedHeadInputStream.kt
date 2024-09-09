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

package com.ritense.valtimo.web.logging

import mu.KotlinLogging
import java.io.IOException
import java.io.InputStream

class CopiedHeadInputStream(
    val inputStream: InputStream,
    val buffer: IntArray = IntArray(DEFAULT_BUFFER_SIZE),
    val onHeadReady: (ByteArray) -> Unit = { _: ByteArray -> }
) : InputStream() {
    private var index: Int = 0

    private var closed: Boolean = false

    override fun read(): Int {
        checkClosed()
        val b = inputStream.read()
        if (b == -1 && index < buffer.size || index == buffer.size) {
            onHeadReady(buffer.take(index).map { it.toByte() }.toByteArray())
            index++
        } else if (index < buffer.size) {
            buffer[index] = b
            index++
        }
        return b
    }

    override fun read(b: ByteArray): Int {
        checkClosed()
        return super.read(b)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        checkClosed()
        return super.read(b, off, len)
    }

    override fun skip(n: Long): Long {
        checkClosed()
        return inputStream.skip(n)
    }

    override fun available(): Int {
        checkClosed()
        return inputStream.available()
    }

    override fun close() {
        if (!closed) {
            closed = true
            inputStream.close()
        }
    }

    override fun mark(readlimit: Int) = inputStream.mark(readlimit)
    override fun reset() = inputStream.reset()
    override fun markSupported() = inputStream.markSupported()

    private fun checkClosed() {
        if (closed) {
            throw IOException("Stream is closed")
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}