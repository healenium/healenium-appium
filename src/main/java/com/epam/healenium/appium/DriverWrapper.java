package com.epam.healenium.appium;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.appium.java_client.AppiumDriver;
import java.net.URL;
import javassist.util.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;

/**
 * <p>A wrapper for the android driver provided by appium.
 * locators that indicate that an element is a subject to healing.</p>
 * <p>Overrides logic for MobileDriver.findElement* methods</p>
 */
@Slf4j
public final class DriverWrapper {

    /**
     * Instantiates the self-healing driver.
     *
     * @param delegate the original driver.
     */
    public static AppiumDriver wrap(AppiumDriver delegate) {
        Config config = ConfigFactory.systemProperties().withFallback(ConfigFactory.load());
        AppiumEngine engine = new AppiumEngine(delegate, config);
        return create(engine);
    }

    static AppiumDriver create(AppiumEngine engine){
        try{
            AppiumDriver origin = engine.getWebDriver();
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(AppiumDriver.class);
            factory.setFilter(
                method -> {
                    String methodName = method.getName();
                    return methodName.startsWith("findElement") || methodName.equalsIgnoreCase("switchTo");
                }
            );
            return (AppiumDriver)factory.create(
                new Class<?>[]{URL.class, Capabilities.class},
                new Object[]{origin.getRemoteAddress(), origin.getCapabilities()},
                new ProxyMethodHandler(engine)
            );
        } catch (Exception ex){
            log.error("Failed to create wrapper!", ex);
            return engine.getWebDriver();
        }
    }

}
