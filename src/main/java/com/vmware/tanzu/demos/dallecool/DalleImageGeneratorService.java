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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class DalleImageGeneratorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DalleImageGeneratorService.class);
    private final OpenAiConfiguration openai;
    private final WebClient client;
    private final String apiEndpoint = "/v1/images/generations";

    public DalleImageGeneratorService(OpenAiConfiguration openai, WebClient client) {
        this.openai = openai;
        this.client = client;
    }

    public Mono<String> generateImage(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("Prompt must not be empty");
        }
        LOGGER.info("Sending request to DALL-E: {}", prompt);
        final var req = new ImageGenerationRequest(prompt);
        return client.post().uri(openai.api() + apiEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openai.key())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(ImageGenerationResponse.class)
                .doOnSuccess(resp -> {
                    LOGGER.info("Received response from DALL-E for request: {}", prompt);
                })
                .filter(resp -> resp.data.length > 0)
                .map(resp -> resp.data[0].url);
    }

    private record ImageGenerationResponse(ImageGenerationResponseUrl[] data) {
    }

    private record ImageGenerationResponseUrl(String url) {
    }

    private record ImageGenerationRequest(String prompt) {
    }
}
