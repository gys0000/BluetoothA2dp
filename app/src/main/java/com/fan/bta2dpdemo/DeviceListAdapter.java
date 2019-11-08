package com.fan.bta2dpdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FanFan on 18-12-5.
 */

public class DeviceListAdapter extends BaseAdapter implements View.OnClickListener {


    private List<DeviceBean> mDevList;
    private Map<BluetoothDevice, DeviceBean> mDevMap;
    private Context mContext;
    private Handler mHandler;

    public DeviceListAdapter(Context context, Handler handler) {
        mContext = context;
        mDevList = new ArrayList<>();
        mDevMap = new HashMap<>();
        mHandler = handler;
    }

    /**
     * 当扫描到新的设备时调用，将设备添加到map中
     * @param device
     * @param rssi
     */
    public void addDev(BluetoothDevice device, int rssi, int state) {
        if (device == null) {
            return;
        }
        DeviceBean deviceBean = null;
        if (mDevMap.containsKey(device)) {
            deviceBean = mDevMap.get(device);
            deviceBean.setRssi(rssi);
        } else {
            deviceBean = new DeviceBean();
            deviceBean.setDevice(device);
            deviceBean.setName(device.getName());
            deviceBean.setRssi(rssi);
            deviceBean.setAddress(device.getAddress());
            deviceBean.setState(state);
        }

        mDevMap.put(device, deviceBean);
        moveMapToList();
        notifyDataSetChanged();
    }

    /**
     * 当device的连接状态发生改变时调用，更新map中device的信息
     * @param device
     * @param state
     */
    void updateConnState(BluetoothDevice device, int state){
        if (!mDevMap.containsKey(device)) {
            return;
        }
        DeviceBean deviceBean = mDevMap.get(device);
        deviceBean.setState(state);
        mDevMap.put(device, deviceBean);
        moveMapToList();
        notifyDataSetChanged();
    }

    /**
     * 将map中的信息同步更新到list
     */
    private void moveMapToList() {
        mDevList.clear();
        for (Map.Entry<BluetoothDevice, DeviceBean> entry :
                mDevMap.entrySet()) {
            mDevList.add(entry.getValue());
        }
    }

    @Override
    public int getCount() {
        return mDevList.size();
    }

    @Override
    public Object getItem(int i) {
        return mDevList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
        }

        //显示name,若name为null,则显示address
        TextView nameView = view.findViewById(R.id.dev_name);
        //显示device的地址
        TextView devAddr = view.findViewById(R.id.dev_addr);
        Button connectView = view.findViewById(R.id.connect_btn);


        int rssi = mDevList.get(i).getRssi();
        int state = mDevList.get(i).getState();
        String address = mDevList.get(i).getAddress();
        String  name = mDevList.get(i).getName();
        nameView.setText(name == null ? address : (name + ":" + address));
        devAddr.setText(address);
        switch (state){
            case BluetoothAdapter.STATE_CONNECTED:
                connectView.setText(R.string.disconnect);
                connectView.setEnabled(true);
                break;
            case BluetoothAdapter.STATE_CONNECTING:
                connectView.setText(R.string.connecting);
                connectView.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_DISCONNECTED:
                connectView.setText(R.string.connected);
                connectView.setEnabled(true);
                break;
            case BluetoothAdapter.STATE_DISCONNECTING:
                connectView.setText(R.string.disconnecting);
                connectView.setEnabled(false);
                break;
            default:
                connectView.setText(R.string.state_error);
                connectView.setEnabled(false);
                break;
        }
        connectView.setTag(R.id.tag_device, mDevList.get(i));
        connectView.setTag(R.id.tag_state, state);
        connectView.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        DeviceBean deviceBean = (DeviceBean) view.getTag(R.id.tag_device);
        int connState = (int) view.getTag(R.id.tag_state);
        if (connState != BluetoothAdapter.STATE_CONNECTED && connState != BluetoothAdapter.STATE_DISCONNECTED){
            return;
        }
        Message msg = Message.obtain();
        msg.what = connState == BluetoothAdapter.STATE_DISCONNECTED ? Constants.MESSAGE_START_CONNECT : Constants.MESSAGE_START_DISCONNECT;
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.EXTRA_DEVICE, deviceBean.getDevice());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
}
