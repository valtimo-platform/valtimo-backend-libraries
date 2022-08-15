/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.resource.service

import com.fasterxml.jackson.core.type.TypeReference
import com.ritense.resource.domain.MetadataType
import com.ritense.valtimo.contract.json.Mapper
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.inputStream
import kotlin.io.path.notExists
import kotlin.io.path.readText

class TemporaryResourceStorageService {

    fun store(inputStream: InputStream, metadata: Map<String, Any> = emptyMap()): String {
        val dataFile = Files.createTempFile(TEMP_DIR, "temporaryResource", ".tmp")
        dataFile.toFile().outputStream().use { inputStream.copyTo(it) }

        val mutableMetadata = metadata.toMutableMap()
        mutableMetadata[MetadataType.FILE_PATH.toString()] = dataFile.absolutePathString()
        val metaDataFile = Files.createTempFile(TEMP_DIR, "temporaryResourceMetadata", ".json")
        metaDataFile.toFile().writeText(Mapper.INSTANCE.get().writeValueAsString(mutableMetadata))

        return metaDataFile.absolutePathString()
    }

    fun deleteResource(id: String): Boolean {
        val metaDataFile = Path(id)
        if (metaDataFile.notExists()) {
            return false
        }
        val typeRef = object : TypeReference<Map<String, Any>>() {}
        val metadata = Mapper.INSTANCE.get().readValue(metaDataFile.readText(), typeRef)
        val dataFile = Path(metadata[MetadataType.FILE_PATH.toString()] as String)
        val deleted = Files.deleteIfExists(dataFile)
        Files.deleteIfExists(metaDataFile)
        return deleted
    }

    fun getResourceContentAsInputStream(id: String): InputStream {
        val metadata = getResourceMetadata(id)
        val dataFile = Path(metadata[MetadataType.FILE_PATH.toString()] as String)
        return dataFile.inputStream()
    }

    fun getResourceMetadata(id: String): Map<String, Any> {
        val metaDataFile = Path(id)
        if (metaDataFile.notExists()) {
            throw IllegalArgumentException("No resource found with id '$id'")
        }
        val typeRef = object : TypeReference<Map<String, Any>>() {}
        return Mapper.INSTANCE.get().readValue(metaDataFile.readText(), typeRef)
    }

    companion object {
        val TEMP_DIR: Path = Files.createTempDirectory("temporaryResourceDirectory")
    }
}
