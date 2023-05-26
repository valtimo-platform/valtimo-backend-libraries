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

open class BaseInputStream(
    val inputStream: InputStream
) : InputStream() {

    protected var closed: Boolean = false

    override fun read(): Int {
        if (closed) {
            throw IOException("Stream is closed")
        }
        return inputStream.read()
    }

    override fun read(b: ByteArray): Int {
        if (closed) {
            throw IOException("Stream is closed")
        }
        return super.read(b)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (closed) {
            throw IOException("Stream is closed")
        }
        return super.read(b, off, len)
    }

    override fun skip(n: Long): Long {
        if (closed) {
            throw IOException("Stream is closed")
        }
        return inputStream.skip(n)
    }

    override fun available(): Int {
        if (closed) {
            throw IOException("Stream is closed")
        }
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
}