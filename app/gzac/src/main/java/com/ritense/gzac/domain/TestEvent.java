package com.ritense.gzac.domain;

import com.ritense.valtimo.web.sse.event.BaseSseEvent;

public class TestEvent extends BaseSseEvent {
    public TestEvent() {
        super("CASE_CREATED");
    }
}
