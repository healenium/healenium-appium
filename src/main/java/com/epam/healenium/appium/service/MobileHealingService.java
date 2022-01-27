package com.epam.healenium.appium.service;

import com.epam.healenium.SelectorComponent;
import com.epam.healenium.appium.elementcreators.XPathCreator;
import com.epam.healenium.service.HealingService;
import com.epam.healenium.treecomparing.Node;
import com.typesafe.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Set;

public class MobileHealingService extends HealingService {

    public MobileHealingService(Config finalizedConfig, WebDriver driver) {
        super(finalizedConfig, driver);
    }

    @Override
    protected By construct(Node node, Set<SelectorComponent> detailLevel) {
        return By.xpath(new XPathCreator().create(node));
    }

}
