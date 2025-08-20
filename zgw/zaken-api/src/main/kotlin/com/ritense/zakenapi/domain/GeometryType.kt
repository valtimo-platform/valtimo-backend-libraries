package com.ritense.zakenapi.domain

import com.fasterxml.jackson.annotation.JsonValue

enum class GeometryType(@JsonValue val key: String) {
    POINT("Point"),
    MULTI_POINT("MultiPoint"),
    LINE_STRING("LineString"),
    MULTI_LINE_STRING("MultiLineString"),
    POLYGON("Polygon"),
    GEOMETRY_COLLECTION("GeometryCollection"),
    MULTI_POLYGON("MultiPolygon")
}