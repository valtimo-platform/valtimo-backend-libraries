/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.valtimo.web.util;

import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for ResponseEntity creation.
 */
public interface ResponseUtil {

    /**
     * Wrap the optional into a {@link ResponseEntity} with an {@link HttpStatus#OK} status, or if it's empty, it
     * returns a {@link ResponseEntity} with {@link HttpStatus#NOT_FOUND}.
     *
     * @param <X>           type of the response
     * @param maybeResponse response to return if present
     * @return response containing {@code maybeResponse} if present or {@link HttpStatus#NOT_FOUND}
     */
    static <X> ResponseEntity<X> wrapOrNotFound(Optional<X> maybeResponse) {
        return wrapOrNotFound(maybeResponse, null);
    }

    /**
     * Wrap the optional into a {@link ResponseEntity} with an {@link HttpStatus#OK} status with the headers, or if it's
     * empty, it returns a {@link ResponseEntity} with {@link HttpStatus#NOT_FOUND}.
     *
     * @param <X>           type of the response
     * @param maybeResponse response to return if present
     * @param header        headers to be added to the response
     * @return response containing {@code maybeResponse} if present or {@link HttpStatus#NOT_FOUND}
     */
    static <X> ResponseEntity<X> wrapOrNotFound(Optional<X> maybeResponse, HttpHeaders header) {
        return maybeResponse.map(response -> ResponseEntity.ok().headers(header).body(response))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}