package com.example.r_ni.maptest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SinglePlacePm25 extends AppCompatActivity {

    private ImageView search_btn;
    private EditText input;
    private TextView output;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_place_pm25);

        search_btn = (ImageView) findViewById(R.id.image_search);
        input = (EditText) findViewById(R.id.text_singlepm25);
        output = (TextView)findViewById(R.id.textView_singlepm25show);

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String placetosearch = input.getText().toString();
                server_connect(placetosearch);
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                server_connect(placetosearch);
            }
        });
    }

    private void server_connect(final String search){
        /*runOnUiThread(new Runnable() {
            public void run() {
                ImageView img= (ImageView) findViewById(R.id.imgChart);
                img.setImageResource(R.drawable.loading);
            }
        });*/
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
                    socket = new Socket("192.168.31.172", 8888
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
                        bw.write(search);
                        // 立即發送
                        bw.flush();
                    } catch (IOException e) {}

                    /*System.out.println("sleep");
                    TimeUnit.SECONDS.sleep(5);
                    System.out.println("wake up");*/
                    /**
                     * 接收東西回來
                     */
                    try{
                        System.out.println("in");
                        // 步骤1：创建输入流对象InputStream
                        is = socket.getInputStream();

                        // 步骤2：创建输入流读取器对象 并传入输入流对象
                        // 该对象作用：获取服务器返回的数据
                        isr = new InputStreamReader(is);
                        br = new BufferedReader(isr);

                        // 步骤3：通过输入流读取器对象 接收服务器发送过来的数据 （response是String型別）
                        response = br.readLine();
                        //把pm25數值 取小數點後兩位
                        float response_float = Float.parseFloat(response);
                        int tmp = (int) (response_float*100);
                        float finalAns = tmp/100;
                        //pm25數值做等級篩選
                        if(finalAns<35){
                            final String reply= String.valueOf(finalAns)
                                    +"\n低"
                                    +"\n可正常戶外活動";
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    output.setText(reply);
                                    output.setTextColor(Color.GREEN);
                                }
                            });
                        }
                        else if(finalAns>=35 && finalAns<53){
                            final String reply= String.valueOf(finalAns)
                                    +"\n中"
                                    +"\n有心臟、呼吸道及心血管疾病的成人與孩童感受到癥狀時，應考慮減少體力消耗，特別是減少戶外活動。";
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    output.setText(reply);
                                    output.setTextColor(Color.YELLOW);
                                }
                            });
                        }
                        else if(finalAns>=53 && finalAns<70){
                            final String reply= String.valueOf(finalAns)
                                    +"\n高"
                                    +"\n任何人如果有不適，如眼痛，咳嗽或喉嚨痛等，應該考慮減少戶外活動。";
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    output.setText(reply);
                                    output.setTextColor(Color.RED);
                                }
                            });
                        }
                        else if(finalAns>=70){
                            final String reply= String.valueOf(finalAns)
                                    +"\n非常高"
                                    +"\n任何人如果有不適，如眼痛，咳嗽或喉嚨痛等，應減少體力消耗，特別是減少戶外活動。";
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    output.setText(reply);
                                    output.setTextColor(Color.RED);
                                }
                            });
                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("error");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
