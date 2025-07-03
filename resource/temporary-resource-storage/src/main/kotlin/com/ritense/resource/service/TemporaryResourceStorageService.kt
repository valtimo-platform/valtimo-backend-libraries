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

package com.ritense.resource.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.resource.domain.MetadataType
import com.ritense.temporaryresource.domain.ResourceStorageMetadata
import com.ritense.temporaryresource.domain.ResourceStorageMetadataId
import com.ritense.temporaryresource.domain.StorageMetadataKeys
import com.ritense.temporaryresource.domain.getEnumFromKey
import com.ritense.temporaryresource.repository.ResourceStorageMetadataRepository
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.upload.MimeTypeDeniedException
import com.ritense.valtimo.contract.upload.ValtimoUploadProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.tika.Tika
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.io.BufferedInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.SecureRandom
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.pathString
import kotlin.io.path.readText

@Service
@SkipComponentScan
class TemporaryResourceStorageService(
    private val random: SecureRandom = SecureRandom(),
    valtimoResourceTempDirectory: String = "",
    private val uploadProperties: ValtimoUploadProperties,
    private val objectMapper: ObjectMapper,
    private val repository: ResourceStorageMetadataRepository
) {
    val tempDir: Path = if (valtimoResourceTempDirectory.isNotBlank()) {
        Path.of(valtimoResourceTempDirectory)
    } else {
        TEMP_DIR
    }

    init {
        logger.info { "Using the following path for temporary file resources: '$tempDir'" }
    }

    fun store(inputStream: InputStream, metadata: Map<String, Any> = emptyMap()): String {
        val dataFile = BufferedInputStream(inputStream).use { bis ->
            if (uploadProperties.acceptedMimeTypes.isNotEmpty()) {
                //Tika marks the stream, reads the first few bytes and resets it when done.
                // The mediatype will only contain the mimetype, any extra parameters are stripped off
                val mediaType = Tika().detect(bis).split(";")[0].trim()
                if (!uploadProperties.acceptedMimeTypes.contains(mediaType)) {
                    throw MimeTypeDeniedException("$mediaType is not whitelisted for uploads.")
                }
            }
            val tempFile = Files.createTempFile(tempDir, "temporaryResource", ".tmp")
            tempFile.toFile().outputStream().use { bis.copyTo(it) }
            tempFile
        }

        val metaDataContent = metadata + mapOf(
            MetadataType.FILE_PATH.key to dataFile.absolutePathString(),
            MetadataType.FILE_SIZE.key to dataFile.fileSize().toString()
        )
        val metaDataFile = Files.createTempFile(tempDir, "${random.nextLong().toULong()}-", ".json")
        writeMetaDataFile(metaDataFile, metaDataContent)

        return metaDataFile.nameWithoutExtension
    }

    /**
     * This method can be used to enrich the metadata before it is handled.
     */
    fun patchResourceMetaData(id: String, metaData: Map<String, Any?>) {
        require(!metaData.containsKey(MetadataType.FILE_PATH.key)) { "${MetadataType.FILE_PATH.key} cannot be patched!" }

        val metaDataFile = getMetaDataFileFromResourceId(id)
        require(!metaDataFile.notExists()) { "No resource found with id '$id'" }

        val originalMetaData = getMetadataFromFile(metaDataFile, false)
        // Since the metadata does not allow null values, this code removes the key when the value is null
        val newMetaData = (originalMetaData + metaData)
            .mapNotNull { (key, value) -> if (value != null) Pair(key, value) else null }
            .toMap()

        writeMetaDataFile(metaDataFile, newMetaData)
    }

    private fun writeMetaDataFile(file: Path, metaDataContent: Map<String, Any>) {
        file.toFile().writeText(objectMapper.writeValueAsString(metaDataContent))
    }

    fun deleteResource(id: String): Boolean {
        val metaDataFile = getMetaDataFileFromResourceId(id)
        if (metaDataFile.notExists()) {
            return false
        }
        val typeRef = object : TypeReference<Map<String, Any>>() {}
        val metadata = objectMapper.readValue(metaDataFile.readText(), typeRef)
        val dataFile = Path(metadata[MetadataType.FILE_PATH.key] as String)
        val deleted = Files.deleteIfExists(dataFile)
        Files.deleteIfExists(metaDataFile)
        return deleted
    }

    fun getResourceContentAsInputStream(id: String): InputStream {
        val metadata = getResourceMetadata(id, false)
        val dataFile = Path(metadata[MetadataType.FILE_PATH.key] as String)
        return dataFile.inputStream()
    }

    fun getResourceMetadata(id: String): Map<String, Any> {
        return getResourceMetadata(id, true)
    }

    internal fun getResourceMetadata(id: String, filterPath: Boolean): Map<String, Any> {
        val metaDataFile = getMetaDataFileFromResourceId(id)
        require(!metaDataFile.notExists()) { "No resource found with id '$id'" }

        return getMetadataFromFile(metaDataFile, filterPath)
    }

    internal fun getMetaDataFileFromResourceId(resourceId: String): Path {
        val safeFileName = Path("$resourceId.json").fileName.toString()
        return Path.of(tempDir.pathString, safeFileName)
    }

    internal fun getMetadataFromFile(metaDataFile: Path, filterPath: Boolean): Map<String, Any> {
        val typeRef = object : TypeReference<Map<String, Any>>() {}
        return objectMapper.readValue(metaDataFile.readText(), typeRef)
            .filter {
                !filterPath || it.key != MetadataType.FILE_PATH.key
            }
    }

    fun getMetadataValue(resourceStorageFieldId: String, metadataKey: String): String {
        return getEnumFromKey(metadataKey).fold(
            onSuccess = { enumKey ->
                getMetadataValueOrNull(resourceStorageFieldId, enumKey)
                    ?: throw IllegalStateException("Resource $resourceStorageFieldId does not exist")
            },
            onFailure = { exception ->
                throw IllegalStateException("Failed to resolve metadata key '$metadataKey'", exception)
            }
        )
    }

    fun getMetadataValueOrNull(resourceStorageFieldId: String, metadataKey: StorageMetadataKeys): String? {
        return repository.findByIdOrNull(
            ResourceStorageMetadataId(
                fileId = resourceStorageFieldId,
                metadataKey = metadataKey
            )
        )?.metadataValue
    }

    fun saveMetadataValue(resourceStorageFieldId: String, metadataKey: StorageMetadataKeys, metadataValue: String?) {
        repository.save(
            ResourceStorageMetadata(
                id = ResourceStorageMetadataId(
                    fileId = resourceStorageFieldId,
                    metadataKey = metadataKey
                ),
                metadataValue = metadataValue,
            )
        )
    }

    companion object {
        val logger = KotlinLogging.logger {}
        val TEMP_DIR: Path = Files.createTempDirectory("temporaryResourceDirectory")
    }
}
