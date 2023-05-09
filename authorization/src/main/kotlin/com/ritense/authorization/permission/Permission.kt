package com.ritense.authorization.permission

import com.ritense.authorization.Action
import com.ritense.valtimo.contract.database.QueryDialectHelper
import org.hibernate.annotations.Type
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


@Entity
@Table(name = "permission")
data class Permission(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "resource_type")
    val resourceType: Class<*>,

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    val action: Action,

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "conditions", columnDefinition = "json")
    val conditions: List<PermissionCondition> = emptyList(),

    @Column(name = "role_key", nullable = false)
    val roleKey: String,
) {
    fun <T> appliesTo(resourceType: Class<T>, entity: Any?): Boolean {
        return if (this.resourceType == resourceType) {
            if (entity == null && conditions.isNotEmpty()) {
                return false
            }
            conditions
                .map { it.isValid(entity!!) }
                .all { it }
        } else {
            false
        }
    }

    fun <T: Any> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        return criteriaBuilder
            .and(
                *conditions.map {
                    it.toPredicate(
                        root,
                        query,
                        criteriaBuilder,
                        resourceType,
                        queryDialectHelper)
                }.toTypedArray()
            )
    }
}