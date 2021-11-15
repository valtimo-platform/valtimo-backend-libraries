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

package com.ritense.mail.domain.blacklist;

import com.ritense.valtimo.contract.basictype.EmailAddress;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "blacklisted_email")
public class BlacklistedEmail {

    @Id
    @EqualsAndHashCode.Include()
    @Column(name = "email", columnDefinition = "VARCHAR(500)")
    private String emailAddress;

    @Column(name = "date_created", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "cause", columnDefinition = "VARCHAR(500)")
    private String cause;

    public BlacklistedEmail(String emailAddress, String cause) {
        Assert.notNull(emailAddress, "emailAddress is required");
        this.emailAddress = emailAddress;
        this.cause = cause;
        this.dateCreated = LocalDateTime.now();
    }

    public EmailAddress emailAddress() {
        return EmailAddress.from(emailAddress);
    }

}