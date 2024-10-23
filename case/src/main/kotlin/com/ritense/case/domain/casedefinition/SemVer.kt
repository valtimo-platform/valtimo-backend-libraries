package com.ritense.case.domain.casedefinition

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Transient

@Embeddable
data class SemVer(
    @Column(name = "semver_version")
    private val version: String = "1.0.0" // Single column for the version
) {

    @Transient
    private val major: Int

    @Transient
    private val minor: Int

    @Transient
    private val patch: Int

    init {
        // Split the version string into major, minor, and patch components
        val parts = version.split(".")
        require(parts.size == 3) { "Version must consist of three parts" }

        // Initialize the major, minor, and patch values
        major = parts[0].toInt()
        minor = parts[1].toInt()
        patch = parts[2].toInt()

        // Validate that all version parts are non-negative
        require(major >= 0) { "Major version must be greater than or equal to 0" }
        require(minor >= 0) { "Minor version must be greater than or equal to 0" }
        require(patch >= 0) { "Patch version must be greater than or equal to 0" }
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }

    companion object {
        fun fromString(version: String): SemVer {
            return SemVer(version)
        }
    }
}