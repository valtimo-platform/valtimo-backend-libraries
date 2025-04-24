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

package com.ritense.valtimo.contract.repository

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.semver4j.Semver
import org.semver4j.SemverException
import java.math.BigInteger
import java.util.Locale
import java.util.regex.Pattern

@Converter
class SemverConverter: AttributeConverter<Semver?, String?> {
    override fun convertToDatabaseColumn(attribute: Semver?): String? {
        return SemverConverter.convertToDatabaseColumn(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): Semver? {
        return SemverConverter.convertToEntityAttribute(dbData)
    }

    companion object {
        private val maxInt = BigInteger.valueOf(Int.MAX_VALUE.toLong())
        private val parsePattern = Pattern.compile("(\\d{1,6})\\.(\\d{1,6})\\.(\\d{1,6})(.*)")

        fun convertToDatabaseColumn(attribute: Semver?): String? {
            return attribute?.let {
                var resultVersion = String.format(
                    Locale.ROOT, "%06d.%06d.%06d",
                    it.major,
                    it.minor,
                    it.patch
                )
                if (it.preRelease.isNotEmpty()) {
                    resultVersion = "$resultVersion-${java.lang.String.join(".", it.preRelease)}"
                }

                if (it.build.isNotEmpty()) {
                    resultVersion = "$resultVersion+${java.lang.String.join(".", it.build)}"
                }

                return resultVersion
            }
        }

        fun convertToEntityAttribute(dbData: String?): Semver? {
            return dbData?.let { parse(it) }
        }

        fun parse(version: String): Semver {
            val matcher = parsePattern.matcher(version)
            if (!matcher.matches()) {
                throw SemverException(String.format(Locale.ROOT, "Version [%s] is not valid semver.", version))
            } else {
                val major = parseInt(matcher.group(1))
                val minor = parseInt(matcher.group(2))
                val patch = parseInt(matcher.group(3))
                val preReleaseAndBuild = matcher.group(4)
                return Semver(String.format(Locale.ROOT, "$major.$minor.$patch$preReleaseAndBuild"))
            }
        }

        private fun parseInt(maybeInt: String): Int {
            val secureNumber = BigInteger(maybeInt)
            if (maxInt.compareTo(secureNumber) < 0) {
                throw SemverException(String.format(Locale.ROOT, "Value [%s] is too big.", maybeInt))
            } else {
                return secureNumber.toInt()
            }
        }
    }
}