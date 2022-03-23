# healenium-appium
appium adaptor for self-healing

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.epam.healenium/healenium-appium/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.epam.healenium/healenium-appium/)
[![Build Status](https://github.com/healenium/healenium-appium/workflows/Java-CI-test/badge.svg)](https://github.com/healenium/healenium-appium/workflows/Java-CI-test/badge.svg)

## How to start

### 0. For version 1.2.3 and higher start hlm-backend by [instruction](https://github.com/healenium/healenium-backend) 

### 0.1 Use example to get to know Healenium-appium solution: [healenium-appium-example](https://github.com/healenium/example_appium_mvn)

### 1. Add dependency 

for Gradle projects:
``` 
dependencies {
    compile group: 'com.epam.healenium', name: 'healenium-appium', version: '1.2.3'
}
```

for Maven projects:
``` 

<dependency>
	<groupId>com.epam.healenium</groupId>
	<artifactId>healenium-appium</artifactId>
	<version>1.2.3</version>
</dependency>
```
### 2. Driver initialization
 Wrapping driver instance with default config:
``` 
    //Appium settings for choosen mobile platform (Android support implemented)
    DesiredCapabilities dc = new DesiredCapabilities();

    dc.setCapability(MobileCapabilityType.DEVICE_NAME, "emulator-5554");
    dc.setCapability(MobileCapabilityType.PLATFORM_NAME, "android");
    
    //used custom Login Form application on android emulator
    dc.setCapability(MobileCapabilityType.APP, "https://github.com/healenium/example_appium_mvn/raw/feature/EPMHLM-209/src/test/resources/apps/login-form.apk");

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
	recovery-tries = 1
	score-cap = 0.5
	heal-enabled = true
	serverHost = localhost
	serverPort = 7878
	imitatePort = 8000
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
