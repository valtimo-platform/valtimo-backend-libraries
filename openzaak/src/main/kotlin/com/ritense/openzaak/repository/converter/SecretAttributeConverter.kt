/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.repository.converter

import javax.inject.Inject
import javax.persistence.AttributeConverter

class SecretAttributeConverter @Inject constructor(private val bean: Encryptor) : AttributeConverter<String, String> {

    override fun convertToDatabaseColumn(attribute: String): String {
        return bean.encrypt(attribute)
    }

    override fun convertToEntityAttribute(encryptedProperty: String): String {
        return bean.decrypt(encryptedProperty)
    }

}