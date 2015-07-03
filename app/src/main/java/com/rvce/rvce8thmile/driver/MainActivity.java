package com.rvce.rvce8thmile.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


public class MainActivity extends ActionBarActivity {

    EditText busno,license;
    Button mybtn;
    Double latitude,longitude;
    String ans;
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gps = new GPSTracker(getApplicationContext());
        busno=(EditText) findViewById(R.id.editText);
        license=(EditText) findViewById(R.id.editText2);
        mybtn=(Button) findViewById(R.id.mybtn);

        mybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final SharedPreferences prefs = getSharedPreferences(
                        MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);


                SharedPreferences.Editor editor = prefs.edit();
                if(!busno.getText().equals(""))
                {
                    if(prefs.contains("busno"))
                        editor.remove("busno");
                    editor.putString("busno",busno.getText().toString());
                }
                if(!license.getText().equals(""))
                {
                    if(prefs.contains("license"))
                        editor.remove("license");
                    editor.putString("license",license.getText().toString());
                }

                editor.apply();

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
                        //HttpPost httpPost=new HttpPost("https://rotaractrvce.com/bidn/updateuser.php");
                        HttpPost httpPost=new HttpPost("http://rotaractrvce.com/bidn/updatebus.php");
                        BasicResponseHandler responseHandler = new BasicResponseHandler();
                        List<NameValuePair> nameValuePair=new ArrayList<NameValuePair>(4);
                        nameValuePair.add(new BasicNameValuePair("busno",busno.getText().toString()));
                        nameValuePair.add(new BasicNameValuePair("license",license.getText().toString()));
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
                Toast.makeText(getApplicationContext(), latitude.toString() + " : " + longitude.toString(), Toast.LENGTH_SHORT).show();

                startService(new Intent(MainActivity.this, TTSService.class));
            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
