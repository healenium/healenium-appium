package com.epam.healenium.appium.handlers.proxy;

import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.By;

import java.util.Map;
import java.util.function.Function;

public interface MobileBaseHandler {

    Map<String, Function<String, By>> MOBILE_METHOD_MAP =
            ImmutableMap.<String, Function<String, By>>builder()
                    .put("findElementByClassName", By::className)
                    .put("findElementsByClassName", By::className)
                    .put("findElementByName", By::name)
                    .put("findElementsByName", By::name)
                    .put("findElementByTagName", By::tagName)
                    .put("findElementsByTagName", By::tagName)
                    .put("findElementByPartialLinkText", By::partialLinkText)
                    .put("findElementsByPartialLinkText", By::partialLinkText)
                    .put("findElementByLinkText", By::linkText)
                    .put("findElementsByLinkText", By::linkText)
                    .put("findElementByCssSelector", By::cssSelector)
                    .put("findElementsByCssSelector", By::cssSelector)
                    .put("findElementById", By::id)
                    .put("findElementsById", By::id)
                    .build();
}
