package com.example.r_ni.maptest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class pythonDraw extends AppCompatActivity {

    private Button sendBtn;
    private EditText textboxPlace, textboxStartTime, textboxStayTime, textboxDate;

    //////////the thing of server///////////
    /*主 变量*/
    // 主线程Handler
    // 用于将从server获取的消息显示出来
    private Handler mMainHandler;

    // Socket变量
    private Socket socket;

    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;

    /*接收server消息 变量*/
    // 输入流对象
    InputStream is;

    // 输入流读取器对象
    InputStreamReader isr ;
    BufferedReader br ;

    // 接收server发送过来的消息
    String response;
    private JSONObject json_read;
    public static JSONArray myjs_array;

    byte[] data = new byte[100000];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_python_draw);

        sendBtn = (Button)findViewById(R.id.sendbtn);
        textboxPlace = (EditText)findViewById(R.id.textboxPlace); //ex:台北101/淡水老街/陽明山/圓山大飯店
        textboxStartTime = (EditText)findViewById(R.id.textboxStartTime); //ex:2155
        textboxStayTime = (EditText)findViewById(R.id.textboxStayTime);// ex:180/220/60
        textboxDate = (EditText)findViewById(R.id.textboxDate);// ex:0711

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String placeStr = textboxPlace.getText().toString();
                String startStr = textboxStartTime.getText().toString();
                String stayStr = textboxStayTime.getText().toString();
                String dateStr = textboxDate.getText().toString();
                String result = placeStr+"*"+startStr+"*"+stayStr+"*"+dateStr;
                System.out.println(result);
                server_connect(result);
            }
        });

    }

    private void server_connect(final String strSend){
        runOnUiThread(new Runnable() {
            public void run() {
                ImageView img= (ImageView) findViewById(R.id.imgChart);
                img.setImageResource(R.drawable.loading);
            }
        });
        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();
        /**
         * 创建客户端 & server的连接
         * 並且接收server的訊息
         */
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建Socket对象 & 指定服务端的IP 及 端口号
                    socket = new Socket("192.168.31.172", 6665
                    );
                    // 判断客户端和服务器是否连接成功
                    //System.out.println(socket.isConnected());

                    /**
                     * 傳送東西出去
                     */
                    try {
                        // 創造網路輸出串流
                        BufferedWriter bw;
                        bw = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));
                        // 寫入訊息到串流
                        bw.write(strSend);
                        // 立即發送
                        bw.flush();
                    } catch (IOException e) {}

                    /**
                     * 接收圖片回來
                     */
                    try{
                        TimeUnit.SECONDS.sleep(25);
                        InputStream stream = socket.getInputStream();
                        System.out.println("get input stream");
                        //byte[] data = new byte[26784];
                        int count = stream.read(data);
                        System.out.print("data count is "+count);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                                ImageView img= (ImageView) findViewById(R.id.imgChart);

                                img.setImageBitmap(bmp);

                                /*ImageView imageView = new ImageView(pythonDraw.this);
                                // Set the Bitmap data to the ImageView
                                imageView.setImageBitmap(bmp);
                                // Get the Root View of the layout
                                ViewGroup layout = (ViewGroup) findViewById(android.R.id.content);
                                // Add the ImageView to the Layout
                                layout.addView(imageView);*/
                            }
                        });

                    }catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
