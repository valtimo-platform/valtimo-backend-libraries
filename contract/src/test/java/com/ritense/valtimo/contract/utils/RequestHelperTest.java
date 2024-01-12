package com.ritense.valtimo.contract.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.ZoneOffset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RequestHelperTest {
    MockHttpServletRequest mockRequest;

    @BeforeEach
    public void setup() {
        mockRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    @Test
    void getZoneOffsetShouldDefaultToUtcIfHeaderEmpty() {
        ZoneOffset zoneOffset = RequestHelper.getZoneOffset();

        assertThat(zoneOffset).isSameAs(ZoneOffset.UTC);
    }

    @Test
    void getZoneOffsetShouldDefaultToUtcIfHeaderInvalid() {
        mockRequest.addHeader("Zone-Offset", "invalid zone offset");

        ZoneOffset zoneOffset = RequestHelper.getZoneOffset();

        assertThat(zoneOffset).isSameAs(ZoneOffset.UTC);
    }

    @Test
    void getZoneOffsetShouldReturnZoneOffsetSetIfValidHeader() {
        mockRequest.addHeader("Zone-Offset", "+01:00");

        ZoneOffset zoneOffset = RequestHelper.getZoneOffset();

        assertThat(zoneOffset).isSameAs(ZoneOffset.of("+01:00"));
    }
}