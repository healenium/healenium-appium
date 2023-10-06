# Healenium-Appium

[![Docker Pulls](https://img.shields.io/docker/pulls/healenium/hlm-backend.svg?maxAge=25920)](https://hub.docker.com/u/healenium)
[![License](https://img.shields.io/badge/license-Apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![@healenium](https://img.shields.io/badge/Telegram-%40healenium-orange.svg)](https://t.me/healenium)

### Table of Contents

[Overall information](#overall-information)

[Compatibility with OSs](#compatibility-with-oss)

[Healenium Appium installation](#healenium-appium-installation)

[Language Examples](#language-examples)
* [Java](#java)
* [Python](#python)


### Overall information
Self-healing framework based on Selenium and able to use all Selenium supported languages like Java/Python/JS/C#
Healenium acts as proxy between client and Appium server.

`Docker-compose` includes the following services:
- `postgres-db` (PostgreSQL database to store etalon selector / healing / report)
- `hlm-proxy` (Proxy client request to Appium server)
- `hlm-backend` (CRUD service)
- `selector-imitator` (Convert healed locator to convenient format)

### Compatibility with OSs

Support: Android Web/native and IOS Web/native apps.

### Healenium-Appium installation

Clone Healenium repository:
```sh
git clone https://github.com/healenium/healenium.git
```

> Before run healenium you have to specify appium server host and port using appropriate environment variables of hlm-proxy container: APPIUM_SERVER_URL

Example setup hlm-proxy's env variables in case of local Appium server (specified by default):

```dockerfile
    - APPIUM_SERVER_URL=http://host.docker.internal:4723/wd/hub
```

Run Healenium with Appium only

```sh
docker-compose -f docker-compose-appium.yaml up -d
```

Run Healenium with Appium and Selenoid

```sh
docker-compose up -d
```

### Language examples

```
    /**
    * "http://127.0.0.1:8085" OR "http://localhost:8085" if you are using locally running proxy server
    *
    * if you want to use a remote proxy server,
    * specify the ip address of this server - "http://remote_ip_address:8085"
    */
```

###### Java:
```java
    String nodeURL = "http://localhost:8085";

    MutableCapabilities cap = new MutableCapabilities();
    cap.setCapability("platformName", "android");
    cap.setCapability("deviceName", "emulator-5554");
    cap.setCapability("browserName", "chrome");
    cap.setCapability("nativeWebScreenshot",true);

    AppiumDriver driver = new AppiumDriver(new URL(nodeURL), cap);
```

###### Python
```py
    nodeURL = "http://localhost:8085"
    
    # Set the desired capabilities.
    desired_caps = {}
    desired_caps['platformName'] = 'Android'
    desired_caps['platformVersion'] = '9'
    desired_caps['deviceName'] = 'emulator-5554'
    desired_caps['browserName'] = 'chrome'
    desired_caps['nativeWebScreenshot'] = 'true'

    wd = webdriver.Remote('http://127.0.0.1:8085', desired_caps)
```

## Community / Support

* [Telegram chat](https://t.me/healenium)
* [GitHub Issues](https://github.com/healenium/healenium/issues)
* [YouTube Channel](https://www.youtube.com/channel/UCsZJ0ri-Hp7IA1A6Fgi4Hvg)
