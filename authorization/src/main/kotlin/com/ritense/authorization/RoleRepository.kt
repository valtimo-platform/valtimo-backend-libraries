package com.ritense.authorization

import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository: JpaRepository<Role, String>