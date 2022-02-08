package com.ritense.objectsapi.taak

import java.util.Optional

fun <T> Optional<T>.orNull(): T? = orElse(null)