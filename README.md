# healenium-appium
appium adaptor for self-healing

[ ![Download](https://api.bintray.com/packages/epam/healenium/healenium-appium/images/download.svg) ](https://bintray.com/epam/healenium/healenium-appium/_latestVersion)
[![Build Status](https://github.com/healenium/healenium-appium/workflows/Java-CI-test/badge.svg)](https://github.com/healenium/healenium-appium/workflows/Java-CI-test/badge.svg)

## How to start

### 0. Add dependency 
for Gradle projects:
``` 
repositories {
    maven {
        url  "https://dl.bintray.com/epam/healenium"
    }
    mavenCentral()
}
dependencies {
    compile group: 'com.epam.healenium', name: 'healenium-appium', version: '1.0.0'
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
	<version>1.0.0</version>
</dependency>
```

### 1. Driver initialization
 Wrapping driver instance with default config:
``` 
    //declare delegate driver
    AppiumDriver driver = new AndroidDriver<AndroidElement>(new URL("http://127.0.0.1:4723/wd/hub"), dc);
    //adding healing support
    driver = DriverWrapper.wrap(driver);
 ```
Default config values:
``` 
    recovery-tries = 3
    basePath = sha/healenium
    reportPath = build/reports
    screenshotPath = build/screenshots/
    heal-enabled = true
 ```

 > recovery-tries - list of proposed healed locators

 > basePath - folder to store base locators path

 > **Important!** Do not delete data from the folder where files with new locators are stored. They are used to perform self-healing in next automation runs

 > reportPath - folder to save test report with healing information

 > screenshotPath - folder to save screenshots of healed elements

 > heal-enabled - you could enable or disable healing by setting true or false flag to this variable

* Suggested way is to declare custom config or property file (ex. sha.properties) and set
``` basePath = sha/selenium```

Also you could set configs via -D or System properties, for example to turn off healing for current test run:
```-Dheal-enabled=false```