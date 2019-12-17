package org.apache.cordova.geolocation;

import android.content.Intent;

/**
 * @edited sachin.gupta
 */
public class ApiConstants {

	public static final String PUSH_REGISTER_URL = "http://intranet.kelltontech.com/leaves/updateDeviceToken";
	//public static final String PUSH_REGISTER_URL http://192.168.13.103/mitrr/trunk/leaves/updateDeviceToken";
	// public static final String WEB_VIEW_LIVE_URL ="http://intranet.kelltontech.com/mobile/index.html";

	// Google project id
	public static final String GOOGLE_SENDER_ID = "945356471123";//"76298911568";//541058271279";

	public static final String DISPLAY_MESSAGE_ACTION = "com.kelltontech.intranet.gcm.DISPLAY_MESSAGE";

	public static final String EXTRA_MESSAGE = "message";

	public static final String ACTION_NOTIFICATION_CLICKED = "com.kelltontech.intranet.NOTIFICATION_CLICKED";

	public static final String LOCAL_BRAODCAST_LOCATION = "BROADCAST_INTENT_ACTION";
	public static final String LOCAL_BRAODCAST_LOCATION_ERROR = "LOCAL_BRAODCAST_LOCATION_ERROR";

	public static final String EXTRA_LOCATION_LATITUDE = "lat";

	public static final String EXTRA_LOCATION_LONGITUDE = "longt";
}
