package com.ritense.documentenapi.event

import com.ritense.outbox.domain.BaseEvent

class DocumentInformatieObjectDownloaded (documentInformatieobjectUrl: String) : BaseEvent(
    type = "com.ritense.gzac.drc.enkelvoudiginformatieobject.downloaded",
    resultType = "java.io.InputStream",
    resultId = documentInformatieobjectUrl,
    result = null
)