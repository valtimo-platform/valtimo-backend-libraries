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

package com.ritense.zgw

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.zgw.ClientTools.Companion.optionalQueryParam
import com.ritense.zgw.domain.ZgwErrorResponse
import com.ritense.zgw.exceptions.ClientErrorException
import com.ritense.zgw.exceptions.RequestFailedException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import kotlin.test.assertEquals

class ClientToolsTest {

    @Test
    fun `zgwErrorHandler should return response on ok response`() {
        val mockRequest = mock<ClientRequest>()
        val mockNext = mock<ExchangeFunction>()
        val mockResponse = mock<ClientResponse>()
        whenever(mockResponse.statusCode()).thenReturn(HttpStatus.OK)
        whenever(mockNext.exchange(mockRequest)).thenReturn(Mono.just(mockResponse))
        val filterResponse = ClientTools.zgwErrorHandler().filter(mockRequest, mockNext).block()
        assertEquals(mockResponse, filterResponse)
    }

    @Test
    fun `zgwErrorHandler should return BadRequestException on 400 response`() {
        val mockRequest = mock<ClientRequest>()
        val mockNext = mock<ExchangeFunction>()
        val mockResponse = mock<ClientResponse>()
        whenever(mockResponse.statusCode()).thenReturn(HttpStatus.BAD_REQUEST)
        whenever(mockNext.exchange(mockRequest)).thenReturn(Mono.just(mockResponse))
        val errorResponse = ZgwErrorResponse(
            code = "code",
            detail = "detail",
            instance = "instance",
            invalidParams = emptyList(),
            status = 400,
            title = "title",
            type = "type"
        )
        whenever(mockResponse.bodyToMono(String::class.java)).thenReturn(
            Mono.just(
                ObjectMapper().writeValueAsString(
                    errorResponse
                )
            )
        )
        val exception = assertThrows<ClientErrorException> {
            ClientTools.zgwErrorHandler().filter(mockRequest, mockNext).block()
        }
        assertEquals(exception.statusCode, HttpStatus.BAD_REQUEST)
        assertEquals(exception.response, errorResponse)
    }

    @Test
    fun `zgwErrorHandler should return RequestFailedException on 400 response with not matching body structure`() {
        val mockRequest = mock<ClientRequest>()
        val mockNext = mock<ExchangeFunction>()
        val mockResponse = mock<ClientResponse>()
        whenever(mockResponse.statusCode()).thenReturn(HttpStatus.BAD_REQUEST)
        whenever(mockNext.exchange(mockRequest)).thenReturn(Mono.just(mockResponse))
        val errorResponse = "error"
        whenever(mockResponse.bodyToMono(String::class.java)).thenReturn(Mono.just(errorResponse))
        val exception = assertThrows<RequestFailedException> {
            ClientTools.zgwErrorHandler().filter(mockRequest, mockNext).block()
        }
        assertEquals(exception.statusCode, HttpStatus.BAD_REQUEST)
        assertEquals(exception.responseBody, errorResponse)
    }

    @Test
    fun `zgwErrorHandler should return RequestFailedException on 500 response`() {
        val mockRequest = mock<ClientRequest>()
        val mockNext = mock<ExchangeFunction>()
        val mockResponse = mock<ClientResponse>()
        whenever(mockResponse.statusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR)
        whenever(mockNext.exchange(mockRequest)).thenReturn(Mono.just(mockResponse))
        val errorResponse = "error"
        whenever(mockResponse.bodyToMono(String::class.java)).thenReturn(Mono.just(errorResponse))
        val exception = assertThrows<RequestFailedException> {
            ClientTools.zgwErrorHandler().filter(mockRequest, mockNext).block()
        }
        assertEquals(exception.statusCode, HttpStatus.INTERNAL_SERVER_ERROR)
        assertEquals(exception.responseBody, errorResponse)
    }

    @Test
    fun `optionalQueryParam should add param when not null`() {
        val builder = mock<UriBuilder>()
        val result = builder.optionalQueryParam("test", "test")
        verify(builder).queryParam("test", "test")
        assertEquals(result, builder)
    }

    @Test
    fun `optionalQueryParam should not add param when null`() {
        val builder = mock<UriBuilder>()
        val result = builder.optionalQueryParam("test", null)
        verify(builder, never()).queryParam(any(), any())
        assertEquals(result, builder)
    }
}