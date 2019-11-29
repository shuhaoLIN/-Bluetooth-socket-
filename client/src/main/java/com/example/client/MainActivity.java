package com.example.client;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{
    EditText input_text;
    Button  send,find, tryToConnect;
    ListView show_list;
    ArrayAdapter<String> adapter;
    ArrayList<String> bluetoothList;

    BluetoothUtils utils;

    MyOnClickListener clickListener;

    Handler handler;

    /**
     * states
     */
    public static final int ADDLIST = 1;
    public static final int CONNCETSUCCEED = 2;
    public static final int CONNCETFALURE = 3;
    public static final int BONDSUCCESS = 4;
    public static final int BONDFALURE = 5;

    /**
     * some tips' key
     */
    public static final String DEVICENAME = "DEVICENAME";
    public static final String LISTITEM = "LISTITEM";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                //super.handleMessage(msg);
                Bundle bundle;
                switch (msg.what){
                    case ADDLIST:
                        bundle = msg.getData();
                        String string = bundle.getString(LISTITEM);
                        bluetoothList.add(string);
                        adapter.add(string);
                        adapter.notifyDataSetChanged();
                        break;
                    case CONNCETSUCCEED:
                        Toast.makeText(MainActivity.this, "连接成功,可以开始发送信息", Toast.LENGTH_SHORT).show();
                        bundle = msg.getData();
                        String name = bundle.getString(MainActivity.DEVICENAME);
                        input_text.setVisibility(View.VISIBLE);
                        send.setVisibility(View.VISIBLE);
                        send.setText("send to " + name);
                        break;
                    case CONNCETFALURE:
                        Toast.makeText(MainActivity.this, "连接失败，请重新尝试", Toast.LENGTH_SHORT).show();
                        break;
                    case BONDSUCCESS:
                        Toast.makeText(MainActivity.this, "匹配成功，开始进行连接", Toast.LENGTH_SHORT).show();
                        utils.tryToConnect();
                        break;
                    case BONDFALURE:
                        Toast.makeText(MainActivity.this, "匹配失败，请重新尝试", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        input_text = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        find = (Button) findViewById(R.id.find);
        tryToConnect = (Button)findViewById(R.id.tryToConnect);
        show_list = (ListView)findViewById(R.id.show_list);

        bluetoothList = new ArrayList<>();
        adapter= new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, bluetoothList
        );
        show_list.setAdapter(adapter);
        show_list.setOnItemClickListener(new MyOnItemClickListener());
        utils = new BluetoothUtils(BluetoothAdapter.getDefaultAdapter(), MainActivity.this, handler);
        clickListener = new MyOnClickListener();

        send.setOnClickListener(clickListener);
        find.setOnClickListener(clickListener);
        tryToConnect.setOnClickListener(clickListener);

    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String MAC = bluetoothList.get(position).replaceAll("设备名：.*","");
            Toast.makeText(MainActivity.this, MAC, Toast.LENGTH_SHORT).show();
            //尝试进行配对
            boolean isSeed = utils.tryToBond(MAC);
        }
    }
    class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.send:
                    String sendmessage = input_text.getText().toString();
                    utils.writeMessage(sendmessage.getBytes());
                    break;
                case R.id.find:
                    bluetoothList.clear();
                    adapter.clear();
                    ArrayList<String> mid = utils.getAdaptedBlueTooth();
                    if(mid != null){
                        bluetoothList.addAll(mid);
                        adapter.addAll(bluetoothList);
                        Toast.makeText(MainActivity.this, bluetoothList.size()+"",Toast.LENGTH_LONG).show();
                        adapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(MainActivity.this, "暂无配对蓝牙信息", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

}
