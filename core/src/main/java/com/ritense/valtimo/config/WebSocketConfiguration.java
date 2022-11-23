/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {


    public static final String TASK_CHANGED_CHANNEL = "/topic/taskChanged";

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // Message received on one of those below destinationPrefixes will
        // be automatically routed to controllers @MessageMapping
        // Used for inbound messages
//        config.setApplicationDestinationPrefixes("/app")

        // These are the endpoints the client can subscribe to.
        // For outbound messages
        config.enableSimpleBroker(TASK_CHANGED_CHANNEL);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Handshake endpoint
        registry
            .addEndpoint("/socket")
            .setAllowedOrigins("*");
            //.withSockJS();
    }
}
