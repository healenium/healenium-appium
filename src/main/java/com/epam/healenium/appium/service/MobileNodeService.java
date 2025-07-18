package com.epam.healenium.appium.service;

import com.epam.healenium.model.Context;
import com.epam.healenium.service.NodeService;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.NodeBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.*;

@Slf4j
public class MobileNodeService extends NodeService {

    private static final List<String> KEY_ATTRIBUTES = Arrays.asList("resource-id", "class", "content-desc", "text", "bounds");

    @Override
    public List<Node> getNodePath(WebDriver driver, WebElement element, Context context) {
        try {
            return buildNodePath(driver, element);
        } catch (Exception e) {
            log.error("Failed to build node path for element. Attributes: {}", getWebElementAttributes(element), e);
            throw new IllegalStateException("Cannot build node path: " + e.getMessage(), e);
        }
    }

    private List<Node> buildNodePath(WebDriver driver, WebElement element) {
        String xmlString = driver.getPageSource();
        log.debug("Page source length: {}", xmlString.length());

        Document doc = Jsoup.parse(xmlString, "", Parser.xmlParser());
        Element matchingElement = findMatchingElement(doc, element);

        List<Node> nodePath = new ArrayList<>();
        Element currentElement = matchingElement;

        while (currentElement != null) {
            Node node = elementToNode(currentElement);
            nodePath.add(node);
            currentElement = currentElement.parent();
        }

        Collections.reverse(nodePath);
        log.info("Built node path with {} nodes", nodePath.size());
        return new LinkedList<>(nodePath);
    }

    private Element findMatchingElement(Document doc, WebElement webElement) {
        List<Element> elements = new ArrayList<>(doc.getAllElements());
        log.debug("Total elements in document: {}", elements.size());

        if (elements.isEmpty()) {
            throw new IllegalStateException("Document contains no elements");
        }

        Map<String, String> webElementAttrs = getWebElementAttributes(webElement);

        for (String attr : KEY_ATTRIBUTES) {
            String webValue = webElementAttrs.get(attr);
            if (StringUtils.isBlank(webValue)) {
                log.debug("Skipping filter for attribute '{}' with empty or null value", attr);
                continue;
            }

            elements.removeIf(e -> {
                String docValue = e.attributes().get(attr);
                String normalizedDocValue = StringUtils.defaultString(docValue);
                return !webValue.equals(normalizedDocValue);
            });

            log.debug("Elements after filtering by '{}': {}", attr, elements.size());

            if (elements.size() == 1) {
                log.info("Found matching element with attribute '{}'", attr);
                return elements.get(0);
            }
            if (elements.isEmpty()) {
                log.error("No elements match attribute '{}' with value '{}'. WebElement attributes: {}",
                        attr, webValue, webElementAttrs);
                throw new IllegalStateException("No elements match attribute '" + attr + "' with value '" + webValue + "'");
            }
        }

        log.warn("Multiple elements ({}) remain after filtering. Selecting best match.", elements.size());
        return selectBestMatch(elements, webElementAttrs);
    }

    private Element selectBestMatch(List<Element> elements, Map<String, String> webElementAttrs) {
        Element bestMatch = null;
        int maxMatches = -1;

        for (Element element : elements) {
            int matchCount = 0;
            for (String attr : KEY_ATTRIBUTES) {
                String webValue = webElementAttrs.get(attr);
                String docValue = StringUtils.defaultString(element.attributes().get(attr));
                if (webValue.equals(docValue)) {
                    matchCount++;
                }
            }
            if (matchCount > maxMatches) {
                maxMatches = matchCount;
                bestMatch = element;
            }
        }

        if (bestMatch == null) {
            log.error("No best match found among {} elements. WebElement attributes: {}", elements.size(), webElementAttrs);
            throw new IllegalStateException("Unable to select a best match among multiple elements");
        }

        log.info("Selected best match with {} matching attributes", maxMatches);
        return bestMatch;
    }

    private Map<String, String> getWebElementAttributes(WebElement webElement) {
        Map<String, String> attributes = new HashMap<>();
        for (String attr : KEY_ATTRIBUTES) {
            try {
                String value = webElement.getAttribute(attr);
                if (value.equals("null")) {
                    attributes.put(attr, "");
                } else {
                    attributes.put(attr, StringUtils.defaultString(value));
                }
            } catch (Exception e) {
                log.debug("Failed to get attribute '{}' from WebElement", attr, e);
                attributes.put(attr, "");
            }
        }
        return attributes;
    }

    private Node elementToNode(Element element) {
        Map<String, String> attributes = new HashMap<>();
        for (Attribute attr : element.attributes()) {
            attributes.put(attr.getKey(), attr.getValue());
        }

        String index = element.attributes().getIgnoreCase("index");
        return new NodeBuilder()
                .setId(element.attributes().getIgnoreCase("resource-id"))
                .setTag(element.attributes().getIgnoreCase("class"))
                .setClasses(Collections.singleton(element.attributes().getIgnoreCase("content-desc")))
                .setIndex(StringUtils.isEmpty(index) ? 0 : Integer.parseInt(index))
                .setMobileAttributes(attributes)
                .build();
    }
}