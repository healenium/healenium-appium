package com.epam.healenium.appium;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;

/**
 * A proxy that wraps {@link org.openqa.selenium.WebDriver.TargetLocator}, so that {@link WebDriver#switchTo()} preserves healing functionality.
 */
class TargetLocatorProxyInvocationHandler implements InvocationHandler {

    private final TargetLocator delegate;
    private final AppiumEngine engine;

    TargetLocatorProxyInvocationHandler(TargetLocator delegate, AppiumEngine engine) {
        this.delegate = delegate;
        this.engine = engine;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(delegate, args);
        boolean isProxy = result instanceof DriverWrapper;
        boolean isWebDriver = result instanceof WebDriver;
        if (isWebDriver && !isProxy) {
            return DriverWrapper.create(engine);
        } else {
            return result;
        }
    }
}
