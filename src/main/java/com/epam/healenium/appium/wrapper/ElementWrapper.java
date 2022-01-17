package com.epam.healenium.appium.wrapper;

import com.epam.healenium.SelfHealingEngine;
import com.epam.healenium.appium.handlers.proxy.MobileWebElementProxyHandler;
import javassist.util.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

@Slf4j
public class ElementWrapper {

    public static <T extends WebElement> T wrap(T delegate, SelfHealingEngine engine) {
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(delegate.getClass());
            factory.setFilter(method -> method.getName().startsWith("findElement"));

            return (T) factory.create(
                    new Class<?>[]{},
                    new Object[]{},
                    new MobileWebElementProxyHandler(delegate, engine)
            );
        } catch (Exception ex) {
            log.error("Failed to create wrapper!", ex);
            return delegate;
        }
    }

}
