package com.ritense.objectsapi.taak

import com.ritense.connector.domain.ConnectorProperties
import com.ritense.objectsapi.service.ObjectsApiProperties

class TaakProperties(
    var objectsApiProperties: ObjectsApiProperties = ObjectsApiProperties(),
): ConnectorProperties