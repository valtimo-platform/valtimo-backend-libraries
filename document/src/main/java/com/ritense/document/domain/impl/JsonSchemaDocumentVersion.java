/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.document.domain.impl;

import com.ritense.document.domain.DocumentVersion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class JsonSchemaDocumentVersion implements DocumentVersion {

    private JsonDocumentContent documentContent;
    private JsonSchemaDocumentDefinitionId documentDefinitionId;
    private String versionAsString;

    private JsonSchemaDocumentVersion(JsonSchemaDocument document) {
        // Document is mutable, this class is lazy, if document is updated before a version is computed
        // the object representing the document version before modification will equal the modified version!
        // Hence, store refs to the content and def instead. As those are immutable!
        this.documentContent = document.content();
        this.documentDefinitionId = document.definitionId();
        this.versionAsString = null;
    }

    private JsonSchemaDocumentVersion(String versionAsString) {
        // Ideally: split this class into two implementations of DocumentVersion, one dynamic/lazy/document based other lazy
        // For now: two constructors
        this.documentContent = null;
        this.documentDefinitionId = null;
        this.versionAsString = versionAsString;
    }

    /**
     * Returns the document version for the given document.
     *
     * @param document The document of which the version will be computed.
     * @return The document version
     */
    public static JsonSchemaDocumentVersion of(JsonSchemaDocument document) {
        return new JsonSchemaDocumentVersion(document);
    }

    /**
     * Returns the document version from a document-version-as-string representation (aka: deserialize).
     *
     * @param precomputed The string representation of a JsonSchemaDocumentVersion.
     * @return The (deserialized) document version
     */
    public static JsonSchemaDocumentVersion from(String precomputed) {
        return new JsonSchemaDocumentVersion(precomputed);
    }

    /**
     * String representation of the document version.
     *
     * @return The serialized document version
     */
    @Override
    public String toString() {
        if (versionAsString == null) {
            versionAsString = computeHashVersion();
        }
        return versionAsString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JsonSchemaDocumentVersion that = (JsonSchemaDocumentVersion) o;
        return this.toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionAsString);
    }

    private String computeHashVersion() {
        var digest = new DigestUtils(MessageDigestAlgorithms.SHA_256).getMessageDigest();
        digest.update(String.valueOf(documentContent.asJson().hashCode()).getBytes());
        digest.update(documentDefinitionId.name().getBytes());
        if (documentDefinitionId.version() > 1) {
            digest.update(Long.toString(documentDefinitionId.version()).getBytes());
        }
        var digestedBytes = digest.digest();
        return Hex.encodeHexString(digestedBytes);
    }

}