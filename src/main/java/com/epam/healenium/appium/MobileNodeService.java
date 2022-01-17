package com.epam.healenium.appium;

import com.epam.healenium.appium.elementcreators.XPathCreator;
import com.epam.healenium.service.NodeService;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.NodeBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class MobileNodeService extends NodeService {

    public MobileNodeService(WebDriver driver) {
        super(driver);
    }

    @Override
    public List<Node> getNodePath(WebElement element) {
        log.debug("* getNodePath start: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        List<Node> path = new LinkedList<>();

        List<WebElement> ancestors = getHierarchyElements(element);

        ancestors.forEach(it -> path.add(toNode(it)));
        log.debug("* getNodePath finish: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        return path;
    }

    private List<WebElement> getHierarchyElements(WebElement element) {
        Node node = toNode(element);
        String xPath = new XPathCreator().create(node);
        List<WebElement> webElements = new ArrayList<>();
        webElements.add(element);
        boolean hasNext = true;
        while (hasNext) {
            xPath = xPath.concat("/..");
            try {
                WebElement element1 = driver.findElement(By.xpath(xPath));
                webElements.add(element1);
            } catch (Exception e) {
                hasNext = false;
            }
        }
        Collections.reverse(webElements);
        return webElements;
    }


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
}
