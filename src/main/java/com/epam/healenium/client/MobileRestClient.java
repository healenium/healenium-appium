/**
 * Healenium-appium Copyright (C) 2019 EPAM
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.healenium.client;

import com.epam.healenium.converter.MobileNodeDeserializer;
import com.epam.healenium.converter.MobileNodeSerializer;
import com.epam.healenium.mapper.MobileHealeniumMapper;
import com.epam.healenium.mapper.MobileHealeniumMapperImpl;
import com.epam.healenium.model.MobileRequestDto;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.Scored;
import com.epam.healenium.utils.MobileSystemUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.openqa.selenium.By;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * Wrapper for {@code RestTemplate} class.
 * Main purpose - encapsulate consumer from really used client and invocation complexity
 */

@Slf4j
public class MobileRestClient {

    private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final String baseUrl;
    private final String sessionKey;
    private final ObjectMapper objectMapper;
    private final MobileHealeniumMapper mapper;

    public MobileRestClient(Config config) {
        objectMapper = initMapper();
        baseUrl = "http://" + config.getString("serverHost") + ":" + config.getInt("serverPort") + "/healenium";
        sessionKey = config.hasPath("sessionKey") ? config.getString("sessionKey") : "";
        mapper = new MobileHealeniumMapperImpl();
    }

    private OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private ObjectMapper initMapper() {
        SimpleModule module = new SimpleModule("node");
        module.addSerializer(Node.class, new MobileNodeSerializer());
        module.addDeserializer(Node.class, new MobileNodeDeserializer());
        ObjectMapper mapper = new ObjectMapper().registerModule(module);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        return mapper;
    }

    public void selectorRequest(By by, StackTraceElement element, List<Node> nodePath) {
        MobileRequestDto mobileRequestDto = mapper.buildDto(by, element, nodePath);
        try {
            RequestBody body = RequestBody.create(JSON, objectMapper.writeValueAsString(mobileRequestDto));
            Request request = new Request.Builder()
                    .url(baseUrl)
                    .post(body)
                    .build();
            okHttpClient().newCall(request).execute();
        } catch (Exception e) {
            log.warn("Failed to make response");
        }
    }

    public void selectorRequestTest(By by, StackTraceElement element, List<Node> nodePath, String oldElement, String newElement, String oldMethod, String newMethod) {
        MobileRequestDto mobileRequestDto = mapper.buildDto(by, element, nodePath);
        try {
            RequestBody body = RequestBody.create(JSON, objectMapper.writeValueAsString(mobileRequestDto).replace(oldElement, newElement).replace(oldMethod, newMethod));
            Request request = new Request.Builder()
                    .url(baseUrl)
                    .post(body)
                    .build();
            okHttpClient().newCall(request).execute();
        } catch (Exception e) {
            log.warn("Failed to make response");
        }
    }

    /**
     * Collect results from previous healing
     * @param locator
     * @param element
     * @param page
     */
    public void healRequest(By locator, StackTraceElement element, String page, List<Scored<By>> choices, Scored<By> healed, byte[] screenshot) {
        MobileRequestDto mobileRequestDto = mapper.buildDto(locator, element, page, choices, healed, screenshot);
        try {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("screenshot", buildScreenshotName(), RequestBody.create(MediaType.parse("image/png"), screenshot))
                    .addFormDataPart("dto", objectMapper.writeValueAsString(mobileRequestDto))
                    .build();

            Request request = new Request.Builder()
                    .addHeader("sessionKey", sessionKey)
                    .addHeader("instance", MobileSystemUtils.getHostIpAddress())
                    .addHeader("hostProject", MobileSystemUtils.getHostProjectName())
                    .url(baseUrl + "/healing")
                    .post(requestBody)
                    .build();
            okHttpClient().newCall(request).execute();
        } catch (Exception e) {
            log.warn("Failed to make response", e);
        }
    }

    /**
     * Get node path for given selector
     * @param locator
     * @param element
     * @return
     */
    public Optional<List<Node>> getLastValidPath(By locator, StackTraceElement element) {
        List<Node> nodes = null;
        MobileRequestDto mobileRequestDto = mapper.buildDto(locator, element);
        try {
            HttpUrl.Builder httpBuilder = HttpUrl.parse(baseUrl).newBuilder()
                    .addQueryParameter("locator", mobileRequestDto.getLocator())
                    .addQueryParameter("className", mobileRequestDto.getClassName())
                    .addQueryParameter("methodName", mobileRequestDto.getMethodName());
            Request request = new Request.Builder()
                    .addHeader("sessionKey", sessionKey)
                    .url(httpBuilder.build())
                    .get()
                    .build();
            Response response = okHttpClient().newCall(request).execute();
            if (response.code() == 200) {
                String result = response.body().string();
                nodes = objectMapper.readValue(result, new TypeReference<List<Node>>() {
                });
            }
        } catch (Exception ex) {
            log.warn("Failed to make response", ex);
        }
        return Optional.ofNullable(nodes);
    }

    /**
     * Builds ID for element that represent selector meta
     *
     * @param className  the fully qualified name of the class
     * @param methodName the name of the method
     * @param locator    the selector value
     * @return
     */
    public String buildKey(String className, String methodName, String locator) {
        String rawKey = className.concat(methodName) + locator.hashCode();
        return DigestUtils.md5DigestAsHex(rawKey.trim().getBytes(StandardCharsets.UTF_8));
    }

    /**
     *
     * @return
     */
    private String buildScreenshotName() {
        return "screenshot_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy-hh-mm-ss").withLocale(Locale.US)) + ".png";
    }
}
