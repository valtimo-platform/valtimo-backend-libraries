/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.accessandentitlement.web.rest;

import com.ritense.valtimo.accessandentitlement.domain.Authority;
import com.ritense.valtimo.accessandentitlement.domain.AuthorityRequest;
import com.ritense.valtimo.accessandentitlement.service.AuthorityService;
import com.ritense.valtimo.web.rest.util.HeaderUtil;
import com.ritense.valtimo.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AuthorityResource {

    private static final String AUTHORITY = "authority";
    private static final Logger logger = LoggerFactory.getLogger(AuthorityResource.class);
    private final AuthorityService authorityService;

    public AuthorityResource(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @PostMapping(value = "/authorities")
    public ResponseEntity<Authority> createAuthority(
        @RequestBody @Valid AuthorityRequest authorityRequest
    ) throws URISyntaxException {
        logger.debug("REST request to create Authority : {}", authorityRequest);
        final Authority authority = authorityService.createAuthority(authorityRequest);
        return ResponseEntity.created(new URI("/api/v1/authorities/" + authority.getName()))
            .headers(HeaderUtil.createEntityCreationAlert(AUTHORITY, authority.getName()))
            .body(authority);
    }

    @PutMapping(value = "/authorities")
    public ResponseEntity<Authority> updateAuthority(@RequestBody @Valid AuthorityRequest authorityRequest) {
        final Authority authority = authorityService.updateAuthority(authorityRequest);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(AUTHORITY, authority.getName()))
            .body(authority);
    }

    @GetMapping(value = "/authorities")
    public ResponseEntity<List<Authority>> getAllAuthorities(Pageable pageable) {
        logger.debug("REST request to get a page of Authorities");
        Page<Authority> page = authorityService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/v1/authorities");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping(value = "/authorities/{name}")
    public ResponseEntity<Authority> getAuthority(@PathVariable String name) {
        logger.debug("REST request to get Authority by name : {}", name);
        return authorityService.findBy(name)
            .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(value = "/authorities/{name}")
    public ResponseEntity<Void> deleteAuthority(@PathVariable String name) throws IllegalAccessException {
        logger.debug("REST request to delete Authority : {}", name);
        authorityService.deleteAuthority(name);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(AUTHORITY, name)).build();
    }

}
