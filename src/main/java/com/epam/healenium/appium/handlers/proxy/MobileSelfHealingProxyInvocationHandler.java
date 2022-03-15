/**
 * Healenium-appium Copyright (C) 2019 EPAM
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.healenium.appium.handlers.proxy;

import com.epam.healenium.PageAwareBy;
import com.epam.healenium.SelfHealingEngine;
import com.epam.healenium.appium.wrapper.ElementWrapper;
import com.epam.healenium.handlers.proxy.BaseHandler;
import javassist.util.proxy.MethodHandler;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MobileSelfHealingProxyInvocationHandler extends BaseHandler implements MethodHandler, MobileBaseHandler {

    public final static String PAGE_PREFIX = "page";

    public MobileSelfHealingProxyInvocationHandler(SelfHealingEngine engine) {
        super(engine);
    }

    @Override
    public Object invoke(Object proxy, Method method, Method proceed, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "findElement":
                WebElement element = findElement((By) args[0]);
                return engine.isProxy() ? element : wrap(element);
            case "findElements":
                List<WebElement> elements = findElements((By) args[0]);
                return engine.isProxy() ? elements : elements.stream().map(this::wrap).collect(Collectors.toList());
            case "switchTo":
                WebDriver.TargetLocator switched = (WebDriver.TargetLocator) method.invoke(driver, args);
                ClassLoader loader = driver.getClass().getClassLoader();
                return engine.isProxy() ? wrapTarget(switched, loader) : switched;
            default:
                By locator = MOBILE_METHOD_MAP.get(method.getName()).apply((String) args[0]);
                if (method.getName().contains("findElements")) {
                    List<WebElement> elementsBy = findElements(locator);
                    return engine.isProxy() ? elementsBy : elementsBy.stream().map(this::wrap).collect(Collectors.toList());
                } else {
                    WebElement el = findElement(locator);
                    return engine.isProxy() ? el : wrap(el);
                }
        }
    }

    private Object wrap(WebElement element) {
        return Optional.ofNullable(element).map(it -> ElementWrapper.wrap(it, engine)).orElse(null);
    }

    @Override
    protected PageAwareBy awareBy(By by) {
        return (by instanceof PageAwareBy) ? (PageAwareBy) by : PageAwareBy.by(PAGE_PREFIX, by);
    }

}
