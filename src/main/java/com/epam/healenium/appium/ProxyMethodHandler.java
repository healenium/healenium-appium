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

import com.epam.healenium.engine.data.LocatorInfo;
import com.typesafe.config.Config;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.MobileSelector;
import javassist.util.proxy.MethodHandler;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.remote.ScreenshotException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class ProxyMethodHandler implements MethodHandler {

    public final static String PAGE_PREFIX = "page";

    private final AppiumDriver delegate;
    private final AppiumEngine engine;
    private final LocatorInfo info = new LocatorInfo();
    private final Config config;

    ProxyMethodHandler(AppiumEngine engine) {
        this.delegate = engine.getWebDriver();
        this.config = engine.getConfig();
        this.engine = engine;
    }

    @Override
    public Object invoke(Object proxy, Method method, Method proceed, Object[] args) throws Throwable {
        //TODO: replace all find methods by Enum
        switch (method.getName()) {
            case "findElementByClassName":
                return findElement(By.className((String) args[0]));
            case "findElementByName":
                return findElement(By.name((String) args[0]));
            case "findElementByTagName":
                return findElement(By.tagName((String) args[0]));
            case "findElementByPartialLinkText":
                return findElement(By.partialLinkText((String) args[0]));
            case "findElementByLinkText":
                return findElement(By.linkText((String) args[0]));
            case "findElementByCssSelector":
                return findElement(By.cssSelector((String) args[0]));
            case "findElementById":
                return findElement(By.id((String) args[0]));
            case "findElementByAccessibilityId":
                return findElement(MobileSelector.ACCESSIBILITY.toString(), (String) args[0], "By.accessibilityId: " + (String) args[0]);
            case "findElementsByAccessibilityId":
                return findElements(MobileSelector.ACCESSIBILITY.toString(), (String) args[0], "By.accessibilityId: " + (String) args[0]);
            case "findElementByXPath":
                return findElement(By.xpath((String) args[0]));
            case "findElement":
                log.debug("Caught findElement: invoking the healing version...");
                return findElement((By) args[0]);
            case "findElements":
                log.debug("Caught findElements: invoking the healing version...");
                return findElements((By) args[0]);
            case "switchTo":
                log.debug("Caught switchTo");
                WebDriver.TargetLocator switched = (WebDriver.TargetLocator) proceed.invoke(delegate, args);
                ClassLoader classLoader = delegate.getClass().getClassLoader();
                return Proxy.newProxyInstance(
                    classLoader,
                    new Class[]{WebDriver.TargetLocator.class},
                    new TargetLocatorProxyInvocationHandler(switched, engine));
            default:
                return proceed.invoke(delegate, args);
        }
    }

    private WebElement findElement(By by) {
        if (config.getBoolean("heal-enabled")) {
            String page = PAGE_PREFIX;
            try {
                log.info("Proxy method findElement {}", by.toString());
                WebElement element = delegate.findElement(by);
                engine.savePath(by, page, element);
                return element;
            } catch (NoSuchElementException ex) {
                log.warn("Failed to find an element using locator {}\nReason: {}\nTrying to heal...", by.toString(), ex.getMessage());
                return heal(by.toString(), page, ex).orElse(null);
            }
        } else {
            return delegate.findElement(by);
        }
    }

    private WebElement findElement(String selector, String using, String locator) {
        if (config.getBoolean("heal-enabled")) {
            String page = PAGE_PREFIX;
            try {
                log.info("Proxy method findElement {} using: {}", selector, using);
                WebElement element = delegate.findElement(selector, using);
                savePath(locator, page, element);
                return element;
            } catch (NoSuchElementException ex) {
                log.warn("Failed to find an element using locator {}\nReason: {}\nTrying to heal...", locator, ex.getMessage());
                return heal(locator, page, ex).orElse(null);
            }
        } else {
            return delegate.findElement(selector, using);
        }
    }

    private List<MobileElement> findElements(By by) {
        if (config.getBoolean("heal-enabled")) {
            String page = PAGE_PREFIX;
            try {
                log.info("Proxy method findElements {}", by.toString());
                List<MobileElement> elements = delegate.findElements(by);
                if (elements.isEmpty()) {
                    throw new NoSuchElementException("Failed to find an element");
                }
                savePath(by, page, elements);
                return elements;
            } catch (NoSuchElementException ex) {
                log.warn("Failed to find an element using locator {}\nReason: {}\nTrying to heal...", by.toString(), ex.getMessage());
                return heals(by.toString(), page, ex).orElse(Collections.emptyList());
            }
        } else {
            return delegate.findElements(by);
        }
    }

    private List<MobileElement> findElements(String selector, String using, String locator) {
        if (config.getBoolean("heal-enabled")) {
            String page = PAGE_PREFIX;
            try {
                log.info("Proxy method findElement {} using: {}", selector, using);
                List<MobileElement> elements = delegate.findElements(selector, using);
                if (elements.isEmpty()) {
                    throw new NoSuchElementException("Failed to find an element");
                }
                savePath(locator, page, elements);
                return elements;
            } catch (NoSuchElementException ex) {
                log.warn("Failed to find an element using locator {}\nReason: {}\nTrying to heal...", locator, ex.getMessage());
                return heals(locator, page, ex).orElse(Collections.emptyList());
            }
        } else {
            return delegate.findElements(selector, using);
        }
    }

    private void savePath(Object locator, String page, WebElement element) {
        if (config.getBoolean("heal-enabled")) {
            engine.savePath(locator, page, element);
        }
    }

    private void savePath(Object locator, String page, List<MobileElement> elements) {
        if (config.getBoolean("heal-enabled")) {
            for (MobileElement element : elements) {
                engine.savePath(locator, page, element);
            }
        }
    }

    private Optional<WebElement> heal(String locator, String pageName, NoSuchElementException e) {
        log.info("locator.hashCode of {} = {}", locator, locator.hashCode());

        if (!engine.isPathExists(locator, pageName)) {
            log.warn("Healing canceled because no locator data exists");
            return Optional.empty();
        }
        LocatorInfo.Entry entry = reportBasicInfo(pageName, e);
        return healLocator(locator, pageName).map(healed -> {
            reportFailedInfo(locator, entry, healed);
            engine.saveLocator(info);
            return delegate.findElement(healed);
        });
    }

    private Optional<List<MobileElement>> heals(String locator, String pageName, NoSuchElementException e) {
        log.info("locator.hashCode of {} = {}", locator, locator.hashCode());

        if (!engine.isPathExists(locator, pageName)) {
            log.warn("Healing canceled because no locator data exists");
            return Optional.empty();
        }
        LocatorInfo.Entry entry = reportBasicInfo(pageName, e);
        return healLocator(locator, pageName).map(healed -> {
            reportFailedInfo(locator, entry, healed);
            engine.saveLocator(info);
            return delegate.findElements(healed);
        });
    }

    private void reportFailedInfo(String locator, LocatorInfo.Entry infoEntry, By healed) {
        infoEntry.setFailedLocatorValue(locator);
        infoEntry.setFailedLocatorType(locator.substring(0, locator.indexOf(':')));
        infoEntry.setHealedLocatorValue(healed.toString());
        infoEntry.setScreenShotPath(captureScreen());
        int pos = info.getElementsInfo().indexOf(infoEntry);
        if (pos != -1) {
            info.getElementsInfo().set(pos, infoEntry);
        } else {
            info.getElementsInfo().add(infoEntry);
        }
    }

    private LocatorInfo.Entry reportBasicInfo(String pageName, NoSuchElementException e) {
        Optional<StackTraceElement> elOpt = Optional.ofNullable(e).flatMap(it -> getStackTraceForPageObject(it.getStackTrace(), pageName));
        return elOpt.map(el -> {
            LocatorInfo.PageAsClassEntry entry = new LocatorInfo.PageAsClassEntry();
            entry.setFileName(el.getFileName());
            entry.setLineNumber(el.getLineNumber());
            entry.setMethodName(el.getMethodName());
            entry.setDeclaringClass(el.getClassName());
            return (LocatorInfo.Entry) entry;
        }).orElseGet(() -> {
            log.debug("No pageObject Class for NoSuchElementException: ");
            LocatorInfo.SimplePageEntry entry = new LocatorInfo.SimplePageEntry();
            entry.setPageName(pageName);
            return entry;
        });
    }

    private Optional<StackTraceElement> getStackTraceForPageObject(StackTraceElement[] elements, String pageName) {
        return Arrays
            .stream(elements)
            .filter(element -> {
                String className = element.getClassName();
                String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
                return simpleClassName.equals(pageName);
            })
            .findFirst();
    }

    private Optional<By> healLocator(Object locator, String page) {
        log.debug("* healLocator start: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        List<By> choices = engine.findNewLocations(locator, page, pageSource());
        Optional<By> result = choices.stream().findFirst();
        result.ifPresent(primary ->
            log.warn("Using healed locator: {}", primary.toString()));
        choices.stream().skip(1).forEach(otherChoice ->
            log.warn("Other choice: {}", otherChoice.toString()));
        if (!result.isPresent()) {
            log.warn("New element locators have not been found");
        }
        log.debug("* healLocator finish: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        return result;
    }

    private String captureScreen() {
        String path = "No screenshot available";
        try {
            byte[] source = engine.getWebDriver().getScreenshotAs(OutputType.BYTES);
            FileHandler.createDir(new File(config.getString("screenshotPath")));
            File file =
                new File(config.getString("screenshotPath") + "screenshot_" + LocalDateTime
                    .now()
                    .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy-hh-mm-ss").withLocale(Locale.US)) + ".png");
            Files.write(file.toPath(), source, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            path = file.getPath().replaceAll("\\\\", "/");
            path = ".." + path.substring(path.indexOf("/sc"));

        } catch (IOException | ScreenshotException e) {
            log.warn("Failed to capture screenshot!\n Reason: {}", e.getMessage());
        }
        return path;
    }

    private String pageSource() {
        return engine.getWebDriver().getPageSource();
    }

}
