package com.fan.bta2dpdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

/**
 * Created by FanFan on 18-12-5.
 * utils
 */

public class Utils {

    private static final boolean DEBUG = true;
    private static final boolean VERBOSE = true;
    private static Toast sToast = null;


    /**
     * error log
     *
     * @param tag tag
     * @param msg msg
     */
    public static void logE(String tag, String msg) {
        Log.e(tag, msg);
    }

    /**
     * debug log
     *
     * @param tag tag
     * @param msg msg
     */
    public static void logD(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    /**
     * verbose log
     *
     * @param tag tag
     * @param msg msg
     */
    public static void logV(String tag, String msg) {
        if (VERBOSE) {
            Log.v(tag, msg);
        }
    }

    /**
     * show the short toast
     * @param context context
     * @param resId resource  id
     */
    public static void showT(Context context, int resId) {
        if (sToast != null) {
            sToast.cancel();
        }
        sToast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
        sToast.show();
    }

    /**
     * show the short toast
     * @param context context
     * @param toastStr resource string
     */
    public static void showT(Context context, String toastStr) {
        if (sToast != null) {
            sToast.cancel();
        }
        sToast = Toast.makeText(context, toastStr, Toast.LENGTH_SHORT);
        sToast.show();
    }

    /**
     * 保存a2dp连接，只保存通过本应用连接的dev设备
     * @param context
     * @param deviceBean
     * @param state
     */
    public static void saveConnectedDevice(Context context, BluetoothDevice deviceBean, int state){
        SharedPreferences.Editor editor = context.getSharedPreferences(
                Constants.PREF_CONNECTED_DEVICE, Context.MODE_PRIVATE).edit();
        if (state == BluetoothAdapter.STATE_CONNECTED) {
            editor.putString(Constants.PREF_DEVICE_ADDRESS, deviceBean.getAddress());
            editor.putString(Constants.PREF_DEVICE_NAME, deviceBean.getName());
        }else{
            editor.remove(Constants.PREF_DEVICE_ADDRESS);
            editor.remove(Constants.PREF_DEVICE_NAME);
        }

        editor.apply();
    }

    /**
     * 获取到保存的a2dp连接
     * @param context
     * @return
     */
    static DeviceBean fetchConnectedDevice(Context context){
        DeviceBean deviceBean = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.PREF_CONNECTED_DEVICE, Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(Constants.PREF_DEVICE_NAME, null);
        String address = sharedPreferences.getString(Constants.PREF_DEVICE_ADDRESS, null);
        if (address != null) {
            deviceBean = new DeviceBean();
            deviceBean.setName(name == null ? address : name);
            deviceBean.setAddress(address);
            deviceBean.setState(BluetoothAdapter.STATE_CONNECTED);
        }
        return deviceBean;
    }
}
