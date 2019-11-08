package com.fan.bta2dpdemo;

/**
 * Created by FanFan on 18-12-5.
 */

public class Constants {

    /**the request code of enable bluetooth*/
    static final int REQUEST_ENABLE_BT = 0;
    static final int MESSAGE_START_SCAN = 0;
    static final int MESSAGE_STOP_SCAN = 1;
    static final int MESSAGE_START_CONNECT = 2;
    static final int MESSAGE_START_DISCONNECT = 3;
    static final int MESSAGE_ACTION_FOUND = 4;
    static final int MESSAGE_CONNECTION_STATE_CHANGED = 5;
    static final String EXTRA_DEVICE= "extra_device";
    static final String EXTRA_RSSI = "extra_rssi";
    static final String EXTRA_CLASS = "extra_class";
    static final String EXTRA_CONNECTION_STATE = "extra_connection_state";

    static final String PREF_CONNECTED_DEVICE = "pref_connected_device";
    static final String PREF_DEVICE_ADDRESS = "pref_device_address";
    static final String PREF_DEVICE_NAME = "pref_device_name";

    static final int RSSI_UNKNOWN = -1;

}
