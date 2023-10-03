package com.ritense.gzac.web.rest;

import com.ritense.gzac.domain.TestEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestResource {
    private ApplicationEventPublisher applicationEventPublisher;

    public TestResource(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/event")
    public ResponseEntity publishTestEvent() {
        applicationEventPublisher.publishEvent(new TestEvent());
        return ResponseEntity.noContent().build();
    }
}