package com.ritense.externalevent.messaging.`in`

import com.ritense.externalevent.messaging.ExternalDomainMessage

@Deprecated("Since 12.0.0")
class ExternalIdUpdatedConfirmationMessage (val externalId: String) : ExternalDomainMessage()
