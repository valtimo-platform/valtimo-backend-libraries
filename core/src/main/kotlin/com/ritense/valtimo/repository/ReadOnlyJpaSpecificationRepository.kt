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

package com.ritense.valtimo.repository

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery
import java.util.Optional
import java.util.function.Function

@NoRepositoryBean
interface ReadOnlyJpaSpecificationRepository<T, ID> : Repository<T, ID>, JpaSpecificationExecutor<T> {

    fun findAll(): List<T>

    fun findAll(sort: Sort): List<T>

    fun findAllById(ids: Iterable<ID>): List<T>

    fun <S : T> findAll(example: Example<S>): List<S>

    fun <S : T> findAll(example: Example<S>, sort: Sort): List<S>

    fun findAll(pageable: Pageable): Page<T>

    fun <S : T> findOne(example: Example<S>): Optional<S>

    fun <S : T> findAll(example: Example<S>, pageable: Pageable): Page<S>

    fun <S : T> count(example: Example<S>): Long

    fun <S : T> exists(example: Example<S>): Boolean

    fun <S : T, R> findBy(example: Example<S>, queryFunction: Function<FetchableFluentQuery<S>, R>): R

    fun findById(id: ID): Optional<T>

    fun existsById(id: ID): Boolean

    fun count(): Long

    override fun findOne(spec: Specification<T>?): Optional<T>

    override fun findAll(spec: Specification<T>?): List<T>

    override fun findAll(spec: Specification<T>?, pageable: Pageable): Page<T>

    override fun findAll(spec: Specification<T>?, sort: Sort): List<T>

    override fun count(spec: Specification<T>?): Long

    override fun exists(spec: Specification<T>): Boolean
}