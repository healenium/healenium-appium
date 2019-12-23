/**
 * Healenium-web Copyright (C) 2019 EPAM
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
import com.epam.healenium.engine.HealException;
import com.epam.healenium.engine.SelfHealingEngine;
import com.epam.healenium.engine.data.PathStorage;
import com.epam.sha.treecomparing.DocumentParser;
import com.epam.sha.treecomparing.JsoupXMLParser;
import com.epam.sha.treecomparing.Node;
import com.epam.sha.treecomparing.NodeBuilder;
import com.typesafe.config.Config;
import io.appium.java_client.AppiumDriver;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * A healing engine that encapsulates all the healing logic, leaving the persistence to {@link PathStorage} abstraction and elements and locators handling to the driver.
 */
@Slf4j
@SuppressWarnings("unchecked")
public class AppiumEngine extends SelfHealingEngine<AppiumDriver, WebElement> {

    AppiumEngine(AppiumDriver driver, Config config) {
        super(driver, config);
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

    @Override
    public DocumentParser getParser() {
        return new JsoupXMLParser();
    }

    private By toLocator(Node node) {
        log.debug("ToLocator by Node: {}", node);
        By locator = construct(node);
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
