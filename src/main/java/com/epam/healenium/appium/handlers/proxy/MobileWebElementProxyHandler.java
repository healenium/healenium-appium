package com.epam.healenium.appium.handlers.proxy;

import com.epam.healenium.SelfHealingEngine;
import com.epam.healenium.appium.wrapper.ElementWrapper;
import com.epam.healenium.handlers.proxy.WebElementProxyHandler;
import io.appium.java_client.MobileElement;
import javassist.util.proxy.MethodHandler;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MobileWebElementProxyHandler extends WebElementProxyHandler implements MethodHandler, MobileBaseHandler {

    public <T extends MobileElement> MobileWebElementProxyHandler(WebElement delegate, SelfHealingEngine engine) {
        super(delegate, engine);
    }

    @Override
    public Object invoke(Object o, Method method, Method method1, Object[] args) {
        switch (method.getName()) {
            case "findElement":
                return wrap(findElement((By) args[0]));
            case "findElements":
                List<WebElement> elements = findElements((By) args[0]);
                return elements.stream().map(this::wrap).collect(Collectors.toList());
            default:
                By locator = MOBILE_METHOD_MAP.get(method.getName()).apply((String) args[0]);
                if (method.getName().contains("findElements")) {
                    List<WebElement> elementsBy = findElements(locator);
                    return elementsBy.stream().map(this::wrap).collect(Collectors.toList());
                } else {
                    return wrap(findElement(locator));
                }
        }
    }

    private Object wrap(WebElement element) {
        return Optional.ofNullable(element).map(it -> ElementWrapper.wrap(it, engine)).orElse(null);
    }

}
