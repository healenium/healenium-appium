package com.epam.healenium.appium.service;

import com.epam.healenium.appium.MobileSelectorComponent;
import com.epam.healenium.model.Context;
import com.epam.healenium.model.HealedElement;
import com.epam.healenium.service.HealingService;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.Scored;
import com.typesafe.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MobileHealingService extends HealingService {

    private final List<Set<MobileSelectorComponent>> mobileSelectorDetailLevels;

    private final static List<Set<MobileSelectorComponent>> MOBILE_TEMP = new ArrayList<Set<MobileSelectorComponent>>() {{
        add(EnumSet.of(MobileSelectorComponent.RESOURCE_ID));
        add(EnumSet.of(MobileSelectorComponent.MOBILE_ID));
        add(EnumSet.of(MobileSelectorComponent.TEXT));
        add(EnumSet.of(MobileSelectorComponent.CONTENT_DESC));
        add(EnumSet.of(MobileSelectorComponent.MOBILE_TAG, MobileSelectorComponent.RESOURCE_ID, MobileSelectorComponent.MOBILE_ID, MobileSelectorComponent.CONTENT_DESC));
        add(EnumSet.of(MobileSelectorComponent.MOBILE_PARENT, MobileSelectorComponent.MOBILE_TAG, MobileSelectorComponent.RESOURCE_ID, MobileSelectorComponent.MOBILE_ID, MobileSelectorComponent.CONTENT_DESC));
        add(EnumSet.of(MobileSelectorComponent.MOBILE_PATH));
    }};

    public MobileHealingService(Config finalizedConfig, WebDriver driver) {
        super(finalizedConfig, driver);
        this.mobileSelectorDetailLevels = Collections.unmodifiableList(MOBILE_TEMP);
    }

    @Override
    protected HealedElement toLocator(Scored<Node> node, Context context) {
        for (Set<MobileSelectorComponent> detailLevel : mobileSelectorDetailLevels) {
            By locator = mobileConstruct(node.getValue(), detailLevel);
            if (locator == null) {
                continue;
            }
            List<WebElement> elements = driver.findElements(locator);
            if (elements.size() == 1 && !context.getElementIds().contains(((RemoteWebElement) elements.get(0)).getId())) {
                Scored<By> byScored = new Scored<>(node.getScore(), locator);
                context.getElementIds().add(((RemoteWebElement) elements.get(0)).getId());
                HealedElement healedElement = new HealedElement();
                healedElement.setElement(elements.get(0)).setScored(byScored);
                return healedElement;
            }
        }
        return null;
    }

    private By mobileConstruct(Node node, Set<MobileSelectorComponent> detailLevel) {
        String xpath = detailLevel.stream()
                .map(component -> component.createComponent(node))
                .collect(Collectors.joining());
        if (!xpath.isEmpty()) {
            return xpath.startsWith("[@")
                    ? By.xpath("//*".concat(xpath))
                    : By.xpath("//".concat(xpath));
        }
        return null;
    }
}
