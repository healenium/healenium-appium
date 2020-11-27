# healenium-appium
appium adaptor for self-healing

[ ![Download](https://api.bintray.com/packages/epam/healenium/healenium-appium/images/download.svg) ](https://bintray.com/epam/healenium/healenium-appium/_latestVersion)
[![Build Status](https://github.com/healenium/healenium-appium/workflows/Java-CI-test/badge.svg)](https://github.com/healenium/healenium-appium/workflows/Java-CI-test/badge.svg)

## How to start

### 0. For version 1.1 and higher start hlm-backend by [instruction](https://github.com/healenium/healenium-backend) 

### 0.1 Add dependency 

for Gradle projects:
``` 
repositories {
    maven {
        url  "https://dl.bintray.com/epam/healenium"
    }
    mavenCentral()
}
dependencies {
    compile group: 'com.epam.healenium', name: 'healenium-appium', version: '1.1'
}
```

for Maven projects:
``` 
<repositories>
     <repository>
        <snapshots>
          <enabled>false</enabled>
        </snapshots>
        <id>bintray-epam-healenium</id>
        <name>bintray</name>
        <url>https://dl.bintray.com/epam/healenium</url>
     </repository>
</repositories>

<dependency>
	<groupId>com.epam.healenium</groupId>
	<artifactId>healenium-appium</artifactId>
	<version>1.1</version>
</dependency>
```
### 1. Driver initialization
 Wrapping driver instance with default config:
``` 
    //Appium settings for choosen mobile platform (Android support implemented)
    DesiredCapabilities dc = new DesiredCapabilities();

    dc.setCapability(MobileCapabilityType.DEVICE_NAME, "emulator-5554");
    dc.setCapability(MobileCapabilityType.PLATFORM_NAME, "android");
    
    //used default Calculator application on android emulator
    dc.setCapability("appPackage", "com.android.calculator2");
    dc.setCapability("appActivity", ".Calculator");

    /*
       You could replace your old successfuly tested locators by new values
       to check healenium-appium working without tested appication modification.
       You must start your replacement key in dc.setCapability with prefix 'test_data'.
       For example:
       
       dc.setCapability("test_data:old_test_method_name:old_locator", "new_test_method_name:new_locator");
       Such key-value pairs will be processed.
    */
    dc.setCapability("test_data:testResultOk:result", "testResultHealed:resul");

    //declare delegate driver
    AppiumDriver driver = new AndroidDriver<AndroidElement>(new URL("http://127.0.0.1:4723/wd/hub"), dc);
    //adding healing support
    driver = DriverWrapper.wrap(driver);
 ```
 Old versions of healenium-appium use file system to store locators and report-data. Since version 1.1 
 healenium-appium uses healenium-backend for these purposes. File system storage also supports and could
 be used when backend-integration set to 'false'.
 Default config values:
``` 
    recovery-tries = 3
    basePath = sha/healenium
    reportPath = build/reports
    screenshotPath = build/screenshots/
    heal-enabled = true
    backend-integration = true
    serverHost = localhost
    serverPort = 7878
 ```

 > recovery-tries - list of proposed healed locators

 > basePath - folder to store base locators path

 > **Important!** Do not delete data from the folder where files with new locators are stored. They are used to perform self-healing in next automation runs

 > reportPath - folder to save test report with healing information

 > screenshotPath - folder to save screenshots of healed elements

 > heal-enabled - you could enable or disable healing by setting true or false flag to this variable

 > backend-integration - you could enable or disable usage of healenium-backend to store locators and report-data
 
 > serverHost - ip or name where hlm-backend instance is installed
 
 > serverPort - port on which hlm-backend instance is installed (7878 by default)

* Suggested way is to declare custom config or property file (ex. sha.properties) and set
``` basePath = sha/selenium```

Also you could set configs via -D or System properties, for example to turn off healing for current test run:
```-Dheal-enabled=false```
