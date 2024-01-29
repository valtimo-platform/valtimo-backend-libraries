package com.ritense.inbox

import reactor.core.publisher.Sinks

class InboxSink: Sinks.Many<String> by Sinks.many().multicast().onBackpressureBuffer()