package com.ritense.resource.event

import org.springframework.context.ApplicationEvent

class ResourceStorageMetadataAvailableEvent(
    source: Any,
    val resourceId: String,
    val documentId: String,
    val documentUrl: String,
    val downloadUrl: String,
) : ApplicationEvent(source)