package com.ritense.authorization.utils

import jakarta.persistence.criteria.AbstractQuery
import org.hibernate.query.sqm.function.SelfRenderingSqmAggregateFunction

object QueryUtils {

    fun isCountQuery(query: AbstractQuery<*>) =
        query.selection is SelfRenderingSqmAggregateFunction
        &&
        (query.selection as SelfRenderingSqmAggregateFunction<out Any>).functionName == "count"
}