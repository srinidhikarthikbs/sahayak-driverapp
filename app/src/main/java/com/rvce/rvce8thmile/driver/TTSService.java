package com.rvce.rvce8thmile.driver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;

public class TTSService extends Service implements TextToSpeech.OnInitListener{

    Double latitude,longitude;
    private String str;
    String ans;
    GPSTracker gps;
    String busno,license;
    private static final String TAG="TTSService";

    @Override

    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public void onCreate() {


        Log.v(TAG, "oncreate_service");
        str ="turn left please ";
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {

            sayHello(str);
            Log.v(TAG, "onstart_service");
        gps = new GPSTracker(getApplicationContext());
            super.onStart(intent, startId);
        final SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        busno=prefs.getString("busno","nobuses");
        license=prefs.getString("license","unlicensed");

        final android.os.Handler h=new android.os.Handler();
        Runnable r=new Runnable() {
            @Override
            public void run() {

                // create class object


                // check if GPS enabled
                if(gps.canGetLocation()){
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();



                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

                Thread t=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpClient httpClient=new DefaultHttpClient();
                        HttpPost httpPost=new HttpPost("http://rotaractrvce.com/bidn/updatebus.php");
                        //HttpPost httpPost=new HttpPost("http://ibmhackblind.mybluemix.net/updatebus.php");
                        BasicResponseHandler responseHandler = new BasicResponseHandler();
                        List<NameValuePair> nameValuePair=new ArrayList<NameValuePair>(4);
                        nameValuePair.add(new BasicNameValuePair("busno",busno));
                        nameValuePair.add(new BasicNameValuePair("license",license));
                        nameValuePair.add(new BasicNameValuePair("x",Double.toString(latitude)));
                        nameValuePair.add(new BasicNameValuePair("y",Double.toString(longitude)));
                        try {
                            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                        } catch (UnsupportedEncodingException e) {
                            // log exception
                            e.printStackTrace();
                        }
                        try {
                            ans= httpClient.execute(httpPost, responseHandler);
                            //Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(),latitude.toString()+" : "+longitude.toString(),Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(),ans,Toast.LENGTH_SHORT).show();
                latitude= (double) 0;
                longitude= (double) 0;
                h.postDelayed(this,5000);
            }
        };
        h.post(r);

    }

    @Override
    public void onInit(int status) {
        Log.v(TAG, "oninit");

    }
    private void sayHello(String str) {

    }
}