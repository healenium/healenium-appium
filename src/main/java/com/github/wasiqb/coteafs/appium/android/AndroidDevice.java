package com.github.wasiqb.coteafs.appium.android;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.wasiqb.coteafs.appium.service.AppiumServer;
import com.github.wasiqb.coteafs.appium.service.Device;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;

/**
 * @author wasiq.bhamla
 * @since 13-Apr-2017 5:32:01 PM
 */
public class AndroidDevice extends Device {
	private static final Logger log;

	static {
		log = LogManager.getLogger (AndroidDevice.class);
	}

	private AndroidDriver <AndroidElement> driver;

	/**
	 * @author wasiq.bhamla
	 * @since 13-Apr-2017 9:12:47 PM
	 * @param server
	 * @param name
	 */
	public AndroidDevice (final AppiumServer server, final String name) {
		super (server, name);
	}

	/**
	 * @author wasiq.bhamla
	 * @since 13-Apr-2017 3:33:10 PM
	 * @return Android Device
	 */
	public AndroidDriver <AndroidElement> getAndroidDevice () {
		log.trace ("Getting Android device driver...");
		return this.driver;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.wasiqb.coteafs.appium.service.Device#start()
	 */
	@Override
	public void start () {
		log.trace ("Starting Android device driver...");
		this.driver = new AndroidDriver <AndroidElement> (this.server.getServiceUrl (), this.capabilities);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.wasiqb.coteafs.appium.service.Device#stop()
	 */
	@Override
	public void stop () {
		if (this.driver != null) {
			log.trace ("Closign app on Android device...");
			this.driver.closeApp ();

			log.trace ("Quitting Android device driver...");
			this.driver.quit ();
		}
		else {
			log.trace ("Android device driver already stopped...");
		}
	}
}