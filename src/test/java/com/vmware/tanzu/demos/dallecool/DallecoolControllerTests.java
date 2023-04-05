/*
 * Copyright (c) 2023 VMware, Inc. or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vmware.tanzu.demos.dallecool;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
public class DallecoolControllerTests {
    @Autowired
    private TestRestTemplate client;

    @Test
    void testGenerateImage() {
        final var jsonReq = """
                {
                    "prompt": "Hello"
                }
                """;
        final var jsonResp = """
                {
                    "data": [
                        {
                            "url": "http://foo.bar"
                        }
                    ]
                }
                """;
        stubFor(post("/v1/images/generations")
                .withRequestBody(equalToJson(jsonReq))
                .willReturn(okJson(jsonResp)));
        final var resp = client.getForEntity("/api/v1/image?prompt=Hello", DallecoolController.ImageResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(postRequestedFor(urlEqualTo("/v1/images/generations"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer changeme"))
                .withHeader(HttpHeaders.USER_AGENT, equalTo("dallecool"))
                .withRequestBody(equalToJson(jsonReq)));
        assertThat(resp.getBody().prompt()).isEqualTo("Hello");
        assertThat(resp.getBody().url()).isEqualTo("http://foo.bar");
    }
}
