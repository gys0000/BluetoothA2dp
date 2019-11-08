package com.fan.bta2dpdemo;

import android.bluetooth.BluetoothDevice;

/**
 * Created by FanFan on 18-12-5.
 */

public class DeviceBean {

    private BluetoothDevice device;
    private String name;
    private int rssi;
    private int state;
    private String address;

    public DeviceBean(BluetoothDevice device, String name, int rssi, int state, String address) {
        this.device = device;
        this.name = name;
        this.rssi = rssi;
        this.state = state;
        this.address = address;
    }

    public DeviceBean() {
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "deviceBean info: " +
                "name = " + name
                + ", rssi = " + rssi
                + ", state = " + state
                + ", address = " + address;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DeviceBean) && address.equals(((DeviceBean) obj).getAddress());
    }
}
