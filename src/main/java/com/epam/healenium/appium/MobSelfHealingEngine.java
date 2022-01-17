package com.epam.healenium.appium;

import com.epam.healenium.SelfHealingEngine;
import com.epam.healenium.treecomparing.JsoupXMLParser;
import com.epam.healenium.treecomparing.Node;
import com.typesafe.config.Config;
import io.appium.java_client.AppiumDriver;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class MobSelfHealingEngine extends SelfHealingEngine {

    public MobSelfHealingEngine(@NotNull WebDriver delegate, @NotNull Config config) {
        super(delegate, config);
    }

    @Override
    public String getCurrentUrl() {
        return (String) ((AppiumDriver) getWebDriver()).getCapabilities().asMap()
                .getOrDefault("appActivity", "");
    }

    @Override
    public String pageSource() {
        return getWebDriver().getPageSource();
    }

    @Override
    public Node parseTree(String tree) {
        return new JsoupXMLParser().parse(new ByteArrayInputStream(tree.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public byte[] captureScreen(WebElement element) {
        return null;
    }
}
