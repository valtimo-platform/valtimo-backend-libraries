package com.ritense.valtimo.script

import com.ritense.valtimo.camunda.domain.ValtimoScript
import org.springframework.data.jpa.repository.JpaRepository

interface ValtimoScriptRepository : JpaRepository<ValtimoScript, String>