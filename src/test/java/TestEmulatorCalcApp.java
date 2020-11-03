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

import com.epam.healenium.appium.DriverWrapper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.remote.MobileCapabilityType;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class TestEmulatorCalcApp {

    private static AppiumDriver appiumDriver;

    @BeforeAll
    public static void setUp() throws MalformedURLException {
        DesiredCapabilities dc = new DesiredCapabilities();
        dc.setCapability(MobileCapabilityType.DEVICE_NAME, "emulator-5554");
        dc.setCapability(MobileCapabilityType.PLATFORM_NAME, "android");
        dc.setCapability("appPackage", "com.android.calculator2");
        dc.setCapability("appActivity", ".Calculator");

        //declare delegate driver
        appiumDriver = new AndroidDriver<AndroidElement>(new URL("http://127.0.0.1:4723/wd/hub"), dc);

        // FIXME Couldn't establish connection with AppiumDriver by default
        // AppiumDriver ad = new AppiumDriver(new URL("http://127.0.0.1:4723/wd/hub"), dc);

        //adding healing support
        appiumDriver = DriverWrapper.wrap(appiumDriver);
    }

    @Test
    public void testResultOk() {
        testAddOperation();
        Assert.assertEquals(appiumDriver.findElementById("result").getText(), "61");
    }

    @Test
    public void testResultHealed() {
        testAddOperation();
        Assert.assertEquals(appiumDriver.findElementById("resul").getText(), "61");
    }

    @Test
    public void testFindElementsOk() {
        List<MobileElement> elements = appiumDriver.findElements(By.id("digit_7"));
        Assert.assertEquals("Digit 7", "7", elements.get(0).getText());
    }

    @Test
    public void testFindElementsHealed() {
        List<MobileElement> elements = appiumDriver.findElements(By.id("digit_77"));
        Assert.assertEquals("Healed digit", "7", elements.get(0).getText());
    }

    private void testAddOperation() {
        MobileElement el1 = (MobileElement) appiumDriver.findElementById("digit_2");
        el1.click();
        MobileElement el2 = (MobileElement) appiumDriver.findElementById("digit_5");
        el2.click();
        MobileElement el3 = (MobileElement) appiumDriver.findElementByAccessibilityId("plus");
        el3.click();
        MobileElement el4 = (MobileElement) appiumDriver.findElementById("digit_3");
        el4.click();
        MobileElement el5 = (MobileElement) appiumDriver.findElementById("digit_6");
        el5.click();
        MobileElement el6 = (MobileElement) appiumDriver.findElementByAccessibilityId("equals");
        el6.click();
    }

    @AfterAll
    public static void tearDown() {
        if (appiumDriver != null) {
            appiumDriver.quit();
        }
    }
}
