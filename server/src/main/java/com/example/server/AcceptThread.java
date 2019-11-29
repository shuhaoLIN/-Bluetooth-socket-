package com.example.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


/**
 * Created by lenovo on 2019/11/20.
 */
public class AcceptThread extends Thread {
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothServerSocket mmServerSocket;
    BluetoothSocket socket = null;
    InputStream inputStream;
    OutputStream outputStream;
    private final Context context;
    static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "ok";
    private static Handler handler;

    public AcceptThread(BluetoothAdapter bluetoothAdapter, Context context, Handler handler) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        this.context = context;
        BluetoothServerSocket tmp = null;
        mBluetoothAdapter = bluetoothAdapter;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
        }
        mmServerSocket = tmp;
        this.handler = handler;
        ToastString("初始化了新的线程");
    }
    public AcceptThread(BluetoothAdapter bluetoothAdapter, Context context) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        this.context = context;
        BluetoothServerSocket tmp = null;
        mBluetoothAdapter = bluetoothAdapter;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
        }
        mmServerSocket = tmp;
        ToastString("两个参数的，初始化了新的线程");
    }
    public void run() {
        ToastString("run");
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
                ToastString("连接成功");
                inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;

                while(true){
                    outputStream= socket.getOutputStream();
                    bytes = inputStream.read(buffer);
                    if(bytes == 0 || bytes == -1){
                        ToastString("数据返回为" + bytes);
                        Message  message = handler.obtainMessage();
                        message.what = MainActivity.CONNECTBREAK;
                        handler.sendMessage(message);
                        break;
                    }
                    Message  message = handler.obtainMessage();
                    message.what = MainActivity.GETMESSAGE;
                    Bundle bundle = new Bundle();
                    bundle.putString(MainActivity.MESSAGEKEY, new String(buffer,0,bytes));
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                ToastString("出现exception");
                Message  message = handler.obtainMessage();
                message.what = MainActivity.CONNECTBREAK;
                handler.sendMessage(message);
                break;
            }finally {
                try {
                    //关闭的同学需要关闭serversocket，因为是要重新进行初始化，并且连接的。
                    socket.close();
                    cancel();
                    ToastString("socket关闭了");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
//            if(socket !=null){
//                Toast.makeText(context, "连接成功", Toast.LENGTH_SHORT).show();
//            }else{
//                Toast.makeText(context, "连接bu成功", Toast.LENGTH_SHORT).show();
//
//            }
            // If a connection was accepted
//            if (socket != null) {
//                // Do work to manage the connection (in a separate thread)
//                //manageConnectedSocket(socket);
//                chatThread = new ChatThread(socket);
//                chatThread.run();
//                try {
//                    mmServerSocket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                break;
//            }
        }
    }

//    private void manageConnectedSocket(BluetoothSocket socket) {
//        chatThread = new ChatThread(socket);
//    }
//
//    private void writeBytes(byte[] bytes) {
//        chatThread.write(bytes);
//    }
//
//    private void readStart() {
//        chatThread.run();
//    }

    /**
     * Will cancel the listening socket, and cause the thread to finish
     */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
        }
    }
    public void ToastString(String string){
        Message m = handler.obtainMessage();
        m.what = MainActivity.TOAST;
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOASTSTRING, string);
        m.setData(bundle);
        handler.sendMessage(m);
    }
    public void writeToSocket(String string){
        try {
            outputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
