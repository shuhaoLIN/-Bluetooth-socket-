package com.example.server;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    public static final int GETMESSAGE = 0;
    public static final String MESSAGEKEY = "message_key";
    public static final String TOASTSTRING = "toaststring";
    public static final int CONNECTBREAK = 1;
    public static final int TOAST = 2;

    Handler handler;
    TextView show_message;

    AcceptThread acceptThread;

    Button send ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        show_message = (TextView)findViewById(R.id.show_message);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case GETMESSAGE:
                        show_message.append("\n" + msg.getData().getString(MESSAGEKEY).toString());
                        break;
                    case CONNECTBREAK:
                        show_message.append("\n 客户端断开连接或者连接产生中断，重新开启等待" );
                        acceptThread = new AcceptThread(BluetoothAdapter.getDefaultAdapter(), MainActivity.this, handler);
                        acceptThread.start();
                        break;
                    case TOAST:
                        show_message.append("\n"+msg.getData().getString(TOASTSTRING));
                        break;
                }
            }
        };
        acceptThread = new AcceptThread(BluetoothAdapter.getDefaultAdapter(), this, handler);
        acceptThread.start();

        send = (Button)findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptThread.writeToSocket("ookokokok");
            }
        });
    }
}
