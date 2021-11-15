package com.ritense.externalevent.messaging.`in`

import com.ritense.externalevent.messaging.ExternalDomainMessage

class ExternalIdUpdatedConfirmationMessage (val externalId: String) : ExternalDomainMessage()
