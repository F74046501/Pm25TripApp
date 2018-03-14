package com.example.r_ni.maptest;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapsActivity extends Activity implements OnMapReadyCallback{

    ///////////the thing of server///////////
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

    //////////////the things of map///////////////
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        MapFragment mapFrag;
        final MapFragment mapfragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapfragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney and move the camera
        server_connect();
        mMap = googleMap;
        LatLng sydney = new LatLng(23.973875, 120.982024);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        setUpMap();
    }

    // 移動地圖到參數指定的位置
    private void moveMap(LatLng place) {
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(place)
                        .zoom(7)
                        .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setUpMap() {
        // 建立位置的座標物件
        LatLng place = new LatLng(23.973875, 120.982024);
        // 移動地圖
        moveMap(place);
    }

    private void server_connect(){
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
                    socket = new Socket("172.20.10.5", 9999);
                    // 判断客户端和服务器是否连接成功
                    System.out.println(socket.isConnected());

                    /**
                     * 接收server消息
                     */
                    try {
                        // 步骤1：创建输入流对象InputStream
                        is = socket.getInputStream();

                        // 步骤2：创建输入流读取器对象 并传入输入流对象
                        // 该对象作用：获取服务器返回的数据
                        isr = new InputStreamReader(is);
                        br = new BufferedReader(isr);

                        // 步骤3：通过输入流读取器对象 接收服务器发送过来的数据 （response是String型別）
                        response = br.readLine();

                        // 步骤4:通知主线程,将接收的消息显示到界面
                        Message msg = Message.obtain();//Message是thread的東西
                        msg.what = 0;
                        mMainHandler.sendMessage(msg);

                        if(response!=null){
                            try{
                                myjs_array  = new JSONArray(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        // 实例化主线程,""用于更新接收过来的消息""
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                if(response!=null){
                    try{
                        myjs_array  = new JSONArray(response);
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(),"response is null", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }

                JSONObject myjObject = null;
                try {
                    myjObject = myjs_array.getJSONObject(0);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"jsonarray[0] is null", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                try {
                    String lat = myjObject.getString("gps_lat");
                    String lon = myjObject.getString("gps_lon");
                    float f_lat = Float.parseFloat(lat);
                    float f_lon = Float.parseFloat(lon);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(f_lat,f_lon)));
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"can't find that index", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        };
    }


}