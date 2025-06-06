package com.ritense.iko.service
import java.util.function.Function
interface QueryParamValueResolverFactory {

    /**
     * Creates a resolver for query-param-based value resolution.
     *
     * @param queryParam The query string context these values belong to
     * @return a resolver that handles one requestedValue at a time within the same context.
     */
    fun createResolver(queryParam: QueryString): Function<String, Any?>

    fun resolveValues(
        queryParam: QueryString,
        requestedValues: Collection<String>
    ): Map<String, Any?>
}


