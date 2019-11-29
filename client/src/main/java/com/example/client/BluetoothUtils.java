package com.example.client;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Created by lenovo on 2019/11/20.
 */
public class BluetoothUtils {
    static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter adapter;
    BluetoothDevice device; // 这是连接的设备
    BluetoothSocket socket; //这是连接的socket
    InputStream input;
    OutputStream output;
    Context context;

    ConnectThread connectThread;

    BluetoothReceiver receiver;
    IntentFilter intentFilter;
    Handler handler;

    ArrayList<String> bluetoothList;
    private boolean isRegisted = false;

    public BluetoothUtils(BluetoothAdapter adapter, Context mainActivity,Handler handler) {
        this.adapter = adapter;
        this.context = mainActivity;
        this.handler = handler;
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        receiver = new BluetoothReceiver();
    }

    /**
     * 获取已经配对的蓝牙信息
     */
    public ArrayList<String> getAdaptedBlueTooth() {
        Toast.makeText(context, "开始获取", Toast.LENGTH_SHORT).show();
        bluetoothList = new ArrayList<>();
        if (adapter != null) {//是否有蓝牙设备
            if (adapter.isEnabled()) {//蓝牙设备是否可用
                Set<BluetoothDevice> devices = adapter.getBondedDevices();//获取到已经匹配的蓝牙对象
                if (devices.size() > 0) {
                    for (Iterator iterator = devices.iterator(); iterator.hasNext(); ) {
                        BluetoothDevice device = (BluetoothDevice) iterator.next();
                        bluetoothList.add(device.getAddress() + "设备名：" + device.getName());
                    }
                    searchForAvailableBluetooth();
                    return bluetoothList;
                } else {
                    Toast.makeText(context, "没有绑定的蓝牙设备", Toast.LENGTH_SHORT).show();
                    searchForAvailableBluetooth();
                }
            } else {
                Toast.makeText(context, "蓝牙未打开", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//请求打开蓝牙
                context.startActivity(intent);
            }
        } else {
            Toast.makeText(context, "没有发现蓝牙", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    /**
     *
     * @param MAC
     * @return
     */
    public boolean tryToBond(String MAC) {
        BluetoothDevice device = adapter.getRemoteDevice(MAC);
        //尝试进行连接
        try {
            tryToCancel();
            if(socket != null && socket.isConnected()){
                socket.close();
            }
            Method createBondMethod = null;
            createBondMethod = BluetoothDevice.class.getMethod("createBond");
            createBondMethod.invoke(device);
            this.device = device;
            Toast.makeText(context, "device 配对并且实例化", Toast.LENGTH_SHORT).show();

            //实例化socket后面进行连接
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
            input = socket.getInputStream();
            output = socket.getOutputStream();

            Message message = handler.obtainMessage();
            message.what = MainActivity.BONDSUCCESS;
            handler.sendMessage(message);
            return true;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message message = handler.obtainMessage();
        message.what = MainActivity.BONDFALURE;
        handler.sendMessage(message);
        return false;
    }
    public void tryToCancel(){
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }



        //这一块真的气死我了！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
//        if(socket != null) {
//            try {
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if(isRegisted == true) //没搞明白怎么判断是否进行了注册
//        {
//            context.unregisterReceiver(receiver);
//            isRegisted = false;
//        }
//        if(input != null){
//            try {
//                input.close();
//                output.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if(connectThread.isAlive()){
//            connectThread.interrupt();
//        }

    }

    public void tryToConnect(){
        tryToCancel();
        //发起连接
        connectThread = new ConnectThread();
        connectThread.start();
    }
    /**
     * 发起异步线程进行连接
     */
    class ConnectThread extends Thread{
        @Override
        public void run() {
            try {
//                if(socket.isConnected()){
//                    socket.
//                }
                socket.connect(); // 发起连接
                System.out.println("aaaaaaaaaaaaaaaaaa");
                //通知主线程连接成功
                Message message = handler.obtainMessage();
                message.what = MainActivity.CONNCETSUCCEED;
                Bundle bundle = new Bundle();
                System.out.println(device.getName() + "  " + device.getAddress());
                bundle.putString(MainActivity.DEVICENAME, device.getName());
                message.setData(bundle);
                handler.sendMessage(message);


                //运行read
                while(true){
                    byte[] buffer = new byte[1024];
                    int bytes = input.read(buffer);
                    Message m = handler.obtainMessage();
                    m.what = MainActivity.ADDLIST;
                    Bundle b = new Bundle();
                    b.putString(MainActivity.LISTITEM, new String(buffer, 0, bytes));
                    m.setData(b);
                    handler.sendMessage(m);
                }
            } catch (IOException e) {
                e.printStackTrace();
                tryToCancel();
                //通知主线程连接失败
                Message message = handler.obtainMessage();
                message.what = MainActivity.CONNCETFALURE;
                handler.sendMessage(message);
            }
        }
    }

    public void writeMessage(byte[] bytes){
        try {
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 创建广播，接收扫描到的蓝牙信息
     */
    private class BluetoothReceiver extends BroadcastReceiver {

        /**
         * 这个receive每一次扫描到都会被调用
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //bluetoothList.add(device.getAddress() + "设备名：" + device.getName());//接收获取到的蓝牙地址信息
            //这里应该使用handler进行数据的传递才对，然后更新UI界面
            Message message = handler.obtainMessage();
            message.what = MainActivity.ADDLIST;
            String  string = device.getAddress() + "设备名：" + device.getName();
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.LISTITEM, string);
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }
    /**
     * 注册一个广播，并且扫描课配对的蓝牙信息
     */
    public void searchForAvailableBluetooth(){
        //1.注册一个广播，用于接收“发现设备”的广播

        if(context != null){
            context.registerReceiver(receiver, intentFilter);
            isRegisted = true;
        }
        //创建蓝牙适配器，并开始扫描
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions((Activity)context,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            }
        }
        if(adapter.startDiscovery()){
            Toast.makeText(context, "扫描开始",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "扫描未开始",Toast.LENGTH_SHORT).show();
        }
    }
}
