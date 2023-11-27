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

package com.ritense.dashboard.datasource

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import mu.KotlinLogging
import java.lang.reflect.Method

abstract class AnnotatedClassResolver {

    inline fun <reified T : Annotation> findMethodsWithAnnotation(): List<Method> {
        return ClassGraph()
            .rejectPackages(*REJECT_PACKAGES)
            .enableClassInfo()
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan(1)
            .getClassesWithMethodAnnotation(T::class.java)
            .filter { canLoadClass<T>(it) }
            .flatMap { it.methodInfo }
            .filter { it.hasAnnotation(T::class.java) }
            .map { it.loadClassAndGetMethod() }
    }

    inline fun <reified T : Annotation> findClassesWithAnnotation(): List<Class<*>> {
        return ClassGraph()
            .rejectPackages(*REJECT_PACKAGES)
            .enableClassInfo()
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan(1)
            .getClassesWithAnnotation(T::class.java)
            .filter { canLoadClass<T>(it) }
            .loadClasses()
    }

    inline fun <reified T> canLoadClass(classInfo: ClassInfo): Boolean {
        return try {
            classInfo.loadClass()
            true
        } catch (e: Exception) {
            logger.warn { "Unable to load ${T::class.simpleName} ${classInfo.name} class, skipped" }
            logger.debug(e) { "Unable to load ${T::class.simpleName} ${classInfo.name} because of the following exception" }
            false
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
        val REJECT_PACKAGES = arrayOf(
            "org.springframework",
            "org.apache",
            "org.hibernate",
            "org.camunda",
            "org.eclipse",
            "org.bouncycastle",
            "org.gradle",
            "liquibase",
            "org.assertj",
            "camundajar",
            "org.codehaus",
            "kotlin",
            "net.bytebuddy",
            "io.netty",
            "org.glassfish",
            "oracle",
            "com.google",
            "groovy",
            "org.keycloak",
            "com.fasterxml",
            "reactor",
            "org.junit",
            "javax",
            "com.sun",
            "org.h2",
            "org.jboss",
            "com.carrotsearch",
            "com.ctc",
            "org.aspectj",
            "kotlinx",
            "connectjar",
            "groovyjarjarantlr4",
            "com.github",
            "org.mockito",
            "ch.qos",
            "org.postgresql",
            "org.spockframework",
            "javassist",
            "com.microsoft",
            "io.micrometer",
            "io.vavr",
            "org.checkerframework",
            "io.github",
            "com.datical",
            "org.joda",
            "io.smallrye",
            "io.swagger",
            "org.yaml",
            "picocli",
            "groovyjarjarpicocli",
            "antlr",
            "groovyjarjarantlr",
            "org.springdoc",
            "com.jayway",
            "com.esotericsoftware",
            "org.dom4j",
            "org.everit",
            "org.xmlunit",
            "io.jsonwebtoken",
            "net.rubygrapefruit",
            "nonapi",
            "org.objectweb",
            "com.vladmihalcea",
            "org.jvnet",
            "camundafeel",
            "groovyjarjarasm",
            "com.codahale",
            "org.hamcrest",
            "net.minidev",
            "com.thoughtworks",
            "io.holunda"
        )
    }
}