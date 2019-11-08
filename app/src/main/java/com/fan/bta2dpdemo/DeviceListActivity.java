package com.fan.bta2dpdemo;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by FanFan on 18-12-5.
 *
 */

public class DeviceListActivity extends Activity implements View.OnClickListener {


    private BluetoothAdapter btAdapter;
    private Context mContext;
    private Button mScanView;
    private ListView mList;
    private DeviceListAdapter mAdapter;
    private BluetoothA2dp bluetoothA2dp;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Utils.logD(TAG, "msg.what" + msg.what);
            switch (msg.what) {
                case Constants.MESSAGE_START_SCAN:
                    updateSearchView(true);
                    break;
                case Constants.MESSAGE_STOP_SCAN:
                    updateSearchView(false);
                    break;
                case Constants.MESSAGE_ACTION_FOUND:
                    Bundle bundle = msg.getData();

                    BluetoothDevice device = bundle.getParcelable(Constants.EXTRA_DEVICE);
                    int rssi = bundle.getInt(Constants.EXTRA_RSSI);
                    mAdapter.addDev(device, rssi, BluetoothAdapter.STATE_DISCONNECTED);
                    break;
                case Constants.MESSAGE_START_CONNECT:
                    if (btAdapter.isDiscovering()) {
                        btAdapter.cancelDiscovery();
                    }
                    Bundle bundleConn = msg.getData();
                    BluetoothDevice deviceConn = bundleConn.getParcelable(Constants.EXTRA_DEVICE);
                    connectA2dp(deviceConn);
                    break;
                case Constants.MESSAGE_START_DISCONNECT:
                    Bundle disconnBundle = msg.getData();
                    BluetoothDevice disconnDev = disconnBundle.getParcelable(Constants.EXTRA_DEVICE);
                    disconnectA2dp(disconnDev);
                    break;
                case Constants.MESSAGE_CONNECTION_STATE_CHANGED:
                    Bundle chanbundle = msg.getData();
                    BluetoothDevice chanDevice = chanbundle.getParcelable(Constants.EXTRA_DEVICE);
                    int state = chanbundle.getInt(Constants.EXTRA_CONNECTION_STATE);
                    mAdapter.updateConnState(chanDevice, state);
                    break;
                default:
                    break;
            }
        }
    };


    private static final String TAG = "DeviceListActivity";
    private BluetoothManager bluetoothManager;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        /**
         * 判断本机设备是否支持蓝牙
         */
        bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter =   bluetoothManager.getAdapter();
        if (btAdapter == null) {
            Utils.logE(TAG, getString(R.string.bluetooth_is_not_supported));
            finish();
        }

        /**
         * 判断蓝牙是否开启，需要permission.bluetooth 权限
         */
        if (!btAdapter.isEnabled()) {
            enableBluetooth();
        }

        /**
         * 注册需要监听的广播
         */
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED");

        mContext.registerReceiver(mReceiver, filter);

        initView();

    }

    /**
     * 初始化view
     */
    private void initView() {
        mScanView = findViewById(R.id.search_btn);
        mList = findViewById(R.id.list_view);

        mAdapter = new DeviceListAdapter(mContext, mHandler);
        mList.setAdapter(mAdapter);

        mScanView.setOnClickListener(this);

        //bind a2dp profile
        btAdapter.getProfileProxy(mContext, mA2dpListener, BluetoothProfile.A2DP);
        downloadConnectedDev();
    }

    /**
     * 加载已经连接的device
     */
    private void downloadConnectedDev() {
        DeviceBean deviceBean = Utils.fetchConnectedDevice(mContext);
        if (deviceBean != null) {
            String address = deviceBean.getAddress();
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            deviceBean.setDevice(device);
            mAdapter.addDev(device, Constants.RSSI_UNKNOWN, BluetoothAdapter.STATE_CONNECTED);
            Utils.logD(TAG,deviceBean.toString());
        }

    }

    /**
     * 更新search view
     *
     * @param isScanning 是否开启了扫描
     */
    private void updateSearchView(boolean isScanning) {
        mScanView.setText(isScanning ? R.string.scanning : R.string.start_scan);
        mScanView.setEnabled(!isScanning);
    }

    /**
     * 开启蓝牙
     */
    private void enableBluetooth() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, Constants.REQUEST_ENABLE_BT);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        /**
         * 关闭应用时停止扫描
         */
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                //蓝牙开启失败，finish
                Utils.logE(TAG, getString(R.string.bluetooth_enable_failure));
                /**提示用户，蓝牙开启失败*/
                Utils.showT(mContext, R.string.bluetooth_enable_failure);
                finish();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_btn:
                /**开启扫描，需要Bluetooth admin权限*/
                btAdapter.startDiscovery();
                break;
            default:
                break;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: "+intent.getAction() );
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            Utils.logD(TAG, action);
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:/**扫描开启时的广播*/
                    mHandler.sendEmptyMessage(Constants.MESSAGE_START_SCAN);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:/**扫描结束时广播*/
                    mHandler.sendEmptyMessage(Constants.MESSAGE_STOP_SCAN);
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:/**蓝牙状态发生改变*/
                    break;
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:/**A2DP设备连接状态发生改变*/
                    BluetoothDevice deviceConn = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothAdapter.STATE_DISCONNECTED);
                    Bundle bundleConn = new Bundle();
                    bundleConn.putParcelable(Constants.EXTRA_DEVICE, deviceConn);
                    bundleConn.putInt(Constants.EXTRA_CONNECTION_STATE, state);
                    Message msgConn = Message.obtain();
                    msgConn.what = Constants.MESSAGE_CONNECTION_STATE_CHANGED;
                    msgConn.setData(bundleConn);
                    mHandler.sendMessage(msgConn);
                    Utils.saveConnectedDevice(mContext, deviceConn, state);
                    break;
                case BluetoothDevice.ACTION_FOUND:/**扫描到设备时的action*/
                    BluetoothClass btClass = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
                    if (btClass.getMajorDeviceClass() != BluetoothClass.Device.Major.AUDIO_VIDEO) {
                        /**本demo只处理a2dp设备，所以只显示a2dp，过滤掉其他设备*/
                        break;
                    }
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Message msg = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.EXTRA_DEVICE, device);
                    bundle.putInt(Constants.EXTRA_RSSI, rssi);
                    msg.setData(bundle);
                    msg.what = Constants.MESSAGE_ACTION_FOUND;
                    mHandler.sendMessage(msg);
                    break;
                default:
                    break;
            }
        }
    };

    private final BluetoothProfile.ServiceListener mA2dpListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            if (i == BluetoothProfile.A2DP) {
                bluetoothA2dp = (BluetoothA2dp) bluetoothProfile;
            }
        }

        @Override
        public void onServiceDisconnected(int i) {
            if (i == BluetoothProfile.A2DP) {
                bluetoothA2dp = null;
            }
        }
    };

    /**
     * 建立a2dp设备的连接
     *
     * @param device device
     */
    private void connectA2dp(BluetoothDevice device) {
        if (bluetoothA2dp == null || device == null) {
            return;
        }

        try {
            Method method = BluetoothA2dp.class.getMethod("connect", new Class[]{BluetoothDevice.class});
            method.invoke(bluetoothA2dp, device);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Utils.logE(TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Utils.logE(TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Utils.logE(TAG, e.getMessage());
        }
    }

    /**
     * 断开当前a2dp设备
     *
     * @param device device
     */
    private void disconnectA2dp(BluetoothDevice device) {
        if (bluetoothA2dp == null || device == null) {
            return;
        }
        try {
            Method method = BluetoothA2dp.class.getMethod("disconnect", new Class[]{BluetoothDevice.class});
            method.invoke(bluetoothA2dp, device);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Utils.logE(TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Utils.logE(TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Utils.logE(TAG, e.getMessage());
        }
    }
}
