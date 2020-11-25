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
package com.epam.healenium.appium;

import com.epam.healenium.appium.elementcreators.XPathCreator;
import com.epam.healenium.client.RestClient;
import com.epam.healenium.HealException;
import com.epam.healenium.SelfHealingEngine;
import com.epam.healenium.data.PathStorage;
import com.epam.healenium.treecomparing.*;
import com.epam.healenium.utils.StackUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.appium.java_client.AppiumDriver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A healing com.epam.healenium.engine that encapsulates all the healing logic, leaving the persistence to {@link PathStorage} abstraction and elements and locators handling to the driver.
 */
@Slf4j
@SuppressWarnings("unchecked")
public class AppiumEngine<D extends AppiumDriver> extends SelfHealingEngine<D,WebElement> {

    private static final Config DEFAULT_CONFIG = ConfigFactory.systemProperties().withFallback(ConfigFactory.load("healenium.properties").withFallback(ConfigFactory.load()));

    @Getter
    private final RestClient client;
    @Getter
    private final Map testData = new HashMap();

    AppiumEngine(D driver, Config config) {
        super(driver, config);
        Config finalizedConfig = ConfigFactory.load(config).withFallback(DEFAULT_CONFIG);
        client = new RestClient(finalizedConfig);
        for (Map.Entry entry: driver.getCapabilities().asMap().entrySet()) {
            if (((String) entry.getKey()).contains("test_data")) {
                testData.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public D getWebDriver() {
        return super.getWebDriver();
    }

    @Override
    public List<Node> getNodePath(WebElement element) {
        log.debug("* getNodePath start: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        List<Node> path = new LinkedList<>();

        String ancestorsXPath = element.toString().substring(element.toString().lastIndexOf(":") + 1, element.toString().length() - 1) + "/ancestor::*";
        List<WebElement> ancestors = getWebDriver().findElements(By.xpath(ancestorsXPath));
        ancestors.add(element);
        ancestors.forEach(it -> path.add(toNode(it)));
        log.debug("* getNodePath finish: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        return path;
    }

    /**
     * @param locator    page aware locator
     * @param targetPage the new HTML page source on which we should search for the element
     * @return a list of candidate locators, ordered by revelance, or empty list if was unable to heal
     */
    public List<By> findNewLocations(Object locator, String context, String targetPage) {
        return findNewNodes(locator, context, targetPage).stream()
            .map(this::toLocator)
            .collect(Collectors.toList());
    }

    /**
     * @param locator page aware locator
     * @param targetPage the new HTML page source on which we should search for the element
     * @return a list of candidate locators, ordered by revelance, or empty list if was unable to heal
     */
    public List<By> findNewLocations(By locator, String targetPage, Optional<StackTraceElement> element) {
        List<By> result = new ArrayList<>();

        element.flatMap(it -> client.getLastValidPath(locator, it))
                // ignore empty result, or will fall on search
                .filter(it-> !it.isEmpty())
                .ifPresent(nodes -> findNewNodes(targetPage, nodes).stream()
                        .map(this::toLocator)
                        .forEach(result::add));
        return result;
    }

    /**
     * Stores the valid locator state: the element it found and the page.
     *
     * @param by         the locator
     * @param webElement the element while it is still accessible by the locator
     */

    public void savePath(By by, WebElement webElement) {
        StackTraceElement traceElement = StackUtils.findOriginCaller(Thread.currentThread().getStackTrace())
                .orElseThrow(()-> new IllegalArgumentException("Failed to detect origin method caller"));
        List<Node> nodes = getNodePath(webElement);
        client.selectorRequest(by, traceElement, nodes);

        String[] locatorParts = by.toString().split(":");
        testData.forEach((key, value) -> {
            if (((String) key).contains(locatorParts[1].trim())) {
                String[] oldTestDataParts = ((String) key).split(":");
                String[] newTestDataParts = ((String) value).split(":");
                client.selectorRequestTest(by, traceElement, nodes, oldTestDataParts[2].trim(),
                        newTestDataParts[1].trim(), oldTestDataParts[1].trim(), newTestDataParts[0].trim());
            }
        });
    }

    @Override
    public DocumentParser getParser() {
        return new JsoupXMLParser();
    }

    private By toLocator(Scored<Node> scoredNode) {
        log.debug("ToLocator by Node: {}", scoredNode.getValue());
        By locator = construct(scoredNode.getValue());
        List<WebElement> elements = getWebDriver().findElements(locator);
        if (elements.size() == 1) {
            return locator;
        }
        throw new HealException();
    }

    /**
     *
     */
    private Node toNode(WebElement element) {
        Map<String, String> otherAttributes = new HashMap<>();
        otherAttributes.put("contentDescription", Objects.toString(element.getAttribute("contentDescription"), ""));
        otherAttributes.put("bounds", element.getAttribute("bounds"));
        otherAttributes.put("checked", element.getAttribute("checked"));
        otherAttributes.put("enabled", element.getAttribute("enabled"));
        otherAttributes.put("selected", element.getAttribute("selected"));
        otherAttributes.put("focused", element.getAttribute("focused"));
        otherAttributes.put("displayed", element.getAttribute("displayed"));
        otherAttributes.put("resourceId", Objects.toString(element.getAttribute("resourceId"), ""));

        return new NodeBuilder()
            .setTag(element.getAttribute("class"))
            .setContent(Collections.singletonList(element.getText()))
            .setOtherAttributes(otherAttributes)
            .build();
    }

    private By construct(Node node) {
        return By.xpath(new XPathCreator().create(node));
    }

}
