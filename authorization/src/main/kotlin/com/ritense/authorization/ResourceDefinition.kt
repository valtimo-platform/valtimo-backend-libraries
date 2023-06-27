package com.ritense.authorization

interface ResourceDefinition<T> {
    fun getAlias(): String
    fun getClazz(): Class<T>
    fun contextToInstance(context: Object): T
    fun getContextProperty(): String
}