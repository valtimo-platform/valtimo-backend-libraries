package com.ritense.valtimo.security.interceptor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

@ConstructorBinding
@AllArgsConstructor
@Getter
@ConfigurationProperties(prefix = "valtimo.security.whitelist")
public class SecurityWhitelistProperties {
    private final List<String> hosts;
}
