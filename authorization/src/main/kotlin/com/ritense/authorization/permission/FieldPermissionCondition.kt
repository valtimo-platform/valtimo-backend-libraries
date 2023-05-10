package com.ritense.authorization.permission

import com.fasterxml.jackson.annotation.JsonTypeName
import com.ritense.authorization.permission.FieldPermissionCondition.Companion.FIELD
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@JsonTypeName(FIELD)
data class FieldPermissionCondition(
    val field: String,
    val value: Any?
    // TODO: We might have to support operators here as well. Currently, this condition behaves as EQUAL_TO.
) : ReflectingPermissionCondition(PermissionConditionType.FIELD) {
    override fun <T : Any> isValid(entity: T): Boolean {
        return findEntityFieldValue(entity, field) == value
    }

    override fun <T : Any> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val path: Path<Any>? = createDatabaseObjectPath(field, root, resourceType)

        return criteriaBuilder.equal(path, this.value)
    }

    companion object {
        const val FIELD = "field"
    }
}