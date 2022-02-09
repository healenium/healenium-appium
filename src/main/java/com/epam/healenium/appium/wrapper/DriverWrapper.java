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
package com.epam.healenium.appium.wrapper;


import com.epam.healenium.SelfHealingEngine;
import com.epam.healenium.appium.MobileSelfHealingEngine;
import com.epam.healenium.appium.utils.MobileStackTraceReader;
import com.epam.healenium.appium.handlers.proxy.MobileSelfHealingProxyInvocationHandler;
import com.epam.healenium.appium.service.MobileHealingService;
import com.epam.healenium.appium.service.MobileNodeService;
import com.epam.healenium.client.RestClient;
import com.epam.healenium.mapper.HealeniumMapper;
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
@SuppressWarnings("unchecked")
public final class DriverWrapper {

    /**
     * Instantiates the self-healing driver.
     *
     * @param delegate the original driver.
     */
    public static <T extends AppiumDriver> T wrap(T delegate) {
        return wrap(delegate, null);
    }

    public static <T extends AppiumDriver> T wrap(T delegate, Config config) {
        if(config == null){
            config = ConfigFactory.systemProperties().withFallback(ConfigFactory.load());
        }
        SelfHealingEngine engine = new MobileSelfHealingEngine(delegate, config);
        engine.setClient(new RestClient(engine.getConfig()));
        engine.setNodeService(new MobileNodeService(delegate));
        engine.setHealingService(new MobileHealingService(engine.getConfig(), delegate));
        engine.getClient().setMapper(new HealeniumMapper(new MobileStackTraceReader()));
        return create(engine);
    }

    public static <T extends AppiumDriver> T create(SelfHealingEngine engine){
        T origin = (T) engine.getWebDriver();
        try{
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(origin.getClass());
            factory.setFilter(method -> method.getName().startsWith("findElement")
                    || method.getName().equalsIgnoreCase("switchTo"));
            return (T) factory.create(
                    new Class<?>[]{URL.class, Capabilities.class},
                    new Object[]{origin.getRemoteAddress(), origin.getCapabilities()},
                    new MobileSelfHealingProxyInvocationHandler(engine)
            );
        } catch (Exception ex){
            log.error("Failed to create wrapper!", ex);
            return origin;
        }
    }

}
