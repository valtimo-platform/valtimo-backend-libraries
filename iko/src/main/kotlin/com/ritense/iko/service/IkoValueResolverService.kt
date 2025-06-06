package com.ritense.iko.service

class IkoValueResolverService(
    val ikoValueResolver: IkoValueResolver
) {

    fun resolveValues(
        queryString: QueryString,
        requestedValues: Collection<String>
    ): Map<String, Any?> {
        // return toResolverFactoryMap(requestedValues).map { (resolverFactory, requestedValues) ->
        val resolver = ikoValueResolver.createResolver(queryString)
        // Create a list of resolved Map entries
        val aa = requestedValues.associateWith { requestedValue ->
            requestedValue to resolver.apply(trimPrefix(requestedValue))
        }
        return aa
    }

    private fun trimPrefix(value: String) = value.substringAfter(DELIMITER)

    companion object {
        const val DELIMITER = ":"
    }

}